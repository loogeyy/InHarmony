package com.example.inharmony.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
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
import com.example.inharmony.R;
import com.example.inharmony.Search;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONArray;
import org.json.JSONException;

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
import kaaes.spotify.webapi.android.models.Album;
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
    private EditText etChangeBio;
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

    private Search.ActionListener mActionListener;

    private String username;
    private String password;

    // bundle values
    private String token;
    private boolean newSignUp;

    private Track favTrack;
    private Artist favArtist;
    private AlbumSimple favAlbum;

    private JSONArray featureAvgs;
    private JSONArray featureWeights;

    private File photoFile;
    private ParseFile photo;
    private String photoFileName = "photo.jpg";
    public final static int PICK_PHOTO_CODE = 1046;
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    private List<String> genreList = new ArrayList<>();

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etAge = view.findViewById(R.id.etAge);
        etName = view.findViewById(R.id.etName);
        genres = view.findViewById(R.id.genres);
        btnFavSong = view.findViewById(R.id.btnFavSong);
        btnFavAlbum = view.findViewById(R.id.btnFavAlbum);
        etChangeBio = view.findViewById(R.id.etChangeBio);
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

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
            tvWelcomeText.setText(bundle.getString("tvWelcomeText"));
            if (newSignUp) {
                try {
                    featureAvgs = new JSONArray(bundle.getString("featureAvgs"));
                    featureWeights = new JSONArray(bundle.getString("featureWeights"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {
            Log.i(TAG, "BUNDLE WAS NULL");
        }

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!newSignUp) {
                   favTrack = service.getTracks(ParseUser.getCurrentUser().get("favTrack").toString()).tracks.get(0);
                   favArtist = service.getArtists(ParseUser.getCurrentUser().get("favArtist").toString()).artists.get(0);
                   List <Album> albums = service.getAlbums(ParseUser.getCurrentUser().get("favAlbum").toString()).albums;
                    for (Album a : albums) {
                        if (a.images.size() != 0) {
                            if (a.images.get(0).url.equals(ParseUser.getCurrentUser().get("favAlbumImageUrl"))) {
                                favAlbum = a;
                                break;
                            }
                        }
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateProfile(favTrack, favArtist, favAlbum);
                        }
                    });
                }
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                username = service.getMe().email;
                password = service.getMe().id;

                service.getSeedsGenres(new Callback<SeedsGenres>() {
                    @Override
                    public void success(SeedsGenres seedsGenres, Response response) {
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

        if (newSignUp) {
            bottomMenu.setVisibility(View.GONE);
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
        }

    }

    private void populateProfile(Track favTrack, Artist favArtist, AlbumSimple favAlbum) {
        if (!newSignUp){
            tvEditFavTrack.setText(favTrack.name.toString() + " - " + favTrack.artists.get(0).name.toString());
            tvEditFavArtist.setText(favArtist.name.toString());
            tvEditFavAlbum.setText(favAlbum.name.toString());
            if (favTrack.album.images.size() != 0) {
                Image image = favTrack.album.images.get(0);
                Glide.with(getContext()).load(image.url).into(ivEditFavTrack);
            }

            if (favArtist.images.size() != 0) {
                Image image = favArtist.images.get(0);
                Glide.with(getContext()).load(image.url).into(ivEditFavArtist);
            }

            if (favAlbum.images.size() != 0) {
                Image image = favAlbum.images.get(0);
                Glide.with(getContext()).load(image.url).into(ivEditFavAlbum);
            }

            etAge.setText(ParseUser.getCurrentUser().get("age").toString());
            etName.setText(ParseUser.getCurrentUser().get("name").toString());

            ParseFile profilePic = (ParseFile) ParseUser.getCurrentUser().get("profilePic");
            if (profilePic != null) {
                Glide.with(getContext()).load(profilePic.getUrl()).into(ivChangeProfilePic);
            } else {
                ivChangeProfilePic.setImageResource(R.drawable.nopfp);
            }

            if (ParseUser.getCurrentUser().get("bio") != null) {
                etChangeBio.setText(ParseUser.getCurrentUser().get("bio").toString());
            }
        }
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
                    if (!TextUtils.isDigitsOnly(etAge.getText().toString())){
                        etAge.setError("Please enter your age as a number.");
                        return;
                    }
                    if (Integer.parseInt(etAge.getText().toString()) < 13) {
                        etAge.setError("Users must be above 13 to make an account");
                        return;
                    }

                    if (favTrack == null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Please select your favorite track", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (favArtist == null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Please select your favorite artist", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (favAlbum == null) {
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Please select your favorite album", Toast.LENGTH_LONG).show();
                        return;
                    }

                    age = Integer.parseInt(etAge.getText().toString());

                    selectedGenres = getSelectedGenres();
                    if (selectedGenres.length() == 0) {
                        ((TextView)genres.getSelectedView()).setError("Please select up to 3 genres.");
                        ((TextView) genres.getSelectedView()).setText("Please select up to 3 genres.");
                        return;
                    }

                    if (photoFile != null) {
                        photo = new ParseFile(photoFile);
                    }

                    String bio = null;
                    if (etChangeBio.getText() != null) {
                        bio = etChangeBio.getText().toString();
                    }

                    try {
                        createUser(username, password, name, age, bio, selectedGenres, photo, favTrack, favArtist, favAlbum, featureAvgs, featureWeights, token);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                // only update user in the database
                else {
                    if (favTrack != null) {
                        //ParseUser.getCurrentUser().put("favTrack", favTrack.name + " - " + favTrack.artists.get(0).name);
                        ParseUser.getCurrentUser().put("favTrack", favTrack.id);

                    }
                    if (favArtist != null) {
                        ParseUser.getCurrentUser().put("favArtist", favArtist.id);
                        //ParseUser.getCurrentUser().put("favArtist", favArtist.name);
                    }
                    if (favAlbum != null) {
                        ParseUser.getCurrentUser().put("favAlbum", favAlbum.id);
                        //ParseUser.getCurrentUser().put("favAlbum", favAlbum.name);
                        if (favAlbum.images.size() != 0) {
                            ParseUser.getCurrentUser().put("favAlbumImageUrl", favAlbum.images.get(0).url);
                        }
                    }

                    SpotifyApi spotifyApi = new SpotifyApi();
                    spotifyApi.setAccessToken(token);
                    SpotifyService service = spotifyApi.getService();

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

                    if (!TextUtils.isEmpty(etChangeBio.getText())) {
                        String bio = etChangeBio.getText().toString();
                        ParseUser.getCurrentUser().put("bio", bio);
                    }

                    selectedGenres = getSelectedGenres();
                    if (selectedGenres.length() == 0) {
                        ((TextView) genres.getSelectedView()).setError("Please select up to 3 genres.");
                        ((TextView) genres.getSelectedView()).setText("Please select up to 3 genres.");
                        return;
                    }
                    ParseUser.getCurrentUser().put("favGenres", selectedGenres);

                    if (photoFile != null) {
                        photo = new ParseFile(photoFile);
                        ParseUser.getCurrentUser().put("profilePic", photo);
                    }
                    ParseUser.getCurrentUser().saveInBackground();
                    toProfileFragment();
                }

            }
        });
    }

    private void toProfileFragment() {
        bottomMenu.setVisibility(View.VISIBLE);
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
        Fragment fragment = new ProfileFragment(true, ParseUser.getCurrentUser());
        Bundle bundle = new Bundle();
        bundle.putString(ProfileFragment.EXTRA_TOKEN, token);
        bundle.putBoolean("newSignUp", false);
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
    }

    private void checkFavSongButtonClicked() {
        btnFavSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(SearchFragment.EXTRA_TOKEN, token);
                bundle.putString(SearchFragment.SEARCH_TYPE, "TRACK");
                bundle.putString("TYPE", "profile");
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
                           }

                    }
                });
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
                        }

                    }
                });

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
                        }

                    }
                });
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
    private void createUser(String username, String password, String name, Integer age, String bio, JSONArray selectedGenres, ParseFile parseFile, Track track, Artist artist, AlbumSimple album, JSONArray featureAvgs, JSONArray featureWeights, String token) throws ParseException {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.put("name", name);
        user.put("age", age);
        user.put("favGenres", selectedGenres);
        user.put("favArtist", artist.id);
        user.put("favTrack", track.id);
        user.put("favAlbum", album.id);
        user.put("featureAvgs", featureAvgs);
        user.put("featureWeights", featureWeights);

        if (bio != null) {
            user.put("bio", bio);
        }

        if (favAlbum.images.size() != 0) {
            user.put("favAlbumImageUrl", favAlbum.images.get(0).url);
        }

        if (parseFile != null) {
            Log.i(TAG, "new pfp found");
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (null == e) {
                        user.put("profilePic", parseFile);
                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    return;
                                }
                            }
                        });
                    }
                }
            });
        }
        else {
            Log.i(TAG, "new pfp not found");
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        return;
                    } else {
                        toProfileFragment();
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
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoFile = getPhotoFileUri(photoFileName);

        Log.i("CONTEXT", getContext().toString());
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

         if (intent.resolveActivity(getActivity().getApplicationContext().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }

    }

    private void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            Log.i(TAG, "correct");
            // picture taken
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                ivChangeProfilePic.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!" + resultCode + " request: " + requestCode, Toast.LENGTH_SHORT).show();
            }
        }
        if ((data != null) && requestCode == PICK_PHOTO_CODE && (resultCode == RESULT_OK)) {
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
         File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
            }
        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);

    }

}