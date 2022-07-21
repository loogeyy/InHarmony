package com.example.inharmony.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.Player;
import com.example.inharmony.PlayerService;
import com.example.inharmony.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class ProfileFragment extends Fragment {
    private static final String TAG = "MyProfileFragment";
    private ImageView btnEditProfile;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvBio;

    private TextView tvFavAlbum;
    private TextView tvFavTrack;
    private TextView tvFavArtist;
    private ImageView ivFavAlbum;
    private TextView tvFavGenres;
    private ImageView ivFavTrack;
    private ImageView ivFavArtist;
    private ImageView ivPlayButton;

    private boolean myProfile;
    private ParseUser user;

    private Track favTrack;
    private Artist favArtist;
    private AlbumSimple favAlbum;

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private String token;
    private boolean newSignUp;

    private Player mPlayer;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayer = ((PlayerService.PlayerBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer = null;
        }
    };

    public ProfileFragment() {
        // Required empty public constructor
    }

    public ProfileFragment(boolean myProfile, ParseUser user) {
        this.myProfile = myProfile;
        this.user = user;
    }

    @Override
    public void onDestroyView() {
        mPlayer.release();
        super.onDestroyView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        ivProfilePic = view.findViewById(R.id.ivChatProfilePic);
        tvName = view.findViewById(R.id.tvName);
        tvFavGenres = view.findViewById(R.id.tvFavGenres);
        tvFavAlbum = view.findViewById(R.id.tvFavAlbum);
        tvFavArtist = view.findViewById(R.id.tvFavArtist);
        tvFavTrack = view.findViewById(R.id.tvFavTrack);
        ivFavArtist = view.findViewById(R.id.ivFavArtist);
        ivFavAlbum = view.findViewById(R.id.ivFavAlbum);
        ivFavTrack = view.findViewById(R.id.ivFavTrack);
        ivPlayButton = view.findViewById(R.id.ivPlayButton);
        tvBio = view.findViewById(R.id.tvBio);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
        } else {
            Log.i(TAG, "BUNDLE WAS NULL");
        }

        if (!myProfile) {
            btnEditProfile.setVisibility(View.GONE);
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
        }

        getContext().bindService(PlayerService.getIntent(getContext()), mServiceConnection, Activity.BIND_AUTO_CREATE);
        ivPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked: " + favTrack.name.toString());
                SpotifyApi spotifyApi = new SpotifyApi();
                spotifyApi.setAccessToken(token);
                SpotifyService service = spotifyApi.getService();
                selectTrack(favTrack);
            }
        });

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.release();
                Fragment fragment = new EditProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("newSignUp", false);
                String welcomeText = "Edit your profile details below.";
                bundle.putString("tvWelcomeText", welcomeText);
                bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment, "EDITPROFILE").addToBackStack(null).commit();
            }
        });

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!newSignUp) {
                    favTrack = service.getTracks(user.get("favTrack").toString()).tracks.get(0);
                    favArtist = service.getArtists(user.get("favArtist").toString()).artists.get(0);
                    List<Album> albums = service.getAlbums(user.get("favAlbum").toString()).albums;
                    for (Album a : albums) {
                        if (a.images.size() != 0) {
                            if (a.images.get(0).url.equals(user.get("favAlbumImageUrl"))) {
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

    }

    private void populateProfile(Track favTrack, Artist favArtist, AlbumSimple favAlbum) {
        ArrayList<String> genres = (ArrayList<String>) user.get("favGenres");
        String favGenres = "";
        for (int i = 0; i < genres.size(); i++) {
            if (i == 0) {
                favGenres = genres.get(i);
            } else {
                favGenres = favGenres + ", " + genres.get(i);
            }
        }
        tvFavGenres.setText(favGenres);
        tvFavTrack.setText(favTrack.name.toString() + " - " + favTrack.artists.get(0).name.toString());
        tvFavArtist.setText(favArtist.name.toString());
        tvFavAlbum.setText(favAlbum.name.toString());
        if (favTrack.album.images.size() != 0) {
            Image image = favTrack.album.images.get(0);
            Glide.with(getContext()).load(image.url).into(ivFavTrack);
        }

        if (favArtist.images.size() != 0) {
            Image image = favArtist.images.get(0);
            Glide.with(getContext()).load(image.url).into(ivFavArtist);
        }

        if (favAlbum.images.size() != 0) {
            Image image = favAlbum.images.get(0);
            Glide.with(getContext()).load(image.url).into(ivFavAlbum);
        }

        tvName.setText(user.get("name").toString() + ", " + user.get("age").toString());

        if (user.get("bio") != null) {
            tvBio.setText(user.get("bio").toString());
        }

        ParseFile profilePic = (ParseFile) user.get("profilePic");
        if (profilePic != null) {
            Glide.with(getContext()).load(profilePic.getUrl()).into(ivProfilePic);
        } else {
            ivProfilePic.setImageResource(R.drawable.nopfp);
        }

        if (favTrack.preview_url == null) {
            ivPlayButton.setVisibility(View.GONE);
        }
    }

    public void selectTrack(Track track) {

        String previewUrl = track.preview_url;

        if (mPlayer == null) {
            Log.i(TAG, "mPlayer is Null");
            return;
        }

        String currentTrackUrl = mPlayer.getCurrentTrack();

        if (currentTrackUrl == null || !currentTrackUrl.equals(previewUrl)) {
            Log.i(TAG, "Play");
            mPlayer.play(previewUrl);
            ivPlayButton.setImageResource(R.drawable.pause);

        }
        else if (mPlayer.isPlaying()) {
            Log.i(TAG, "Pause");
            mPlayer.pause();
            ivPlayButton.setImageResource(R.drawable.play);
        } else {
            Log.i(TAG, "Resume");
            ivPlayButton.setImageResource(R.drawable.pause);
            mPlayer.resume();
        }
    }


}