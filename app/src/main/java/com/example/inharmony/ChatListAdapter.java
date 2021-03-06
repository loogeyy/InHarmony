package com.example.inharmony;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inharmony.fragments.ChatFragment;
import com.example.inharmony.fragments.ProfileFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    Context context;
    List<ParseUser> users;
    String token;
    int lastPosition = -1;

    public ChatListAdapter(Context context, List<ParseUser> users, String token) {
        this.context = context;
        this.users = users;
        this.token = token;
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvChatName;
        TextView tvChatPreview;
        ImageView ivChatProfilePic;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatName = itemView.findViewById(R.id.tvChatName);
            tvChatPreview = itemView.findViewById(R.id.tvChatPreview);
            ivChatProfilePic = itemView.findViewById(R.id.ivChatProfilePic);
            itemView.setOnClickListener(this);
        }

        public void bind(ParseUser user) {
            String name = "";
            try {
                name = user.fetchIfNeeded().get("name").toString();
                tvChatName.setText(name);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            try {
                ParseFile profilePic = (ParseFile) user.fetchIfNeeded().get("profilePic");
                if (profilePic != null) {
                    Glide.with(context).load(profilePic.getUrl()).into(ivChatProfilePic);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            checkNameClicked(user);

            List<Message> sentMessages;
            List<Message> receivedMessages;

            ParseQuery<Message> sentMessagesParseQuery = ParseQuery.getQuery(Message.class);
            sentMessagesParseQuery.whereEqualTo("sender", ParseUser.getCurrentUser());
            sentMessagesParseQuery.whereEqualTo("receiver", user);
            sentMessagesParseQuery.orderByDescending("createdAt");

            ParseQuery<Message> receivedMessagesParseQuery = ParseQuery.getQuery(Message.class);
            receivedMessagesParseQuery.whereEqualTo("sender", user);
            receivedMessagesParseQuery.whereEqualTo("receiver", ParseUser.getCurrentUser());
            receivedMessagesParseQuery.orderByDescending("createdAt");

            try {
                sentMessages = sentMessagesParseQuery.find();
                receivedMessages = receivedMessagesParseQuery.find();

                // new chat preview
                if ((sentMessages.size() == 0) && receivedMessages.size() == 0) {
                    tvChatPreview.setText("Start a new chat with your match!");
                    return;
                }

                // incoming track preview
                if ((sentMessages.size() == 0) && (receivedMessages.size() > 0)) {
                    Message message = receivedMessages.get(0);
                    if (message.getBody() != null) {
                        tvChatPreview.setText(user.get("name") + ": " + message.getBody());
                    } else {
                        tvChatPreview.setText(user.get("name") + " shared a track!");
                    }
                    return;
                }

                // outgoing track preview
                if ((receivedMessages.size() == 0) && (sentMessages.size() > 0)) {
                    Message message = sentMessages.get(0);
                    if (message.getBody() != null) {
                        tvChatPreview.setText("You: " + message.getBody());
                    } else {
                        tvChatPreview.setText("You shared a track.");
                    }
                    return;
                }

                // most recent chat preview
                if ((receivedMessages.size() > 0) && (sentMessages.size() > 0)) {
                    Date sentLatest = (Date) sentMessages.get(0).fetchIfNeeded().getCreatedAt();
                    Date receivedLatest = (Date) receivedMessages.get(0).fetchIfNeeded().getCreatedAt();
                    if (sentLatest.after(receivedLatest)) {
                        if (sentMessages.get(0).getBody() != null) {
                            tvChatPreview.setText("You: " + sentMessages.get(0).getBody());
                        } else {
                            tvChatPreview.setText("You shared a track");
                        }
                    } else {
                        if (receivedMessages.get(0).getBody() != null) {
                            tvChatPreview.setText(user.get("name") + ": " + receivedMessages.get(0).getBody());
                        } else {
                            tvChatPreview.setText(user.get("name") + " shared a track!");
                        }
                    }
                }
                }
            catch (ParseException e) {
                e.printStackTrace();
            }
        }

        private void checkNameClicked(ParseUser user) {
            tvChatName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileFragment fragment = new ProfileFragment(user);
                    Bundle bundle = new Bundle();
                    bundle.putString(ProfileFragment.EXTRA_TOKEN, token);
                    bundle.putBoolean("newSignUp", false);
                    fragment.setArguments(bundle);
                    ((MainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            ChatFragment fragment = new ChatFragment();

            Bundle bundle = new Bundle();
            bundle.putBoolean("newSignUp", false);
            bundle.putParcelable("user", users.get(position));
            bundle.putString(ChatFragment.EXTRA_TOKEN, token);

            fragment.setArguments(bundle);
            ((MainActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment, "CHATFRAGMENT").addToBackStack(null).commit();
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatListView = LayoutInflater.from(context).inflate(R.layout.item_chatlist, parent, false);
        return new ChatViewHolder(chatListView);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ParseUser user = users.get(position);
        holder.bind(user);
        setAnimation(holder.itemView, position);

    }

    public void setAnimation(View animatedView, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            animatedView.startAnimation((animation));
            lastPosition = position;
        }
    }


}
