package ru.vita_control.photo_legs;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Questionnaire extends Activity implements View.OnClickListener {
    Button start_button;
    Button stop_button;
    Button but1;
    TextView Tw;
    TextView Tw1;
    TextView Tw2;
    ImageView Im;
    boolean reading = false;
    private Thread recordingThread = null;
    File sdDir = null;
    ProgressBar progressBar;
    private AudioRecord audioRecord;
    private int myBufferSize = 8192;
    private  long iterator = System.currentTimeMillis() / 1000L;
    private int mCount = 0;

    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_BPP = 16;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.recordscheme);

        start_button = findViewById(R.id.button1);
        stop_button = findViewById(R.id.button2);

        but1 = findViewById(R.id.button3);

        Tw = findViewById(R.id.textView);
        Tw1 = findViewById(R.id.textView3);
        Tw2 = findViewById(R.id.helloTextView);
        Im = findViewById(R.id.imageView);
        start_button.setOnClickListener(this);
        stop_button.setOnClickListener(this);
        but1.setOnClickListener(this);
        String sdState = android.os.Environment.getExternalStorageState();
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            sdDir = android.os.Environment.getExternalStorageDirectory();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                reading = true;
                break;
            case R.id.button2:
//                reading = false;
//                stopRecorder();
                Tw2.setText("Я насчитал " + ++mCount + " ворон");
                break;
            case R.id.button3:
                mCount = 0;
                Tw2.setText("Я насчитал " + mCount + " ворон");
                break;
        }
    }
}

