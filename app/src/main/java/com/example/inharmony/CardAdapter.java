package com.example.inharmony;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.tasks.LoadAlbumTask;
import com.example.inharmony.tasks.LoadArtistTask;
import com.example.inharmony.tasks.LoadBasicTask;
import com.example.inharmony.tasks.LoadProfileTask;
import com.example.inharmony.tasks.LoadTrackTask;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class CardAdapter extends ArrayAdapter<Card> {

    private static final String TAG = "CardAdapter";
    Context context;
    String token;
    private ParseUser user;


    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
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


    public CardAdapter(Context context, int resourceId, List<Card> cards, String token, boolean newSignUp) {
        super(context, resourceId, cards);
        this.token = token;
        this.newSignUp = newSignUp;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        Log.i(TAG, "position: " + String.valueOf(position));

        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        getContext().bindService(PlayerService.getIntent(getContext()), mServiceConnection, Activity.BIND_AUTO_CREATE);

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();
        Card card_item = getItem(position);
        user = card_item.getUser();

        new LoadProfileTask(view, token, service).execute(user);

        return view;

    }

    //    @Override
//    public void onDestroyView() {
//        mPlayer.release();
//        super.onDestroyView();
//
//    }

}
