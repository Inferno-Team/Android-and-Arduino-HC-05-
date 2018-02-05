package com.infernoteam.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    OutputStream outputStream;
    InputStream inputStream;
    TextView controllerName;
    Fragment fragment;
    String address;
    BluetoothSocket socket;
    int handlerState=0;
    TextView _from;
    String language="en-US";
    CheckBox checkBox;
    ArrayList<String>ar;
    private     SpeechRecognizer sr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        controllerName = (TextView) findViewById(R.id.ControllerName);
        _from = (TextView) findViewById(R.id._from);
        checkBox=(CheckBox)findViewById(R.id.Checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (checkBox.isChecked())language="ar-JO";
                else language="en-US";
                System.out.println(language);
            }
        });
        sr = SpeechRecognizer.createSpeechRecognizer(this);
        sr.setRecognitionListener(new listener());
    }

    public void sendData(View view) {
        sendDataToHc_05();
    }
    private void sendDataToHc_05(){
        disconnect(true);
        BTConnect();
        BTSendData("send");
        ArrayList<String>arr_pin=new ArrayList<>();
        arr_pin.add("2");
        arr_pin.add("3");
        arr_pin.add("4");
        arr_pin.add("5");
        arr_pin.add("6");
        arr_pin.add("8");
        arr_pin.add("9");
        arr_pin.add("12");
        arr_pin.add("13");
        AutoSuggestAdapter adapter =
                new AutoSuggestAdapter
                        (this, android.R.layout.simple_list_item_1, arr_pin);

        final AlertDialog  dialog=new AlertDialog.Builder(this)
                .setView(R.layout.send_dialog)
                .setTitle("send data via bluetooth")
                .show();
        final AutoCompleteTextView input_pin =
                (AutoCompleteTextView)dialog. findViewById(R.id.input);

        Button button=(Button)dialog.findViewById(R.id.go);
        final Spinner spinner=(Spinner)dialog.findViewById(R.id.Spinner);
        ar=new ArrayList<>();
        ar.add("");
        ar.add("OFF");
        ar.add("ON");
        ArrayAdapter<String>ad=new ArrayAdapter<>
                (this,android.R.layout.simple_expandable_list_item_1,ar);
        assert spinner!= null;
        spinner.setAdapter(ad);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Toast.makeText(MainActivity.this, spinner.getSelectedItem().toString()
                            , Toast.LENGTH_SHORT).show();
                    if (input_pin != null && input_pin.getText().length() > 0) {
                        String send_mess = input_pin.getText().toString() + " " +
                                spinner.getSelectedItem().toString();


                        BTSendData(send_mess);
                    }else Toast.makeText(MainActivity.this,
                            "some think not correct please try again later", Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
        assert input_pin != null;
        input_pin.setAdapter(adapter);
        input_pin.setThreshold(1);
    }
    public void getData(View view) {
        BTConnect();
        BTSendData("receive");
        disconnect(false);
        BTConnect();
        BTSendData("H&T");
        BTGetData(socket);
    }

    private void BTGetData( BluetoothSocket socket)  {
        ConnectedThread connectedThread;
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    private void BTSendData(String str) {
        if(socket.isConnected()) {
            try {
                outputStream.write(str.getBytes());
                System.out.println(str);
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void BTConnect() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!address.equals("")) {
            BluetoothDevice device = adapter.getRemoteDevice(address);
            UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

            } catch (Exception ignored) {
            }
        } else Toast.makeText(this, "please connect to HC-05 device and Try again",
                Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onResume() {
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        String deviceName = preferences.getString("controllerName", "NA");
        address = preferences.getString("controllerAddress", "");
        controllerName.setText(deviceName);
        super.onResume();

    }

    public void selectController(View view) {
        Intent intent = new Intent(this, SelectController.class);
        startActivity(intent);
    }

    public void canceling(View view) {
        disconnect(true);
    }
    private void disconnect(boolean iss){
        if(socket!=null && socket.isConnected()){
            try {
                if(iss)BTSendData("close");
                socket.close();
                outputStream=null;
                inputStream=null;
                socket=null;
                fragment=new MyFragmentManager();
                FragmentManager manager=getSupportFragmentManager();
                Bundle x=new Bundle();
                x.putString("mess","PAUSED !!");
                fragment.setArguments(x);
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment,fragment);
                transaction.commit();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void speechToText(View view) {
        promptSpeechInput();
    }


    private class ConnectedThread extends Thread {
        int avilableBytes = 0;

         ConnectedThread(BluetoothSocket socket) {
            InputStream temp = null;
            try {
                temp = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
             System.out.println(temp);
        }

        public void run() {
            try {
                int bytes;
                String readMessage="";
                while (true) {
                    try {
                        if (inputStream!=null){
                            avilableBytes=inputStream.available();
                            byte[] buffer = new byte[avilableBytes];
                            if (avilableBytes > 0) {
                                bytes = inputStream.read(buffer);
                                readMessage += new String(buffer);
                                if (bytes > 2) {

                                    bt_handler.
                                            obtainMessage(handlerState, bytes, -1, readMessage)
                                            .sendToTarget();
                                    readMessage="";
                                } else {
                                    SystemClock.sleep(100);
                                }
                            }
                        }else break;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("HandlerLeak")
    Handler bt_handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==handlerState){
                String readMessage=(String)msg.obj;
                fragment=new MyFragmentManager();
                FragmentManager manager=getSupportFragmentManager();
                Bundle x=new Bundle();
                x.putString("mess",readMessage);
                fragment.setArguments(x);
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment,fragment);
                transaction.commitNow();
                Log.v("TAG", readMessage);
            }
        }
    };
    private void promptSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,language);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.infernoteam.myapplication");
        //   intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 20000); // value to wait

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);  // 1 is the maximum number of results to be returned.
        sr.startListening(intent);
    }
    class listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params){ }
        public void onBeginningOfSpeech(){ }
        public void onRmsChanged(float rmsdB){ }
        public void onBufferReceived(byte[] buffer) { }
        public void onEndOfSpeech(){ }
        public void onError(int error) {
            Toast.makeText(MainActivity.this,
                    "please connect to internet\n"
                    +"error code : "+error
                    , Toast.LENGTH_LONG).show();
        }
        public void onResults(Bundle results) {
            ArrayList<String> result = results.
                    getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            assert result != null;
            _from.setText(result.get(0));
            switch (result.get(0)){
                case "select controller":{
                    Intent intent = new Intent(MainActivity.this,
                            SelectController.class);
                    startActivity(intent);}break;
                case "receive":{
                    BTConnect();
                    BTSendData("H&T");
                    BTGetData(socket);
                }break;
                case "استلام":{
                    BTConnect();
                    BTSendData("H&T");
                    BTGetData(socket);
                }break;
                case "اغلاق":{finish();}break;
                case"قطع الاتصال":{disconnect(true);}break;
                case "اختيار الجهاز":{
                    Intent intent = new Intent(MainActivity.this,
                            SelectController.class);
                    startActivity(intent);
                }break;
                case"لغه انجليزيه":{language="en-US";checkBox.setChecked(false);}break;
                case "مساعده":{help();}break;
                case "help":{help();}break;
                case "disconnect":{disconnect(true);}break;
                case "close":{finish();}break;
                case "Arabic language":{language="ar-JO";checkBox.setChecked(true);}break;
                case "send":{sendDataToHc_05();}break;
                case "ارسال":{sendDataToHc_05();}break;
                default:
                    Toast.makeText(MainActivity.this,
                            "No match try again please", Toast.LENGTH_SHORT).show();
            }
        }
        public void onPartialResults(Bundle partialResults){

        }
        public void onEvent(int eventType, Bundle params) {

        }
        private void help(){
            ArrayList<String>helps=new ArrayList<>();
            if(language.equals("ar-JO")){
                helps=new ArrayList<>();
                helps.add("اغلاق");
                helps.add("قطع الاتصال");
                helps.add("اختيار الجهاز");
                helps.add("اغلاق");
                helps.add("مساعده");
                helps.add("ارسال");
                helps.add("استلام");
                helps.add("لغه انجليزيه");

            }else if(language.equals("en-US")){
                helps=new ArrayList<>();
                helps.add("close");
                helps.add("disconnect");
                helps.add("select controller");
                helps.add("close");
                helps.add("help");
                helps.add("send");
                helps.add("receive");
                helps.add("Arabic language");
            }
            fragment=new MyFragmentManager();
            Bundle bundle=new Bundle();
            bundle.putString("mess","help");
            bundle.putSerializable("helps",helps);
            FragmentManager manager = getSupportFragmentManager();
            fragment.setArguments(bundle);
            FragmentTransaction transaction=manager.beginTransaction();
            transaction.replace(R.id.fragment,fragment);
            transaction.commit();
        }
    }
}

