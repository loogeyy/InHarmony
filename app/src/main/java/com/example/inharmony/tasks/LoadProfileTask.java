package com.example.inharmony.tasks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.inharmony.Message;
import com.example.inharmony.Player;
import com.example.inharmony.PlayerService;
import com.example.inharmony.R;
import com.example.inharmony.fragments.EditProfileFragment;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPublic;

public class LoadProfileTask extends AsyncTask<ParseUser, Void, Void> {
    private static final String TAG = "LoadProfileTask";

    View view;
    String token;
    SpotifyService service;
    boolean newSignUp;

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
    private ImageView btnSpotifyProfile;

    private ParseUser user;
    private FragmentManager fragmentManager;

    private String profileUrl;
    private Track favTrack;
    private Artist favArtist;
    private AlbumSimple favAlbum;
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


    public LoadProfileTask(View view, String token, SpotifyService service, ParseUser user, FragmentManager fragmentManager) {
        this.view = view;
        this.token = token;
        this.service = service;
        this.user = user;
        this.fragmentManager = fragmentManager;
    }

    @Override
    protected void onPreExecute() {
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
        btnSpotifyProfile = view.findViewById(R.id.btnSpotifyProfile);

        if (!user.getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
            btnEditProfile.setVisibility(View.GONE);
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
        }

        view.getContext().bindService(PlayerService.getIntent(view.getContext()), mServiceConnection, Activity.BIND_AUTO_CREATE);
    }

    @Override
    protected Void doInBackground(ParseUser... parseUsers) {
        ParseUser user = parseUsers[0];
        favTrack = service.getTracks(user.get("favTrack").toString()).tracks.get(0);
        favArtist = service.getArtists(user.get("favArtist").toString()).artists.get(0);
        String favAlbumId = user.get("favAlbum").toString();
        List<Album> albums = service.getAlbums(favAlbumId.toString()).albums;
        for (Album a : albums) {
            if (a.id.equals(favAlbumId)) {
                favAlbum = a;
                break;
            }
//
        }
        UserPublic userProfile = service.getUser(user.get("spotifyProfileId").toString());
        Map<String, String> externalUrls = userProfile.external_urls;
        profileUrl = externalUrls.get("spotify");

        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
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
            Glide.with(view.getContext()).load(image.url).into(ivFavTrack);
        }

        if (favArtist.images.size() != 0) {
            Image image = favArtist.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavArtist);
        }

        if (favAlbum.images.size() != 0) {
            Image image = favAlbum.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavAlbum);
        }

        tvName.setText(user.get("name").toString() + ", " + user.get("age").toString());

        if (user.get("bio") != null) {
            tvBio.setText(user.get("bio").toString());
        }

        ParseFile profilePic = (ParseFile) user.get("profilePic");
        if (profilePic != null) {
            Glide.with(view.getContext()).load(profilePic.getUrl()).into(ivProfilePic);
        } else {
            ivProfilePic.setImageResource(R.drawable.nopfp);
        }

        if (favTrack.preview_url == null) {
            ivPlayButton.setVisibility(View.GONE);
        }

        checkEditProfileButtonClicked();
        checkPlayButtonClicked();
        checkSpotifyButtonClicked();

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


    private void checkSpotifyButtonClicked() {
        btnSpotifyProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(profileUrl));
                view.getContext().startActivity(intent);
                Log.i(TAG, profileUrl);
            }
        });
    }

    private void checkPlayButtonClicked() {
        ivPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked: " + favTrack.name.toString());
                selectTrack(favTrack);
            }
        });
    }

    private void checkEditProfileButtonClicked() {
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
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment, "EDITPROFILE").addToBackStack(null).commit();
            }
        });
    }
}
