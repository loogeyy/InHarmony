package com.example.inharmony;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.livequery.ParseLiveQueryClient;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Match.class);
        ParseObject.registerSubclass(Message.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("JX2BuavKtJbxKC4d2YmVpaxIaTZQ8Z9YqoFrCHXE")
                .clientKey("wMstVXcWjICq7G8i13nkrPQWqorx1Yy0viT0IGYI")
                .server("https://parseapi.back4app.com")
                .build()
        );

        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();

    }
}
