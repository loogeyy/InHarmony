package com.example.inharmony.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.inharmony.ChatListAdapter;
import com.example.inharmony.Match;
import com.example.inharmony.R;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {
    private static final String TAG = "ChatListFragment";

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private static String token;
    private boolean newSignUp;
    List<ParseUser> users;

    public ChatListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chatlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvChats = view.findViewById(R.id.rvChats);
        TextView tvNoChats = view.findViewById(R.id.tvNoChats);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(ChatListFragment.EXTRA_TOKEN);
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
            for (Match match : resultsB) {
                users.add(match.getUserOne());
                chatListAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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