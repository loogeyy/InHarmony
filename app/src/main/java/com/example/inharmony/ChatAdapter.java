package com.example.inharmony;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inharmony.tasks.LoadChatTrackTask;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final String TAG = "ChatAdapter";
    private List<Message> mMessages;
    private Context mContext;
    private String token;
    private SpotifyService service;

    private static final int MESSAGE_OUTGOING = 0;
    private static final int MESSAGE_INCOMING = 1;

    public ChatAdapter(Context context, List<Message> messages, String token) {
        mMessages = messages;
        mContext = context;
        this.token = token;
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = mMessages.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return MESSAGE_OUTGOING;
        } else {
            return MESSAGE_INCOMING;
        }
    }

    private boolean isMe(int position) {
        Message message = mMessages.get(position);
        boolean isMe = false;
        try {
            isMe = (message.getSender() != null) && (message.getSender().fetch().getUsername().equals(ParseUser.getCurrentUser().getUsername()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return isMe;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == MESSAGE_INCOMING) {
            View contactView = inflater.inflate(R.layout.message_incoming, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else if (viewType == MESSAGE_OUTGOING) {
            View contactView = inflater.inflate(R.layout.message_outgoing, parent, false);
            return new OutgoingMessageViewHolder(contactView);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    public abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(Message message);
    }

    public class IncomingMessageViewHolder extends MessageViewHolder {
        ImageView imageOther;
        TextView body;
        TextView name;
        TextView tvIncomingTrack;
        ImageView ivIncomingTrack;
        ImageView ivPlayIncoming;

          public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            imageOther = itemView.findViewById(R.id.ivProfileOther);
            body = itemView.findViewById(R.id.tvOtherBody);
            name = itemView.findViewById(R.id.tvSender);
            tvIncomingTrack = itemView.findViewById(R.id.tvIncomingTrack);
            ivIncomingTrack = itemView.findViewById(R.id.ivIncomingTrack);
            ivPlayIncoming = itemView.findViewById(R.id.ivPlayIncoming);
        }

        @Override
        void bindMessage(Message message) {
            ParseUser user = message.getSender();
            try {
                name.setText(user.fetch().getString("name"));
                if (user.get("profilePic") != null) {
                    ParseFile image = (ParseFile) user.get("profilePic");
                    Glide.with(mContext).load(image.getUrl()).into(imageOther);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (message.getBody() == null) {
                body.setVisibility(View.GONE);
            } else {
                body.setVisibility(View.VISIBLE);
                body.setText(message.getBody());
            }

            if (message.getTrackId() == null) {
                toggleTrackVisibility(false);
            } else {
                toggleTrackVisibility(true);
                startSpotifyService();
                LoadChatTrackTask loadChatTrackTask = new LoadChatTrackTask(itemView, token, service, false);
                loadChatTrackTask.execute(message);
            }
        }

        private void toggleTrackVisibility(Boolean enabled) {
            if (enabled) {
                tvIncomingTrack.setVisibility(View.VISIBLE);
                ivPlayIncoming.setVisibility(View.VISIBLE);
                ivIncomingTrack.setVisibility(View.VISIBLE);
            } else {
                tvIncomingTrack.setVisibility(View.GONE);
                ivPlayIncoming.setVisibility(View.GONE);
                ivIncomingTrack.setVisibility(View.GONE);
            }
        }

    }

    public class OutgoingMessageViewHolder extends MessageViewHolder {
        ImageView imageMe;
        TextView body;
        TextView tvOutgoingTrack;
        ImageView ivOutgoingTrack;
        ImageView ivPlayOutgoing;

        public OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            imageMe = itemView.findViewById(R.id.ivProfileMe);
            body = itemView.findViewById(R.id.tvBody);
            tvOutgoingTrack = itemView.findViewById(R.id.tvOutgoingTrack);
            ivOutgoingTrack = itemView.findViewById(R.id.ivOutgoingTrack);
            ivPlayOutgoing = itemView.findViewById(R.id.ivPlayOutgoing);
        }

        @Override
        public void bindMessage(Message message) {
            ParseUser user = message.getSender();
                if (user.get("profilePic") != null) {
                    ParseFile image = (ParseFile) user.get("profilePic");
                    Glide.with(mContext).load(image.getUrl()).into(imageMe);
                  }
            if (message.getBody() == null) {
                body.setVisibility(View.GONE);
            } else {
                body.setVisibility(View.VISIBLE);
                body.setText(message.getBody());
            }

            if (message.getTrackId() == null) {
                toggleTrackVisibility(false);
            } else {
                toggleTrackVisibility(true);
                startSpotifyService();
                LoadChatTrackTask loadChatTrackTask = new LoadChatTrackTask(itemView, token, service, true);
                loadChatTrackTask.execute(message);
            }
        }

        private void toggleTrackVisibility(Boolean enabled) {
            if (enabled) {
                tvOutgoingTrack.setVisibility(View.VISIBLE);
                ivOutgoingTrack.setVisibility(View.VISIBLE);
                ivPlayOutgoing.setVisibility(View.VISIBLE);
            } else {
                tvOutgoingTrack.setVisibility(View.GONE);
                ivOutgoingTrack.setVisibility(View.GONE);
                ivPlayOutgoing.setVisibility(View.GONE);
            }
        }

    }

    private void startSpotifyService() {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(token);
        service = spotifyApi.getService();
    }
}
