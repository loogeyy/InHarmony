package com.example.inharmony.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import kaaes.spotify.webapi.android.models.AudioFeaturesTrack;
import kaaes.spotify.webapi.android.models.AudioFeaturesTracks;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Track;


public class MatchingFragment extends Fragment {
    private static final String TAG = "MatchingFragment";
    private CardAdapter arrayAdapter;

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    public static String token;
    private boolean newSignUp;
    private SpotifyService service;

    TextView tvNoMatches;

    ArrayList<Card> rowItems;

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

        tvNoMatches = view.findViewById(R.id.tvNoMatches);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
        }

        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        service = spotifyApi.getService();

        rowItems = new ArrayList<Card>();

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
                 if (rowItems.size() != 0) {
                    rowItems.remove(0);
                    arrayAdapter.notifyDataSetChanged();
                }
                 if (rowItems.size() == 0) {
                     Log.i(TAG, "empty");
                     tvNoMatches.setText("Uh Oh! There are no more users to match with.");
                     tvNoMatches.setVisibility(View.VISIBLE);
                } else {
                    //tvNoMatches.setVisibility(View.INVISIBLE);
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
                            try {
                                // if not rejected status, update match in database
                                if (objects.get(0).getString("status").equals("pending")) {
                                    objects.get(0).delete();
                                    Match match = new Match();
                                    match.setUserOne(ParseUser.getCurrentUser());
                                    match.setUserTwo(matchedUser);
                                    match.setStatus("matched");
                                    match.save();
                                    showMatchDialog(matchedUser);
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
                try {
                    if (itemsInAdapter == 0) {
                        updatePotentialMatches();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (rowItems.size() == 0) {
                    tvNoMatches.setVisibility(View.VISIBLE);
                    tvNoMatches.setText("Uh Oh! There are no more users to match with.");
                } else {
                    //tvNoMatches.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });
    }

    private void showMatchDialog(ParseUser matchedUser) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        MatchPopupFragment matchPopupFragment = new MatchPopupFragment(matchedUser);
        Bundle bundle = new Bundle();
        bundle.putBoolean("newSignUp", false);
        bundle.putString(MatchPopupFragment.EXTRA_TOKEN, token);
        Log.i(TAG, "matched user: " + matchedUser.toString());
        matchPopupFragment.setArguments(bundle);
        matchPopupFragment.show(fm, "matchpopup");
    }

    //THE ALGORITHM
    private double similarityScore(ParseUser currentUser, ParseUser user) {
        int FEATURE_LIST_SIZE = 6;
        ArrayList<Double> myFeatureAvgs = (ArrayList<Double>) currentUser.get("featureAvgs");
        ArrayList<Double> myFeatureWeights = (ArrayList<Double>) currentUser.get("featureWeights");

        ArrayList<Double> otherFeatureAvgs = (ArrayList<Double>) user.get("featureAvgs");
        ArrayList<Double> otherFeatureWeights = (ArrayList<Double>) user.get("featureWeights");

        double totalSum = 0;
        double totalSize = 0;

        for (int i = 0; i < FEATURE_LIST_SIZE; i++) {
            double difference = (double)Math.abs((myFeatureAvgs.get(i) - otherFeatureAvgs.get(i)));
            double weightedDifference = ((difference) * (myFeatureWeights.get(i) + otherFeatureWeights.get(i)));
            totalSum += weightedDifference;
            totalSize += (myFeatureWeights.get(i) + otherFeatureWeights.get(i));
        }
        double result = totalSum / totalSize;
        Log.i("Results", user.getString("bio") + " and " + currentUser.getString("bio") + ": " + result);
        return result;
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

                ParseQuery<Match> matchesA = ParseQuery.getQuery(Match.class);
                // to prevent repeats
                matchesA.whereEqualTo(Match.USER_ONE, ParseUser.getCurrentUser());
                matchesA.whereEqualTo(Match.USER_TWO, user);

                ParseQuery<Match> existingMatch = ParseQuery.getQuery(Match.class);
                existingMatch.whereEqualTo(Match.USER_ONE, user);
                existingMatch.whereEqualTo(Match.USER_TWO, ParseUser.getCurrentUser());
                existingMatch.whereEqualTo(Match.STATUS, "matched");

                List<Match> objects = (List<Match>) matchesA.find();
                List<Match> existing = (List<Match>) existingMatch.find();

                if (objects.size() == 0 && existing.size() == 0)  {
                    double score = similarityScore(ParseUser.getCurrentUser(), user);
                    double threshold = 0.05; // maximum difference
                    if (score < threshold) {
                        potentialMatchesList.put(user);
                        Card card = new Card(user);
                        rowItems.add(card);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

        ParseUser.getCurrentUser().put("potentialMatches", potentialMatchesList);
        ParseUser.getCurrentUser().save();
    }
}