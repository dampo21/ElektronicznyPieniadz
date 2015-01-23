package com.example.damian.ecash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Activiti Klienta */
public class ClientActivity extends Activity {

    Button payButton, getButton, configButton, randomButton, showButton;
    TextView msg;

    /** strumien wejsciowy - przychodzacy od serwera */
    public InputStream we = null;
    /** Strumien wyjsciowy - wychodzacy do serwera */
    public OutputStream wy = null;
    /** Bufor odczytu */
    public BufferedReader odczyt = null;
    /** Bufor wysylanych danych */
    public PrintWriter pw = null;

    Random rand = new Random();
    /** parametr publikowany przez Bank */
    public int n = 0;
    /** parametr bezpieczenstwa, rowniez publikowany przez bank */
    public int k = 10;
    /** Tablica randomowych liczb a */
    public int [] a = new int [k];
    /** Tablica randomowych liczb c */
    public int [] c = new int [k];
    /** Tablica randomowych liczb d */
    public int [] d = new int [k];
    /** Tablica randomowych liczb r */
    public int [] r = new int [k];
    /** numer konta Alice */
    public int u = 12345;
    /** licznik zwiazany z katem Alice */
    public int v = 1;
    /** wartosc licznika v przez inkrementacja */
    public int oldV = 1;

    /** Do przetrzymywania pieniadza */
    public List<String> Coin = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        configButton = (Button) findViewById(R.id.buttonConfig);
        payButton = (Button) findViewById(R.id.buttonPay);
        getButton = (Button) findViewById(R.id.buttonTake);
        randomButton = (Button) findViewById(R.id.buttonRandom);
        showButton = (Button) findViewById(R.id.buttonShow);
        msg = (TextView)findViewById(R.id.textCom);
        getButton.setEnabled(false);
        showButton.setEnabled(false);
        payButton.setEnabled(false);

        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("IpBank","192.168.1.5");
        editor.putInt("PortBank", 8868);
        editor.putString("IpShop","192.168.1.12");
        editor.putInt("PortShop",6000);
        editor.commit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        configButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startConfigActivity = new Intent(getApplicationContext(), ClientSettingsActivity.class);
                startActivity(startConfigActivity);
            }
        });


        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Połączenie z Bankiem */
            try {
                    connect("Bank");
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "UnknownHostException: " + e.toString(), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } });


        payButton.setOnClickListener(new View.OnClickListener() {
            /** Połączenie ze Sklepem */
            @Override
            public void onClick(View view) {
                try {
                    connect("Shop");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Task
                for (int i=0; i < k; i++)
                {
                    a[i] = rand.nextInt(100)+1;
                    c[i] = rand.nextInt(100)+1;
                    d[i] = rand.nextInt(100)+1;
                    r[i] = rand.nextInt(100)+1;
                }

                getButton.setEnabled(true);
                showButton.setEnabled(true);
                payButton.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Wylosowano", Toast.LENGTH_LONG).show();
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    Bundle koszyk = new Bundle();
                    String tekst = "";
                    for (int i=0; i < k; i++)
                    {
                        tekst += Integer.toString(a[i]) + ",";
                        tekst += Integer.toString(c[i]) + ",";
                        tekst += Integer.toString(d[i]) + ",";
                        tekst += Integer.toString(r[i]) + ",";
                    }

                    String cc = "";
                    for (int i=0; i<Coin.size(); i++)
                        cc += ","+ Coin.get(i);

                    tekst += (Integer.toString(u) + "," + Integer.toString(v) + "," + Integer.toString(n) + "," + Coin.size() + cc);
                    koszyk.putString("dane", tekst);
                    // Definiujemy cel
                    Intent startShowActivity = new Intent(getApplicationContext(), ClientShowActivity.class);
                    startShowActivity.putExtras(koszyk);
                    // Wysyłamy
                    startActivity(startShowActivity);
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Funkcja do nawiazywania polaczen sieciowych z Bankiem lub Sklepem */
    private void connect(String destination) throws IOException {
//destination : Bank  lub Shop
        SharedPreferences settings = getSharedPreferences("PREFS", 0);
        String ipAdress = settings.getString("Ip"+destination, "192.168.1.18");
        int port = settings.getInt("Port"+destination, 8868);
        Socket socket = new Socket(ipAdress, port);

        wy = socket.getOutputStream();
        pw = new PrintWriter(wy, true);
        we = socket.getInputStream();
        odczyt = new BufferedReader(
                new InputStreamReader(we));

        if(destination.equals("Bank")){         getMoneyFromBank(pw,odczyt);
        }else if(destination.equals("Shop")){         pay(pw, odczyt);        }

        socket.close();

    }


/** Funkcja do pobierania pieniadza z banku */
    public void getMoneyFromBank(PrintWriter pw2, BufferedReader odczyt2) throws IOException {
        String txt = "";
        Coin.clear();
        pw2.println("withdrawal");
        msg.append("#Send to Bank -> withdrawal \n");

        while(!txt.equals("#N")){           // czekamy na "n"
            txt = odczyt2.readLine();
        }
        txt = odczyt2.readLine();
        n = Integer.parseInt(txt);
        msg.append ("Get n = " + n +"\n");
        BigInteger BigN = new BigInteger(txt);
        String temp1, temp2;
        String []valueB = new String [k];              // wartosci B
        pw2.println("valueB");                          // bedziemy obliczac i wysylac watosci B
        for (int i=0; i<k; i++)
        {
            try {
                temp1 = funkcjaG(operationInG(a[i], u, v, i + 1), d[i]);
                temp2 = funkcjaG(a[i], c[i]);
                valueB[i] = funkcjaB (r[i], funkcjaF(temp2, temp1), BigN);
                pw2.println(valueB[i]);
                msg.append("Send B"+(i+1) +": " + valueB[i] + "\n");
            }
            catch (NoSuchAlgorithmException ex)
            {
                msg.append("Exception - NoSuchAlgorithmException \n");
            }
        }
        pw2.println("#EndB");
        txt = odczyt2.readLine();
        txt = odczyt2.readLine();           // dostalismy jakie probki wyslac do banku (u nas k = 5...9)
        int j = Integer.parseInt(txt);
        for (;j<k; j++)
        {
            pw2.println(a[j] + "," + c[j] +","+d[j]+","+r[j]);
            msg.append("Send a = " +a[j] + ", c = " +c[j]+ ", d = " +d[j]+ ", r = " +r[j]+  "\n");
        }
        pw2.println("#EndCheck");

        txt = odczyt2.readLine();
        if (txt.equals("OK"))
        {
            msg.append("#Transaction OK \n\n");


            for (int i=0; i<k/2; i++)
            {
                txt = odczyt2.readLine();
                BigInteger fromBank = new BigInteger(txt);
                msg.append("From Bank = " + fromBank + "\n");
                int odwrotnoscR = algorytmEuklidesa(r[i]);
                msg.append("Invers r = " + odwrotnoscR + "\n");
                fromBank = fromBank.multiply(BigInteger.valueOf(odwrotnoscR));
                fromBank = fromBank.mod(BigInteger.valueOf(n));
                msg.append("C"+(i+1) + "*1/R = " + fromBank + "\n");
                Coin.add(fromBank.toString());
            }
            oldV = v;
            v += k;
        }
        else if (txt.equals("NO"))
        {
            msg.append("#Transaction faild. \n");
            msg.append("#Im not cheater... \n\n");
        }

    }


    /** Funckja do placenia elektronicznym pieniadzem */
    public void pay(PrintWriter pw2, BufferedReader odczytane) throws IOException {
        String txt = "";

        pw2.println("pay");
        msg.append("\n\n#Send to Shop-> Pay \n");
        for (int i=0; i<k/2; i++) {
            pw2.println(Coin.get(i));
            msg.append("C"+(i+1) + " = " + Coin.get(i) +"\n");
        }
        txt = odczytane.readLine();

        msg.append("\n\n#Shop checked me \n");
        String[] parts = txt.split(",");
        for (int i=0; i<k/2; i++)
        {
            msg.append("Get z =" + parts[i] +"\n");
            if (parts[i].equals("1"))
            {
                try {
                    String temp1 = funkcjaG(operationInG(a[i], u, oldV, i + 1), d[i]);
                    txt = Integer.toString(a[i]) + "," + Integer.toString(c[i]) + "," + temp1;
                    msg.append("Send: a = " +a[i] + ", c = " + c[i] + ", y = " + temp1 +"\n");
                }
                catch (NoSuchAlgorithmException ex) {}
            }
            else
            {
                try {
                    String temp1 = funkcjaG(a[i], c[i]);
                    String temp2 = Integer.toString (operationInG(a[i], u, oldV, i + 1) );
                    txt = temp1 + "," + temp2 + "," + Integer.toString(d[i]);
                    msg.append("Send: x = " + temp1 + ", xor = " + temp2 + ", d = " + d[i] + "\n");
                }
                catch (NoSuchAlgorithmException ex) {}
            }
            pw2.println(txt);
        }
        pw2.println("#EndCheck");
        txt = odczytane.readLine();
        if (txt.equals("Accept"))
            msg.append("#Transakcja zrealizowana \n");
        else
            msg.append("#Transakcja nie zrealizowana \n");

        pw2.println("#EndMsg");
   }

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
       // System.out.println("Hex format : " + sb.toString());
        String send = sb.toString();
        return send;
    }

    /** funkcja opublikowana przez bank */
    public static String funkcjaF (String x, String y) throws NoSuchAlgorithmException
    {
      //  System.out.println("fff : " + x);
      //  System.out.println("fff : " + y);
        String dane = x + y;
    //    System.out.println("fff dane : " + dane);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(dane.getBytes());
        byte byteData[] = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
   //     System.out.println("Hex format KONCOWY: " + sb.toString());
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
      //  System.out.println("Fukncja B przed modulo " + result);
        BigInteger koniec = result.mod(n);

        String send = koniec.toString();
        return send;
    }

    /** Do znajdywania odwrotnosci r modulo n */
    public int algorytmEuklidesa(int r)
    {
        int t;
        int a1, a2;
        int u, v, u2, v2;

        a2 = r;
        a1 = t = n;
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

        return x;
        // System.out.println("NWD=a = " + a1 + ", u = " + u + ", v = " + v + ", x = " + x);
    }


}
