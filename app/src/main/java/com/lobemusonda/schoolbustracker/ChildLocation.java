package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 10/23/18.
 */

public class ChildLocation {
    private String childID;
    private BusStation pickUp, dropOff;

    public ChildLocation() {

    }

    public ChildLocation(String childID, BusStation pickUp, BusStation dropOff) {
        this.childID = childID;
        this.pickUp = pickUp;
        this.dropOff = dropOff;
    }

    public String getChildID() {
        return childID;
    }

    public BusStation getPickUp() {
        return pickUp;
    }

    public BusStation getDropOff() {
        return dropOff;
    }
}
