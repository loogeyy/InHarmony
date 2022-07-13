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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;


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
        }

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        service = spotifyApi.getService();

        rowItems = new ArrayList<Card>();



//        for (int i = 0; i < users.size(); i++) {
//            Card card = new Card(users.get(i));
//            if (!(card.getUser().getUsername().equals(ParseUser.getCurrentUser().getUsername()))) {
//                Log.i(TAG, "user added as match");
//                rowItems.add(card);
//                Log.i("Row item", "added");
//            }
//        }


//            for (Card c : rowItems) {
//                Log.i("card:", c.getUser().toString());
//            }

        arrayAdapter = new CardAdapter(getContext(), R.layout.item, rowItems, token, newSignUp);
        try {
            updatePotentialMatches();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
                            try {
                                objects.get(0).delete();
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });

                // update match transaction as rejection
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
                        // if other user has swiped
                        if (objects.size() != 0) {
                            Log.i(TAG, "Existing object found!");
                            try {
                                // if not rejected status, update match in database
                                Log.i(TAG, "status: " + objects.get(0).getString("status"));
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
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                Log.i(TAG, "onAdapterAboutToEmpty triggered");
                Log.i("itemsInAdapter", String.valueOf(itemsInAdapter));
                // Ask for more data here
                try {
                    if (itemsInAdapter == 0) {
                        updatePotentialMatches();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
    }
    //array -> unmatched users
    //array -> rejected users
    //array -> want matches

    //pending -> match

    //query matches to display messaging


    //THE ALGORITHM
    private int similarityScore(ParseUser currentUser, ParseUser user) {
//        service.getTracksAudioFeatures();
//        service.getTopTracks()


        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Pager<Track> songId = service.getTopTracks();
                for (Track track : songId.items) {
                    Log.i(TAG, "SIMILARITY: " + track.name);
                }
            }
        });

        //grab top five songs for user and compare gettracksaudiofeatures
        //including popularity
        //top songs similarity

        return 1;
    }

    //refresh list of potential matches and prevent repeats
    private void updatePotentialMatches() throws ParseException {
        List<ParseUser> users = new ArrayList<>();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        try {
            users = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONArray potentialMatchesList = new JSONArray();

        // iterates through all users query
        for (int i = 0; i < users.size(); i++) {
            ParseUser user = users.get(i);
            if (!(user.getUsername().equals(ParseUser.getCurrentUser().getUsername()))) {
                Log.i("USER:", users.get(i).getUsername());
                Log.i(TAG, "potential match, not current user");

                ParseQuery<Match> matches = ParseQuery.getQuery(Match.class);
                // to prevent repeats
                matches.whereEqualTo(Match.USER_ONE, ParseUser.getCurrentUser());
                matches.whereEqualTo(Match.USER_TWO, user);
                List<Match> objects = (List<Match>) matches.find();

                matches.whereEqualTo(Match.USER_ONE, user);

                if (objects.size() == 0)  {
                    int score = similarityScore(ParseUser.getCurrentUser(), user);
                    int threshold = 0;
                    if (score > threshold) {
                        potentialMatchesList.put(user);
                        Card card = new Card(user);
                        rowItems.add(card);
                        arrayAdapter.notifyDataSetChanged();
                        Log.i("Row item", "added");
                    }
                }
            }
        }
        Log.i(TAG,"NEW CURRENT LIST OF POTENTIAL MATCHES");
        for (int i = 0; i < potentialMatchesList.length(); i++) {
            try {
                ParseUser user = (ParseUser) potentialMatchesList.get(i);
                Log.i("Match ", user.getUsername());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        ParseUser.getCurrentUser().put("potentialMatches", potentialMatchesList);
        ParseUser.getCurrentUser().save();
        Log.i("updatePotentialMatches", "done");



        /*

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