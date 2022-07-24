package com.example.inharmony.tasks.old;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.R;
import com.parse.ParseUser;

import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class LoadArtistTask extends AsyncTask<ParseUser, Void, Artist> {

    private static final String TAG = "LoadArtistTask";
    SpotifyService service;
    View view;
    String token;
    ImageView ivFavArtistCard;
    TextView tvFavArtistCard;

    public LoadArtistTask(View view, String token, SpotifyService service) {
        this.view = view;
        this.token = token;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        ivFavArtistCard = view.findViewById(R.id.ivFavArtistCard);
        tvFavArtistCard = view.findViewById(R.id.tvFavArtistCard);

    }

    @Override
    protected Artist doInBackground(ParseUser... parseUsers) {
        Artist favArtist = service.getArtists(parseUsers[0].get("favArtist").toString()).artists.get(0);
        return favArtist;
    }

    @Override
    protected void onPostExecute(Artist artist) {

        Log.i(TAG, "on post execute: "+ artist.name);


        if (artist.images.size() != 0) {
            Image image = artist.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavArtistCard);
        }
        tvFavArtistCard.setText(artist.name.toString());
    }
}
