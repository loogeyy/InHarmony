package com.example.inharmony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private Button btnLogout;

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
        setContentView(R.layout.activity_main);

        //grabs intent from login including token
        Intent intent = getIntent();
        token = intent.getStringExtra(EXTRA_TOKEN);
        Boolean newSignUp = intent.getExtras().getBoolean(NEW_SIGN_UP);
        Log.i("MAINACTIVITY NEWSIGNUP", String.valueOf(newSignUp));

        bottomMenu = findViewById(R.id.bottomMenu);
        btnLogout = findViewById(R.id.btnLogout);

        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SearchFragment.EXTRA_TOKEN, token);
                switch (item.getItemId()) {
                    case R.id.actionMatch:
                        fragment = new MatchingFragment();
                        break;
                    case R.id.actionMessage:
                        Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                        Log.d("Menu", "Profile pressed");
                        fragment = new SearchFragment();
                        //fragment = new MatchFragment(ParseUser.getCurrentUser());
                        break;
                    case R.id.actionProfile:
                        Log.i("MAINACTIVITY ACTIONRPOFILE", "SWITCHING TO EDIT");
                        fragment = new EditProfileFragment();
                        if (newSignUp) {
                            bundle.putBoolean("newSignUp", true);
                            String welcomeText = "It looks like you're new here! Let's start by filling out some basic profile details.";
                            bundle.putString("tvWelcomeText", welcomeText);
                        } else {
                            bundle.putBoolean("newSignUp", false);
                        }
                    default:
                        Log.i("DEFAULT", "NEW EDITPROFILEFRAGMENT");
                        fragment = new EditProfileFragment();
                        if (newSignUp) {
                            bundle.putBoolean("newSignUp", true);
                        } else {
                            bundle.putBoolean("newSignUp", false);
                        }
                        break;
                }
                fragment.setArguments(bundle);
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // sets default screen
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AuthenticationClient.clearCookies(getApplicationContext());
                CredentialsHandler.setToken(MainActivity.this, null, 0, TimeUnit.SECONDS);
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        if (newSignUp) {
//            Log.i("newSignUp", "Changing to edit profile screen");
//            Fragment fragment = new EditProfileFragment();
//            Bundle bundle = new Bundle();
//            bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
//            bundle.putBoolean("newSignUp", true);
//            String welcomeText = "It looks like you're new here! Let's start by filling out some basic profile details.";
//            bundle.putString("tvWelcomeText", welcomeText);
//            fragment.setArguments(bundle);
//            fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
            bottomMenu.setSelectedItemId(R.id.actionProfile);
        } else {
            bottomMenu.setSelectedItemId(R.id.actionMatch);
        }

    }

}
