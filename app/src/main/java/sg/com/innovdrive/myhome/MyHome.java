package sg.com.innovdrive.myhome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.net.wifi.WifiManager;
import android.content.Context;
import android.net.ConnectivityManager;
import java.util.HashMap;
import java.lang.String;

public class MyHome extends AppCompatActivity {

    private static final String TAG = "MyHome";
    private TextView Speech2Text;
    private TextView TextKeyWords;
    private TextView ReadResponse;
    private ListView WiFiList;
    private TextView ActiveWiFiConnect;
    private ArrayList<String> ListWiFi;
    private EditText IP3124;
    private EditText IP2316;
    private EditText IP1508;
    private EditText IP0700;
    private EditText SelectedPortNumber;

    private int ServerPortNumner = 80;
    private String  ServerIP = "192.168.1.39";
    private sendTCPData TCPData;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home);
        Speech2Text = (TextView) findViewById(R.id.SpeechTextOutput);
        TextKeyWords = (TextView) findViewById(R.id.KeyWords);
        WiFiList = (ListView) findViewById(R.id.configuredWiFiList);
        ActiveWiFiConnect = (TextView) findViewById(R.id.activeWiFi);
        IP3124 = (EditText) findViewById(R.id.editIP3124);
        IP2316 = (EditText) findViewById(R.id.editIP2316);
        IP1508 = (EditText) findViewById(R.id.editIP1508);
        IP0700 = (EditText) findViewById(R.id.editIP0700);
        SelectedPortNumber = (EditText) findViewById(R.id.editIPort);


        final ArrayList<Integer> selectedSSID = new ArrayList<>();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);


        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        int result = netInfo.getType();
        if(result == ConnectivityManager.TYPE_WIFI) {
            String ExtraInfo = netInfo.getExtraInfo();
            ActiveWiFiConnect.setText(ExtraInfo.toString());

            Object context = getApplicationContext();
            final WifiManager wifiManager = (WifiManager) ((Context) context).getSystemService(WIFI_SERVICE);
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

            ListWiFi = new ArrayList<>();

            for( int count = 0 ; count < configuredNetworks.size() ; count++ ) {
                String input = configuredNetworks.get(count).SSID.toString();
                int numberSSID = configuredNetworks.get(count).networkId;
                selectedSSID.add(numberSSID);
                ListWiFi.add(input);
            }

            final StableArrayAdapter adapter = new StableArrayAdapter(this,android.R.layout.simple_list_item_1, ListWiFi);

            WiFiList.setAdapter(adapter);

            WiFiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ActiveWiFiConnect.setText(ListWiFi.get(i));

                    changeWifiSSID ChangeWiifNetwork = new changeWifiSSID();
                    TypesParameters parameters = new TypesParameters(wifiManager,selectedSSID.get(i));
                    ChangeWiifNetwork.execute(parameters);


                }
            });
        }
        else
        {
            ActiveWiFiConnect.setText("WiFi Not Connected");
        }
    }

    private static class TypesParameters{
        WifiManager wifiManager;
        int SSID;

        TypesParameters(WifiManager wifiManager, int SSID){

            this.wifiManager = wifiManager;
            this.SSID = SSID;
        }


    }

    private class changeWifiSSID extends AsyncTask<TypesParameters, Void, Void>{


        @Override
        protected Void doInBackground(TypesParameters... typesParameters) {

            WifiManager WIFIManager = typesParameters[0].wifiManager;
            int SSID = typesParameters[0].SSID;


            WIFIManager.disconnect();

            WIFIManager.enableNetwork(SSID,true);

            return null;
        }
    }

    private class sendTCPData extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... input) {

            Socket socket = null;
            String str = "";

            try{
                InetAddress ServerAddress = InetAddress.getByName(ServerIP);
                socket = new Socket(ServerAddress,ServerPortNumner);

                Log.i(TAG, "Port Number is" + String.valueOf(socket.getLocalPort()));

                if(input[0].equals("high"))
                    str = "GET /H HTTP/1.0\\r\\n\\r\\n" ;

                if(input[0].equals("low"))
                    str = "GET /L HTTP/1.0\\r\\n\\r\\n" ;

                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
                    out.println(str);
                    out.flush();
                    out.close();
                    socket.close();

                    while(socket.isClosed() == false);

                    Log.i(TAG,"Socket and Output Closed");

            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    public void setIPAddress(View view) {

        ServerIP = "";
        ServerIP = IP3124.getText().toString();
        ServerIP += ".";
        ServerIP += IP2316.getText().toString();
        ServerIP += ".";
        ServerIP += IP1508.getText().toString();
        ServerIP += ".";
        ServerIP += IP0700.getText().toString();

        ServerPortNumner = Integer.parseInt(SelectedPortNumber.getText().toString());


    }

    public void getSpeechInput(View view) {

        Intent SpeechTextIntent =  new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SpeechTextIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        SpeechTextIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(SpeechTextIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(SpeechTextIntent,10);
        else
        {
            Toast.makeText(this, "Your Device Do Not Support Speech Input", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case 10:
                    if (resultCode == RESULT_OK && data != null)
                    {
                        ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        Speech2Text.setText(result.get(0));

                        String TextOut = "";

                        if (result.get(0).contains("Lights") || result.get(0).contains("lights"))
                            TextOut = "lights ";

                        if (result.get(0).contains("On") || result.get(0).contains("on"))
                            TextOut += "on";

                        if (result.get(0).contains("Off") || result.get(0).contains("off"))
                            TextOut += "off";

                        if(TextOut.contentEquals("lights on"))
                        {
                            TextKeyWords.setText(TextOut);
                            TCPData = new sendTCPData();
                            String output = "high";
                            TCPData.execute(output);
                        }

                        if(TextOut.contentEquals("lights off"))
                        {
                            TextKeyWords.setText(TextOut);
                            TCPData = new sendTCPData();
                            String output = "low";
                            TCPData.execute(output);
                        }


                    }
                    break;

        }
    }



}
