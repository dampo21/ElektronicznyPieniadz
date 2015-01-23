package com.example.damian.ecash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

/** Activiti Banku */
public class BankActivity extends Activity {

    Button startButton, stopButton, showButton, randomButton;
    TextView ipAdressText, consoleText;
    ScrollView scrollView;
    public static BankActivity bankAct;
    Connection connection = null;
    Random rand = new Random();

    /** Zbior liczb pierwszych, z ktoryche generujemy dwie: p i q */
    public int [] liczbyPierwsze = {17417, 17489, 17579, 17597, 17657, 17681, 17747, 17789, 17837, 17909, 17921, 17957, 17987, 18041, 18047,
            18047, 18059, 27479, 27791, 28661, 29387, 30839, 35531, 38447, 37991, 38609, 26681, 25169, 24371};

    /** oznacza w opracowaniu "p" */
    public int n1 = liczbyPierwsze[rand.nextInt(liczbyPierwsze.length)];
    /** oznacza w opracowaniu "q" */
    public int n2 = liczbyPierwsze[rand.nextInt(liczbyPierwsze.length)];
    /** n razem z wykladnikiem e (u nas 3) tworzy klucz publiczny */
    public BigInteger n = new BigInteger(Integer.toString(n1*n2));
    /** parametr bezpieczenstwa */
    public int k = 10;
    /** liczba odwrotna do e modulo n */
    public int d = 0;
    /** numer konta Alice */
    public int u = 12345;
    /** licznik zwiazany z kontem Alice */
    public int v = 1;
    /** Do przetrzymywania wartosci B */
    public String []valueB = new String [k];
    /** Do przetrzymywania uzytych pieniedzy*/
    public ArrayList<ArrayList<Coin>> usedC = new ArrayList<ArrayList<Coin>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);


        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);
        randomButton = (Button) findViewById(R.id.buttonRandom);
        showButton = (Button) findViewById(R.id.buttonShow);
        ipAdressText =  (TextView) findViewById(R.id.textIp);
        consoleText = (TextView) findViewById(R.id.textConsole);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        bankAct = this;
        stopButton.setEnabled(false);

        d = algorytmEuklidesa();
        ipAdressText.setText("IP : " + getLocalIpAddress());
        Toast.makeText(getApplicationContext(), "Pobieranie IP", Toast.LENGTH_LONG).show();



        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Task
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                write("# \"Start\" \n");
                connection = new Connection(6000, consoleText);
                connection.setContext(bankAct);
                connection.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                write("# \"Stop\" \n");
                connection.end = true;

                try {
                    connection.serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    write("## ERROR \n");
                }
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }

        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Task
                n1 = liczbyPierwsze[rand.nextInt(liczbyPierwsze.length)];
                n2 = liczbyPierwsze[rand.nextInt(liczbyPierwsze.length)];
                n = new BigInteger(Integer.toString(n1*n2));
                d = algorytmEuklidesa();
                write("# \"Wylosowano\" \n");
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Task
                write("\n#POKAZ: \n");
                write("n = " + n + "\nfaktoryzacja: " + n1 + ", " + n2 +"\n");
                write("k = " + k + ", u = " + u + ", v = " + v + "\n");
                write("d = " + d + "\n");

                for (int i=0; i<usedC.size(); i++) {
                    write("Zutyta moneta "+(i+1)+":" + "\n");
                    for (int j=0; j<usedC.get(i).size(); j++)
                        write("C"+j+" =" + usedC.get(i).get(j).key + "\n");
                    write("\n");
                }

            }
        });


    }

    /** Metoda do wypisywania logow */
    public void write(final String msg){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            consoleText.append(msg);
            scrollView.fullScroll(View.FOCUS_DOWN);

            }
        });
    }


    /** Funkcja podajaca parametr N */
    public void giveN(PrintWriter pw, BufferedReader odczyt2) throws IOException {
        pw.println(n);
        write("#Send to Shop: n = " +n+ "\n");
        String txt = "";
        txt = odczyt2.readLine();
    }


    /** Wyplata pieniadza z Banku - generacja pieniadza */
    public void withdrawal(PrintWriter pw, BufferedReader odczyt2) throws IOException {
        String txt = "";
        /** oznaczajaca czy probki przeslane przez Alice sa zgodne */
        Boolean agree = true;

        write("#Receive from Client: withdrawal  \n");
        pw.println("#N");
        pw.println(n);
        write("Send n = "+ n +"\n");

        txt = odczyt2.readLine();
        int i = 0;
        txt = odczyt2.readLine();
        while (!txt.equals("#EndB")) {
            write("Receive B"+(i+1) +": " + txt + "\n");
            valueB[i] = txt;
            txt = odczyt2.readLine();
            i++;
        }
        pw.println("#Give");
        pw.println(k / 2);

        txt = odczyt2.readLine();
        i = 0;
        String[] units = new String[k/2];
        while (!txt.equals("#EndCheck")) {
            units[i] = txt;
            txt = odczyt2.readLine();
            i++;
        }

        write("checkUnits \n");
        for (int j=0; j<(k/2); j++)
        {
            String[] parts = units[j].split(",");
            try {
                String temp1 = funkcjaG(operationInG(Integer.parseInt(parts[0]), u, v, j+(k/2)+1), Integer.parseInt(parts[2]));
                String temp2 = funkcjaG(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                temp1 = funkcjaB (Integer.parseInt(parts[3]), funkcjaF(temp2, temp1), n);           // stary BigN
                if (!temp1.equals(valueB[j+k/2]))
                    agree = false;
            }
            catch (NoSuchAlgorithmException ex)
            {
                ex.getMessage();
            }
        }

        if (agree == true)
        {
            // PKT 5 protokolu
            pw.println("OK");
            write("Units agree \n");

            /** Obliczanie B przez Bank! */
            for (int j=0; j<5; j++)
            {
                BigInteger value = new BigInteger(valueB[j]);
                write("B"+(j+1) + " = " + value  + "\n");
                value = value.modPow(BigInteger.valueOf(d), n); // stare BigInteger.valueOf(n)
                write("B^(1/3) = " + value  + "\n");
                pw.println(value);

            }
            v += k;
        }
        else if (agree == false)
        {
            pw.println("NO");
            write("Not agree unit!! \n");
        }

        write("#endGenerateCash \n");
    };


    /** Wplata pieniadza do Banku */
    public void takeMoney(PrintWriter pw, BufferedReader odczyt2) throws IOException {
        write("#Receive from Shop: deposit  \n");
        String txt="";
        String txt2="";

        /** Oznacza czy pieniadz zostal juz uzyty */
        Boolean inUsedC = false;
        /** zlicza ilosc podobych wektorow w celu wykrycia proby oszustwa */
        int sameUnit = 0;
        /** oznacza czy pieniadz zostal podpisany przez Bank */
        Boolean agree = true;
        /** Do przechowywania odebranego pieniadza */
        ArrayList<Coin> temp = new ArrayList<Coin>();

        while(!txt.equals("#EndMsg")){
            for (int i=0; i<k/2; i++) {
                txt = odczyt2.readLine();
                write("C"+(i+1) +": "+ txt + "\n");
                temp.add(new Coin(txt));
                txt2 = odczyt2.readLine();

                String[] parts = txt2.split(",");
                int which = temp.size() - 1;

                if (parts[0].equals("1"))
                    temp.get(which).setParam(1, parts[1], parts[2], parts[3]);
                else
                    temp.get(which).setParam(0, parts[1], parts[2], parts[3]);

            }
            write("\n");

            for (int i=0; i<usedC.size(); i++) {
                    for (int p = 0; p < usedC.get(i).size(); p++) {
                        for (int j = 0; j < temp.size(); j++) {
                            if (usedC.get(i).get(p).key.equals(temp.get(j).key)) {
                                sameUnit++;
                                break;
                            }
                        }
                    }
                    if (sameUnit == k / 2) {
                        inUsedC = true;
                        break;
                    }
                    sameUnit = 0;
            }

            BigInteger C;
            for (int i=0; i<temp.size(); i++) {

                //  String[] parts = txt.split(",");;
                try {
                    if (temp.get(i).z == 1) {
                        txt = funkcjaF( funkcjaG(Integer.parseInt(temp.get(i).a), Integer.parseInt(temp.get(i).c)) , temp.get(i).y );
                        C = new BigInteger(txt, 16);
                        C = C.mod(n);
                        write("f"+(i+1) + ": " + C + "\n");
                        write("C"+(i+1) + ": "  + temp.get(i).key + "\n");
                        BigInteger sprKey = temp.get(i).key.modPow(new BigInteger(Integer.toString(3)),n);
                        write("C"+(i+1)+"^3: " + sprKey + "\n");
                        if (!sprKey.equals(C))
                            agree = false;
                    }
                    else {
                        txt = funkcjaF( temp.get(i).x , funkcjaG(Integer.parseInt(temp.get(i).xor), Integer.parseInt(temp.get(i).d)) );
                        C = new BigInteger(txt, 16);
                        C = C.mod(n);
                        write("f"+(i+1) + ": " + C + "\n");
                        write("C"+(i+1) + ": "  + temp.get(i).key + "\n");
                        BigInteger sprKey = temp.get(i).key.modPow(new BigInteger(Integer.toString(3)),n);
                        write("C"+(i+1)+"^3: " + sprKey + "\n");
                        if (!sprKey.equals(C))
                            agree = false;
                    }
                }
                catch (NoSuchAlgorithmException ex) { ex.getMessage(); }
            }

            if (agree == true && inUsedC == false)
            {
                write("#Transaction accept \n");
                pw.println("Accept");
                usedC.add(temp);
            }
            else
            {
                write("Coin not agree! \n");
                pw.println("Not accept");
                if (inUsedC == true) {
                    write("Coin used \n");
                    pw.println("Coin was used");
                }
                if (inUsedC == false) {
                    write("I not generate this coin \n");
                    pw.println("Bank not generate this coin");
                }
            }
            txt = odczyt2.readLine();
        }

        pw.println("#EndMsg");
    };


    /** Operacja obliczania:   " ai ^ (u || (v+i)) " do funkcji G */
    public static int operationInG (int a, int u, int v, int i)
    {
        int suma = v+i;
        String sumaString = Integer.toBinaryString(suma);
        String uString = Integer.toBinaryString(u);
        String data = uString+sumaString;
        int foo = Integer.parseInt(data, 2);
        String aBinary = Integer.toBinaryString(a);
        String resztaBinary = Integer.toBinaryString(foo);

        BigInteger i1 = new BigInteger(aBinary, 16);
        BigInteger i2 = new BigInteger(resztaBinary, 16);
        BigInteger res = i1.xor(i2);
        String s3 = res.toString(16);
        int koniec = Integer.parseInt(s3, 2);

        return koniec;
    }


    /** funkcja opublikowana przez bank */
    public static String funkcjaG (int x, int y) throws NoSuchAlgorithmException
    {
        String xString = Integer.toBinaryString(x);
        String yString = Integer.toBinaryString(y);
        String dane = xString + yString;
        int foo = Integer.parseInt(dane, 2);
        dane = Integer.toString(foo);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(dane.getBytes());

        byte byteData[] = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        String send = sb.toString();
        return send;
    }

    /** funkcja opublikowana przez bank */
    public static String funkcjaF (String x, String y) throws NoSuchAlgorithmException
    {
        String dane = x + y;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(dane.getBytes());
        byte byteData[] = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }

        String send = sb.toString();
        return send;
    }

    /** Funkcja obliczajaca probki B */
    public static String funkcjaB (int r, String f, BigInteger n)
    {

        BigInteger value = new BigInteger(f, 16);
        int rr = r*r*r;
        BigInteger BigR = new BigInteger (Integer.toString(rr));
        BigInteger result =  BigR.multiply(value);
        BigInteger koniec = result.mod(n);

        String send = koniec.toString();
        return send;
    }

    /** Do znajdywania d jako odwrotnosci e modulo t */
    public int algorytmEuklidesa()
    {
        int t;
        int a1, a2;
        int u, v, u2, v2;

        a2 = 3;
        a1 = t = (n1-1)*(n2-1);
        u = v2 = 0;
        u2 = v = 1;
        int q;
        while (a2 != 0)
        {
            q = a1/a2;
            int temp1, temp2, temp3;
            temp1 = a1;
            a1 = a2;
            a2 = temp1-q*a2;

            temp2 = u;
            temp3 = v;
            u = u2;
            u2 = temp2-q*u2;
            v = v2;
            v2 = temp3-q*v2;
        }

        int x=u;
        if (u  < 0)
        {
            x = u + t;
        }
      //  write("Liczba odpowiadajaca 1/3 modulo n = " + x);

        return x;
    }


    @Override
    public void onBackPressed() {
        if (connection != null)
        {
            try {
                connection.serverSocket.close();
            }
            catch (Exception ex) {
                ex.getMessage();
            }
            connection.end = true;
            connection = null;
        }
        super.onBackPressed();

    }



    /** Funckja do pobierania adresu IP */
public String getLocalIpAddress() {
    try {
        for (Enumeration<NetworkInterface> en = NetworkInterface
                .getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf
                    .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                System.out.println("ip1--:" + inetAddress);
                System.out.println("ip2--:" + inetAddress.getHostAddress());
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address){
                    String ip = inetAddress.getHostAddress().toString();
                     return ip;
                }
            }
        }
    } catch (Exception ex) {
        Log.e("IP Address", ex.toString());
    }
    return null;
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       getMenuInflater().inflate(R.menu.bank, menu);
        menu.add(0,1,0,"port");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == 1 ){

            Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}


        /** Klasa odpowiadajaca za działanie serwera
         *
         */
class Connection extends Thread {

    /** Gniazdo serwera */
    ServerSocket serverSocket = null;
    /** Gniazdo klienta */
    Socket socket = null;
    /** Strumień wejsciowy*/
    InputStream we = null;
    /** Buffor odczytu danych */
    BufferedReader odczyt = null;
    /** StrumieĹ„ wyjściowy */
    OutputStream wy = null;
    /** Buffor wysyłanych danych */
    PrintWriter pw = null;

    /** Zmienna oznaczajÄ…ca koniec oczekiwania serwera na klienta */
    boolean end = false;
        /** Wiadmość od klienta */
    String fromClient = null;
    /** Panel służący za terminal serwera */
    TextView textArea = null;

            BankActivity active;


public void setContext(BankActivity bA){
    active = bA;
}

    /**
     * Metoda otwierająca gniazdo serwera
     */
    public Connection(int PORT, TextView ta) {
        try {
            textArea = ta;
            serverSocket = new ServerSocket(PORT);

        } catch (IOException e) {
            System.out.println("Nie można utworzyć gniazda serwera.");
            active.write("Nie można utworzyć gniazda serwera. \n");
        }
    }

    /** Watek sprawdzajacy nadejscie nowych polaczen od klientow oraz obslugujacy ich polecenia */
    public void run() {

        while (!end) {

            try {
                active.write("\n#Szukam klienta...  \n");

                socket = serverSocket.accept();
                active.write("#Nawiązano połączenie\n");

                we = socket.getInputStream();
                odczyt = new BufferedReader(new InputStreamReader(we));
                wy = socket.getOutputStream();
                pw = new PrintWriter(wy, true);
                fromClient = odczyt.readLine();
                active.write("C: " + fromClient + " \n");


                if (fromClient.equals("deposit")) {
                    active.takeMoney(pw, odczyt);
                } else if (fromClient.equals("withdrawal")) {
                    active.withdrawal(pw, odczyt);
                } else if (fromClient.equals("giveN")) {
                    active.giveN(pw, odczyt);
                } else {
                    active.write("Nierozpoznane polecenie");
                    active.write("#EndMsg");
                }

                socket.close();
            } catch (Exception e) {
                System.err.println("Server exception: " + e);
            }
        }
    }

}

/** Klasa ulatwiajaca przetrzymywanie pieniadza */
class Coin {
    /** wartosc "czesci" pieniadza */
    BigInteger key;
    /** wartosc z ciagu binarnego Boba */
    int z=2;
    /** Parametry przesylane przez Alice do Boba w celu weryfikacji */
    String a, c, y, x, xor, d;

    String forZ1;
    String forZ0;

    Coin (String key)
    {
        this.key = new BigInteger(key);
    }

    public void setParam (int z, String ax, String cxor, String yd)
    {
        if (z==1)
        {
            this.z = z;
            a = ax;
            c = cxor;
            y = yd;
            forZ1 = z + "," + ax + "," + cxor + "," + yd;
        }
        else
        {
            this.z = z;
            x = ax;
            xor = cxor;
            d = yd;
            forZ0 = z +"," + ax + "," + cxor + "," + yd;
        }

    }

}

