package com.example.inharmony.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.inharmony.MainActivity;
import com.example.inharmony.R;
import com.example.inharmony.ResultListScrollListener;
import com.example.inharmony.Search;
import com.example.inharmony.SearchPresenter;
import com.example.inharmony.SearchResultsAdapter;

import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;


public class SearchFragment extends Fragment implements Search.View {

    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    private static final String KEY_CURRENT_QUERY = "CURRENT_QUERY";
    public static String SEARCH_TYPE = "SEARCH_TYPE";
    private String searchType;
    private String token;

    private RecyclerView resultsList;
    private SearchView searchView;

    private Search.ActionListener mActionListener;

    private LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
    private SearchFragment.ScrollListener mScrollListener = new SearchFragment.ScrollListener(mLayoutManager);
    private SearchResultsAdapter mAdapter;

    private class ScrollListener extends ResultListScrollListener {

        public ScrollListener(LinearLayoutManager layoutManager) {
            super(layoutManager);
        }

        @Override
        public void onLoadMore() {
            mActionListener.loadMoreResults(searchType);
        }
    }


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getContext(), "SearchFragment", Toast.LENGTH_SHORT).show();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(SearchFragment.EXTRA_TOKEN);
            Toast.makeText(getContext(), "token found: " + token, Toast.LENGTH_SHORT).show();
            searchType = bundle.getString(SearchFragment.SEARCH_TYPE);
            Log.i("SEARCH FRAGMENT SEARCHTYPE", searchType);
        }

        mActionListener = new SearchPresenter(getContext(), this);
        mActionListener.init(token);

        // Setup search field
        searchView = (SearchView) view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("SEARCH TYPE IS", SEARCH_TYPE);
                if (searchType.equals("TRACK")) {
                    mActionListener.searchTracks(query);
                    searchView.clearFocus();
                }
                if (searchType.equals("ARTIST")) {
                    mActionListener.searchArtists(query);
                    searchView.clearFocus();
                }
                if (searchType.equals("ALBUM")) {
                    mActionListener.searchAlbums(query);
                    searchView.clearFocus();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        // Setup search results list
        mAdapter = new SearchResultsAdapter(searchType, getContext(), new SearchResultsAdapter.ItemSelectedListener()  {
            @Override
            public void onItemSelectedTrack(View itemView, Track track) {
                //mActionListener.selectTrack(track); // this makes it play, use this code for later

                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
                bundle.putParcelable("favTrack", track);
                //bundle.putString(EditProfileFragment.SEARCH_TYPE, "TRACK");
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

            }

            @Override
            public void onItemSelectedArtist(View itemView, Artist artist) {
                mActionListener.selectArtist(artist); // in searchpresenter, doesn't need to do anything

                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);
                bundle.putParcelable("favArtist", artist);
                //bundle.putString(EditProfileFragment.SEARCH_TYPE, "TRACK");
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

            }

            @Override
            public void onItemSelectedAlbum(View itemView, Album album) {
                mActionListener.selectAlbum(album);


                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(EditProfileFragment.EXTRA_TOKEN, token);

                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();

            }


        });

        resultsList = view.findViewById(R.id.search_results);
        resultsList.setHasFixedSize(true);
        resultsList.setLayoutManager(mLayoutManager);
        resultsList.setAdapter(mAdapter);
        resultsList.addOnScrollListener(mScrollListener);

        // If Activity was recreated wit active search restore it
        if (savedInstanceState != null) {
            String currentQuery = savedInstanceState.getString(KEY_CURRENT_QUERY);
            if (searchType.equals("TRACK")) {
                mActionListener.searchTracks(currentQuery);
            }
            if (searchType.equals("ARTIST")) {
                mActionListener.searchArtists(currentQuery);
            }
            if (searchType.equals("ALBUM")) {
                mActionListener.searchAlbums(currentQuery);
            }
            //mActionListener.searchTracks(currentQuery);
        }

    }

    @Override
    public void reset() {
        mScrollListener.reset();
        mAdapter.clearData();
    }

    @Override
    public void addDataTracks(List<Track> tracks) {
        mAdapter.addDataTracks(tracks);
    }

    @Override
    public void addDataArtists(List<Artist> artists) {
        mAdapter.addDataArtists(artists);
    }

    @Override
    public void addDataAlbums(List<Album> albums) {
        mAdapter.addDataAlbums(albums);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActionListener.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionListener.resume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActionListener.getCurrentQuery() != null) {
            outState.putString(KEY_CURRENT_QUERY, mActionListener.getCurrentQuery());
        }
    }

    @Override
    public void onDestroy() {
        mActionListener.destroy();
        super.onDestroy();
    }

}