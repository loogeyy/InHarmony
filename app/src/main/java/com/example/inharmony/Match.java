package com.example.inharmony;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Match")
public class Match extends ParseObject {
    public static final String USER_ONE = "userOne";
    public static final String USER_TWO = "userTwo";
    public static final String STATUS = "status";

    public String getStatus() { return getString(STATUS); }

    public void setStatus(String status) {
        put(STATUS, status);
    }

    public ParseUser getUserOne() {
        return getParseUser(USER_ONE);
    }

    public void setUserOne(ParseUser user) {
        put(USER_ONE, user);
    }

    public ParseUser getUserTwo() {
        return getParseUser(USER_TWO);
    }

    public void setUserTwo(ParseUser user) {
        put(USER_TWO, user);
    }
}
