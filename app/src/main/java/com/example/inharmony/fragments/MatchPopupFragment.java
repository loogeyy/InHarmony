package com.example.inharmony.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.MainActivity;
import com.example.inharmony.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class MatchPopupFragment extends DialogFragment {
    private ParseUser matchedUser;
    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private String token;

    public MatchPopupFragment() {
        // Required empty public constructor
    }

    public MatchPopupFragment(ParseUser user) {
        matchedUser = user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_popup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);
        }

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater dialog = LayoutInflater.from(getActivity());
        View view = dialog.inflate(R.layout.fragment_match_popup, null);
        TextView tvMatchDisplay = view.findViewById(R.id.tvMatchDisplay);
        ImageView userOne = view.findViewById(R.id.userOne);
        ImageView userTwo = view.findViewById(R.id.userTwo);
        Button btnKeepMatching = view.findViewById(R.id.btnKeepMatching);
        Button btnNewChat = view.findViewById(R.id.btnNewChat);

        Log.i("user", String.valueOf(matchedUser));

        tvMatchDisplay.setText("You Matched a Beat with " + matchedUser.get("name").toString() + "!");

        ParseFile profilePicOne = (ParseFile) matchedUser.get("profilePic");
        ParseFile profilePicTwo = (ParseFile) ParseUser.getCurrentUser().get("profilePic");
        if (profilePicOne != null) {
            Glide.with(getContext()).load(profilePicOne.getUrl()).into(userOne);
        }
        if (profilePicTwo != null) {
            Glide.with(getContext()).load(profilePicTwo.getUrl()).into(userTwo);
        }

        alertDialogBuilder.setView(view);
        AlertDialog alert = alertDialogBuilder.create();

        btnKeepMatching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              alert.dismiss();
            }
        });

        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
                ChatFragment fragment = new ChatFragment();

                Bundle bundle = new Bundle();
                bundle.putBoolean("newSignUp", false);
                bundle.putParcelable("user", matchedUser);
                bundle.putString(ChatFragment.EXTRA_TOKEN, token);

                fragment.setArguments(bundle);
                ((MainActivity)getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
            }
        });

        return alert;
    }


}