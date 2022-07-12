package com.example.inharmony.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inharmony.CardAdapter;
import com.example.inharmony.Match;
import com.example.inharmony.Player;
import com.example.inharmony.R;
import com.example.inharmony.Card;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;


public class MatchingFragment extends Fragment {
    private static final String TAG = "MatchingFragment";
    private CardAdapter arrayAdapter;
    private int i;

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    public static String token;
    private boolean newSignUp;
    private SpotifyService service;

    ListView listView;
    ArrayList<Card> rowItems;
    private String objectId;

    public MatchingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_matching, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
        } else {
            Log.i("MatchingFragment", "BUNDLE WAS NULL");
        }

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        service = spotifyApi.getService();

        rowItems = new ArrayList<Card>();
        List<ParseUser> users = new ArrayList<>();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        try {
            users = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // updatePotentialMatches(users);

        for (int i = 0; i < users.size(); i++) {
            Card card = new Card(users.get(i));
            if (!(card.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()))) {
                Log.i(TAG, "user added as match");
                rowItems.add(card);
                Log.i("Row item", "added");
            }
        }

            if (rowItems.size() == 0) {
                Log.i("Row items", "Cards are empty");
            }
                    Log.i("Matching", query.toString());


            for (Card c : rowItems) {
                Log.i("card:", c.getUser().toString());
            }
        arrayAdapter = new CardAdapter(getContext(), R.layout.item, rowItems, token, newSignUp);



        SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) view.findViewById(R.id.flingContainer);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                Log.i(TAG, "before removing: " + rowItems.get(0).getUser().getUsername());
                Log.i(TAG, "size: " + rowItems.size());
                if (rowItems.size() != 0) {
                    rowItems.remove(0);
                    //Log.i(TAG, "after removing: " + rowItems.get(0).getUser().getUsername());
                    Log.i(TAG, "size: " + rowItems.size());
                    arrayAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                Card card = (Card) dataObject;
                ParseUser rejectedUser = card.getUser();
                ParseQuery<Match> matches = ParseQuery.getQuery(Match.class);

                // check if other user has swiped on current one already
                matches.whereEqualTo(Match.USER_ONE, rejectedUser);
                matches.whereEqualTo(Match.USER_TWO, ParseUser.getCurrentUser());

                matches.findInBackground(new FindCallback<Match>() {
                    @Override
                    public void done(List<Match> objects, ParseException e) {
                        if (objects.size() != 0) {
                            Log.i(TAG, "Existing object found!");
                            try {
                                objects.get(0).delete();
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                        else {
                            Match match = new Match();
                            match.setUserOne(ParseUser.getCurrentUser());
                            match.setUserTwo(rejectedUser);
                            match.setStatus("rejected");
                            try {
                                match.save();
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }

                        //find it and update
                        //.put(STATUS, "rejected");
                    }
                });

                Toast.makeText(getContext(), "Swiped left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Card card = (Card) dataObject;
                ParseUser matchedUser = card.getUser();
                ParseQuery<Match> matches = ParseQuery.getQuery(Match.class);

                // check if other user has swiped on current one already
                matches.whereEqualTo(Match.USER_ONE, matchedUser);
                matches.whereEqualTo(Match.USER_TWO, ParseUser.getCurrentUser());

                matches.findInBackground(new FindCallback<Match>() {
                    @Override
                    public void done(List<Match> objects, ParseException e) {
                        // if other user swiped right
                        if (objects.size() != 0) {
                            Log.i(TAG, "Existing object found!");
                            try {
                                // if not rejected status, update match in database
                                if (objects.get(0).getString("status").equals("pending")) {
                                    objects.get(0).delete();
                                    Match match = new Match();
                                    match.setUserOne(ParseUser.getCurrentUser());
                                    match.setUserTwo(matchedUser);
                                    match.setStatus("matched");
                                    match.save();
                                    //display "You Matched!" screen here
                                }

                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                        // if new match transaction
                        else {
                            Log.i(TAG, "Existing object not found!");
                            Match match = new Match();
                            match.setUserOne(ParseUser.getCurrentUser());
                            match.setUserTwo(matchedUser);
                            match.setStatus("pending");
                            try {
                                match.save();
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                Toast.makeText(getContext(), "Swiped right!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
//                // Ask for more data here
                //updatePotentialMatches(users);
                TextView tvNoMatches = view.findViewById(R.id.tvNoMatches);
                if (rowItems.size() == 0) {
                    tvNoMatches.setVisibility(View.VISIBLE);
                    tvNoMatches.setText("Uh Oh! There are no more users to match with.");
                } else {
                    tvNoMatches.setVisibility(View.INVISIBLE);
                }
//                rowItems.add("XML ".concat(String.valueOf(i)));
//                arrayAdapter.notifyDataSetChanged();
//                Log.d("LIST", "notified");
//                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });

//
//        // Optionally add an OnItemClickListener
//        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClicked(int itemPosition, Object dataObject) {
//                Toast.makeText(getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
//            }
//        });

    }
    //array -> unmatched users
    //array -> rejected users
    //array -> want matches

    //pending -> match
    //trinary boolean -> matched, rejected, potential
    //rejection -> delete it from teh match database
    //query matches to display messaging

    private int similarityScore(ParseUser currentUser, ParseUser user) {
        //service.getTracksAudioFeatures();
        return 0;
    }

    private void updatePotentialMatches(List<ParseUser> users) {
        JSONArray potentialMatchesList;

        if (ParseUser.getCurrentUser().get("potentialMatches") == null) {
            potentialMatchesList = new JSONArray();
        } else {
            potentialMatchesList = (JSONArray) ParseUser.getCurrentUser().get("potentialMatches");
        }

        for (int i = 0; i < users.size(); i++) {
            ParseUser user = users.get(i);
            Log.i("USER:", users.get(i).getUsername());
            Card card = new Card(user);
            //Card card = new Card(users.get(i).getObjectId(), users.get(i).getUsername());
            rowItems.add(card);
            Log.i("Row item", "added");
            int score = similarityScore(ParseUser.getCurrentUser(), user);
            if (score > 10) {
                potentialMatchesList.put(user);
                rowItems.add(card);
                return;
            }

        }


        if (true) {
           // potentialMatchesList.put()
        }

        //grab top five songs for user and compare gettracksaudiofeatures
        //including popularity
        //top songs similarity

        //THE ALGORITHM
        /*
        if user has less than 5 people in its potential matches list -> refreshes
        run the algorithm -> store potential matches into potential matches array (of parse users), arrayUser -> check:
            query.whereequalsto(userMatchOne, arrayUser) {
            for each match in qeury:
                if match.
                if (already seen (enter logic here) ) {
                    remvoe from potential matches list
            }   if (not already seen (enter logic here)) {

            }
        display the matches on the matching profile

         */


    }

}