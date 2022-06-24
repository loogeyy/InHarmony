package com.example.inharmony;

import androidx.appcompat.app.AppCompatActivity;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.SeedsGenres;
import okhttp3.Call;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignUpActivity extends AppCompatActivity {
    static final String TAG = "SignUpActivity";
    static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private List<String> genreList = Arrays.asList("hi", "hello", "hey");

    private MultiSpinnerSearch genres;
    private TextView tvWelcomeText;
    private Button btnUpdateProfile;
    private EditText etName;
    private EditText etAge;
    //private EditText etGender;

    public static Intent createIntent(Context context) {
        return new Intent(context, SignUpActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        genres = findViewById(R.id.genres);
        tvWelcomeText = findViewById(R.id.tvWelcomeText);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        //etGender = findViewById(R.id.etGender);



        Intent intent = getIntent();
        String token = intent.getStringExtra(EXTRA_TOKEN);
        tvWelcomeText.setText(intent.getStringExtra("tvWelcomeText"));

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();
        // Why doesn't this work like it does in LoginActivity?
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.i("Token", token);
                service.getSeedsGenres(new Callback<SeedsGenres>() {
                    @Override
                    public void success(SeedsGenres seedsGenres, Response response) {
                        Log.i(TAG, "Success");
                        genreList = seedsGenres.genres;
                        initializeGenreSelector(genres);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(TAG, "retrofiterror", error);
                    }
                });
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(etName.getText())) {
                    etName.setError("First name is required!");
                    return;
                }
//                if (!TextUtils.isDigitsOnly(etAge.getText())){
//                    etAge.setError("Please enter your age as a number.");
//                    return;
//                }
                if (TextUtils.isEmpty(etAge.getText())) {
                    etAge.setError("Please enter your age.");
                    return;
                }
                if (Integer.parseInt(etAge.getText().toString()) < 13) {
                    etAge.setError("Users must be above 13 to make an account");
                    return;
                }


                    Intent i = new Intent(SignUpActivity.this, MainActivity.class);

            }
        });
    }

    private List<KeyPairBoolData> generateGenresList(List<String> list) {
        List<KeyPairBoolData> newList = new ArrayList<>();
        Log.i("Genres Size ", String.valueOf(list.size()));

        // set up list of displayed genres as unselected
        for (int i = 0; i < list.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(i + 1);
            keyPairBoolData.setName(list.get(i));
            keyPairBoolData.setSelected(false);
            newList.add(keyPairBoolData);
        }

        return newList;
    }


    private void initializeGenreSelector(MultiSpinnerSearch genres) {
        genres.setSearchEnabled(true);
        genres.setSearchHint("Select your top genres");
        genres.setEmptyTitle("No matching genres found");
        genres.setClearText("Reset");
        genres.setItems(generateGenresList(genreList), new MultiSpinnerListener() {
            @Override
            public void onItemsSelected(List<KeyPairBoolData> items) {
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).isSelected()) {
                        Log.i(TAG, i + " : " + items.get(i).getName() + " : " + items.get(i).isSelected());
                    }
                }
            }
        });
        genres.setLimit(3, new MultiSpinnerSearch.LimitExceedListener() {
            @Override
            public void onLimitListener(KeyPairBoolData data) {
                Toast.makeText(getApplicationContext(),
                        "You may only sel", Toast.LENGTH_LONG).show();
            }
        });
    }
}