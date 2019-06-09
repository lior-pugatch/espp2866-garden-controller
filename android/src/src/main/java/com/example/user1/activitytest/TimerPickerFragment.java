package com.example.user1.activitytest;

import android.app.Activity;
import android.app.Dialog;


import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;


/**
 * Created by User1 on 06/06/2017.
 */

public class TimerPickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private static int hour;
    private static int minute;
    SetTime setTime;

    public TimerPickerFragment()
    {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        // Create a new instance of TimePickerDialog and return it
        Bundle bundle = getArguments();
        return new TimePickerDialog(getActivity(), this, bundle.getInt("Hour"), bundle.getInt("Minute"),
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;

        setTime.setHour(hourOfDay);
        setTime.setMinute(minute);
        setTime.updateTime();
    }

    public int getHour(){
        return  hour;
    }

    public int getMinute(){
        return  minute;
    }

    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);
        Activity a = (Activity)c;
        setTime = (SetTime) a;
    }
}