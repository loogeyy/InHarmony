package com.example.inharmony;

import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private static final String TAG = "ChatAdapter";
    private List<Message> mMessages;
    private Context mContext;
    private ParseUser mUser;

    private static final int MESSAGE_OUTGOING = 0;
    private static final int MESSAGE_INCOMING = 1;

    public ChatAdapter(Context context, ParseUser user, List<Message> messages) {
        mUser = user;
        mMessages = messages;
        mContext = context;
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
            Log.i("MESSAGE", "OUTGOING");
            return MESSAGE_OUTGOING;
        } else {
            Log.i("MESSAGE", "INCOMING");
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
        Log.i("mMessages size", String.valueOf(mMessages.size()));

        if (viewType == MESSAGE_INCOMING) {
            Log.i(TAG, "message incoming display");
            View contactView = inflater.inflate(R.layout.message_incoming, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else if (viewType == MESSAGE_OUTGOING) {
            Log.i(TAG, "message outgoing display");
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

          public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            imageOther = itemView.findViewById(R.id.ivProfileOther);
            body = itemView.findViewById(R.id.tvOtherBody);
            name = itemView.findViewById(R.id.tvSender);
        }

        @Override
        void bindMessage(Message message) {
            ParseUser user = message.getSender();
            try {
                name.setText(user.fetch().getUsername());
                if (user.get("profilePic") != null) {
                    ParseFile image = (ParseFile) user.get("profilePic");
                    Glide.with(mContext).load(image.getUrl()).into(imageOther);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            body.setText(message.getBody());

        }

    }

    public class OutgoingMessageViewHolder extends MessageViewHolder {
        ImageView imageMe;
        TextView body;

        public OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            imageMe = itemView.findViewById(R.id.ivProfileMe);
            body = itemView.findViewById(R.id.tvBody);
        }

        @Override
        public void bindMessage(Message message) {
            ParseUser user = message.getSender();
                if (user.get("profilePic") != null) {
                    ParseFile image = (ParseFile) user.get("profilePic");
                    Glide.with(mContext).load(image.getUrl()).into(imageMe);
                  }

            body.setText(message.getBody());
        }
    }
}
