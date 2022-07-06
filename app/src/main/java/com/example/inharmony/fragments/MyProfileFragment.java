package com.example.inharmony.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inharmony.R;
import com.parse.ParseUser;

public class MyProfileFragment extends Fragment {
    private ImageView btnEditProfile;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvAge;
    private TextView tvBio;
    private TextView tvFavGenres;
    private ImageView ivFavArtist;
    private TextView tvFavArtist;
    private ImageView ivFavAlbum;
    private TextView tvFavAlbum;
    private ImageView ivFavTrack;
    private TextView tvFavTrack;

    private boolean myProfile;
    private ParseUser user;


    public MyProfileFragment() {
        // Required empty public constructor
    }

    public MyProfileFragment(boolean myProfile, ParseUser user) {
        this.myProfile = myProfile;
        this.user = user;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName = view.findViewById(R.id.tvName);
        tvAge = view.findViewById(R.id.tvAge);
        tvFavGenres = view.findViewById(R.id.tvFavGenres);
        tvFavAlbum = view.findViewById(R.id.tvFavAlbum);
        tvFavArtist = view.findViewById(R.id.tvFavArtist);
        tvFavTrack = view.findViewById(R.id.tvFavTrack);
        ivFavArtist = view.findViewById(R.id.ivFavArtist);
        ivFavAlbum = view.findViewById(R.id.ivFavAlbum);
        ivFavTrack = view.findViewById(R.id.ivFavTrack);

        if (!myProfile) {
            btnEditProfile.setVisibility(View.GONE);
        } else {
            btnEditProfile.setVisibility(View.VISIBLE);
        }

        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    }