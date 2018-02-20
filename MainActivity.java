package com.groove.battlej.speechrecogdemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getName();

    private SpeechRecognizer mSpeechRecognizer;

    private Handler mHandler = new Handler();

    TextView responseText;

    Intent mSpeechIntent;

    boolean killCommanded = false;

    private static final String[] VALID_COMMANDS = {
            "what day is it",
            "where am I",
            "who are you",
            "exit"
    };

    private static final int VALID_COMMANDS_SIZE = VALID_COMMANDS.length;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        responseText = (TextView) findViewById(R.id.responseText);

   //     Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
   //     setSupportActionBar(toolbar);

   //     FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
   //     fab.setOnClickListener(new View.OnClickListener() {
   //         @Override
   //         public void onClick(View view) {
   //             Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
   //                     .setAction("Action", null).show();
   //         }
   //     });
    }

    @Override
    protected void onStart() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        SpeechListener mRecognitionListener = new SpeechListener();
        mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
        mSpeechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.androiddev101.ep8");

        // Given an hint to the recognizer about what the user is going to say
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        // Specify how many results you want to receive. The results will be sorted
        // where the first result is the one with higher confidence.
        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20);


        mSpeechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        //aqcuire the wakelock to keep the screen on until user exits/closes app
  //      final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
  //      this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
  //      this.mWakeLock.acquire();
        mSpeechRecognizer.startListening(mSpeechIntent);
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String getResponse(int command){
        Calendar c = Calendar.getInstance();

        String retString =  "Sorry, No can do";
        SimpleDateFormat dfDate_day;
        switch (command) {
            case 0:
                dfDate_day = new SimpleDateFormat("dd/MM/yyyy");
                retString= " Today is " + dfDate_day.format(c.getTime());
                break;
            case 1:
                retString = "In a box";
                break;
            case 2:
                retString = "Answering Machine";
                break;
            case 3:
                killCommanded = true;
                break;

            default:
                break;
        }
        return retString;
    }

    @Override
    protected void onPause() {
        //kill the voice recognizer
        if(mSpeechRecognizer != null){
            mSpeechRecognizer.destroy();
            mSpeechRecognizer = null;

        }
 //       this.mWakeLock.release();
        super.onPause();
    }

    private void processCommand(ArrayList<String> matchStrings){
        String response = "No can do.";
        int maxStrings = matchStrings.size();
        boolean resultFound = false;
        for(int i =0; i < VALID_COMMANDS_SIZE && !resultFound;i++){
            for(int j=0; j < maxStrings && !resultFound; j++){
                if(StringUtils.getLevenshteinDistance(matchStrings.get(j), VALID_COMMANDS[i]) <(VALID_COMMANDS[i].length() / 3) ){                    response = getResponse(i);
                }
            }
        }

        final String finalResponse = response;
        mHandler.post(new Runnable() {
            public void run() {
                responseText.setText(finalResponse);
            }
        });

    }
    class SpeechListener implements RecognitionListener {
        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "buffer recieved ");
        }
        public void onError(int error) {
            //if critical error then exit
            if(error == SpeechRecognizer.ERROR_CLIENT || error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS){
                Log.d(TAG, "client error");
            }
            //else ask to repeats
            else{
                Log.d(TAG, "other error");
                mSpeechRecognizer.startListening(mSpeechIntent);
            }
        }
        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent");
        }
        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "partial results");
        }
        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "on ready for speech");
        }
        public void onResults(Bundle results) {
            Log.d(TAG, "on results");
            ArrayList<String> matches = null;
            if(results != null){
                matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if(matches != null){
                    Log.d(TAG, "results are " + matches.toString());
                    final ArrayList<String> matchesStrings = matches;
                    processCommand(matchesStrings);
                    if(!killCommanded)
                        mSpeechRecognizer.startListening(mSpeechIntent);
                    else
                        finish();

                }
            }

        }
        public void onRmsChanged(float rmsdB) {
            //   Log.d(TAG, "rms changed");
        }
        public void onBeginningOfSpeech() {
            Log.d(TAG, "speech begining");
        }
        public void onEndOfSpeech() {
            Log.d(TAG, "speech done");
        }

    };

}
