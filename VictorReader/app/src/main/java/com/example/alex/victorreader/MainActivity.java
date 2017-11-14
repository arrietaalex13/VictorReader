package com.example.alex.victorreader;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
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
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextToSpeech tts;
    Button record, play, stop, playBack, navigate;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Vibrator vib;
    String audioFile = null;
    int fileNo, numOfFiles, currentFile;
    File [] allAudioFiles;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                 + "/VictorReaderAudio");
        if(!audioDir.exists() && !audioDir.isDirectory())
            audioDir.mkdirs();

        Refresh();

        // Set up buttons
        record = (Button) findViewById(R.id.btnRecord);
        play   = (Button) findViewById(R.id.btnPlay);
        stop = (Button) findViewById(R.id.btnStop);
        playBack = (Button) findViewById(R.id.btnPlayback);
        navigate = (Button) findViewById(R.id.btnNavigate);

        //Need to check if any recordings are available, otherwise it crashes
        if(GetAudioFilenames().length == 0)
            playBack.setVisibility(View.GONE);

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
                            "/VictorReaderAudio/" +
                            new Date(System.currentTimeMillis()).toString()
                            + ".3gp";

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
                playBack.setVisibility(View.VISIBLE);

                try {
                    mediaRecorder.stop();
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                }

                mediaRecorder.release();
                mediaRecorder = null;

                Refresh();
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

                // No delay to start, vib for 50 ms, sleep for 400 ms, vib for 200 ms
                long [] pattern = {0, 100, 400, 200};
                //((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(400, 10));
                if(Build.VERSION.SDK_INT >= 26)
                    vib.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vib.vibrate(pattern, -1);
//                tts.speak(testSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

            }
        });

        playBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Protects against someone pushing playback before navigation
                if(currentFile == -1)
                    currentFile = 0;

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource("file://" + allAudioFiles[currentFile].getAbsolutePath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer = null;
            }
        });

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = "";
                currentFile++;

                if(numOfFiles == 0)
                    toSpeak = "Sorry, you don't have any files to play.";
                else if(numOfFiles > 0 && currentFile < numOfFiles)
                    toSpeak = allAudioFiles[currentFile].getName();
                else if(currentFile == numOfFiles) {
                    toSpeak = allAudioFiles[0].getName();
                    currentFile = -1;
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

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

    // Refreshes data in application including number of files
    private void Refresh() {
        allAudioFiles = GetAudioFilenames();
        numOfFiles = allAudioFiles.length;
        currentFile = -1;
    }

}
