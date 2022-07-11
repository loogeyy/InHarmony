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
    private ImageView btnEditProfile;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvAge;
    private TextView tvBio;

    private TextView tvFavAlbum;
    private TextView tvFavTrack;
    private TextView tvFavArtist;
    private ImageView ivFavAlbum;
    private TextView tvFavGenres;
    private ImageView ivFavTrack;
    private ImageView ivFavArtist;
    private ImageView ivPlayButton;

    private boolean myProfile;
    private ParseUser user;

    private Track favTrack;
    private Artist favArtist;
    private AlbumSimple favAlbum;

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
        //this.fragment = fragment;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        Log.i(TAG, "position: " + String.valueOf(position));

//        if (view == null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
//        }

        ivProfilePic = view.findViewById(R.id.ivProfilePicCard);
        tvName = view.findViewById(R.id.tvNameCard);
        tvAge = view.findViewById(R.id.tvAgeCard);
        tvFavGenres = view.findViewById(R.id.tvFavGenresCard);
        tvFavAlbum = view.findViewById(R.id.tvFavAlbumCard);
        tvFavArtist = view.findViewById(R.id.tvFavArtistCard);
        tvFavTrack = view.findViewById(R.id.tvFavTrackCard);
        ivFavArtist = view.findViewById(R.id.ivFavArtistCard);
        ivFavAlbum = view.findViewById(R.id.ivFavAlbumCard);
        ivFavTrack = view.findViewById(R.id.ivFavTrackCard);
        ivPlayButton = view.findViewById(R.id.ivPlayButtonCard);

        getContext().bindService(PlayerService.getIntent(getContext()), mServiceConnection, Activity.BIND_AUTO_CREATE);

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        SpotifyService service = spotifyApi.getService();
        Card card_item = getItem(position);
        user = card_item.getUser();

        new LoadTrackTask(view, token).execute(user);

//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//
//
//                    Log.i(TAG, "username: " + user.getUsername());
//                    favTrack = service.getTracks(user.get("favTrack").toString()).tracks.get(0);
//                    favArtist = service.getArtists(user.get("favArtist").toString()).artists.get(0);
//                    List<Album> albums = service.getAlbums(user.get("favAlbum").toString()).albums;
//                    for (Album a : albums) {
//                        if (a.images.size() != 0) {
//                            if (a.images.get(0).url.equals(user.get("favAlbumImageUrl"))) {
//                                favAlbum = a;
//                                break;
//                            }
//                        }
//                    }
//                    Log.i(TAG, "ASYNC:");
//                    Log.i(TAG, user.getUsername()+"'s favorite song: " + favTrack.name);
//                    Log.i(TAG, user.getUsername()+"'s favorite artist: " + favArtist.name);
//                    Log.i(TAG, user.getUsername()+"'s favorite album: " + favAlbum.name);
//
//                    ((MainActivity)getContext()).runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.i(TAG, "UI THREAD:");
//                            Log.i(TAG, user.getUsername()+"'s favorite song: " + favTrack.name);
//                            Log.i(TAG, user.getUsername()+"'s favorite artist: " + favArtist.name);
//                            Log.i(TAG, user.getUsername()+"'s favorite album: " + favAlbum.name);
//
//                            populateProfile(favTrack, favArtist, favAlbum);
//                            ivPlayButton.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    Log.i("CardAdapter", "Clicked: " + favTrack.name.toString());
//                                    SpotifyApi spotifyApi = new SpotifyApi();
//                                    spotifyApi.setAccessToken(token);
//                                    selectTrack(favTrack);
//                                }
//                            });
//
//                        }
//                    });
//            }
//        });

        return view;

    }

    //    @Override
//    public void onDestroyView() {
//        mPlayer.release();
//        super.onDestroyView();
//
//    }


    private void populateProfile(Track favTrack, Artist favArtist, AlbumSimple favAlbum) {
        ArrayList<String> genres = (ArrayList<String>) user.get("favGenres");
        String favGenres = "";
        for (int i = 0; i < genres.size(); i++) {
            if (i == 0) {
                favGenres = genres.get(i);

            } else {
                favGenres = favGenres + ", " + genres.get(i);
            }
            //Log.i(TAG, "favGenres: " + favGenres);

        }
        Log.i("favGenres text", favGenres);
        tvFavGenres.setText(favGenres);
        tvFavTrack.setText(favTrack.name.toString() + " - " + favTrack.artists.get(0).name.toString());
        tvFavArtist.setText(favArtist.name.toString());
        tvFavAlbum.setText(favAlbum.name.toString());
        if (favTrack.album.images.size() != 0) {
            Image image = favTrack.album.images.get(0);
            Glide.with(getContext()).load(image.url).into(ivFavTrack);
        }

        if (favArtist.images.size() != 0) {
            Image image = favArtist.images.get(0);
            Glide.with(getContext()).load(image.url).into(ivFavArtist);
        }

        if (favAlbum.images.size() != 0) {
            Image image = favAlbum.images.get(0);
            Glide.with(getContext()).load(image.url).into(ivFavAlbum);
        }

        tvAge.setText(user.get("age").toString());
        tvName.setText(user.get("name").toString());

        ParseFile profilePic = (ParseFile) user.get("profilePic");
        if (profilePic != null) {
            Glide.with(getContext()).load(profilePic.getUrl()).into(ivProfilePic);
        } else {
            ivProfilePic.setImageResource(R.drawable.nopfp);
        }

        if (favTrack.preview_url == null) {
            ivPlayButton.setVisibility(View.GONE);
        }
        Log.i(TAG, "successfully set");
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
