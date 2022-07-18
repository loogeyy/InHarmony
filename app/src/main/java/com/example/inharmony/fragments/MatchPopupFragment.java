package com.example.inharmony.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.example.inharmony.MainActivity;
import com.example.inharmony.R;
import com.parse.ParseUser;

public class MatchPopupFragment extends DialogFragment {
    private ParseUser matchedUser;
    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private String token;

    public MatchPopupFragment() {
        // Required empty public constructor
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
            matchedUser = bundle.getParcelable("user");
            token = bundle.getString(EditProfileFragment.EXTRA_TOKEN);

        }

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("You matched with a new user!");
        alertDialogBuilder.setMessage("Start a new chat with them?");
        alertDialogBuilder.setPositiveButton("OK!",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ChatFragment fragment = new ChatFragment();

                Bundle bundle = new Bundle();
                bundle.putBoolean("newSignUp", false);
                bundle.putParcelable("user", matchedUser);
                bundle.putString(ChatFragment.EXTRA_TOKEN, token);

                fragment.setArguments(bundle);
                ((MainActivity)getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }
        });
        alertDialogBuilder.setNegativeButton("Keep Matching", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null && ((Dialog)dialog).isShowing()) {
                    dialog.dismiss();
                }
            }

        });

        return alertDialogBuilder.create();
    }
}