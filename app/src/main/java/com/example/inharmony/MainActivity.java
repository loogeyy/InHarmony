package com.example.inharmony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inharmony.fragments.ChatListFragment;
import com.example.inharmony.fragments.EditProfileFragment;
import com.example.inharmony.fragments.MatchingFragment;
import com.example.inharmony.fragments.ProfileFragment;
import com.example.inharmony.tasks.AlgorithmTask;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.List;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class MainActivity extends AppCompatActivity implements AsyncResponse {
    private static final String TAG = "MainActivity";
    public static String NEW_SIGN_UP = "NEW_SIGN_UP";
    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";

    private BottomNavigationView bottomMenu;
    final FragmentManager fragmentManager = getSupportFragmentManager();

    private String token;
    private boolean newSignUp;

    private Handler handler = new Handler();
    Runnable runnable;
    int delay = 1000 * 60 * 60 * 24; // 24-hour delay

    private AlgorithmTask asyncTask;
    private JSONArray featureAvgs = new JSONArray();
    private JSONArray featureWeights = new JSONArray();

    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "calculateTrackFeatures");
                handler.postDelayed(runnable, delay);
                calculateTrackFeatures();
                ParseUser.getCurrentUser().put("featureAvgs", featureAvgs);
                ParseUser.getCurrentUser().put("featureWeights", featureWeights);
                try {
                    ParseUser.getCurrentUser().save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MAINACTIVITY", "onCreate!");
        setContentView(R.layout.activity_main);

        //grabs intent from login including token
        Intent intent = getIntent();
        token = intent.getStringExtra(EXTRA_TOKEN);
        newSignUp = intent.getExtras().getBoolean(NEW_SIGN_UP);
        Log.i("MAINACTIVITY NEWSIGNUP", String.valueOf(newSignUp));
        calculateTrackFeatures();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        bottomMenu = findViewById(R.id.bottomMenu);
        if (!newSignUp) {
            configureNavigation();
        }
    }

    public void onLogout(MenuItem mi) {
        CredentialsHandler.setToken(MainActivity.this, null, 0, TimeUnit.SECONDS);
        ParseUser.logOutInBackground();
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void calculateTrackFeatures() {
        Log.i(TAG, "similarityScore");

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();

        asyncTask = new AlgorithmTask(service);
        asyncTask.delegate = this;
        asyncTask.execute();
    }

    @Override
    public void processFinish(List<JSONArray> featureList) {
        Log.i(TAG, "processFinish");
        featureAvgs = featureList.get(0);
        featureWeights = featureList.get(1);
        if (newSignUp) {
            configureNavigation();
        }
//
//        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                Fragment fragment;
//                Bundle bundle = new Bundle();
//                switch (item.getItemId()) {
//                    case R.id.actionMatch:
//                        fragment = new MatchingFragment();
//                        bundle.putString(MatchingFragment.EXTRA_TOKEN, token);
//                        bundle.putBoolean("newSignUp", false);
//                        fragment.setArguments(bundle);
//                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
//                        break;
//
//                    case R.id.actionMessage:
//                        Log.d("Menu", "Profile pressed");
//                        fragment = new ChatListFragment();
//                        fragment.setArguments(bundle);
//                        bundle.putString(ChatListFragment.EXTRA_TOKEN, token);
//                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
//                        break;
//
//                    case R.id.actionProfile:
//                        Log.i("MAINACTIVITY ACTIONPROFILE", "SWITCHING TO EDIT");
//                        Log.i("MainActivity", "newSignUp: " + newSignUp);
//                        Log.i(TAG, "featureWeights: " + featureWeights);
//                        Log.i(TAG, "featureAvgs: " + featureAvgs);
//                        if (ParseUser.getCurrentUser() == null) {
//                            Log.i(TAG, "fragmentstarting");
//                            fragment = new EditProfileFragment();
//                            bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
//                            bundle.putBoolean("newSignUp", true);
//                            bundle.putString("featureAvgs", featureAvgs.toString());
//                            bundle.putString("featureWeights", featureWeights.toString());
//                            String welcomeText = "It looks like you're new here! Let's start by filling out some basic profile details.";
//                            bundle.putString("tvWelcomeText", welcomeText);
//                            fragment.setArguments(bundle);
//                            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "EDITPROFILE").addToBackStack(null).commit();
//                        }
//                        else {
//                            Log.i(TAG, "fragmentstarting");
//                            fragment = new ProfileFragment(ParseUser.getCurrentUser());
//                            bundle.putString(ProfileFragment.EXTRA_TOKEN, token);
//                            bundle.putBoolean("newSignUp", false);
//                            String welcomeText = "Edit your profile details below.";
//                            bundle.putString("tvWelcomeText", welcomeText);
//                            fragment.setArguments(bundle);
//                            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
//                        }
//                        break;
//                    default:
//                        Log.i("DEFAULT", "NEW EDITPROFILEFRAGMENT");
//                        fragment = new EditProfileFragment();
//                        if (ParseUser.getCurrentUser() == null) {
//                            bundle.putBoolean("newSignUp", true);
//                        } else {
//                            bundle.putBoolean("newSignUp", false);
//                        }
//                        fragment.setArguments(bundle);
//                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
//                        break;
//                }
//                return true;
//            }
//        });
//
//        if (ParseUser.getCurrentUser() == null) {
//            bottomMenu.setSelectedItemId(R.id.actionProfile);
//        } else {
//            bottomMenu.setSelectedItemId(R.id.actionMatch);
//        }
    }

    private void configureNavigation() {

        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                Bundle bundle = new Bundle();
                switch (item.getItemId()) {
                    case R.id.actionMatch:
                        fragment = new MatchingFragment();
                        bundle.putString(MatchingFragment.EXTRA_TOKEN, token);
                        bundle.putBoolean("newSignUp", false);
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                        break;

                    case R.id.actionMessage:
                        Log.d("Menu", "Profile pressed");
                        fragment = new ChatListFragment();
                        fragment.setArguments(bundle);
                        bundle.putString(ChatListFragment.EXTRA_TOKEN, token);
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                        break;

                    case R.id.actionProfile:
                        Log.i("MAINACTIVITY ACTIONPROFILE", "SWITCHING TO EDIT");
                        Log.i("MainActivity", "newSignUp: " + newSignUp);
                        Log.i(TAG, "featureWeights: " + featureWeights);
                        Log.i(TAG, "featureAvgs: " + featureAvgs);
                        if (ParseUser.getCurrentUser() == null) {
                            Log.i(TAG, "fragmentstarting");
                            fragment = new EditProfileFragment();
                            bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
                            bundle.putBoolean("newSignUp", true);
                            bundle.putString("featureAvgs", featureAvgs.toString());
                            bundle.putString("featureWeights", featureWeights.toString());
                            String welcomeText = "It looks like you're new here! Let's start by filling out some basic profile details.";
                            bundle.putString("tvWelcomeText", welcomeText);
                            fragment.setArguments(bundle);
                            Log.i(TAG, "switching to edit screen");
                            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "EDITPROFILE").addToBackStack(null).commit();
                        }
                        else {
                            Log.i(TAG, "fragmentstarting");
                            fragment = new ProfileFragment(ParseUser.getCurrentUser());
                            bundle.putString(ProfileFragment.EXTRA_TOKEN, token);
                            bundle.putBoolean("newSignUp", false);
                            String welcomeText = "Edit your profile details below.";
                            bundle.putString("tvWelcomeText", welcomeText);
                            fragment.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                        }
                        break;
                    default:
                        Log.i("DEFAULT", "NEW EDITPROFILEFRAGMENT");
                        fragment = new EditProfileFragment();
                        if (ParseUser.getCurrentUser() == null) {
                            bundle.putBoolean("newSignUp", true);
                        } else {
                            bundle.putBoolean("newSignUp", false);
                        }
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                        break;
                }
                return true;
            }
        });

        if (ParseUser.getCurrentUser() == null) {
            bottomMenu.setSelectedItemId(R.id.actionProfile);
        } else {
            bottomMenu.setSelectedItemId(R.id.actionMatch);
        }
    }
}
