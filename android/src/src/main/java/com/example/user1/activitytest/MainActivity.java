package com.example.user1.activitytest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";
    private static int timerIdxToEdit = 0;

    EditText controllerIPeditText = null;
    DatagramSocket outgoingUdpSocket = null;
    DatagramSocket incomingUdpSocket = null;
    private static final int LOCAL_UDP_PORT = 10070;
    private static final int CONTROLLER_UDP_PORT = 10080;
    private boolean isConnected = false;
    Object syncToken = new Object();
    ToggleButton toggleWaterOn;
    Handler mHandler = new Handler();
    private static Runnable conFailedRun;
    private static final String FILENAME = "config.txt";


    ArrayList<Timer> timerList = new ArrayList<Timer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        controllerIPeditText = (EditText) findViewById(R.id.etIpAddr);
        toggleWaterOn = (ToggleButton) findViewById(R.id.tgBtnWaterToggle);

        loadConfig();

        conFailedRun = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG," In conFailedRun");
                Toast.makeText(MainActivity.this, "Connection Failed", Toast.LENGTH_LONG).show();
            }
        };

        final ToggleButton toggleConnect = (ToggleButton) findViewById(R.id.tgBtnConnect);

        toggleConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleConnect.isChecked() == true) {
                    Log.d(TAG,"toggleConnect checked");
                    connect();
                    toggleConnect.setChecked(false);
                }
                else {
                    Log.d(TAG,"toggleConnect not checked");
                    isConnected = false;
                }

            }
        });

        toggleWaterOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "Water on status changed: " + isChecked);
                if (isConnected == true) {
                    /* Only Change button status if connected */
                    if (isChecked) {
                        // The toggle is enabled
                        setWaterOn();

                    } else {
                        setWaterOff();
                    }
                }
            }
        });

        try {
            outgoingUdpSocket = new DatagramSocket();
            incomingUdpSocket = new DatagramSocket(LOCAL_UDP_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            public void run() {


                while(true)
                {
                    final int payloadOffset = 3;
                    byte incomingData[] = new byte[1024];
                    DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                    try {
                        incomingUdpSocket.receive(incomingPacket);
                        final byte[] receivedData = incomingPacket.getData();
                        if(receivedData[0] == (byte)0xC0) { /* Got controller schedule*/
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, "Connected! Received schedule from controller", Toast.LENGTH_LONG).show();

                                    int numOfTimers = (int) receivedData[1];
                                    Log.d(TAG," Got schedule message from controller, timers found: " + numOfTimers + " packet len: " + receivedData.length);
                                    int isWaterOn = (int) receivedData[2];
                                    mHandler.removeCallbacks(conFailedRun);

                                    /* Clear the timer list and add new timers */
                                    timerList.clear();
                                    for(int i = 0; i < numOfTimers; i++) {
                                        Timer timer = new Timer(
                                                (int) receivedData[payloadOffset + 5 * i],
                                                /* Day comes in offset of + 1*/
                                                 (receivedData[payloadOffset + 5 * i + 1] - 1),
                                                (int) receivedData[payloadOffset + 5 * i + 2],
                                                (int) receivedData[payloadOffset + 5 * i + 3],
                                                (int) receivedData[payloadOffset + 5 * i + 4]);
                                        timerList.add(timer);
                                    }
                                    isConnected = true;
                                    toggleConnect.setChecked(true);
                                    saveConfig();
                                    if (isWaterOn == 1)
                                    {
                                        toggleWaterOn.setChecked(true);
                                    }
                                    else
                                    {
                                        toggleWaterOn.setChecked(false);
                                    }

                                }
                            });
                        }

                        else if(receivedData[0] == (byte)0x54)
                        {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    String s1 = new String(receivedData);
                                    Toast.makeText(MainActivity.this, "Received time from controller", Toast.LENGTH_LONG).show();
                                    TextView t = (TextView) findViewById(R.id.etCntrlTime);
                                    t.setText(s1.substring(1, receivedData.length));
                                }
                            });
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void loadConfig()
    {


        try {
            InputStream inputStream = getApplicationContext().openFileInput(FILENAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                controllerIPeditText.setText(stringBuilder.toString());
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
    }

    private void saveConfig()
    {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(controllerIPeditText.getText().toString());
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void OnClick_btnNewActivity(View view) {
        Intent intent = new Intent(this, AlarmActivity.class);
        startActivityForResult(intent, Constants.INTENT_REQUEST_NEW);
    }

    public void OnClick_btnViewTimers(View view){
        Intent intent = new Intent(this, ShowTimersActivity.class);
        intent.putExtra("TimersList", timerList);
        startActivityForResult(intent, Constants.INTENT_REQUEST_SELECT_FROM_TABLE);
    }

    private void connect() {
        byte[] data = new byte[]{(byte)0xA0};
        Log.d(TAG," in connect");
        try {
            DatagramPacket getSchedulePacket = new DatagramPacket(data, 0, data.length,
                    InetAddress.getByName(controllerIPeditText.getText().toString()), CONTROLLER_UDP_PORT);
            outgoingUdpSocket.send(getSchedulePacket);

            Log.d(TAG," Setting connection failed message thread");
            if (conFailedRun != null) {
                mHandler.postDelayed(conFailedRun, 5000);
            }
            else
            {
                Log.d(TAG," conFailedRun is null");
            }



        } catch (UnknownHostException e) {
            Toast.makeText(MainActivity.this, "Bad IP address", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setWaterOn()
    {
        byte[] data = new byte[]{(byte)0xA2};
        try {
            DatagramPacket getSchedulePacket = new DatagramPacket(data, 0, data.length,
                    InetAddress.getByName(controllerIPeditText.getText().toString()), CONTROLLER_UDP_PORT);
            outgoingUdpSocket.send(getSchedulePacket);

        } catch (UnknownHostException e) {
            Toast.makeText(MainActivity.this, "Bad IP address", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setWaterOff()
    {
        byte[] data = new byte[]{(byte)0xA3};
        try {
            DatagramPacket getSchedulePacket = new DatagramPacket(data, 0, data.length,
                    InetAddress.getByName(controllerIPeditText.getText().toString()), CONTROLLER_UDP_PORT);
            outgoingUdpSocket.send(getSchedulePacket);

        } catch (UnknownHostException e) {
            Toast.makeText(MainActivity.this, "Bad IP address", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void OnClick_btnSendTimers(View view){
        Log.d(TAG,"in send timers00");
        byte[] data = new byte[1024];
        Log.d(TAG,"in send timers0");
        int nTimers = timerList.size();
        Log.d(TAG,"in send timers1");
        data[0] = (byte)0xA1;
        Log.d(TAG,"in send timers2");
        data[1] = (byte)nTimers;
        Log.d(TAG,"in send timers3");

        Timer timer;

        for (int i = 0; i < nTimers; i++)
        {
            timer = timerList.get(i);
            data[5*i + 2] = (byte)timer.getControllerId();
            data[5*i + 3] = (byte)timer.getDay();
            data[5*i + 4] = (byte)timer.getHour();
            data[5*i + 5] = (byte)timer.getMinute();
            data[5*i + 6] = (byte)timer.getDuration();
        }

        Log.d(TAG,"finshed creating packet of timers... sending it");
        try {
            DatagramPacket sendSchedulePacket = new DatagramPacket(data, 0, nTimers*5 + 2,
                    InetAddress.getByName(controllerIPeditText.getText().toString()), CONTROLLER_UDP_PORT);
            outgoingUdpSocket.send(sendSchedulePacket);
        } catch (UnknownHostException e) {
            Toast.makeText(MainActivity.this, "Bad IP address", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        Log.d(TAG,"in override of startActivityForResult, request code: " + requestCode);
        intent.putExtra("requestCode", requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == Constants.INTENT_REQUEST_NEW && resultCode == Activity.RESULT_OK){
            Timer timer = (Timer) data.getParcelableExtra("timer");
            timerList.add(timer);
            Log.d(TAG,"Added timer to array, day: " + timer.getDayString() + ", Time: "
                    + timer.getHour() + ":" + timer.getMinute() + ", ctrl id: " + timer.getControllerId()
             + ", duration: " + timer.getDuration());
            //Do whatever you want with yourData
        }
        else if (requestCode == Constants.INTENT_REQUEST_SELECT_FROM_TABLE && resultCode == Activity.RESULT_OK)
        {
            timerIdxToEdit = data.getIntExtra("TimerIdx", 0);
            Log.d(TAG, "Returned after long click from table, user requested to edit timer "+ timerIdxToEdit);
            Intent intent = new Intent(this, AlarmActivity.class);
            intent.putExtra("timer", timerList.get(timerIdxToEdit));
            startActivityForResult(intent, Constants.INTENT_REQUEST_EDIT);
        }
        else if (requestCode == Constants.INTENT_REQUEST_EDIT && resultCode == Constants.RETURN_VALUE_UPDATE)
        {
            Timer timer = (Timer) data.getParcelableExtra("timer");
            Log.d(TAG,"Got timer After Edit, day: " + timer.getDayString() + ", Time: "
                    + timer.getHour() + ":" + timer.getMinute() + ", ctrl id: " + timer.getControllerId()
                    + ", duration: " + timer.getDuration());

            timerList.set(timerIdxToEdit, timer);
        }
        else if (requestCode == Constants.INTENT_REQUEST_EDIT && resultCode == Constants.RETURN_VALUE_DELETE)
        {
            Log.d(TAG,"Deleting timer" + timerIdxToEdit);
            timerList.remove(timerIdxToEdit);
        }
    }
}
