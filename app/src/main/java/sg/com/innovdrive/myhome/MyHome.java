package sg.com.innovdrive.myhome;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MyHome extends AppCompatActivity {

    private TextView Speech2Text;
    private TextView TextKeyWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_home);
        Speech2Text = (TextView) findViewById(R.id.SpeechTextOutput);
        TextKeyWords = (TextView) findViewById(R.id.KeyWords);
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
                            TextKeyWords.setText(TextOut);

                    }
                    break;

        }
    }


}
