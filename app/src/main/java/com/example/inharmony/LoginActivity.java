package com.example.inharmony;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Pager;

public class LoginActivity extends AppCompatActivity {
    public static String token = null;

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String CLIENT_ID = "8a91678afa49446c9aff1beaabe9c807";
    private static final String REDIRECT_URI = "testschema://callback";

    private static final int REQUEST_CODE = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        token = CredentialsHandler.getToken(this);
        // existing user is already logged in, redirect past log-in screen
        if ((token != null) && (ParseUser.getCurrentUser() != null)) {
            startMainActivity(token, false);
        }
        // no user logged in, display log-in screen
        else {
            ParseUser.logOutInBackground();
            setContentView(R.layout.activity_login);
        }

    }

    private void loginUser(String username, String password, String token) {
        ParseQuery<ParseUser> allUsers = ParseUser.getQuery();
        allUsers.whereEqualTo("username", username);
        try {
            List<ParseUser> userMatch = allUsers.find();
            // indicates new user -> sign up
            if (userMatch.size() == 0) {
                startMainActivity(token, true);
            }
            // existing user -> log in
            else {
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        startMainActivity(token, false);
                    }
                });
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void onLoginButtonClicked(View view) {
        final AuthenticationRequest request = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                .setScopes(new String[]{"streaming", "user-read-recently-played", "user-modify-playback-state",
                        "playlist-read-collaborative", "user-read-playback-state", "user-read-email", "user-top-read",
                        "playlist-modify-public", "user-library-modify", "user-follow-read", "user-read-currently-playing",
                        "user-library-read", "user-read-private"}).setShowDialog(true)
                .build();

        // open up spotify log in screen
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    // handle post log in authentication screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    CredentialsHandler.setToken(this, token, response.getExpiresIn(), TimeUnit.SECONDS);
                    token = response.getAccessToken();

                    SpotifyApi spotifyApi = new SpotifyApi();
                    spotifyApi.setAccessToken(token);
                    SpotifyService service = spotifyApi.getService();
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            String email = service.getMe().email;
                            String id = service.getMe().id;
                            List<String> genreList = service.getSeedsGenres().genres;
                            Pager<Artist> songId = service.getTopArtists();
                            loginUser(email, id, token);
                        }
                    });
                    break;

                // Auth flow returned an error
                case ERROR:
                    logError("Auth error: " + response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    logError("Auth result: " + response.getType());
            }
        }
    }

    private void startMainActivity(String token, boolean signUp) {
        Intent intent = MainActivity.createIntent(this);
        intent.putExtra(MainActivity.EXTRA_TOKEN, token);
        if (signUp) {
            intent.putExtra(String.valueOf(MainActivity.NEW_SIGN_UP), true);
        } else {
            intent.putExtra(String.valueOf(MainActivity.NEW_SIGN_UP), false);
        }
        startActivity(intent);
        finish();
    }

    private void logError(String msg) {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }
}
