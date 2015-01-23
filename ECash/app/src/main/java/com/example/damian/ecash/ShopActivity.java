package com.example.damian.ecash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
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

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Random;

/** Activiti Sklepu */
public class ShopActivity extends Activity {

    Button settingsButton, waitForClientButton, connectToBankButton, randomButton, showButton;
    ScrollView scrollView;
    TextView ipShop, msgShop;

    /** strumien wejsciowy - przychodzacy od serwera */
    public InputStream we1 = null;
    /** strumien wyjsciowy - wychodzacy do serwera */
    public OutputStream wy1 = null;
    /** Bufor odczytu */
    public BufferedReader odczyt1 = null;
    /** Bufor wysylanych danych */
    public PrintWriter pw1 = null;

    Random rand = new Random();
    public static ShopActivity shopAct;
    ConnectionShop connection = null;

    /** parametr bezpieczenstwa */
    public int k = 10;
    /** losowy ciag binarny do weryfikacji otrzymanego pieniadza */
    public int[] z = new int[k/2];
    /** Do przetrzymywania pieniadza */
    public List<Coin> Coin = new ArrayList<Coin>();
    /** parametr publikowany przez Bank */
    public BigInteger n = new BigInteger("0");
    /** oznacza czy pieniadz zostal podpisany przez Bank */
    public Boolean agree = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        waitForClientButton = (Button) findViewById(R.id.buttonWait);
        connectToBankButton = (Button) findViewById(R.id.buttonShopTake);
        settingsButton = (Button) findViewById(R.id.buttonShopSet);
        randomButton = (Button) findViewById(R.id.buttonRandom);
        showButton = (Button) findViewById(R.id.buttonShow);
        ipShop = (TextView) findViewById(R.id.textIp);
        msgShop = (TextView) findViewById(R.id.textShopMsg);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        shopAct = this;
        waitForClientButton.setEnabled(false);
        connectToBankButton.setEnabled(false);

        ipShop.setText("IP : " + getLocalIpAddress());
        Toast.makeText(getApplicationContext(), "Pobieranie IP", Toast.LENGTH_LONG).show();

        waitForClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                write("# \"Start\" \n");
                try {
                    getN();
                } catch (Exception ex) { ex.getMessage(); };
                connection = new ConnectionShop(6000, msgShop);
                connection.end = false;
                connection.setContext(shopAct);
                connection.start();

            }
        });

        connectToBankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                write("# \"ConnectToBank\" \n");
                try {
                  //  connect();
                   sendCashToBank();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startSettingsActivity = new Intent(getApplicationContext(), ShopSettingsActivity.class);
                startActivity(startSettingsActivity);
            }
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Task
                for (int i=0; i < 5; i++)
                    z[i] = rand.nextInt(2);

                Toast.makeText(getApplicationContext(), "Wylosowano", Toast.LENGTH_LONG).show();
                waitForClientButton.setEnabled(true);
                connectToBankButton.setEnabled(true);
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Task
                write("z = " + z[0] + z[1] + z[2] + z[3] + z[4] + "\n");
                write("n = " + n + "\n");
                for (int i=0; i<Coin.size(); i++)
                    write("C"+(i+1) + " = " + Coin.get(i).key + "\n");

            }
        });
    }

    /** Metoda do wypisywania logow */
    public void write(final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgShop.append(msg);
                scrollView.fullScroll(View.FOCUS_DOWN);

            }
        });
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
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
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

    /** Pobranie wartosci N z banku */
    public void getN () throws IOException {
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        String ipAdress = settings.getString("IpBank", "192.168.1.18");
        int port = settings.getInt("PortBank", 6000);
        Socket socket = new Socket(ipAdress, port);
        String txt = "";

        wy1 = socket.getOutputStream();
        pw1 = new PrintWriter(wy1, true);
        we1 = socket.getInputStream();
        odczyt1 = new BufferedReader(
                new InputStreamReader(we1));

        pw1.println("giveN");
        txt = odczyt1.readLine();
        write("Bank send n: "+txt + "\n");
        n = new BigInteger(txt);
        pw1.println("#EndMsg");

        socket.close();
    }

    /** Funkcja do pobierania pieniadza od klienta */
    public void getCashFromClient(PrintWriter pw, BufferedReader odczyt2) throws IOException {
        String txt = "";
        Coin.clear();
        agree = true;

        while (!txt.equals("#EndMsg")) {

            for (int i=0; i<k/2; i++) {
                txt = odczyt2.readLine();
                write("C" +(i+1) +" = " + txt + "\n");
                Coin.add(new Coin(txt));
            }

            txt = Integer.toString(z[0]);
            for (int i=1; i<k/2; i++)
            {
                txt += "," + Integer.toString(z[i]);
            }
            pw.println(txt);
            write("Send to Client z: " + txt + "\n");
            txt = odczyt2.readLine();
            write("Checked Client...  \n");
            int i = 0;
            while(!txt.equals("#EndCheck")) {
                String[] parts = txt.split(",");
                try {
                    if (z[i] == 1) {
                        txt = funkcjaF( funkcjaG(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])) , parts[2] );
                        Coin.get(i).setParam(z[i], parts[0], parts[1], parts[2]);
                    }
                    else {
                        txt = funkcjaF( parts[0] , funkcjaG(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])) );
                        Coin.get(i).setParam(z[i], parts[0], parts[1], parts[2]);
                    }
                 }
                catch (NoSuchAlgorithmException ex) { ex.getMessage(); }
                BigInteger value = new BigInteger(txt, 16); // wartosc C jak sie uda to zamienic nazwe na C
                value = value.mod(n);

                if (!Coin.get(i).key.modPow(new BigInteger(Integer.toString(3)), n).equals(value)) {
                    agree = false;
                    write("Coin"+(i+1)+" = " + Coin.get(i).key +"\n");
                    write("Coin"+(i+1)+"^3 = " + Coin.get(i).key.modPow(new BigInteger(Integer.toString(3)), n) + "\n");
                    write("f"+(i+1)+" = " +value +"\n");
                }

                txt = odczyt2.readLine();
                i++;
            }
            if (agree)
            {
                write("Agree! \n\n");
                pw.println("Accept");
            }
            else
            {
                write("Cheater! \n\n");
                pw.println("Not accept");
            }
            txt = odczyt2.readLine();

        }

        connection.end = true;
        connectToBankButton.setEnabled(true);
    };

    /** Funkcja do wplacenia pieniadza do banku */
    public void sendCashToBank() throws IOException {
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        String ipAdress = settings.getString("IpBank", "192.168.1.18");
        int port = settings.getInt("PortBank", 6000);
        Socket socket = new Socket(ipAdress, port);
        String txt = "";

        wy1 = socket.getOutputStream();
        pw1 = new PrintWriter(wy1, true);
        we1 = socket.getInputStream();
        odczyt1 = new BufferedReader(
        new InputStreamReader(we1));

        pw1.println("deposit");
        write("#Send to Bank: deposit \n");
        write("#Sending C and Alice's responses... \n");
        for (int i=0; i<k/2; i++)
        {
            pw1.println(Coin.get(i).key);
            if (Coin.get(i).z == 1)
                pw1.println(Coin.get(i).forZ1);
            else
                pw1.println(Coin.get(i).forZ0);
        }

        while(!txt.equals("#EndMsg")){
            txt = odczyt1.readLine();
            write("Bank: "+txt + "\n");

            if (txt.equals("Accept"))
                write("Bank accept transaction \n");
            else {
                write("Bank not accept transaction \n");
                txt = odczyt1.readLine();
                write(txt + " \n");
            }

            pw1.println("#EndMsg");
            txt = odczyt1.readLine();
        }
        socket.close();

    };

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

}

/** Klasa odpowiadajaca za działanie serwera
 *
 */
class ConnectionShop extends Thread {

    /** Gniazdo serwera */
    ServerSocket serverSocket = null;
    /** Gniazdo klienta */
    Socket socket = null;
    /** Strumień wejsciowy*/
    InputStream we = null;
    /** Buffor odczytu danych */
    BufferedReader odczyt = null;
    /** Strumien wyjściowy */
    OutputStream wy = null;
    /** Buffor wysyłanych danych */
    PrintWriter pw = null;

    /** Zmienna oznaczajaca koniec oczekiwania serwera na klienta */
    boolean end = false;
    /** Wiadmość od klienta */
    String fromClient = null;
    /** Panel służący za terminal serwera */
    TextView textArea = null;

    ShopActivity active;


    public void setContext(ShopActivity sA){
        active= sA;
    }

    /**
     * Metoda otwierająca gniazdo serwera
     */
    public ConnectionShop(int PORT, TextView ta) {
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

                if (fromClient.equals("pay")) {
                    active.getCashFromClient(pw, odczyt);
                }
                else { active.write("Nierozpoznane polecenie");
                    active.write("#EndMsg");   }  // Zawsze komunikacje konczymy taką linijką :)

                socket.close();
            } catch (Exception e) {
                System.err.println("Server exception: " + e);
            }
        }
        try {
            serverSocket.close();
        } catch (Exception ex) {
            ex.getMessage();
        }

    }

}


