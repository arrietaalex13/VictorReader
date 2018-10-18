package com.example.alex.victorreader;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceInputTv;
    private ImageButton mSpeakBtn;

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
     * Allows Speech to text capabilites
     */
    private Button talk;

    private Button delete;

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

    public Button [] brailleAr;

    /**
     * Array containing true/false corresponding to any given button that was clicked.
     * True for clicked. False for not clicked.
     */
    public static boolean [][] clickedArray = new boolean[ROWS][COLS];

    public static boolean [] brailleClickedAr = new boolean[7];

    private GridLayout gridLayout;
    private GridLayout brailleLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //btnArray = new Button[ROWS][COLS];
        //brailleAr = new Button[7];

        //gridLayout = (GridLayout) findViewById(R.id.gridlayout);
//        brailleLayout = (GridLayout) findViewById(R.id.braillelayout);
//
//        InitializeBrailleMatrix();

        //InitializeButtonMatrix();
        //ResetClickedMatrix();

        CreateDirectory();

        mVoiceInputTv = (TextView) findViewById(R.id.voiceInput);

        recordTimes = new Integer(0);
        playbackTimes = new Integer(0);
        navigationTimes = new Integer(0);


        Refresh();
        mVoiceInputTv.setText("Welcome!");

        // Sets up log file pathname
        logFile = new File (Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/LogFile.txt");
        WriteToLog("Application opened");
//        ClearFile(logFile);

        // Configures buttons to interact with UI
        record   = (Button) findViewById(R.id.btnRecord);
        //play     = (Button) findViewById(R.id.btnPlay);
        stop     = (Button) findViewById(R.id.btnStop);
        playBack = (Button) findViewById(R.id.btnPlayback);
        navigate = (Button) findViewById(R.id.btnNavigate);
        talk     = (Button) findViewById(R.id.stt);
        delete   = (Button) findViewById(R.id.btnDelete);

        //Need to check if any recordings are available, otherwise it crashes
//        if(GetAudioFilenames().length == 0)
//            playBack.setVisibility(View.GONE);

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
                try {
                    mVoiceInputTv.setText("Recording...");

                    SimpleDateFormat formatLog = new SimpleDateFormat("MMM-dd  hh:mm a");
                    String dateLog = formatLog.format(Date.parse(new Date(System.currentTimeMillis()).toString()));
                    WriteToLog("\'Record\' clicked on   " + dateLog);
                    recordTimes++;

                    // Makes stop button visible and record button invisible
//                record.setVisibility(View.INVISIBLE);
//                stop.setVisibility(View.VISIBLE);

                    mediaRecorder = new MediaRecorder();

                    //Sets up MediaRecorder
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    SimpleDateFormat format = new SimpleDateFormat("MMM-dd  hh:mm:ss");
                    String date = format.format(Date.parse(new Date(System.currentTimeMillis()).toString()));

                    audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/VictorReaderAudio/" + date + ".3gp";

                    mediaRecorder.setOutputFile(audioFile);

                    try {
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                } catch(Exception e) {
                    Log.i("Record Error", "ititt");
                    WriteToLog("Record Error");
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
                try {
                    SimpleDateFormat format = new SimpleDateFormat("MMM-dd  hh:mm a");
                    String date = format.format(Date.parse(new Date(System.currentTimeMillis()).toString()));
                    WriteToLog("\'Stop\' clicked on     " + date);
//                record.setVisibility(View.VISIBLE);
//                stop.setVisibility(View.GONE);
//                playBack.setVisibility(View.VISIBLE);

                    try {
                        mediaRecorder.stop();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }

                    mediaRecorder.release();
                    mediaRecorder = null;

                    Refresh();
                } catch(Exception e) {
                    Log.i("Stop Error", "asasas");
                    WriteToLog("Stop Error");
                }
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
                SimpleDateFormat format = new SimpleDateFormat("MMM-dd  hh:mm a");
                String date = format.format(Date.parse(new Date(System.currentTimeMillis()).toString()));
                WriteToLog("\'Playback\' clicked on " + date);
                playbackTimes++;

                if(allAudioFiles.length != 0) {
                    try {
                        mediaPlayer = new MediaPlayer();
                        try {
                            mVoiceInputTv.setText("Playing file " + currentFile);
                            mediaPlayer.setDataSource("file://" + allAudioFiles[currentFile].getAbsolutePath());
                            mediaPlayer.prepare();
                            mediaPlayer.start();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mediaPlayer = null;
                    } catch (Exception e) {
                        WriteToLog("Playback pushed without file loaded");
                    }
                }
                else
                    mVoiceInputTv.setText("No files to play.");
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
                SimpleDateFormat format = new SimpleDateFormat("MMM-dd  hh:mm a");
                String date = format.format(Date.parse(new Date(System.currentTimeMillis()).toString()));
                WriteToLog("\'Navigate\' clicked on " + date);
                navigationTimes++;
                String toSpeak = "";
                currentFile++;

                if(numOfFiles == 0)
                    toSpeak = "Sorry, you don't have any files to play.";
                else if(numOfFiles > 0 && currentFile < numOfFiles)
//                    toSpeak = allAudioFiles[currentFile].getName();
                    toSpeak = "File " + currentFile;
                else if(currentFile == numOfFiles) {
//                    toSpeak = allAudioFiles[0].getName();
                    toSpeak = "File " + 0;
                    currentFile = 0;
                }
                UpdateTextView();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);

            }
        });

        talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVoiceInput();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SimpleDateFormat format = new SimpleDateFormat("MMM-dd  hh:mm a");
                String date = format.format(Date.parse(new Date(System.currentTimeMillis()).toString()));
                WriteToLog("\'Delete\' clicked on   " + date);
                if(allAudioFiles.length !=0) {
                    allAudioFiles[currentFile].delete();
                    mVoiceInputTv.setText("Deleted file " + currentFile);
                    Refresh();
                }
                else
                    mVoiceInputTv.setText("No file to delete.");
            }
        });


    }
    @Override
    protected void onPause() {
        super.onPause();
        WriteToLog("Application closed");

//        WriteToLog("Record Button:   " + recordTimes.toString());
//        WriteToLog("Playback Button: " + playbackTimes.toString());
//        WriteToLog("Navigate Button: " + navigationTimes.toString());
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
        currentFile = allAudioFiles.length-1;

        UpdateTextView();
//        String str = new String();
//
//        for(int i = 0; i < allAudioFiles.length; i++)
//            str += allAudioFiles[i].getName() + ", ";
//
//        mVoiceInputTv.setText(str);
    }

    /**
     * Creates directory that will store all of the audio files created using the application
     */
    private void CreateDirectory() {
        Log.i("DIR: ", "In directory method");
        File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/VictorReaderAudio");

        if(!audioDir.exists() && !audioDir.isDirectory()) {
            audioDir.mkdirs(); Log.i("DIR: ", "Created directory"); }
    }

    /**
     * Clears out the file that is passed into the function by writing a blank string to it
     * @param file The file to be cleared
     */
//    private void ClearFile(File file) {
//        try {
//            FileOutputStream writer = new FileOutputStream(file);
//            writer.write(("").getBytes());
//            writer.close();
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

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
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void UpdateTextView() {
        if(allAudioFiles.length != 0) {
            mVoiceInputTv.setText("File " + currentFile + " of " + (allAudioFiles.length - 1));
            mVoiceInputTv.append("\nFilename: " + allAudioFiles[currentFile].getName());
        }
    }

//    private void InitializeBrailleMatrix() {
//        brailleAr[0] = (Button) findViewById(R.id.button7);
//        brailleAr[1] = (Button) findViewById(R.id.button8);
//        brailleAr[2] = (Button) findViewById(R.id.button9);
//        brailleAr[3] = (Button) findViewById(R.id.button10);
//        brailleAr[4] = (Button) findViewById(R.id.button11);
//        brailleAr[5] = (Button) findViewById(R.id.button12);
//        brailleAr[6] = (Button) findViewById(R.id.button13);
//
//        for(int i = 0; i < 7; i++) {
//            brailleAr[i].setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        switch(v.getId()) {
//                            case R.id.button7 : MainActivity.brailleClickedAr[0] = true;
//                                Log.i("PRESSED", "state1: " + MainActivity.clickedArray[0][0]);
//                                break;
//
//                            case R.id.button8 : MainActivity.brailleClickedAr[1] = true;
//                                Log.i("PRESSED", "state2: " + MainActivity.clickedArray[0][1]);
//                                break;
//
//
//                            case R.id.button9 : MainActivity.brailleClickedAr[2] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[0][2]);
//                                break;
//
//                            case R.id.button10 : MainActivity.brailleClickedAr[3] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][0]);
//                                break;
//
//                            case R.id.button11 : MainActivity.brailleClickedAr[4] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][1]);
//                                break;
//
//                            case R.id.button12 : MainActivity.brailleClickedAr[5] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][2]);
//                                break;
//
//                            case R.id.button13 : MainActivity.brailleClickedAr[6] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][2]);
//
//                        }
//                    }
//                });
//
//        }
//    }

//    private void InitializeButtonMatrix() {
//        btnArray[0][0] = (Button) findViewById(R.id.button1);
//        btnArray[0][1] = (Button) findViewById(R.id.button2);
//        btnArray[0][2] = (Button) findViewById(R.id.button3);
//        btnArray[1][0] = (Button) findViewById(R.id.button4);
//        btnArray[1][1] = (Button) findViewById(R.id.button5);
//        btnArray[1][2] = (Button) findViewById(R.id.button6);
//
//        for(int i = 0; i < ROWS; i++)
//            for(int j = 0; j < COLS; j++)
//                btnArray[i][j].setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        switch(v.getId()) {
//                            case R.id.button1 : MainActivity.clickedArray[0][0] = true;
//                                Log.i("PRESSED", "state1: " + MainActivity.clickedArray[0][0]);
//                                break;
//
//                            case R.id.button2 : MainActivity.clickedArray[0][1] = true;
//                                Log.i("PRESSED", "state2: " + MainActivity.clickedArray[0][1]);
//                                break;
//
//
//                            case R.id.button3 : MainActivity.clickedArray[0][2] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[0][2]);
//                                break;
//
//                            case R.id.button4 : MainActivity.clickedArray[1][0] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][0]);
//                                break;
//
//                            case R.id.button5 : MainActivity.clickedArray[1][1] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][1]);
//                                break;
//
//                            case R.id.button6 : MainActivity.clickedArray[1][2] = true;
//                                Log.i("PRESSED", "state3: " + MainActivity.clickedArray[1][2]);
//                        }
//                    }
//                });
//    }

//    private void ResetClickedMatrix() {
//        for(int i = 0; i < ROWS; i++)
//            for(int j = 0; j < COLS; j++)
//                clickedArray[i][j] = false;
//
//        for(int i = 0; i < 7; i++)
//            brailleClickedAr[i] = false;
//    }

//    private void checkLetter() {
//        char letter;
//        if(brailleClickedAr[2]) letter = 'a';
//        else if(brailleClickedAr[1] && brailleClickedAr[2]) letter ='b';
//        else if(brailleClickedAr[2] && brailleClickedAr[4]) letter ='c';
//        else if(brailleClickedAr[2] && brailleClickedAr[4] && brailleClickedAr[5]) letter ='d';
//        else if(brailleClickedAr[2] && brailleClickedAr[5]) letter ='e';
//        else if(brailleClickedAr[1] && brailleClickedAr[2] && brailleClickedAr[4]) letter ='f';
//        else if(brailleClickedAr[1] && brailleClickedAr[2]
//                && brailleClickedAr[4] && brailleClickedAr[5]) letter ='g';
//        else if(brailleClickedAr[1] && brailleClickedAr[2] && brailleClickedAr[5]) letter ='h';
//        else if(brailleClickedAr[1] && brailleClickedAr[4]) letter ='i';
//        else if(brailleClickedAr[1] && brailleClickedAr[4] && brailleClickedAr[5]) letter ='j';
//        else if(brailleClickedAr[0] && brailleClickedAr[2]) letter ='k';
//        else if(brailleClickedAr[0] && brailleClickedAr[1] && brailleClickedAr[2]) letter ='l';
//        else if(brailleClickedAr[0] && brailleClickedAr[2] && brailleClickedAr[4]) letter ='m';
//        else if(brailleClickedAr[0] && brailleClickedAr[2]
//                && brailleClickedAr[4] && brailleClickedAr[5]) letter ='n';
//        else if(brailleClickedAr[0] && brailleClickedAr[2] && brailleClickedAr[5]) letter ='o';
//        else if(brailleClickedAr[0] && brailleClickedAr[1]
//                && brailleClickedAr[2] && brailleClickedAr[4]) letter ='p';
//        else if(brailleClickedAr[0] && brailleClickedAr[1]
//                && brailleClickedAr[2] && brailleClickedAr[4] && brailleClickedAr[5]) letter ='q';
//        else if(brailleClickedAr[0] && brailleClickedAr[1]
//                && brailleClickedAr[2] && brailleClickedAr[5]) letter ='r';
//        else if(brailleClickedAr[0] && brailleClickedAr[1] && brailleClickedAr[4]) letter ='s';
//        else if(brailleClickedAr[0] && brailleClickedAr[1]
//                && brailleClickedAr[4] && brailleClickedAr[5]) letter ='t';
//        else if(brailleClickedAr[0] && brailleClickedAr[2] && brailleClickedAr[6]) letter ='u';
//        else if(brailleClickedAr[0] && brailleClickedAr[1]
//                && brailleClickedAr[2] && brailleClickedAr[6]) letter ='v';
//        else if(brailleClickedAr[1] && brailleClickedAr[4]
//                && brailleClickedAr[5] && brailleClickedAr[6]) letter ='w';
//        else if(brailleClickedAr[0] && brailleClickedAr[2]
//                && brailleClickedAr[4] && brailleClickedAr[6]) letter ='x';
//        else if(brailleClickedAr[0] && brailleClickedAr[2]
//                && brailleClickedAr[4] && brailleClickedAr[5] && brailleClickedAr[6]) letter ='y';
//        else if(brailleClickedAr[0] && brailleClickedAr[2]
//                && brailleClickedAr[5] && brailleClickedAr[6]) letter ='z';
//        else letter =' ';
//
//        ResetClickedMatrix();
//
//    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {

                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mVoiceInputTv.setText(result.get(0));
                    WriteToLog("Speech to Text: " + result.get(0));
                }
                else {
                    WriteToLog("Speech to Text: Failed");
                }
                break;
            }

        }
    }

}

