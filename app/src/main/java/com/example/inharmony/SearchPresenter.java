package com.example.inharmony;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import java.util.List;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class SearchPresenter implements Search.ActionListener {

    private static final String TAG = SearchPresenter.class.getSimpleName();
    public static final int PAGE_SIZE = 20;

    private final Context mContext;
    private final Search.View mView;
    private String mCurrentQuery;

    private SearchPager mSearchPager;
    private SearchPager.CompleteListener mSearchListener;

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

    public SearchPresenter(Context context, Search.View view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void init(String accessToken) {
        SpotifyApi spotifyApi = new SpotifyApi();

        if (accessToken != null) {
            spotifyApi.setAccessToken(accessToken);
        } else {
            logError("No valid access token");
        }

        mSearchPager = new SearchPager(spotifyApi.getService());

        mContext.bindService(PlayerService.getIntent(mContext), mServiceConnection, Activity.BIND_AUTO_CREATE);
    }



    @Override
    public void searchTracks(@Nullable String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty() && !searchQuery.equals(mCurrentQuery)) {
            //("query text submit " + searchQuery);
            mCurrentQuery = searchQuery;
            mView.reset();
            mSearchListener = new SearchPager.CompleteListener() {
                @Override
                public void onCompleteTracks(List<Track> items) {
                    mView.addDataTracks(items);
                }

                @Override
                public void onCompleteArtists(List<Artist> artists) {}

                @Override
                public void onCompleteAlbums(List<AlbumSimple> albums) {}

                @Override
                public void onError(Throwable error) {
                    logError(error.getMessage());
                }
            };
            mSearchPager.getFirstPage("TRACK", searchQuery, PAGE_SIZE, mSearchListener);
        }
    }

    @Override
    public void searchArtists(@Nullable String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty() && !searchQuery.equals(mCurrentQuery)) {
            //logMessage("query text submit " + searchQuery);
            mCurrentQuery = searchQuery;
            mView.reset();
            mSearchListener = new SearchPager.CompleteListener() {
                @Override
                public void onCompleteTracks(List<Track> tracks) {
                    mView.addDataTracks(tracks);
                }

                @Override
                public void onCompleteArtists(List<Artist> artists) {
                    mView.addDataArtists(artists);
                }

                @Override
                public void onCompleteAlbums(List<AlbumSimple> albums) {}

                @Override
                public void onError(Throwable error) {
                    logError(error.getMessage());
                }
            };
            mSearchPager.getFirstPage("ARTIST", searchQuery, PAGE_SIZE, mSearchListener);
        }
    }

    @Override
    public void searchAlbums(@Nullable String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty() && !searchQuery.equals(mCurrentQuery)) {
            //logMessage("query text submit " + searchQuery);
            mCurrentQuery = searchQuery;
            mView.reset();
            mSearchListener = new SearchPager.CompleteListener() {

                @Override
                public void onCompleteTracks(List<Track> tracks) {}

                @Override
                public void onCompleteArtists(List<Artist> artists) {
                }

                @Override
                public void onCompleteAlbums(List<AlbumSimple> albums) {
                    mView.addDataAlbums(albums);
                }

                @Override
                public void onError(Throwable error) {
                    logError(error.getMessage());
                }
            };
            mSearchPager.getFirstPage("ALBUM", searchQuery, PAGE_SIZE, mSearchListener);
        }
    }


    @Override
    public void destroy() {
        mContext.unbindService(mServiceConnection);
    }

    @Override
    @Nullable
    public String getCurrentQuery() {
        return mCurrentQuery;
    }

    @Override
    public void resume() {
        mContext.stopService(PlayerService.getIntent(mContext));
    }

    @Override
    public void pause() {
        mContext.startService(PlayerService.getIntent(mContext));
    }

    @Override
    public void loadMoreResults(String searchType) {
        if (searchType.equals("TRACK")) {
            mSearchPager.getNextPage("TRACK", mSearchListener);
        }
        if (searchType.equals("ARTIST")) {
            mSearchPager.getNextPage("ARTIST", mSearchListener);
        }
        if (searchType.equals("ALBUM")) {
            mSearchPager.getNextPage("ALBUM", mSearchListener);
        }

    }

    @Override
    public void selectTrack(Track item) {

        String previewUrl = item.preview_url;

        if (previewUrl == null) {
            logMessage("Track doesn't have a preview");
            return;
        }

        if (mPlayer == null) return;

        String currentTrackUrl = mPlayer.getCurrentTrack();

        if (currentTrackUrl == null || !currentTrackUrl.equals(previewUrl)) {
            mPlayer.play(previewUrl);
        } else if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            mPlayer.resume();
        }
    }



    private void logError(String msg) {
        Toast.makeText(mContext, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg);
    }

    private void logMessage(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        Log.d(TAG, msg);
    }
}
