package com.example.inharmony;

import com.parse.Parse;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String BODY = "body";
    public static final String TRACK_ID = "trackId";

    public ParseUser getSender() {
        return getParseUser(SENDER);
    }

    public void setSender(ParseUser sender) {
        put(SENDER, sender);
    }

    public ParseUser getReceiver() {
        return getParseUser(RECEIVER);
    }

    public void setReceiver(ParseUser receiver) {
        put(RECEIVER, receiver);
    }

    public String getBody() {
        return getString(BODY);
    }

    public void setBody(String body) {
        put(BODY, body);
    }

    public String getTrackId() { return getString(TRACK_ID); }

    public void setTrackId(String trackId) {put(TRACK_ID, trackId); }

}
