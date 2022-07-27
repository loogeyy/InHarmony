package com.example.inharmony;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.inharmony.tasks.LoadMatchCardTask;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class CardAdapter extends ArrayAdapter<Card> {

    private static final String TAG = "CardAdapter";
    String token;
    private ParseUser user;

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";

    public CardAdapter(Context context, int resourceId, List<Card> cards, String token) {
        super(context, resourceId, cards);
        this.token = token;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){

        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();
        Card card_item = getItem(position);
        user = card_item.getUser();

        LoadMatchCardTask loadProfileTask = new LoadMatchCardTask(view, token, service, user);
        loadProfileTask.execute();

        return view;

    }
}
