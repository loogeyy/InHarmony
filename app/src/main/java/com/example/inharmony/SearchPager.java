package com.example.inharmony;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.AlbumsPager;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TracksPager;
import retrofit.client.Response;

public class SearchPager {

    private final SpotifyService mSpotifyApi;
    private int mCurrentOffset;
    private int mPageSize;
    private String mCurrentQuery;

    public interface CompleteListener {
        void onCompleteTracks(List<Track> tracks);
        void onCompleteArtists(List<Artist> artists);
        void onCompleteAlbums(List<AlbumSimple> albums);
        void onError(Throwable error);
    }

    public SearchPager(SpotifyService spotifyApi) {
        mSpotifyApi = spotifyApi;
    }

    public void getFirstPage(String searchType, String query, int pageSize, CompleteListener listener) {
        mCurrentOffset = 0;
        mPageSize = pageSize;
        mCurrentQuery = query;
        if (searchType.equals("TRACK")) {
            getDataTracks(query, 0, pageSize, listener);
        }
        if (searchType.equals("ARTIST")) {
            getDataArtists(query, 0, pageSize, listener);
        }
        if (searchType.equals("ALBUM")) {
            getDataAlbums(query, 0, pageSize, listener);
        }
    }

    public void getNextPage(String searchType, CompleteListener listener) {
        mCurrentOffset += mPageSize;
        if (searchType.equals("TRACK")) {
            Log.i("GET NEXT PAGE", "EQUALS TRACK");
            getDataTracks(mCurrentQuery, mCurrentOffset, mPageSize, listener);
        }
        if (searchType.equals("ARTIST")) {
            Log.i("GET NEXT PAGE", "EQUALS ARTIST");
            getDataArtists(mCurrentQuery, mCurrentOffset, mPageSize, listener);
        }
        if (searchType.equals("ALBUM")) {
            Log.i("GET NEXT PAGE", "EQUALS ALBUM");
            getDataAlbums(mCurrentQuery, mCurrentOffset, mPageSize, listener);
        }
    }

    private void getDataTracks(String query, int offset, final int limit, final CompleteListener listener) {

        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, limit);

        mSpotifyApi.searchTracks(query, options, new SpotifyCallback<TracksPager>() {
            @Override
            public void success(TracksPager tracksPager, Response response) {
                listener.onCompleteTracks(tracksPager.tracks.items);
            }

            @Override
            public void failure(SpotifyError error) {
                listener.onError(error);
            }
        });
    }

    private void getDataArtists(String query, int offset, final int limit, final CompleteListener listener) {

        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, limit);

        mSpotifyApi.searchArtists(query, options, new SpotifyCallback<ArtistsPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                listener.onError(spotifyError);
            }

            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                listener.onCompleteArtists(artistsPager.artists.items);
            }
        });
    }

    private void getDataAlbums(String query, int offset, final int limit, final CompleteListener listener) {

        Map<String, Object> options = new HashMap<>();
        options.put(SpotifyService.OFFSET, offset);
        options.put(SpotifyService.LIMIT, limit);

        mSpotifyApi.searchAlbums(query, options, new SpotifyCallback<AlbumsPager>() {
            @Override
            public void failure(SpotifyError spotifyError) {
                listener.onError(spotifyError);
            }

            @Override
            public void success(AlbumsPager albumsPager, Response response) {
                listener.onCompleteAlbums(albumsPager.albums.items);
            }
        });
    }
}
