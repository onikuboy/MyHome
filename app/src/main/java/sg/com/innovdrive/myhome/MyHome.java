package sg.com.innovdrive.myhome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Adapter;
import android.widget.AdapterView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.BufferedInputStream;
import android.net.wifi.WifiManager;
import android.content.Context;
import android.net.ConnectivityManager;
import java.util.HashMap;
import org.w3c.dom.Text;


public class MyHome extends AppCompatActivity {

    private TextView Speech2Text;
    private TextView TextKeyWords;
    private TextView URLReadBack;
    private ListView WiFiList;
    private TextView ActiveWiFiConnect;
    private ArrayList<String> ListWiFi;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home);
        Speech2Text = (TextView) findViewById(R.id.SpeechTextOutput);
        TextKeyWords = (TextView) findViewById(R.id.KeyWords);
        URLReadBack = (TextView) findViewById(R.id.URLResponse);
        WiFiList = (ListView) findViewById(R.id.configuredWiFiList);
        ActiveWiFiConnect = (TextView) findViewById(R.id.activeWiFi);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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


        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        int result = netInfo.getType();
        if(result == ConnectivityManager.TYPE_WIFI) {
            String ExtraInfo = netInfo.getExtraInfo();
            ActiveWiFiConnect.setText(ExtraInfo.toString());
        }
        else
        {
            ActiveWiFiConnect.setText("WiFi Not Connected");
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


                        }
                    }
                    break;

        }
    }


}
