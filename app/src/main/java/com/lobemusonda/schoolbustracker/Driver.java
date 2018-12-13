package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 9/10/18.
 */

public class Driver {
    private String busNo, status, type;
    private double latitude, longitude, speed;

    public Driver() {

    }

    public Driver(String busNo,double latitude, double longitude, double speed, String status, String type) {
        this.busNo = busNo;
        this.status = status;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
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

    public double getSpeed() {
        return speed;
    }
}
