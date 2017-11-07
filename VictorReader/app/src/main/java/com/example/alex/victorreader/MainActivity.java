package com.example.alex.victorreader;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;
    Button record, play, stop;
    MediaRecorder mediaRecorder;
    Vibrator vib;
    String audioFile = null;
    int fileNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                 + "/VictorReaderAudio");
        if(!audioDir.exists() && !audioDir.isDirectory())
            audioDir.mkdirs();

        GetAudioFilenames();

        // Set up buttons
        record = (Button) findViewById(R.id.btnRecord);
        play   = (Button) findViewById(R.id.btnPlay);
        stop = (Button) findViewById(R.id.btnStop);
        stop.setVisibility(View.INVISIBLE);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.US);
                }
            }
        });

        record.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                record.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);

                mediaRecorder = new MediaRecorder();

                //Set up MediaRecorder
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/VictorReaderAudio/" + Calendar.getInstance().getTime() + ".3gp";

                mediaRecorder.setOutputFile(audioFile);

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {

                    e.printStackTrace();
                }

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setVisibility(View.VISIBLE);
                stop.setVisibility(View.GONE);
                try {
                    mediaRecorder.stop();
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                }

                mediaRecorder.release();
                mediaRecorder = null;
            }
        });

        // Manages text to speech playback
        play.setOnClickListener(new View.OnClickListener() {

            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                String name;
                File [] files = GetAudioFilenames();

                if(fileNo == files.length)
                    fileNo = 0;

                for(fileNo = 0; fileNo < files.length; fileNo++) {
                    name = files[fileNo].getName();
                    tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);

                }

                ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(400, 10));
                //vib.vibrate(VibrationEffect.createOneShot(150,VibrationEffect.DEFAULT_AMPLITUDE));
//                tts.speak(testSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

            }
        });


    }

    // Checks the VictorReader path and returns all the file names
    private File [] GetAudioFilenames() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VictorReaderAudio";
        File dir = new File(path);
        File [] files = dir.listFiles();
        for(int i = 0; i < files.length; i++)
            Log.i("FILES", "File[" +i+"]: " + files[i].getName());


        return files;
    }
}
