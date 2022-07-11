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
import android.widget.Toast;

import com.example.inharmony.CardAdapter;
import com.example.inharmony.R;
import com.example.inharmony.Card;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MatchingFragment extends Fragment {
    private static final String TAG = "MatchingFragment";
    private CardAdapter arrayAdapter;
    private int i;

    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    public static String token;
    private boolean newSignUp;

    ListView listView;
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

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
            newSignUp = bundle.getBoolean("newSignUp");
        } else {
            Log.i("MatchingFragment", "BUNDLE WAS NULL");
        }

        rowItems = new ArrayList<Card>();
        List<ParseUser> users = new ArrayList<>();
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        try {
            users = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        query.findInBackground(new FindCallback<ParseUser>() {
//           @Override
//           public void done(List<ParseUser> objects, ParseException e) {
//               if (e != null) {
//                Log.e("MatchingFragment", e.toString());
//                return;
//               }
//               else {
//                   for (int i = 0; i < objects.size(); i++) {
//                       Log.i("USER:", objects.get(i).getUsername());
//                        Card card = new Card(objects.get(i));
//                        rowItems.add(card);
//                        Log.i("Row item", "added");
//                   }
//               }
//           }
//        });
        for (int i = 0; i < users.size(); i++) {
            Log.i("USER:", users.get(i).getUsername());
            Card card = new Card(users.get(i));
            //Card card = new Card(users.get(i).getObjectId(), users.get(i).getUsername());
            rowItems.add(card);
            Log.i("Row item", "added");
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
                if (rowItems.size() != 0) {
                    rowItems.remove(0);
                    Log.i(TAG, "after removing: " + rowItems.get(0).getUser().getUsername());
                    arrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //remove from list
                Toast.makeText(getContext(), "Swiped left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Toast.makeText(getContext(), "Swiped right!", Toast.LENGTH_SHORT).show();
                // check if it's a mutual match in the database
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
//                // Ask for more data here
//                rowItems.add("XML ".concat(String.valueOf(i)));
//                arrayAdapter.notifyDataSetChanged();
//                Log.d("LIST", "notified");
//                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Toast.makeText(getContext(), "Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}