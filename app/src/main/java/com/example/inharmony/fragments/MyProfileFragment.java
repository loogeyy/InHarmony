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

public class MyProfileFragment extends Fragment {
    private ImageButton btnEditProfile;
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




    public MyProfileFragment() {
        // Required empty public constructor
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
        btnEditProfile = view.findViewById(R.id.btnUpdateProfile);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    }