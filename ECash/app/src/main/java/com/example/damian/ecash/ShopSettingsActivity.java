package com.example.damian.ecash;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/** Activity Sklepu z ustawieniami - takimi jak np adres i port Banku  */
public class ShopSettingsActivity extends Activity {
    Button saveButton;
    EditText ipAdressBank, portBank;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_settings);

        saveButton = (Button) findViewById(R.id.buttonSave);
        ipAdressBank = (EditText)findViewById(R.id.editIpBank);

        portBank = (EditText)findViewById(R.id.editPortBank);




        SharedPreferences settings = getSharedPreferences("PREFS", 0);

        ipAdressBank.setText(settings.getString("IpBank","192.168.1.10"));
        // portBank.setText(settings.getInt("PortBank",6000));


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences settings = getSharedPreferences("PREFS", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("IpBank",ipAdressBank.getText().toString());
                editor.putInt("PortBank",Integer.parseInt(portBank.getText().toString()));

                editor.commit();
                Toast.makeText(getApplicationContext(), "Zapisano ustawienia", Toast.LENGTH_LONG).show();

                finish();

            } });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shop_settings, menu);
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
