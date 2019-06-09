package com.example.user1.activitytest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class AlarmActivity extends AppCompatActivity implements SetTime {

    private static String TAG = "AlarmActivity";

    private EditText timeText;
    private Spinner daysSpinner;
    private Spinner tapsSpinner;
    private Timer timer = new Timer(0, 0, 0, 0, 0);
    private EditText etDuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        LinearLayout ll = (LinearLayout)findViewById(R.id.activity_alarm);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        daysSpinner = (Spinner) findViewById(R.id.spinnerDays);
        daysSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Constants.getDaysList()));

        tapsSpinner = (Spinner) findViewById(R.id.spinnerTaps);
        tapsSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Constants.getTapsList()));

        timeText = (EditText) findViewById(R.id.etTime);
        etDuration = (EditText) findViewById(R.id.etDuration);

        Intent intent = getIntent();
        int requestCode = intent.getIntExtra("requestCode", Constants.INTENT_REQUEST_NEW);
        Log.d(TAG,"in override of onCreate, request code: " + requestCode);

        if (requestCode ==  Constants.INTENT_REQUEST_EDIT){
            Button UpdateTimerBtn = new Button(this);
            UpdateTimerBtn.setText("Update Timer");

            UpdateTimerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent returnIntent = new Intent();
                    timer.setDayByIdx((int) daysSpinner.getSelectedItemId());
                    timer.setControllerId((int) tapsSpinner.getSelectedItemId());
                    timer.setDuration(Integer.parseInt(etDuration.getText().toString()));
                    returnIntent.putExtra("timer", timer);
                    setResult(Constants.RETURN_VALUE_UPDATE, returnIntent);
                    finish();
                }
            });


            Button DeleteTimerBtn = new Button(this);
            DeleteTimerBtn.setText("Delete Timer");

            DeleteTimerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent returnIntent = new Intent();
                    setResult(Constants.RETURN_VALUE_DELETE, returnIntent);
                    finish();
                }
            });

            Timer timerToEdit = (Timer) intent.getParcelableExtra("timer");
            /* Copy data to local timer */
            timer.copyFromObjet(timerToEdit);
            Log.d(TAG,"Got timer to edit, day: " + timer.getDayString() + ", Time: "
                    + timer.getHour() + ":" + timer.getMinute());

            ll.addView(UpdateTimerBtn, lp);
            ll.addView(DeleteTimerBtn, lp);
        }
        else
        {
            Button newTimerBtn = new Button(this);
            newTimerBtn.setText("Create New Timer");

            newTimerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent returnIntent = new Intent();
                    timer.setDayByIdx((int) daysSpinner.getSelectedItemId());
                    timer.setControllerId((int) tapsSpinner.getSelectedItemId());
                    timer.setDuration(Integer.parseInt(etDuration.getText().toString()));
                    returnIntent.putExtra("timer", timer);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            });
            ll.addView(newTimerBtn, lp);
        }
        updateTime();
        tapsSpinner.setSelection(timer.getControllerId());
        daysSpinner.setSelection(timer.getDay());
        etDuration.setText("" + timer.getDuration());

    }


    public void onClick_TimeBox(View view) {
        TimerPickerFragment newFragment = new TimerPickerFragment();
        Bundle timeParams = new Bundle();
        timeParams.putInt("Hour", timer.getHour());
        timeParams.putInt("Minute", timer.getMinute());
        newFragment.setArguments(timeParams);
        newFragment.show(getSupportFragmentManager(), "TimeFragment");

    }

    /* Interface implemantation for timer data fragment */
    public void setHour(int hour) {
        this.timer.setHour(hour);
    }

    public void setMinute(int minute){
        this.timer.setMinute(minute);
    }

    public void updateTime()
    {
        timeText.setText(timer.getTimeString());
    }



    public void onClick_btnSetTimer(View view)
    {
        Intent returnIntent = new Intent();
        timer.setDayByIdx((int) daysSpinner.getSelectedItemId());
        timer.setControllerId((int) tapsSpinner.getSelectedItemId());
        timer.setDuration(Integer.parseInt(etDuration.getText().toString()));
        returnIntent.putExtra("timer", timer);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }


}
