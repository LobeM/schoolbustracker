package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 9/10/18.
 */

public class Driver {
    private String busNo, status, type;
    private double latitude, longitude;

    public Driver() {

    }

    public Driver(String busNo,double latitude, double longitude, String status, String type) {
        this.busNo = busNo;
        this.status = status;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getBusNo() {
        return busNo;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
