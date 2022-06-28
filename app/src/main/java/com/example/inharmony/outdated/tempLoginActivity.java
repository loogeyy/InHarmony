//package com.example.inharmony.temp;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.Toast;
//
//import com.example.inharmony.R;
//import com.spotify.android.appremote.api.ConnectionParams;
//import com.spotify.android.appremote.api.Connector;
//import com.spotify.android.appremote.api.SpotifyAppRemote;
//
//import com.spotify.protocol.types.Track;
//import com.spotify.sdk.android.auth.AuthorizationClient;
//import com.spotify.sdk.android.auth.AuthorizationRequest;
//import com.spotify.sdk.android.auth.AuthorizationResponse;
//
//public class tempLoginActivity extends AppCompatActivity {
//
//    private static String CLIENT_ID = "2a47f203a16d4b45aaaef910d6b4b547";
//    private static final String REDIRECT_URI = "http://com.example.inharmony/callback";
//    private static final int REQUEST_CODE = 1337;
//    private SpotifyAppRemote mSpotifyAppRemote;
//    private String token;
//
//    AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);
//
//    private Button btnConnectToSpotify;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login_temp);
//
//        btnConnectToSpotify = findViewById(R.id.btnConnectToSpotify);
//        btnConnectToSpotify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // set authorization scopes for endpoints
//                // https://developer.spotify.com/documentation/general/guides/authorization/scopes/
//
//                // potential issue with built-in authorization for android sdk not allowing multiple scopes?
//                // https://developer.spotify.com/documentation/android/
//                builder.setScopes(new String[]{"streaming", "user-read-recently-played", "user-modify-playback-state",
//                        "playlist-read-collaborative", "user-read-playback-state", "user-read-email", "user-top-read",
//                        "playlist-modify-public", "user-library-modify", "user-follow-read", "user-read-currently-playing",
//                        "user-library-read", "user-read-private"
//                });
//                AuthorizationRequest request = builder.build();
//
//                AuthorizationClient.openLoginActivity(tempLoginActivity.this, REQUEST_CODE, request);
//                /*
//                 // To start LoginActivity from a Fragment:
//                 Intent intent = AuthorizationClient.createLoginActivityIntent(getActivity(), request);
//                 startActivityForResult(intent, REQUEST_CODE);
//
//                 // To close LoginActivity
//                 AuthorizationClient.stopLoginActivity(getActivity(), REQUEST_CODE);
//
//                 */
//
//            }
//        });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        super.onActivityResult(requestCode, resultCode, intent);
//
//        // Check if result comes from the correct activity
//        if (requestCode == REQUEST_CODE) {
//            // https://spotify.github.io/android-sdk/auth-lib/docs/com/spotify/sdk/android/auth/AuthorizationResponse.html
//            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);
//
//            switch (response.getType()) {
//                // Response was successful and contains auth token
//                case TOKEN:
//                    // Handle successful response
//                    token = response.getAccessToken();
//                    Log.d("token", response.getAccessToken());
//                    Intent i = new Intent(tempLoginActivity.this, tempMatchingActivity.class);
//                    startActivity(i);
//                    //response.g
//                    break;
//
//                // Auth flow returned an error
//                case ERROR:
//                    // Handle error response
//                    break;
//
//                // Most likely auth flow was cancelled
//                default:
//
//                    // Handle other cases
//            }
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        ConnectionParams connectionParams =
//                new ConnectionParams.Builder(CLIENT_ID)
//                        .setRedirectUri(REDIRECT_URI)
//                        .showAuthView(true)
//                        .build();
//
//        SpotifyAppRemote.connect(this, connectionParams,
//                new Connector.ConnectionListener() {
//
//                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
//                        mSpotifyAppRemote = spotifyAppRemote;
//                        Log.d("MainActivity", "Connected! Yay!");
//
//                        // Now you can start interacting with App Remote
//                        connected();
//
//                    }
//
//                    public void onFailure(Throwable throwable) {
//                        Toast.makeText(tempLoginActivity.this, "Please download spotify", Toast.LENGTH_SHORT).show();
//                        Log.e("MainActivity", throwable.getMessage(), throwable);
//
//                        // Something went wrong when attempting to connect! Handle errors here
//                    }
//                });
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
//    }
//
//    private void connected() {
//        // Play a playlist
//        // this is how we can play songs on a user's profile *just replace uri with parsed info from user profile)
//        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
//
//        // Subscribe to PlayerState
//        mSpotifyAppRemote.getPlayerApi()
//                .subscribeToPlayerState()
//                .setEventCallback(playerState -> {
//                    final Track track = playerState.track;
//                    if (track != null) {
//                        Log.d("MainActivity", track.name + " by " + track.artist.name);
//                    }
//                });
//    }
//}