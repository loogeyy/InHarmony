package com.example.inharmony.tasks;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.UserPublic;

public class LoadMatchCardTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "LoadProfileTask";

    View view;
    String token;
    SpotifyService service;
    ParseUser user;

    String bio;
    String basic;
    String profileUrl;
    Album favAlbum;
    Track favTrack;
    Artist favArtist;
    String favGenres;
    ParseFile profilePic;

    TextView tvBioCard;
    TextView tvNameCard;
    TextView tvFavTrack;
    ImageView ivFavTrack;
    ImageView btnSpotify;
    ImageView ivPlayButton;
    TextView tvFavAlbumCard;
    ImageView ivFavAlbumCard;
    TextView tvFavGenresCard;
    TextView tvFavArtistCard;
    ImageView ivFavArtistCard;
    ImageView ivProfilePicCard;

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

    public LoadMatchCardTask(View view, String token, SpotifyService service, ParseUser user) {
        this.view = view;
        this.token = token;
        this.service = service;
        this.user = user;
    }


    @Override
    protected void onPreExecute() {
        tvBioCard = view.findViewById(R.id.tvBioCard);
        tvNameCard = view.findViewById(R.id.tvNameCard);
        tvFavTrack = view.findViewById(R.id.tvFavTrackCard);
        ivFavTrack = view.findViewById(R.id.ivFavTrackCard);
        btnSpotify = view.findViewById(R.id.btnSpotify);
        ivFavAlbumCard = view.findViewById(R.id.ivFavAlbumCard);
        tvFavAlbumCard = view.findViewById(R.id.tvFavAlbumCard);
        ivPlayButton = view.findViewById(R.id.ivPlayButtonCard);
        tvFavGenresCard = view.findViewById(R.id.tvFavGenresCard);
        ivFavArtistCard = view.findViewById(R.id.ivFavArtistCard);
        tvFavArtistCard = view.findViewById(R.id.tvFavArtistCard);
        ivProfilePicCard = view.findViewById(R.id.ivProfilePicCard);

        view.getContext().bindService(PlayerService.getIntent(view.getContext()), mServiceConnection, Activity.BIND_AUTO_CREATE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // basic info
        try {
            basic = user.fetch().get("name") + ", " + user.fetch().get("age");

            profilePic = (ParseFile) user.fetch().get("profilePic");
            if (user.fetch().get("bio") != null) {
                bio = user.fetch().get("bio").toString();
            }
            UserPublic userProfile = service.getUser(user.fetch().get("spotifyProfileId").toString());
            Map<String, String> externalUrls = userProfile.external_urls;
            profileUrl = externalUrls.get("spotify");

            // favorite genres
            ArrayList<String> genres = (ArrayList<String>) user.fetch().get("favGenres");
            favGenres = "";
            for (int i = 0; i < genres.size(); i++) {
                if (i == 0) {
                    favGenres = genres.get(i);

                } else {
                    favGenres = favGenres + ", " + genres.get(i);
                }

            }

            // favorite track
            favTrack = service.getTracks(user.fetch().get("favTrack").toString()).tracks.get(0);

            // favorite artist
            favArtist = service.getArtists(user.fetch().get("favArtist").toString()).artists.get(0);

            // favorite album
            List<Album> albums = service.getAlbums(user.fetch().get("favAlbum").toString()).albums;
            favAlbum = albums.get(0);
            for (Album a : albums) {
                if (a.images.size() != 0) {
                    if (a.images.get(0).url.equals(user.get("favAlbumImageUrl"))) {
                        favAlbum = a;
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


//    @Override
//    protected Void doInBackground(ParseUser... parseUsers) {
//        ParseUser user = parseUsers[0];
//
//        // basic info
//        try {
//            basic = user.fetch().get("name") + ", " + user.fetch().get("age");
//
//        profilePic = (ParseFile) user.fetch().get("profilePic");
//        if (user.get("bio") != null) {
//            bio = user.fetch().get("bio").toString();
//        }
//        UserPublic userProfile = service.getUser(user.fetch().get("spotifyProfileId").toString());
//        Map<String, String> externalUrls = userProfile.external_urls;
//        profileUrl = externalUrls.get("spotify");
//
//        // favorite genres
//        ArrayList<String> genres = (ArrayList<String>) user.fetch().get("favGenres");
//        favGenres = "";
//        for (int i = 0; i < genres.size(); i++) {
//            if (i == 0) {
//                favGenres = genres.get(i);
//
//            } else {
//                favGenres = favGenres + ", " + genres.get(i);
//            }
//
//        }
//
//        // favorite track
//        favTrack = service.getTracks(parseUsers[0].fetch().get("favTrack").toString()).tracks.get(0);
//
//        // favorite artist
//        favArtist = service.getArtists(parseUsers[0].fetch().get("favArtist").toString()).artists.get(0);
//
//        // favorite album
//        List<Album> albums = service.getAlbums(user.fetch().get("favAlbum").toString()).albums;
//        favAlbum = albums.get(0);
//        for (Album a : albums) {
//            if (a.images.size() != 0) {
//                if (a.images.get(0).url.equals(user.get("favAlbumImageUrl"))) {
//                    favAlbum = a;
//                }
//            }
//        }
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Override
    protected void onPostExecute(Void unused) {
        // basic info
        tvNameCard.setText(basic);
        if (profilePic != null) {
            Glide.with(view.getContext()).load(profilePic.getUrl()).into(ivProfilePicCard);
        }
        if (bio != null) {
            tvBioCard.setText(bio);
        }

        // favorite genres
        tvFavGenresCard.setText(favGenres);

        // favorite track
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

        // favorite album
        tvFavAlbumCard.setText(favAlbum.name.toString());
        if (favAlbum.images.size() != 0) {
            Image image = favAlbum.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavAlbumCard);
        }

        // favorite artist
        if (favArtist.images.size() != 0) {
            Image image = favArtist.images.get(0);
            Glide.with(view.getContext()).load(image.url).into(ivFavArtistCard);
        }
        tvFavArtistCard.setText(favArtist.name.toString());

        checkSpotifyButtonClicked();
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

    private void checkSpotifyButtonClicked() {
        btnSpotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(profileUrl));
                view.getContext().startActivity(intent);
            }
        });
    }

}
