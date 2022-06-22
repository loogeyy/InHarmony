package com.example.inharmony;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //ParseObject.registerSubclass(Post.class);
        //ParseObject.registerSubclass(Like.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("JX2BuavKtJbxKC4d2YmVpaxIaTZQ8Z9YqoFrCHXE")
                .clientKey("wMstVXcWjICq7G8i13nkrPQWqorx1Yy0viT0IGYI")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
