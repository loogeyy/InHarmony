package com.example.inharmony;

import com.parse.ParseUser;

public class Card {

    private ParseUser user;

    public Card(ParseUser user) {
            this.user = user;
        }

        public ParseUser getUser() {
            return user;
        }

        public void setUser(ParseUser user) {
            this.user = user;
        }
}
