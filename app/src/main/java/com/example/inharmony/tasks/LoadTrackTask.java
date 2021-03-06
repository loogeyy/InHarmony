package com.example.inharmony.tasks;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.Player;
import com.example.inharmony.PlayerService;
import com.example.inharmony.R;
import com.parse.ParseUser;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class LoadTrackTask extends AsyncTask<ParseUser, Void, Track> {

    private static final String TAG = "LoadTrackTask";
    SpotifyService service;
    View view;
    String token;
    ImageView ivPlayButton;
    ImageView ivFavTrack;
    TextView tvFavTrack;



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

    public LoadTrackTask(View view, String token, SpotifyService service) {
        this.view = view;
        this.token = token;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        tvFavTrack = view.findViewById(R.id.tvFavTrackCard);
        ivPlayButton = view.findViewById(R.id.ivPlayButtonCard);
        ivFavTrack = view.findViewById(R.id.ivFavTrackCard);

    }

    @Override
    protected Track doInBackground(ParseUser... parseUsers) {
        Track favTrack = service.getTracks(parseUsers[0].get("favTrack").toString()).tracks.get(0);
        return favTrack;
    }

    @Override
    protected void onPostExecute(Track favTrack) {

        tvFavTrack.setText(favTrack.name.toString() + " - " + favTrack.artists.get(0).name.toString());

        if (favTrack.preview_url == null) {
            ivPlayButton.setVisibility(View.GONE);
        }

        if (favTrack.album.images.size() != 0) {
            Image image = favTrack.album.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavTrack);
        }

        ivPlayButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SpotifyApi spotifyApi = new SpotifyApi();
            spotifyApi.setAccessToken(token);
            selectTrack(favTrack);
        }
    });
    }

    public void selectTrack(Track track) {

        String previewUrl = track.preview_url;

        if (mPlayer == null) {
            return;
        }

        String currentTrackUrl = mPlayer.getCurrentTrack();

        if (currentTrackUrl == null || !currentTrackUrl.equals(previewUrl)) {
            mPlayer.play(previewUrl);
            ivPlayButton.setImageResource(R.drawable.pause);
        }

        else if (mPlayer.isPlaying()) {
            mPlayer.pause();
            ivPlayButton.setImageResource(R.drawable.play);
        } else {
            ivPlayButton.setImageResource(R.drawable.pause);
            mPlayer.resume();
        }
    }
}

