package com.example.alex.victorreader;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

/**
 * The class where the application runs from.
 */
public class MainActivity extends AppCompatActivity {

    private final static int ROWS = 2;
    private final static int COLS = 3;

    private Integer recordTimes;
    private Integer playbackTimes;
    private Integer navigationTimes;

    /**
     * Used to read back file names.
     */
    private TextToSpeech tts;

    /**
     * It creates a MediaRecorder object to be used and sets up the destination
     *  path for the file. It uses the date and time for filename and starts recording, disappears,
     *  and stops the recording when the stop button is clicked.
     */
    private Button record;

    //private Button play;

    /**
     * This button only appears after the record button has been clicked. It releases
     *  the MediaRecorder object used and updates the list of audio files to include the
     *  audio that was just recorded.
     */
    private Button stop;

    /**
     * This is responsible for playing back the audio files recorded. It uses
     *  the file selected from the navigate button.
     */
    private Button playBack;

    /**
     * It cycles through the list of files and reads the filename aloud that
     *  could be played back.
     */
    private Button navigate;

    /**
     * Object that allows user to record audio.
     */
    private MediaRecorder mediaRecorder;

    /**
     * Object that allows user to play back audio.
     */
    private MediaPlayer mediaPlayer;

    //private Vibrator vib;

    /**
     * Used to name the file to be recorded.
     */
    private String audioFile = null;

    /**
     * Used to locate the log file.
     */
    private File logFile;

    private int fileNo;      // Not quite sure. may delete

    /**
     * The total number of recordings created by the application.
     */
    private int numOfFiles;

    /**
     * The current file to be played back if button is pressed.
     */
    private int currentFile;

    /**
     * Array containing all of the audio files created by the application.
     */
    private File [] allAudioFiles;

    /**
     * Array containing buttons that will map with the overlay.
     */
    public Button [][] btnArray;

    /**
     * Array containing true/false corresponding to any given button that was clicked.
     * True for clicked. False for not clicked.
     */
    public static boolean [][] clickedArray = new boolean[ROWS][COLS];

    private GridLayout gridLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnArray = new Button[ROWS][COLS];

        gridLayout = (GridLayout) findViewById(R.id.gridlayout);

        InitializeButtonMatrix();
        //ResetClickedMatrix();

        recordTimes = new Integer(0);
        playbackTimes = new Integer(0);
        navigationTimes = new Integer(0);

        Refresh();

        // Sets up log file pathname
        logFile = new File (Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/VictorReaderAudio/LogFile.txt");
        ClearFile(logFile);

        // Configures buttons to interact with UI
        record   = (Button) findViewById(R.id.btnRecord);
        //play     = (Button) findViewById(R.id.btnPlay);
        stop     = (Button) findViewById(R.id.btnStop);
        playBack = (Button) findViewById(R.id.btnPlayback);
        navigate = (Button) findViewById(R.id.btnNavigate);

        //Need to check if any recordings are available, otherwise it crashes
        if(GetAudioFilenames().length == 0)
            playBack.setVisibility(View.GONE);

        stop.setVisibility(View.INVISIBLE);

        // Sets up vibrator to be synced with phones vibrator
        //vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                        tts.setLanguage(Locale.US);
                }
            }
        });

        /**
         * RECORD:
         * Method that controls what happens when the record button is clicked
         *  It creates a MediaRecorder object to be used and sets up the destination
         *  path for the file. It uses the date and time for filename and starts recording
         *  until the stop button is clicked.
         */
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordTimes++;

                // Makes stop button visible and record button invisible
                record.setVisibility(View.INVISIBLE);
                stop.setVisibility(View.VISIBLE);

                mediaRecorder = new MediaRecorder();

                //Sets up MediaRecorder
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

        /**
         * STOP:
         * Method that controls what happens when the stop button is clicked
         *  This button only appears after the record button has been clicked. It releases
         *  the MediaRecorder object used and updates the list of audio files to include the
         *  audio that was just recorded.
         */
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

//        /**
//         *
//         */
//        play.setOnClickListener(new View.OnClickListener() {
//
//            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//            @Override
//            public void onClick(View v) {
//                String name;
//                File [] files = GetAudioFilenames();
//
//                if(fileNo == files.length)
//                    fileNo = 0;
//
//                for(fileNo = 0; fileNo < files.length; fileNo++) {
//                    name = files[fileNo].getName();
//                    tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, null);
//
//                }
//
//                // No delay to start, vib for 100 ms, sleep for 400 ms, vib for 200 ms
//                long [] pattern = {0, 100, 400, 200};
//                //((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(400, 10));
//                if(Build.VERSION.SDK_INT >= 26)
//                    vib.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
//                else
//                    vib.vibrate(pattern, -1);
////                tts.speak(testSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
//
//            }
//        });

        /**
         * PLAYBACK:
         * Method that controls what happens when the playBack button is clicked
         *  This is responsible for playing back the audio files recorded. It uses
         *  the file selected from the navigate button.
         */
        playBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playbackTimes++;

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

        /**
         * NAVIGATE:
         * Method that controls what happens when the navigate button is clicked
         *  It cycles through the list of files and reads the filename aloud that
         *  could be played back.
         */
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigationTimes++;
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
    @Override
    protected void onPause() {
        super.onPause();

        WriteToLog("Record Button:   " + recordTimes.toString());
        WriteToLog("Playback Button: " + playbackTimes.toString());
        WriteToLog("Navigate Button: " + navigationTimes.toString());
    }

    /**
     * Checks the VictorReader path and returns all the file names
     * @return An array of all of the recorded files in the directory created by the application
     */
    private File [] GetAudioFilenames() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/VictorReaderAudio";
        File dir = new File(path);
        File [] files = dir.listFiles();

        for(int i = 0; i < files.length; i++)
            Log.i("FILES", "File[" +i+"]: " + files[i].getName());

        return files;
    }

    /**
     * Refreshes data in application including number of files and the current file
     * that the navigation is on.
     */
    private void Refresh() {
        allAudioFiles = GetAudioFilenames();
        numOfFiles = allAudioFiles.length;
        currentFile = -1;
    }

    /**
     * Creates directory that will store all of the audio files created using the application
     */
    private void CreateDirectory() {
        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/VictorReaderAudio");

        if(!audioDir.exists() && !audioDir.isDirectory())
            audioDir.mkdirs();
    }

    /**
     * Clears out the file that is passed into the function by writing a blank string to it
     * @param file The file to be cleared
     */
    private void ClearFile(File file) {
        try {
            FileOutputStream writer = new FileOutputStream(file);
            writer.write(("").getBytes());
            writer.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void WriteToLog(String text) {
        FileOutputStream output;

        try {
            // Appends to file
            output = new FileOutputStream(logFile, true);

            output.write(text.getBytes());
            output.write("\n".getBytes());

            output.close();
        }
        catch(FileNotFoundException e) {

        }
        catch (IOException e) {

        }
    }

    private void InitializeButtonMatrix() {
        btnArray[0][0] = (Button) findViewById(R.id.button1);
        btnArray[0][1] = (Button) findViewById(R.id.button2);
        btnArray[0][2] = (Button) findViewById(R.id.button3);
        btnArray[1][0] = (Button) findViewById(R.id.button4);
        btnArray[1][1] = (Button) findViewById(R.id.button5);
        btnArray[1][2] = (Button) findViewById(R.id.button6);

        for(int i = 0; i < ROWS; i++)
            for(int j = 0; j < COLS; j++)
                btnArray[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch(v.getId()) {
                            case R.id.button1 : MainActivity.clickedArray[0][0] = true;
                                Log.i("PRESSED", "state1: " + MainActivity.clickedArray[0][0]);
                                break;

                            case R.id.button2 : MainActivity.clickedArray[0][1] = true;
                                Log.i("PRESSED", "state2: " + MainActivity.clickedArray[0][1]);
                                break;


                            case R.id.button3 : MainActivity.clickedArray[0][2] = true;
                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[0][2]);
                                break;

                            case R.id.button4 : MainActivity.clickedArray[1][0] = true;
                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][0]);
                                break;

                            case R.id.button5 : MainActivity.clickedArray[1][1] = true;
                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][1]);
                                break;

                            case R.id.button6 : MainActivity.clickedArray[1][2] = true;
                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][2]);
                                break;
                        }
                    }
                });
    }

    private void ResetClickedMatrix() {
        for(int i = 0; i < ROWS; i++)
            for(int j = 0; j < COLS; j++)
                clickedArray[i][j] = false;
    }

}

