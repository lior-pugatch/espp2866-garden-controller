package com.example.user1.activitytest;


public class Constants {

    private static String[] daysList = {"Sunday", "Monday", "Tuesday",
    "Wednesday", "Thursday", "Friday", "Saturday"};

    public static String[] tapsList = {"Tap 1", "Tap 2"};

    public static String[] getDaysList()
    {
        return daysList;
    }
    public static String[] getTapsList()
    {
        return tapsList;
    }

    public String getDayByIndex(int idx){
        return this.daysList[idx];
    }

    public String getTapByIndex(int idx){
        return this.tapsList[idx];
    }

    public static final int INTENT_REQUEST_NEW = 1;
    public static final int INTENT_REQUEST_EDIT = 2;
    public static final int INTENT_REQUEST_SELECT_FROM_TABLE = 3;

    /* Notice that on back pressed return value will be 0 (CANCELED)*/
    public static final int RETURN_VALUE_UPDATE = 1;
    public static final int RETURN_VALUE_DELETE = 2;

}
