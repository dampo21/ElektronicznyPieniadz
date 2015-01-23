package com.example.damian.ecash;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/** Activity Klienta z ustawieniami - takimi jak np adres i port Banku oraz Sklepu  */
public class ClientSettingsActivity extends Activity {

    Button saveButton;
    EditText ipAdressBank, portBank, ipAdressShop, portShop;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_settings);

        saveButton = (Button) findViewById(R.id.buttonSave);
        ipAdressBank = (EditText)findViewById(R.id.editIpBank);
        ipAdressShop = (EditText)findViewById(R.id.editIpShop);
        portBank = (EditText)findViewById(R.id.editPortBank);
        portShop = (EditText)findViewById(R.id.editPortShop);



        SharedPreferences settings = getSharedPreferences("PREFS", 0);

        ipAdressBank.setText(settings.getString("IpBank","192.168.1.10"));
        // portBank.setText(settings.getInt("PortBank",6000));
        ipAdressShop.setText(settings.getString("IpShop","192.168.1.11"));
        // portShop.setText(settings.getInt("PortShop",6000));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings = getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("IpBank",ipAdressBank.getText().toString());
                editor.putInt("PortBank",Integer.parseInt(portBank.getText().toString()));
                editor.putString("IpShop",ipAdressShop.getText().toString());
                editor.putInt("PortShop",Integer.parseInt(portShop.getText().toString()));
                editor.commit();
                Toast.makeText(getApplicationContext(), "Zapisano ustawienia", Toast.LENGTH_LONG).show();

                finish();

            } });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.client_settings, menu);
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
