package com.example.inharmony.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
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
import com.example.inharmony.tasks.LoadMatchCardTask;
import com.example.inharmony.tasks.LoadProfileTask;
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

public class ProfileFragment extends Fragment {
    private static final String TAG = "MyProfileFragment";

    private ParseUser user;
    private SpotifyService service;


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

    public ProfileFragment(ParseUser user) {
        this.user = user;
    }

    @Override
    public void onDestroyView() {
        if (mPlayer != null) {
            mPlayer.release();
        }
        super.onDestroyView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
        } else {
            Log.i(TAG, "BUNDLE WAS NULL");
        }

        initializeSpotifyService();
        LoadProfileTask loadProfileTask = new LoadProfileTask(view, token, service, user, getActivity().getSupportFragmentManager());
        loadProfileTask.execute(user);

    }

    private void initializeSpotifyService() {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        service = spotifyApi.getService();
    }

}