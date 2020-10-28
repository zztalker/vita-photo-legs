package ru.vita_control.photo_legs;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import io.sentry.core.Sentry;

import static android.widget.Toast.makeText;

public class MainActivity extends Activity implements OnClickListener {
    Button start_button;
    Button stop_button;
    //    Button new_button;
    Button obn_button;
    TextView Tw;
    TextView Count;
    EditText TInfo;
    boolean reading = false;
    private Thread recordingThread = null;
    File sdDir = null;
    public static final String app_id = "VklUQS1QaG90by1MZWdz";
    public static Integer deviceID = -1;

    public static final String APP_PREFERENCES = "vita-photo-legs";
    public static final String APP_PREFERENCES_COUNTER = "counter";
    private SharedPreferences mSettings;
    public static final String adress = "https://app.vita-control.ru/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        ArrayList<String> permissions_to_request = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            permissions_to_request.add(Manifest.permission.CAMERA);

        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            permissions_to_request.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            permissions_to_request.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            permissions_to_request.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissions_to_request.size() > 0) {
            String[] _permissions = permissions_to_request.toArray(new String[permissions_to_request.size()]);
            ActivityCompat.requestPermissions(this,
                    _permissions,
                    1);
        }
        setContentView(R.layout.main);
        start_button = findViewById(R.id.button1);
        stop_button = findViewById(R.id.button2);

//        new_button = findViewById(R.id.button3);

        obn_button = findViewById(R.id.button4);
        TInfo = findViewById(R.id.editTextTextPersonName);
        Tw = findViewById(R.id.textView);
        Count = findViewById(R.id.textView2);
        start_button.setOnClickListener(this);
        stop_button.setOnClickListener(this);

        obn_button.setOnClickListener(this);
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView version = findViewById(R.id.currentVersion);
            version.setText("Версия приложения: " + pInfo.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String sdState = android.os.Environment.getExternalStorageState();
        if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
            sdDir = android.os.Environment.getExternalStorageDirectory();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button1:
                Intent intent = new Intent(MainActivity.this, TakeShoot.class);
                String strPat = TInfo.getText().toString();
                intent.putExtra("hello", strPat);
                startActivity(intent);
                break;
            case R.id.button2:
                Intent intent1 = new Intent(MainActivity.this, Questionnaire.class);
                int i1 = mSettings.getInt(APP_PREFERENCES_COUNTER, 0);
                intent1.putExtra("hello", i1);
                startActivity(intent1);
                break;
            case R.id.button4:
                System.exit(0);;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Запоминаем данные
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putInt(APP_PREFERENCES_COUNTER, deviceID);
        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            try {
                deviceID = mSettings.getInt(APP_PREFERENCES_COUNTER, 0);
            }
            catch (Exception e) {
                Sentry.captureException(e);
                deviceID = 0;
            };
            Count.setText("ID =  " + deviceID);
            if (deviceID <= 0) {
                try {
                    new CheckID().execute();
                } catch (Exception e) {
                    Sentry.captureException(e);
                    Toast toast = Toast.makeText(MainActivity.this, "Не удалось получить айди. Перезапустите приложения для восстановления связи.", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }


    }

    class CheckID extends AsyncTask<Void, Void, Void> {
        String resultString = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                String myURL = adress + "register/";

                String parammetrs = "name=" + app_id;
                byte[] data = null;
                InputStream is = null;


                try {
                    URL url = new URL(myURL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", "" + Integer.toString(parammetrs.getBytes().length));
                    conn.setDoOutput(true);
                    conn.setDoInput(true);


                    // конвертируем передаваемую строку в UTF-8
                    data = parammetrs.getBytes("UTF-8");


                    OutputStream os = conn.getOutputStream();


                    // передаем данные на сервер
                    os.write(data);
                    os.flush();
                    os.close();
                    data = null;
                    conn.connect();
                    int responseCode = conn.getResponseCode();


                    // передаем ответ сервер
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Log.i("Otvet", String.valueOf(responseCode));
                    if (responseCode == 200) {    // Если все ОК (ответ 200)
                        is = conn.getInputStream();

                        byte[] buffer = new byte[8192]; // размер буфера


                        // Далее так читаем ответ
                        int bytesRead;


                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }


                        data = baos.toByteArray();
                        resultString = new String(data, "UTF-8");  // сохраняем в переменную ответ сервера, у нас "OK"
                        Log.i("resultdata", resultString);
                        deviceID = Integer.parseInt(resultString);
                        Count.setText("ID =  " + deviceID);
                        SharedPreferences.Editor editor = mSettings.edit();
                        editor.putInt(APP_PREFERENCES_COUNTER, deviceID);
                        editor.apply();
                    } else {

                    }

                    conn.disconnect();

                } catch (MalformedURLException e) {

                    resultString = "MalformedURLException:" + e.getMessage();
                } catch (IOException e) {

                    resultString = "IOException:" + e.getMessage();
                } catch (Exception e) {

                    resultString = "Exception:" + e.getMessage();
                }
            } catch (Exception e) {
                Sentry.captureException(e);
                e.printStackTrace();
            }
            return null;
        }

    }

}