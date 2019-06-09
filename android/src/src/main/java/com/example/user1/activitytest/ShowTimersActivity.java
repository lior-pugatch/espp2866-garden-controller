package com.example.user1.activitytest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class ShowTimersActivity extends AppCompatActivity {

    TableLayout table;
    private ArrayList<Timer> timerList;
    private static String TAG = "ShowTimersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_timers);
        Log.d(TAG, "In on create");

        table = (TableLayout) findViewById(R.id.timersTable);

        timerList = getIntent().getParcelableArrayListExtra("TimersList");

        Log.d(TAG, "Got timer's list, len: " + timerList.size());

        for (int i = 0; i < timerList.size(); i++){
            Log.d(TAG, "Starting adding data to row...");
            TableRow row = new TableRow(this);
            row.setGravity(Gravity.CENTER);
            row.setLongClickable(true);
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = getIntent();
                    intent.putExtra("TimerIdx", view.getId());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    return true;
                }
            });

            row.setId(i);
            Timer timer = timerList.get(i);

            TextView tvControllerId = new TextView(this);
            tvControllerId.setText(Constants.tapsList[timer.getControllerId()]);
            tvControllerId.setGravity(Gravity.CENTER);
            tvControllerId.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

            TextView tvDay = new TextView(this);
            tvDay.setText(timer.getDayString());
            tvDay.setGravity(Gravity.CENTER);
            tvDay.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

            TextView tvTime = new TextView(this);
            tvTime.setText(timer.getTimeString());
            tvTime.setGravity(Gravity.CENTER);
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

            TextView tvDuration = new TextView(this);
            tvDuration.setText(timer.getDuration() + " Mins");
            tvDuration.setGravity(Gravity.CENTER);
            tvDuration.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);

            row.addView(tvControllerId);
            row.addView(tvDay);
            row.addView(tvTime);
            row.addView(tvDuration);


            Log.d(TAG, "Finished adding data to row, adding row to table...");
            table.addView(row);
        }

    }
}
