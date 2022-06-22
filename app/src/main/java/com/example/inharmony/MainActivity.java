package com.example.inharmony;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inharmony.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.spotify.sdk.android.authentication.AuthenticationClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Track;

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

    static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    private static final String KEY_CURRENT_QUERY = "CURRENT_QUERY";

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
        String token = intent.getStringExtra(EXTRA_TOKEN);

        bottomMenu = findViewById(R.id.bottomMenu);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AuthenticationClient.clearCookies(getApplicationContext());
                CredentialsHandler.setToken(MainActivity.this, null, 0, TimeUnit.SECONDS);
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });
        bottomMenu.setSelectedItemId(R.id.actionProfile);
        bottomMenu.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = new SearchFragment();
                switch (item.getItemId()) {

                    case R.id.actionMatch:
                        //fragment = new MatchFragment();
                        break;
                    case R.id.actionMessage:
                        break;
                    case R.id.actionProfile:
                    default:
                        Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                        Log.d("Menu", "Profile pressed");
                        fragment = new SearchFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(SearchFragment.EXTRA_TOKEN, token);
                        fragment.setArguments(bundle);
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        //fragment = new MatchFragment(ParseUser.getCurrentUser());
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // sets default screen

    }
}
