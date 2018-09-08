package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 9/8/18.
 */

public class Child {
    private String childId, firstName, lastName, busNo;

    public Child() {

    }

    public Child(String childId, String firstName, String lastName, String busNo) {
        this.childId = childId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.busNo = busNo;
    }

    public String getChildId() {
        return childId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getBusNo() {
        return busNo;
    }
}
