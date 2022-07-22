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

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class LoadAlbumTask extends AsyncTask<ParseUser, Void, Album> {
    private static final String TAG = "LoadTrackTask";
    SpotifyService service;
    View view;
    String token;
    ImageView ivFavAlbumCard;
    TextView tvFavAlbumCard;

    public LoadAlbumTask(View view, String token, SpotifyService service) {
        this.view = view;
        this.token = token;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        ivFavAlbumCard = view.findViewById(R.id.ivFavAlbumCard);
        tvFavAlbumCard = view.findViewById(R.id.tvFavAlbumCard);

    }

    @Override
    protected Album doInBackground(ParseUser... parseUsers) {
        Album album = new Album();
        ParseUser user = parseUsers[0];
        List<Album> albums = service.getAlbums(user.get("favAlbum").toString()).albums;
                    for (Album a : albums) {
                        if (a.images.size() != 0) {
                            if (a.images.get(0).url.equals(user.get("favAlbumImageUrl"))) {
                                album = a;
                                Log.i(TAG, album.name);
                                Log.i(TAG, album.images.get(0).url);
                            }
                        }
                    }
        return album;
    }

    @Override
    protected void onPostExecute(Album favAlbum) {
        tvFavAlbumCard.setText(favAlbum.name.toString());
        if (favAlbum.images.size() != 0) {
            Image image = favAlbum.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavAlbumCard);
        }
    }

}

