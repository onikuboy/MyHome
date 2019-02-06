package sg.com.innovdrive.myhome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.io.InputStream;
import java.io.BufferedInputStream;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.net.ConnectivityManager;
import java.util.HashMap;
import org.w3c.dom.Text;
import java.lang.String;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import 	java.io.BufferedReader;

public class MyHome extends AppCompatActivity {

    private static final String TAG = "MyHome";
    private TextView Speech2Text;
    private TextView TextKeyWords;
    private TextView ReadResponse;
    private ListView WiFiList;
    private TextView ActiveWiFiConnect;
    private ArrayList<String> ListWiFi;

    Boolean KeyWordsUpdated = false;

    Handler UIHandler;
    Thread Thread1 = null;
    Thread Thread3 = null;


    public static final int ServerPortNumner = 80;
    public static final String  ServerIP = "192.168.1.39";

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home);
        Speech2Text = (TextView) findViewById(R.id.SpeechTextOutput);
        TextKeyWords = (TextView) findViewById(R.id.KeyWords);
        ReadResponse = (TextView) findViewById(R.id.getResponse);
        WiFiList = (ListView) findViewById(R.id.configuredWiFiList);
        ActiveWiFiConnect = (TextView) findViewById(R.id.activeWiFi);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        UIHandler = new Handler();


        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        int result = netInfo.getType();
        if(result == ConnectivityManager.TYPE_WIFI) {
            String ExtraInfo = netInfo.getExtraInfo();
            ActiveWiFiConnect.setText(ExtraInfo.toString());

            Object context = getApplicationContext();
            WifiManager wifiManager = (WifiManager) ((Context) context).getSystemService(WIFI_SERVICE);
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

            ListWiFi = new ArrayList<>();

            for( int count = 0 ; count < configuredNetworks.size() ; count++ ) {
                String input = configuredNetworks.get(count).SSID.toString();
                ListWiFi.add(input);
            }

            final StableArrayAdapter adapter = new StableArrayAdapter(this,android.R.layout.simple_list_item_1, ListWiFi);

            WiFiList.setAdapter(adapter);

            WiFiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ActiveWiFiConnect.setText(ListWiFi.get(i));
                }
            });
        }
        else
        {
            ActiveWiFiConnect.setText("WiFi Not Connected");
        }
/*
        this.Thread1 = new Thread(new Thread1());
        this.Thread1.start();
        this.Thread3 = new Thread(new Thread3());
        this.Thread3.start();
*/
    }

    class Thread1 implements Runnable{

        @Override
        public void run() {
            Socket socket = null;

            try{
                InetAddress ServerAddress = InetAddress.getByName(ServerIP);
                socket = new Socket(ServerAddress,ServerPortNumner);

                Thread2 commThread = new Thread2(socket);
                new Thread(commThread).start();
                return;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    class Thread2 implements Runnable{

        private Socket ClientSocket;
        private BufferedReader input;
        private BufferedOutputStream output;


        public Thread2(Socket ClientSocket){
            this.ClientSocket = ClientSocket;
            try{
                this.input = new BufferedReader(new InputStreamReader(this.ClientSocket.getInputStream()));

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try{
                    String read = input.readLine();
                    if(read != null){
                        UIHandler.post(new updateUIThread(read));
                    }
                    else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                }catch (IOException e){
                    e.printStackTrace();

                }
            }
        }
    }

    class Thread3 implements Runnable{

        @Override
        public void run() {
            Socket socket = null;

            try{
                InetAddress ServerAddress = InetAddress.getByName(ServerIP);
                socket = new Socket(ServerAddress,ServerPortNumner);

                Thread4 sendThread = new Thread4(socket);
                new Thread(sendThread).start();
                return;
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    class Thread4 implements Runnable{

        private Socket ClientSocket;

        public Thread4(Socket ClientSocket){
            this.ClientSocket = ClientSocket;
            try {
                String str = "GET / HTTP/1.0\\r\\n\\r\\n" ;
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(ClientSocket.getOutputStream())),
                        true);
                if(KeyWordsUpdated == true){
                    out.println(str);
                    out.flush();
                    KeyWordsUpdated = false;
                }
                else
                    out.close();


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {

                    Thread3 = new Thread(new Thread3());
                    Thread3.start();
                    return;

                }
            }
    }

    private class sendTCPData extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            Socket socket = null;

            try{
                InetAddress ServerAddress = InetAddress.getByName(ServerIP);
                socket = new Socket(ServerAddress,ServerPortNumner);

                Log.i(TAG, "Port Number is" + String.valueOf(socket.getLocalPort()));

                String str = "GET / HTTP/1.0\\r\\n\\r\\n" ;
                PrintWriter out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),
                        true);
//                if(KeyWordsUpdated == true){
                    out.println(str);
                    out.flush();
//                    KeyWordsUpdated = false;
//                }
//                else
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

    class KeyWordsTextChanged implements TextWatcher{
        private Context mContext;
        private TextView mTextview;

        public KeyWordsTextChanged(Context context, TextView edittextview) {
            super();
            this.mContext = context;
            this.mTextview = edittextview;

        }
        @Override
        public void afterTextChanged(Editable arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub

        }
    }

    class updateUIThread implements Runnable{
        private String msg;

        public updateUIThread(String str){
            this.msg = str;
        }

        @Override
        public void run() {
            ReadResponse.setText(msg);
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

                        if(TextOut.contentEquals("lights on") || TextOut.contentEquals("lights off") )
                        {
                            TextKeyWords.setText(TextOut);
                            //SendBroadcast(TextOut);
                            KeyWordsUpdated = true;

                            sendTCPData TCPData = new sendTCPData();
                            TCPData.execute();
                        }
                    }
                    break;

        }
    }



}
