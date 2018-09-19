package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 9/8/18.
 */

public class Child {
    private String childId, firstName, lastName, driverID;

    public Child() {

    }

    public Child(String childId, String firstName, String lastName, String driverID) {
        this.childId = childId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.driverID = driverID;
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

    public String getDriverID() {
        return driverID;
    }
}
