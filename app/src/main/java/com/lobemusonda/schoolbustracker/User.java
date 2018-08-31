package com.lobemusonda.schoolbustracker;

/**
 * Created by lobemusonda on 8/29/18.
 */

public class User {
    public String name, email, type;

    public User () {
        type = "parent";
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
