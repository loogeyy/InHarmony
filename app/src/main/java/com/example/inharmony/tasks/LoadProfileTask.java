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
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class LoadProfileTask extends AsyncTask<ParseUser, Void, Void> {
    private static final String TAG = "LoadProfileTask";
    SpotifyService service;
    View view;
    String token;
    ParseFile profilePic;
    String favGenres;
    Album favAlbum;
    Track favTrack;
    Artist favArtist;
    String basic;
    TextView tvNameCard;
    TextView tvFavGenresCard;
    ImageView ivProfilePicCard;
    ImageView ivFavAlbumCard;
    TextView tvFavAlbumCard;
    ImageView ivPlayButton;
    ImageView ivFavTrack;
    TextView tvFavTrack;
    ImageView ivFavArtistCard;
    TextView tvFavArtistCard;

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

    public LoadProfileTask(View view, String token, SpotifyService service) {
        this.view = view;
        this.token = token;
        this.service = service;
    }

    @Override
    protected void onPreExecute() {
        tvNameCard = view.findViewById(R.id.tvNameCard);
        tvFavGenresCard = view.findViewById(R.id.tvFavGenresCard);
        ivFavAlbumCard = view.findViewById(R.id.ivFavAlbumCard);
        tvFavAlbumCard = view.findViewById(R.id.tvFavAlbumCard);
        tvFavTrack = view.findViewById(R.id.tvFavTrackCard);
        ivPlayButton = view.findViewById(R.id.ivPlayButtonCard);
        ivFavTrack = view.findViewById(R.id.ivFavTrackCard);
        ivFavArtistCard = view.findViewById(R.id.ivFavArtistCard);
        tvFavArtistCard = view.findViewById(R.id.tvFavArtistCard);
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


        List<Album> albums = service.getAlbums(user.get("favAlbum").toString()).albums;
        for (Album a : albums) {
            if (a.images.size() != 0) {
                if (a.images.get(0).url.equals(user.get("favAlbumImageUrl"))) {
                    favAlbum = a;
                }
            }
        }
        favTrack = service.getTracks(parseUsers[0].get("favTrack").toString()).tracks.get(0);
        favArtist = service.getArtists(parseUsers[0].get("favArtist").toString()).artists.get(0);

        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        tvNameCard.setText(basic);
        tvFavGenresCard.setText(favGenres);
        if (profilePic != null) {
            //Glide.with(view.getContext()).load(profilePic.getUrl()).into(ivProfilePicCard);
        }
        tvFavAlbumCard.setText(favAlbum.name.toString());
        if (favAlbum.images.size() != 0) {
            Image image = favAlbum.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavAlbumCard);
        }

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
                Log.i("CardAdapter", "Clicked: " + favTrack.name.toString());
                SpotifyApi spotifyApi = new SpotifyApi();
                spotifyApi.setAccessToken(token);
                selectTrack(favTrack);
            }
        });
        if (favArtist.images.size() != 0) {
            Image image = favArtist.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavArtistCard);
        }
        tvFavArtistCard.setText(favArtist.name.toString());
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