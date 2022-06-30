package com.example.inharmony;

import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Albums;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class Search {

    public interface View {
        void reset();

        void addDataTracks(List<Track> items);

        void addDataArtists(List<Artist> items);

        void addDataAlbums(List<AlbumSimple> items);


    }

    public interface ActionListener {

        void init(String token);

        String getCurrentQuery();

        void searchTracks(String searchQuery);

        void searchArtists(String searchQuery);

        void searchAlbums(String searchQuery);

        void loadMoreResults(String searchType);

        void selectTrack(Track item);

        void selectArtist(Artist artist);

        void selectAlbum(AlbumSimple album);

        void resume();

        void pause();

        void destroy();

    }
}
