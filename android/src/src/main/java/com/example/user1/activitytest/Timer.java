package com.example.user1.activitytest;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by User1 on 05/06/2017.
 */


public class Timer implements Parcelable {
    private int day;
    private int hour;
    private int minute;
    private int controllerId;
    private int duration;

    // Constructor
    public Timer( int controllerId, int day, int hour, int minute, int duration){
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.controllerId = controllerId;
        this.duration = duration;
    }

    public void copyFromObjet(Timer other){
        this.day = other.day;
        this.minute = other.minute;
        this.hour = other.hour;
        this. controllerId = other.controllerId;
        this.duration = other.duration;
    }
    // Getter and setter methods



    public String getDayString(){
        Constants day = new Constants();
        return day.getDayByIndex(this.day);
    }
    public int getHour(){
        return this.hour;
    }

    public int getMinute(){
        return this.minute;
    }

    public int getDuration(){
        return this.duration;
    }

    public int getControllerId(){
        return this.controllerId;
    }


    public void setDayByIdx(int day){
        this.day = day;
    }
    public void setHour(int hour){
        this.hour = hour;
    }

    public void setMinute(int minute){

        this.minute = minute;
    }

    public void setControllerId(int controllerId){

        this.controllerId = controllerId;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public int getDay(){
        return this.day;
    }
    // Parcelling part
    public Timer(Parcel in){
        int[] data = new int[5];

        in.readIntArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.day = data[0];
        this.hour = data[1];
        this.minute = data[2];
        this.controllerId = data[3];
        this.duration = data[4];
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[] {
                this.day,
                this.hour,
                this.minute,
                this.controllerId,
                this.duration});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Timer createFromParcel(Parcel in) {
            return new Timer(in);
        }

        public Timer[] newArray(int size) {
            return new Timer[size];
        }
    };

    public String getTimeString(){
        return padNumberToString(this.hour) + ":" + padNumberToString(this.minute);
    }

    private String padNumberToString(int n)
    {
        if (n < 10)
            return ("0" + Integer.toString(n));
        else
            return Integer.toString(n);
    }
}