package com.example.damian.ecash;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


/** Dodatkowe activity Klienta gdzie wyswietlane sa parametry */
public class ClientShowActivity extends Activity {

    Button closeButton;
    TextView consoleText;
    ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_show);

        closeButton = (Button) findViewById(R.id.button);
        consoleText = (TextView) findViewById(R.id.textConsole);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        Bundle przekazanedane = getIntent().getExtras();
        String przekazanytekst = przekazanedane.getString("dane");
        String[] parts = przekazanytekst.split(",");

        String [] a = new String [10];
        String [] c = new String [10];
        String [] d = new String [10];
        String [] r = new String [10];

        for (int i=0; i < 10; i++)
        {
            a[i] = parts[i*4];
            c[i] = parts[1+(i*4)];
            d[i] = parts[2+(i*4)];
            r[i] = parts[3+(i*4)];
        }

        write ("a = " + a[0]);
        for (int i=1; i < 10; i++)
            write (", " + a[i]);
        write("\n");

        write ("c = " + c[0]);
        for (int i=1; i < 10; i++)
            write (", " + c[i]);
        write("\n");

        write ("d = " + d[0]);
        for (int i=1; i < 10; i++)
            write (", " + d[i]);
        write("\n");

        write ("r = " + r[0]);
        for (int i=1; i < 10; i++)
            write (", " + r[i]);
        write("\n");

        write("u = " + parts[40] + ", v = " + parts[41] + ", n = " + parts[42] + "\n");
        for (int i=0; i< Integer.parseInt(parts[43]); i++)
            write("C"+(i+1) + " = " + parts[44+i] + "\n");

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            } });

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client_show, menu);
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
}
