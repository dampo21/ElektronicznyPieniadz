package com.example.damian.ecash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/** Glowne Activiti - startowe
 *
 * @author Damian Pobrotyn
 * @author Kamil Rostecki
 * */
public class MyActivity extends Activity {

    /** Przycisk uruchamiajacy aplikacje Banku */
    public Button buttonBank;
    /** Przycisk uruchamiajacy aplikacje Klienta */
    public Button buttonClient;
    /** Przycisk uruchamiajacy aplikacje Sklepu */
    public Button buttonShop;
    /** Przycisk zamykajacy aplikacje */
    public Button buttonExit;
    TextView textHeader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        buttonBank = (Button) findViewById(R.id.buttonBank);
        buttonClient = (Button) findViewById(R.id.buttonClient);
        buttonShop = (Button) findViewById(R.id.buttonShop);
        buttonExit = (Button) findViewById(R.id.buttonExit);
        textHeader =  (TextView) findViewById(R.id.textHeader);

        textHeader.setText(textHeader.getText()+"  : ");

        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

   Intent startClientActivity = new Intent(getApplicationContext(), ClientActivity.class);
                startActivity(startClientActivity);


            }

        });



        buttonBank.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent startBankActivity = new Intent(getApplicationContext(), BankActivity.class);
                                            startActivity(startBankActivity);

        } });


        buttonShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startShopActivity = new Intent(getApplicationContext(), ShopActivity.class);
                startActivity(startShopActivity);
            } });




        buttonExit.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View view) {
                                          finish();
                                         // System.exit(0);
                                             android.os.Process.killProcess(android.os.Process.myPid());
                                             System.exit(1);
                                            // Intent intent = new Intent(Intent.ACTION_MAIN);
                                           //  intent.addCategory(Intent.CATEGORY_HOME);
                                           //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                           //  startActivity(intent);
                                         }

                                     }
        );


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
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
