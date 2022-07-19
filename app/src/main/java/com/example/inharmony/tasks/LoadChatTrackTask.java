package com.example.inharmony.tasks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.Message;
import com.example.inharmony.Player;
import com.example.inharmony.PlayerService;
import com.example.inharmony.R;
import com.example.inharmony.fragments.MatchingFragment;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class LoadChatTrackTask extends AsyncTask<Message, Void, Track> {

    private static final String TAG = "LoadChatTrackTask";
    SpotifyService service;
    View view;
    String token;
    Boolean sendType;
    TextView tvIncomingTrack;
    ImageView ivIncomingTrack;
    ImageView ivPlayIncoming;
    TextView tvOutgoingTrack;
    ImageView ivOutgoingTrack;
    ImageView ivPlayOutgoing;


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

    public LoadChatTrackTask(View view, String token, SpotifyService service, Boolean sendType) {
        this.view = view;
        this.token = token;
        this.service = service;
        this.sendType = sendType;
    }


    @Override
    protected void onPreExecute() {
        tvOutgoingTrack = view.findViewById(R.id.tvOutgoingTrack);
        ivOutgoingTrack = view.findViewById(R.id.ivOutgoingTrack);
        ivPlayOutgoing = view.findViewById(R.id.ivPlayOutgoing);
        tvIncomingTrack = view.findViewById(R.id.tvIncomingTrack);
        ivIncomingTrack = view.findViewById(R.id.ivIncomingTrack);
        ivPlayIncoming = view.findViewById(R.id.ivPlayIncoming);
        view.getContext().bindService(PlayerService.getIntent(view.getContext()), mServiceConnection, Activity.BIND_AUTO_CREATE);

    }

    @Override
    protected Track doInBackground(Message... messages) {
        Track track = service.getTracks(messages[0].get("trackId").toString()).tracks.get(0);
        // Log.i("LoadTrackTask", String.valueOf(parseUsers[0].getUsername()));
        return track;
    }

    @Override
    protected void onPostExecute(Track track) {

        // outgoing
        if (sendType) {
            tvOutgoingTrack.setText(track.name + " - " + track.artists.get(0).name.toString());
            if (track.preview_url == null) {
                ivPlayOutgoing.setVisibility(View.GONE);
            }
            if (track.album.images.size() != 0) {
                Image image = track.album.images.get(0);
                Glide.with(view.getContext()).load(image.url).into(ivOutgoingTrack);
            }

            ivPlayOutgoing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Clicked: " + track.name.toString());
                    SpotifyApi spotifyApi = new SpotifyApi();
                    spotifyApi.setAccessToken(token);
                    selectTrack(track);
                }
            });
        }
        // incoming
        else {
            tvIncomingTrack.setText(track.name + " - " + track.artists.get(0).name.toString());
            if (track.preview_url == null) {
                ivPlayIncoming.setVisibility(View.GONE);
            }
            if (track.album.images.size() != 0) {
                Image image = track.album.images.get(0);
                Glide.with(view.getContext()).load(image.url).into(ivIncomingTrack);
            }
            ivPlayIncoming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Clicked: " + track.name.toString());
                    SpotifyApi spotifyApi = new SpotifyApi();
                    spotifyApi.setAccessToken(token);
                    selectTrack(track);
                }
            });
        }


    }

    public void selectTrack(Track track) {

        String previewUrl = track.preview_url;
        Log.i(TAG, "previewUrl:" + track.preview_url);

        if (mPlayer == null) {
            Log.i(TAG, "mPlayer is Null");
            return;
        }

        String currentTrackUrl = mPlayer.getCurrentTrack();

        if (currentTrackUrl == null || !currentTrackUrl.equals(previewUrl)) {
            Log.i(TAG, "Play");
            mPlayer.play(previewUrl);
            if (sendType) {
                ivPlayOutgoing.setImageResource(R.drawable.pause);
            } else {
                ivPlayIncoming.setImageResource(R.drawable.pause);
            }
        }

        else if (mPlayer.isPlaying()) {
            Log.i(TAG, "Pause");
            mPlayer.pause();
            if (sendType) {
                ivPlayOutgoing.setImageResource(R.drawable.play);
            } else {
                ivPlayIncoming.setImageResource(R.drawable.play);
            }
        } else {
            Log.i(TAG, "Resume");if (sendType) {
                ivPlayOutgoing.setImageResource(R.drawable.pause);
            } else {
                ivPlayIncoming.setImageResource(R.drawable.pause);
            }
            mPlayer.resume();
        }
    }
}

