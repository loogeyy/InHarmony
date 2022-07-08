package com.example.inharmony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.inharmony.fragments.EditProfileFragment;
import com.example.inharmony.fragments.MatchingFragment;
import com.example.inharmony.fragments.MyProfileFragment;
import com.example.inharmony.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseUser;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    /*
    SENDING:
    Fragment fragment = new PostDetailsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("post", posts.get(getAdapterPosition()));
                    fragment.setArguments(bundle);
                    fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();

    RECEIVING:
     Bundle bundle = this.getArguments();
        if (bundle != null) {
            user = bundle.getParcelable("user");
        }
     */
    private BottomNavigationView bottomMenu;

    public static String NEW_SIGN_UP = "NEW_SIGN_UP";
    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    private String token;

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Search.ActionListener mActionListener;

    private LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
    private SearchResultsAdapter mAdapter;

    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MAINACTIVITY", "onCreate!");
        setContentView(R.layout.activity_main);

        //grabs intent from login including token
        Intent intent = getIntent();
        token = intent.getStringExtra(EXTRA_TOKEN);
        Boolean newSignUp = intent.getExtras().getBoolean(NEW_SIGN_UP);
        Log.i("MAINACTIVITY NEWSIGNUP", String.valueOf(newSignUp));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.logo);



        bottomMenu = findViewById(R.id.bottomMenu);

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
                        Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                        Log.d("Menu", "Profile pressed");
                        fragment = new MatchingFragment();
                        //bundle.putString(SearchFragment.SEARCH_TYPE, "TRACK");
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

                        //fragment = new MatchFragment(ParseUser.getCurrentUser());
                        break;
                    case R.id.actionProfile:
                        Log.i("MAINACTIVITY ACTIONPROFILE", "SWITCHING TO EDIT");
                        Log.i("MainActivity", "newSignUp: " + newSignUp);
                        if (ParseUser.getCurrentUser() == null) {
                            fragment = new EditProfileFragment();
                            bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
                            bundle.putBoolean("newSignUp", true);
                            String welcomeText = "It looks like you're new here! Let's start by filling out some basic profile details.";
                            bundle.putString("tvWelcomeText", welcomeText);
                            fragment.setArguments(bundle);
                            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "EDITPROFILE").addToBackStack(null).commit();

                        } else {
                            fragment = new MyProfileFragment(true, ParseUser.getCurrentUser());
                            bundle.putString(MyProfileFragment.EXTRA_TOKEN, token);
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

    public void onLogout(MenuItem mi) {
        //AuthenticationClient.clearCookies(getApplicationContext());
        CredentialsHandler.setToken(MainActivity.this, null, 0, TimeUnit.SECONDS);
        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

}
