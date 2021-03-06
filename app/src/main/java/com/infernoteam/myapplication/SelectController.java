
package com.infernoteam.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class SelectController extends AppCompatActivity {
    private BluetoothAdapter myBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_controller);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (myBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"N/A", Toast.LENGTH_LONG).show();
        }
        else if (!myBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            startActivity(new Intent(this,SelectController.class));
        }
        else {
            setMyListView();
        }
    }
    private void setMyListView(){
        ListView myListView = (ListView) findViewById(R.id.deviceListView);
        ArrayAdapter<String> BTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        myListView.setAdapter(BTArrayAdapter);
        Set<BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
            BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] deviceDetails = ((TextView) view).getText().toString().split("\n");
                SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("controllerName", deviceDetails[0]);
                editor.putString("controllerAddress", deviceDetails[1]);
                editor.apply();
                finish();
            }
        });
    }


}
