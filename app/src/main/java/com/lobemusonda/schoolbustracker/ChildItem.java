package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 8/15/18.
 */

public class ChildItem {
    private String mChildName;
    private String mBusNo;
    private double mLatitude;
    private double mLongitude;

    public ChildItem(String childName, String busNo, double latitude, double longitude) {
        mChildName = childName;
        mBusNo = busNo;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getmChildName() {
        return mChildName;
    }

    public void setmBusNo(String busNo) {
        mBusNo = busNo;
    }

    public String getmBusNo() {
        return mBusNo;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(int mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(int mLongitude) {
        this.mLongitude = mLongitude;
    }
}
