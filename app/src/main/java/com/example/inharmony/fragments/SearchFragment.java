package com.example.inharmony.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import kaaes.spotify.webapi.android.models.Track;


public class SearchFragment extends Fragment implements Search.View {

    public static final String EXTRA_TOKEN = "EXTRA_TOKEN";
    private static final String KEY_CURRENT_QUERY = "CURRENT_QUERY";

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
            mActionListener.loadMoreResults();
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

        String token = "";
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(SearchFragment.EXTRA_TOKEN);
            Toast.makeText(getContext(), "token found: " + token, Toast.LENGTH_SHORT).show();
        }

        mActionListener = new SearchPresenter(getContext(), this);
        mActionListener.init(token);

        // Setup search field
        searchView = (SearchView) view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mActionListener.search(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        // Setup search results list
        mAdapter = new SearchResultsAdapter(getContext(), new SearchResultsAdapter.ItemSelectedListener() {
            @Override
            public void onItemSelected(View itemView, Track item) {
                mActionListener.selectTrack(item);
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
            mActionListener.search(currentQuery);
        }

    }

    @Override
    public void reset() {
        mScrollListener.reset();
        mAdapter.clearData();
    }

    @Override
    public void addData(List<Track> items) {
        mAdapter.addData(items);
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