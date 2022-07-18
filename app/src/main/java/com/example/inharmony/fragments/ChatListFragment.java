package com.example.inharmony.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.inharmony.CardAdapter;
import com.example.inharmony.ChatListAdapter;
import com.example.inharmony.Match;
import com.example.inharmony.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyService;

public class ChatListFragment extends Fragment {
    private static final String TAG = "ChatListFragment";
    private ChatListAdapter chatListAdapter;

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private static String token;
    private boolean newSignUp;
    List<ParseUser> users;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chatlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvChats = view.findViewById(R.id.rvChats);
        TextView tvNoChats = view.findViewById(R.id.tvNoChats);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
        }
        users = new ArrayList<>();
        ChatListAdapter chatListAdapter = new ChatListAdapter(getContext(), users, token);
        rvChats.setAdapter(chatListAdapter);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        ParseQuery<Match> matchesA = ParseQuery.getQuery(Match.class);
        matchesA.whereEqualTo(Match.USER_ONE, ParseUser.getCurrentUser());
        matchesA.whereEqualTo(Match.STATUS, "matched");
        try {
            List<Match> resultsA = matchesA.find();
            Log.i("size of matchA", String.valueOf(resultsA.size()));

            for (Match match : resultsA) {
                users.add(match.getUserTwo());
                chatListAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ParseQuery<Match> matchesB = ParseQuery.getQuery(Match.class);
        matchesB.whereEqualTo(Match.USER_TWO, ParseUser.getCurrentUser());
        matchesB.whereEqualTo(Match.STATUS, "matched");
        try {
            List<Match> resultsB = matchesB.find();
            Log.i("size of matchB", String.valueOf(resultsB.size()));

            for (Match match : resultsB) {
                users.add(match.getUserOne());
                chatListAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.i("user list size", String.valueOf(users.size()));
        if (users.size() != 0) {
            tvNoChats.setVisibility(View.GONE);
            rvChats.setVisibility(View.VISIBLE);
        } else {
            tvNoChats.setVisibility(View.VISIBLE);
            rvChats.setVisibility(View.GONE);
        }

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvChats.getContext(), manager.getOrientation());
        rvChats.addItemDecoration(dividerItemDecoration);

    }
}