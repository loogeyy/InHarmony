package com.example.inharmony.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.R;

import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.SpotifyService;

public class LoadBasicTask extends AsyncTask<ParseUser, Void, Void> {
    private static final String TAG = "LoadBasicTask";
    SpotifyService service;
    View view;
    String token;
    ParseFile profilePic;

    String favGenres;
    String basic;
    TextView tvNameCard;
    TextView tvFavGenresCard;
    ImageView ivProfilePicCard;

    public LoadBasicTask(View view, String token, SpotifyService service) {
        this.view = view;
        this.token = token;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        tvNameCard = view.findViewById(R.id.tvNameCard);
        tvFavGenresCard = view.findViewById(R.id.tvFavGenresCard);

    }

    @Override
    protected Void doInBackground(ParseUser... parseUsers) {
        ParseUser user = parseUsers[0];

        ArrayList<String> genres = (ArrayList<String>) user.get("favGenres");
        favGenres = "";
        for (int i = 0; i < genres.size(); i++) {
            if (i == 0) {
                favGenres = genres.get(i);

            } else {
                favGenres = favGenres + ", " + genres.get(i);
            }

        }

        basic = user.get("name")+ ", " + user.get("age");
        profilePic = (ParseFile) user.get("profilePic");
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        tvNameCard.setText(basic);
        tvFavGenresCard.setText(favGenres);
        if (profilePic != null) {
            //Glide.with(view.getContext()).load(profilePic.getUrl()).into(ivProfilePicCard);
        }
    }
}
