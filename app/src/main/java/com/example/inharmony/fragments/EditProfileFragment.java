package com.example.inharmony.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.example.inharmony.MainActivity;
import com.example.inharmony.R;
import com.example.inharmony.SignUpActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.SeedsGenres;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditProfileFragment extends Fragment {

    static final String TAG = "EditProfileFragment";
    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private List<String> genreList = Arrays.asList("hi", "hello", "hey");

    private MultiSpinnerSearch genres;
    private TextView tvWelcomeText;
    private Button btnUpdateProfile;
    private EditText etName;
    private EditText etAge;
    private BottomNavigationView bottomMenu;

    private boolean newSignUp;
    private String username;
    private String password;
    private String token;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        genres = view.findViewById(R.id.genres);
        tvWelcomeText = view.findViewById(R.id.tvWelcomeText);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        etName = view.findViewById(R.id.etName);
        etAge = view.findViewById(R.id.etAge);
        bottomMenu = getActivity().findViewById(R.id.bottomMenu);
        //etGender = findViewById(R.id.etGender);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
            tvWelcomeText.setText(bundle.getString("tvWelcomeText"));
            Toast.makeText(getContext(), "token found: " + token, Toast.LENGTH_SHORT).show();
        }

        if (newSignUp) {
            bottomMenu.setVisibility(View.GONE);
        }


//        //Intent intent = getIntent();
//        newSignUp = intent.getExtras().getBoolean("newSignUp");
//        token = intent.getStringExtra(EXTRA_TOKEN);
//        tvWelcomeText.setText(intent.getStringExtra("tvWelcomeText"));

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                username = service.getMe().email;
                password = service.getMe().id;
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
                        Log.e(TAG, "Retrofit error", error);
                    }
                });
            }
        });

        checkButtonClicked();
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, EditProfileFragment.class);
    }


    private void checkButtonClicked() {
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("newsignup", String.valueOf(newSignUp));

                String name;
                Integer age;
                JSONArray selectedGenres = new JSONArray();

                // create a new user in the database
                if (newSignUp) {
                    // all fields must be filled out
                    if (TextUtils.isEmpty(etName.getText())) {
                        etName.setError("First name is required!");
                        return;
                    }
                    name = etName.getText().toString();

                    if (TextUtils.isEmpty(etAge.getText())) {
                        etAge.setError("Please enter your age.");
                        return;
                    }
                    if (!TextUtils.isDigitsOnly(etAge.getText())){
                        etAge.setError("Please enter your age as a number.");
                        return;
                    }
                    if (Integer.parseInt(etAge.getText().toString()) < 13) {
                        etAge.setError("Users must be above 13 to make an account");
                        return;
                    }

                    age = Integer.parseInt(etAge.getText().toString());

                    //Log.i("LIST OF SELECTED ITEMS", selectedGenres.toString());
                    selectedGenres = getSelectedGenres();
                    if (selectedGenres.length() == 0) {
                        ((TextView)genres.getSelectedView()).setError("Please select up to 3 genres.");
                        ((TextView) genres.getSelectedView()).setText("Please select up to 3 genres.");
                        return;
                    }

                    createUser(username, password, name, age, selectedGenres, token);
                    Log.i("HELLO", "HELLO");
                }
                // only update user in the database
                else {
                    if (!TextUtils.isEmpty(etName.getText())) {
                        name = etName.getText().toString();
                        ParseUser.getCurrentUser().put("name", name);
                    }

                    if (!TextUtils.isEmpty(etAge.getText())) {
                        if (Integer.parseInt(etAge.getText().toString()) < 13) {
                            etAge.setError("Users must be above 13 to make an account");
                            return;
                        }
                        if (!TextUtils.isDigitsOnly(etAge.getText())){
                            etAge.setError("Please enter your age as a number.");
                            return;
                        }
                        age = Integer.parseInt(etAge.getText().toString());
                        ParseUser.getCurrentUser().put("age", age);
                    }

                    if (selectedGenres.length() != 0) {
                        ((TextView)genres.getSelectedView()).setError("Please select up to 3 genres.");
                        ((TextView) genres.getSelectedView()).setText("Please select up to 3 genres.");
                        selectedGenres = getSelectedGenres();
                        ParseUser.getCurrentUser().put("favGenre", selectedGenres);
                    }
                }

                Intent i = new Intent(getContext(), MainActivity.class);
                startActivity(i);
            }
        });
    }

    // populates genres into the dropdown
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
        if (!newSignUp) {
            JSONArray array = (JSONArray) ParseUser.getCurrentUser().get("favGenres");
            for (int j = 0; j < array.length(); j++) {
                try {
                    String genre = array.get(j).toString();
                    Integer index = newList.indexOf(genre);

                    KeyPairBoolData updated = new KeyPairBoolData();
                    updated.setName(genre);
                    updated.setSelected(true);
                    newList.set(index, updated);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return newList;
    }

    // set up dropdown properties
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
        // maximum of 3 genres can be selected
        genres.setLimit(3, new MultiSpinnerSearch.LimitExceedListener() {
            @Override
            public void onLimitListener(KeyPairBoolData data) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "You may only select 3 genres", Toast.LENGTH_LONG).show();
            }
        });
    }

    // add new user to the database
    private void createUser(String username, String password, String name, Integer age, JSONArray selectedGenres, String token) {
        Log.i(TAG, "Attempting to create user " + username + "...");
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.put("name", name);
        user.put("age", age);
        user.put("favGenres", selectedGenres);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.i("createUser TOKEN: ", token);
                    Log.i("create user E code:", Integer.toString(e.getCode()));
                    Log.e(TAG, "Issue with sign up", e);
                    //https://parseplatform.org/Parse-SDK-dotNET/api/html/T_Parse_ParseException_ErrorCode.htm
                    return;
                }
            }
        });
    }

    private JSONArray getSelectedGenres() {
        List<KeyPairBoolData> selectedGenres = genres.getSelectedItems();
        JSONArray list = new JSONArray();
        for (int i = 0; i < selectedGenres.size(); i++) {
            list.put(selectedGenres.get(i).getName());
        }
        return list;
    }
    }