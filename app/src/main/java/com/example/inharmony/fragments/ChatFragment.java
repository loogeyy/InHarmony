package com.example.inharmony.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.inharmony.ChatAdapter;
import com.example.inharmony.Message;
import com.example.inharmony.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class ChatFragment extends Fragment {
    static final String TAG = "CHATFRAGMENT";
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;
    public static String EXTRA_TOKEN = "EXTRA_TOKEN";
    private String token;

    private ParseUser user;
    private EditText etMessage;
    private ImageButton ibSend;
    private RecyclerView rvChat;
    private ImageView btnAddTrack;
    private ImageView ivTrack;
    private TextView tvTrack;
    private Button btnCancel;
    private List<Message> messages;
    private TextView tvChatTitle;
    private Button btnChatProfile;

    private Track track;

    private boolean firstLoad;
    ChatAdapter chatAdapter;

    public ChatFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etMessage = view.findViewById(R.id.etMessage);
        ibSend = view.findViewById(R.id.ibSend);
        rvChat = view.findViewById(R.id.rvChat);
        btnAddTrack = view.findViewById(R.id.btnAddTrack);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvTrack = view.findViewById(R.id.tvTrack);
        ivTrack = view.findViewById(R.id.ivTrack);
        btnChatProfile = view.findViewById(R.id.btnChatProfile);
        tvChatTitle = view.findViewById(R.id.tvChatTitle);

        messages = new ArrayList<>();
        firstLoad = true;

        btnCancel.setVisibility(View.GONE);
        tvTrack.setVisibility(View.GONE);
        ivTrack.setVisibility(View.GONE);

        Bundle bundle = getArguments();
        if (bundle != null) {
            user = bundle.getParcelable("user");
            token = bundle.getString(ChatFragment.EXTRA_TOKEN);
        }

        tvChatTitle.setText("Chat with " + user.get("name"));
        chatAdapter = new ChatAdapter(getContext(), user, messages, token);
        rvChat.setAdapter(chatAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);
        refreshMessages();

        checkSendMessageBtnClicked();
        checkAttachTrackBtnClicked();
        checkCancelTrackBtnClicked();
        checkViewProfileBtnClicked();
    }

    private void refreshMessages() {

        ParseQuery<Message> queryA = ParseQuery.getQuery(Message.class);
        queryA.whereEqualTo(Message.SENDER, ParseUser.getCurrentUser());
        queryA.whereEqualTo(Message.RECEIVER, user);

        ParseQuery<Message> queryB = ParseQuery.getQuery(Message.class);
        queryB.whereEqualTo(Message.SENDER, user);
        queryB.whereEqualTo(Message.RECEIVER, ParseUser.getCurrentUser());

        List<ParseQuery<Message>> queryList = new ArrayList<>();
        queryList.add(queryA);
        queryList.add(queryB);
        ParseQuery<Message> fullQuery = ParseQuery.or(queryList);
        fullQuery.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
        fullQuery.orderByDescending("createdAt");
        fullQuery.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> objects, ParseException e) {
                if (e == null) {
                    messages.clear();
                    messages.addAll(objects);
                    chatAdapter.notifyDataSetChanged(); // update adapter
                    // Scroll to the bottom of the list on initial load
                    if (firstLoad) {
                        rvChat.scrollToPosition(0);
                        firstLoad = false;
                    }
                } else {
                    Log.e("message", "Error Loading Messages" + e);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
 }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void checkAttachTrackBtnClicked() {
        btnAddTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment fragment = new SearchFragment();
                Bundle bundle = new Bundle();
                bundle.putString(fragment.EXTRA_TOKEN, token);
                bundle.putString(fragment.SEARCH_TYPE, "TRACK");
                bundle.putString("TYPE", "message");
                fragment.setArguments(bundle);

                getActivity().getSupportFragmentManager().setFragmentResultListener("selectedTrack", ChatFragment.this, new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        if (result != null) {
                            track = result.getParcelable("selectedTrack");
                            Log.i(TAG, "track id: " + track.id);
                            tvTrack.setVisibility(View.VISIBLE);
                            ivTrack.setVisibility(View.VISIBLE);
                            btnCancel.setVisibility(View.VISIBLE);
                            tvTrack.setText(track.name + " - " + track.artists.get(0).name);
                            Image image = track.album.images.get(0);
                            if (image != null) {
                               Glide.with(getContext()).load(image.url).into(ivTrack);
                            }
                        }

                    }
                });
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                if (!fragment.isAdded()) {
                    ft.add(R.id.flContainer, fragment);
                }
                ft.show(fragment);
                ft.hide(ChatFragment.this).addToBackStack(null);
                ft.commit();
            }
        });
    }

    private void checkSendMessageBtnClicked() {
        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = new Message();
                String body = etMessage.getText().toString();
                if (body.isEmpty()) {
                    Log.i(TAG, "no body detected");
                    if (track != null) {
                        Log.i(TAG, "track detected with no body");
                        message.setTrackId(track.id);
                    } else {
                        etMessage.setError("Message cannot be empty.");
                        return;
                    }
                } else {
                    message.setBody(body);
                    Log.i(TAG, "body found");
                    if (track != null) {
                        Log.i(TAG, "track found");
                        message.setTrackId(track.id);
                    }
                }
                message.setSender(ParseUser.getCurrentUser());
                message.setReceiver(user);

                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        refreshMessages();
                        if (e != null) {
                            Log.i(TAG, e.toString());
                        }
                    }
                });

                etMessage.setText(null);
                btnCancel.setVisibility(View.GONE);
                tvTrack.setVisibility(View.GONE);
                ivTrack.setVisibility(View.GONE);
                track = null;

                String websocketUrl = "wss://inharmony.b4a.io";
                ParseLiveQueryClient parseLiveQueryClient = null;
                try {
                    parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(websocketUrl));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                ParseQuery<Message> parseQuery = ParseQuery.getQuery(Message.class);
                SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);
                subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, ((query, object) -> {
                    messages.add(0, object);

                    ((AppCompatActivity) getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatAdapter.notifyDataSetChanged();
                            rvChat.scrollToPosition(0);
                        }
                    });
                }));
            }
        });
    }

    private void checkCancelTrackBtnClicked() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track = null;
                tvTrack.setVisibility(View.GONE);
                ivTrack.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
            }
        });
    }

    private void checkViewProfileBtnClicked() {
        btnChatProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment fragment = new ProfileFragment(user);
                Bundle bundle = new Bundle();
                bundle.putString(ProfileFragment.EXTRA_TOKEN, token);
                bundle.putBoolean("newSignUp", false);
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
            }
        });
    }
    }