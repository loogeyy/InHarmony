package com.example.inharmony.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;

import com.androidbuts.multispinnerfilter.KeyPairBoolData;
import com.androidbuts.multispinnerfilter.MultiSpinnerListener;
import com.androidbuts.multispinnerfilter.MultiSpinnerSearch;
import com.bumptech.glide.Glide;
import com.example.inharmony.MainActivity;
import com.example.inharmony.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.SeedsGenres;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditProfileFragment extends Fragment {

    static final String TAG = "EditProfileFragment";

    public static String FAV_ALBUM = "FAV_ALBUM";
    public static String FAV_TRACK = "FAV_TRACK";
    public static String FAV_ARTIST = "FAV_ARTIST";
    public static String EXTRA_TOKEN = "EXTRA_TOKEN";

    private EditText etAge;
    private EditText etName;
    private Button btnFavSong;
    private Button btnFavAlbum;
    private Button btnFavArtist;
    private TextView tvWelcomeText;
    private Button btnUpdateProfile;
    private ImageButton btnChangePic;
    private MultiSpinnerSearch genres;
    private ImageView ivChangeProfilePic;
    private BottomNavigationView bottomMenu;

    private TextView tvEditFavTrack;
    private TextView tvEditFavAlbum;
    private TextView tvEditFavArtist;
    private ImageView ivEditFavTrack;
    private ImageView ivEditFavAlbum;
    private ImageView ivEditFavArtist;

    private String username;
    private String password;

    // bundle values
    private String token;
    private boolean newSignUp;

    private Track favTrack;
    private Artist favArtist;
    private AlbumSimple favAlbum;

    private File photoFile;
    private ParseFile photo;
    private String photoFileName = "photo.jpg";
    public final static int PICK_PHOTO_CODE = 1046;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private List<String> genreList = Arrays.asList("hi", "hello", "hey");

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "ONCREATEVIEW");
        //EditProfileFragment.this.setInitialSavedState(savedInstanceState);
        if (savedInstanceState == null) {
            Log.i(TAG, "ONCREATEVIEW SAVEDINSTANCESTATE IS NULL");
        } else {
            Log.i(TAG, "ONCREATEVIEW SAVEDINSTANCESTATE CONTAINS STUFF");
        }
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Log.i(TAG,  "IS STATE SAVED: " + String.valueOf(getActivity().getSupportFragmentManager().isStateSaved()));
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated");
// Inflate the layout for this fragment
        if (savedInstanceState != null) {
            favTrack = savedInstanceState.getParcelable(FAV_TRACK);
            favArtist = savedInstanceState.getParcelable(FAV_ARTIST);
            favAlbum = savedInstanceState.getParcelable(FAV_ALBUM);
            token = savedInstanceState.getString(EXTRA_TOKEN);
            newSignUp = savedInstanceState.getBoolean("newSignUp");
            Log.i(TAG, "SAVEDINSTANCESTATE IS NOT NULL");
            // Do something with value if needed
        } else {
            Log.i(TAG, "SAVEDINSTANCESTATE IS NULL");
        }

        //Log.i("EDITPROFILE NEWSIGNUP", String.valueOf(newSignUp));
        etAge = view.findViewById(R.id.etAge);
        etName = view.findViewById(R.id.etName);
        genres = view.findViewById(R.id.genres);
        btnFavSong = view.findViewById(R.id.btnFavSong);
        btnFavAlbum = view.findViewById(R.id.btnFavAlbum);
        btnChangePic = view.findViewById(R.id.btnChangePic);
        btnFavArtist = view.findViewById(R.id.btnFavArtist);
        tvWelcomeText = view.findViewById(R.id.tvWelcomeText);
        bottomMenu = getActivity().findViewById(R.id.bottomMenu);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        ivChangeProfilePic = view.findViewById(R.id.ivChangeProfilePic);

        tvEditFavTrack = view.findViewById(R.id.tvEditFavTrack);
        tvEditFavAlbum = view.findViewById(R.id.tvEditFavAlbum);
        tvEditFavArtist = view.findViewById(R.id.tvEditFavArtist);
        ivEditFavTrack = view.findViewById(R.id.ivEditFavTrack);
        ivEditFavAlbum = view.findViewById(R.id.ivEditFavAlbum);
        ivEditFavArtist = view.findViewById(R.id.ivEditFavArtist);

        //etGender = findViewById(R.id.etGender);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
            //Log.i("WELCOME TEXT", bundle.getString("tvWelcomeText").toString());
            tvWelcomeText.setText(bundle.getString("tvWelcomeText"));
            Toast.makeText(getContext(), "token found: " + token, Toast.LENGTH_SHORT).show();

            if (bundle.getParcelable("favTrack") != null) {
                favTrack = bundle.getParcelable("favTrack");
                Log.i(TAG, "GOT BUNDLE FAVTRACK " + favTrack.toString());
            }

            if (bundle.getParcelable("favArtist") != null) {
                favArtist = bundle.getParcelable("favArtist");
                Log.i(TAG, "GOT BUNDLE FAVARTIST " + favArtist.toString());
            }

            if (bundle.getParcelable("favAlbum") != null) {
                favAlbum = bundle.getParcelable("favAlbum");
                Log.i(TAG, "GOT BUNDLE FAVALBUM " + favAlbum.toString());
            }
        } else {
            Log.i(TAG, "BUNDLE WAS NULL");
        }

        //Log.i("EDITPROFILEFRAGMENT NEWSIGNUP", String.valueOf(newSignUp));

        if (favTrack != null) {
            Log.i("EDITPROFILE FAVTRACK", favTrack.toString());
        } else {
            Log.i("EDITPROFILE FAVTRACK", "NULL");
        }

        if (favArtist != null) {
            Log.i("EDITPROFILE FAVARTIST", favArtist.toString());
        } else {
            Log.i("EDITPROFILE FAVARTIST", "NULL");
        }

        if (favAlbum != null) {
            Log.i("EDITPROFILE FAVALBUM", favAlbum.toString());
        } else {
            Log.i("EDITPROFILE FAVALBUM", "NULL");
        }

        if (token != null) {
            //Log.i("EDITPROFILE TOKEN", token);
        } else {
            //Log.i("EDITPROFILE TOKEN", "NULL");
        }

        if (newSignUp) {
            bottomMenu.setVisibility(View.GONE);
        }
        else {
                etAge.setText(ParseUser.getCurrentUser().get("age").toString());
                etName.setText(ParseUser.getCurrentUser().get("name").toString());

                ParseFile profilePic = (ParseFile) ParseUser.getCurrentUser().get("profilePic");
                if (profilePic != null) {
                    Glide.with(getContext()).load(profilePic.getUrl()).into(ivChangeProfilePic);
                } else {
                    ivChangeProfilePic.setImageResource(R.drawable.nopfp);
                }
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
                //Log.i("Token", token);
                service.getSeedsGenres(new Callback<SeedsGenres>() {
                    @Override
                    public void success(SeedsGenres seedsGenres, Response response) {
                        //Log.i(TAG, "Success");
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

        checkUpdatePhotoButtonClicked();
        checkUpdateProfileButtonClicked();
        checkFavSongButtonClicked();
        checkFavArtistButtonClicked();
        checkFavAlbumButtonClicked();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumed");
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onSTART");
    }

    public static Intent createIntent(Context context) {
        return new Intent(context, EditProfileFragment.class);
    }

    private void checkUpdatePhotoButtonClicked() {
        btnChangePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popUp = new PopupMenu(getContext(), btnChangePic);
                popUp.getMenuInflater().inflate(R.menu.edit_profile_pic_menu, popUp.getMenu());
                popUp.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.photogallery) {
                            onPickPhoto(v);
                            return true;
                        }
                        if (item.getItemId() == R.id.camera) {
                            launchCamera();
                            return true;
                        }
                        return true;
                    }

                });
                popUp.show();
            }
        });

    }

    private void checkUpdateProfileButtonClicked() {
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favTrack != null) {
                    Log.i("EDITPROFILE FAVTRACK", favTrack.toString());
                } else {
                    Log.i("EDITPROFILE FAVTRACK", "NULL");
                }

                if (favArtist != null) {
                    Log.i("EDITPROFILE FAVARTIST", favArtist.toString());
                } else {
                    Log.i("EDITPROFILE FAVARTIST", "NULL");
                }

                if (favAlbum != null) {
                    Log.i("EDITPROFILE FAVALBUM", favAlbum.toString());
                } else {
                    Log.i("EDITPROFILE FAVALBUM", "NULL");
                }
                //Log.i("newsignup", String.valueOf(newSignUp));

                String name;
                Integer age;
                JSONArray selectedGenres = new JSONArray();

                // create a new user in the database
                if (newSignUp) {
                    //Log.i("NEW SIGN UP", "OPENED UP EDIT PROFILE FRAGMENT");
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
                    if (!TextUtils.isDigitsOnly(etAge.getText().toString())){
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

                    if (photoFile != null) {
                        photo = new ParseFile(photoFile);
                    }

                    createUser(username, password, name, age, selectedGenres, photo, token);
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

                    selectedGenres = getSelectedGenres();
                    Log.i("SELECTED GENRES", String.valueOf(selectedGenres));
                    if (selectedGenres.length() == 0) {
                        ((TextView) genres.getSelectedView()).setError("Please select up to 3 genres.");
                        ((TextView) genres.getSelectedView()).setText("Please select up to 3 genres.");
                        return;
                    }
                    ParseUser.getCurrentUser().put("favGenres", selectedGenres);

                    if (photoFile != null) {
                        Log.i("PHOTOFILE NOT NEW", photoFile.toString());
                        photo = new ParseFile(photoFile);
                        ParseUser.getCurrentUser().put("profilePic", photo);
                    }
                    ParseUser.getCurrentUser().saveInBackground();
                }

                Intent i = new Intent(getContext(), MainActivity.class);
                i.putExtra(MainActivity.EXTRA_TOKEN, token);
                i.putExtra(MainActivity.NEW_SIGN_UP, false);
                startActivity(i);
                getActivity().finish();
            }
        });
    }

    private void checkFavSongButtonClicked() {
        btnFavSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SearchFragment.EXTRA_TOKEN, token);
                bundle.putString(SearchFragment.SEARCH_TYPE, "TRACK");
                if (newSignUp) {
                    bundle.putBoolean("newSignUp", true);
                } else {
                    bundle.putBoolean("newSignUp", false);
                }
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().setFragmentResultListener("favTrack", EditProfileFragment.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if (result != null) {
                            favTrack = result.getParcelable("favTrack");
                            tvEditFavTrack.setText(favTrack.name);
                            Image image = favTrack.album.images.get(0);
                            if (image != null) {
                                Glide.with(getContext()).load(image.url).into(ivEditFavTrack);
                            }
                            Log.i(TAG, "TRACK: " + favTrack.toString());
                            //Log.i(TAG, "ARTIST: " + favArtist.toString());
                            //Log.i(TAG, "ALBUM: " + favAlbum.toString());
                        } else {
                            Log.i(TAG, "ONFRAGMENTRESULT BUNDLE IS NULL");
                        }

                    }
                });
                fragment.setArguments(bundle);
                //getActivity().getSupportFragmentManager().saveFragmentInstanceState(EditProfileFragment.this);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    ft.add(R.id.flContainer, fragment);
                }
                    ft.show(fragment);
                    ft.hide(EditProfileFragment.this);
                    ft.commit();
               }
        });
    }

    private void checkFavArtistButtonClicked() {
        btnFavArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SearchFragment.EXTRA_TOKEN, token);
                bundle.putString(SearchFragment.SEARCH_TYPE, "ARTIST");
                if (newSignUp) {
                    bundle.putBoolean("newSignUp", true);
                } else {
                    bundle.putBoolean("newSignUp", false);
                }
                getActivity().getSupportFragmentManager().setFragmentResultListener("favArtist", EditProfileFragment.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if (result != null) {
                            favArtist = result.getParcelable("favArtist");
                            tvEditFavArtist.setText(favArtist.name);
                            Image image = favArtist.images.get(0);
                            if (image != null) {
                                Glide.with(getContext()).load(image.url).into(ivEditFavArtist);
                            }
                            Log.i(TAG, "ARTIST: " + favArtist.toString());
                            //Log.i(TAG, "ARTIST: " + favArtist.toString());
                            //Log.i(TAG, "ALBUM: " + favAlbum.toString());
                        } else {
                            Log.i(TAG, "ONFRAGMENTRESULT BUNDLE IS NULL");
                        }

                    }
                });

                //getActivity().getSupportFragmentManager().saveFragmentInstanceState(EditProfileFragment.this);
                fragment.setArguments(bundle);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    ft.add(R.id.flContainer, fragment);
                }
                ft.show(fragment);
                ft.hide(EditProfileFragment.this);
                ft.commit();
            }
        });
                //getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

    }

    private void checkFavAlbumButtonClicked() {
        btnFavAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SearchFragment.EXTRA_TOKEN, token);
                bundle.putString(SearchFragment.SEARCH_TYPE, "ALBUM");
                if (newSignUp) {
                    bundle.putBoolean("newSignUp", true);
                } else {
                    bundle.putBoolean("newSignUp", false);
                }
                getActivity().getSupportFragmentManager().setFragmentResultListener("favAlbum", EditProfileFragment.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if (result != null) {
                            favAlbum = result.getParcelable("favAlbum");
                            tvEditFavAlbum.setText(favAlbum.name);
                            Image image = favAlbum.images.get(0);
                            if (image != null) {
                                Glide.with(getContext()).load(image.url).into(ivEditFavAlbum);
                            }
                            Log.i(TAG, "ALBUM: " + favAlbum.toString());
                            //Log.i(TAG, "ARTIST: " + favArtist.toString());
                            //Log.i(TAG, "ALBUM: " + favAlbum.toString());
                        } else {
                            Log.i(TAG, "ONFRAGMENTRESULT BUNDLE IS NULL");
                        }

                    }
                });
                //getActivity().getSupportFragmentManager().saveFragmentInstanceState(EditProfileFragment.this);
                fragment.setArguments(bundle);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    ft.add(R.id.flContainer, fragment);
                }
                ft.show(fragment);
                ft.hide(EditProfileFragment.this);
                ft.commit();
            }
        });
    }



    // populates genres into the dropdown
    private List<KeyPairBoolData> generateGenresList(List<String> list) {
        List<KeyPairBoolData> newList = new ArrayList<>();

        // set up list of displayed genres as unselected
        for (int i = 0; i < list.size(); i++) {
            KeyPairBoolData keyPairBoolData = new KeyPairBoolData();
            keyPairBoolData.setId(i + 1);
            keyPairBoolData.setName(list.get(i));
            if (!newSignUp) {
                ArrayList<String> array = (ArrayList<String>) ParseUser.getCurrentUser().get("favGenres");
                if (array.contains(list.get(i))) {
                    keyPairBoolData.setSelected(true);
                } else {
                    keyPairBoolData.setSelected(false);
                }

            } else {
                keyPairBoolData.setSelected(false);
            }

            newList.add(keyPairBoolData);
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
    private void createUser(String username, String password, String name, Integer age, JSONArray selectedGenres, ParseFile parseFile, String token) {
        Log.i(TAG, "Attempting to create user " + username + "...");
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.put("name", name);
        user.put("age", age);
        user.put("favGenres", selectedGenres);
        if (parseFile != null) {
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (null == e) {
                        user.put("profilePic", parseFile);
                        Log.i("PROFILE PIC", "NEW IMAGE UPLOADED");
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
                }
            });
        }
        else {
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

    }

    private JSONArray getSelectedGenres() {
        List<KeyPairBoolData> selectedGenres = genres.getSelectedItems();
        JSONArray list = new JSONArray();
        if (selectedGenres.size() == 0) {
            return list;
        }

        for (int i = 0; i < selectedGenres.size(); i++) {
            list.put(selectedGenres.get(i).getName());
        }
        return list;
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

    }

    private void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Log.i("onPickPhoto","Opening camera roll");
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            // picture taken
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivChangeProfilePic.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if ((data != null) && requestCode == PICK_PHOTO_CODE && (resultCode == RESULT_OK)) {
//            Uri photoUri = data.getData();
//            // how to get photo file?
//            //photoFile = getPhotoFileUri(photoUri.toString());
//
//            // Load the image located at photoUri into selectedImage
//            Bitmap selectedImage = loadFromUri(photoUri);
//
//            // Load the selected image into a preview
//            ivChangeProfilePic.setImageBitmap(selectedImage);
            Uri selectedImage = data.getData();
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), selectedImage);
                Bitmap bitmap = ImageDecoder.decodeBitmap(source);

                // Store smaller bitmap to disk
                // Configure byte output stream
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                // Compress the image further
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
                // Create a new file for the resized bitmap
                File resizedFile = getPhotoFileUri(photoFileName + "_resized");
                try {
                    resizedFile.createNewFile();
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(resizedFile);
                        // Write the bytes of the bitmap to file
                        fos.write(bytes.toByteArray());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ivChangeProfilePic.setImageBitmap(bitmap);
                photoFile = resizedFile;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d("getPhotoUri", "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);

    }

}