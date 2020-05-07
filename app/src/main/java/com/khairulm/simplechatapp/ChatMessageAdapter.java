package com.khairulm.simplechatapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    List<ChatMessage> messageList;
    static DatabaseReference database = null;

    public ChatMessageAdapter(List<ChatMessage> messageList, DatabaseReference database) {
        this.messageList = messageList;
        this.database = database;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_message, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(messageList.get(position));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView senderAvatar;
        public TextView senderName, message, time;
        public String TAG = "ChatMessageAdapter.ViewHolder";
        private Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            context = itemView.getContext();
            senderAvatar = (ImageView) itemView.findViewById(R.id.userAvatar);
            senderName = (TextView) itemView.findViewById(R.id.userName);
            message = (TextView) itemView.findViewById(R.id.messageStr);
            time = (TextView) itemView.findViewById(R.id.messageTime);
        }

        public void bind(final ChatMessage chatMessage) {
            if (chatMessage != null){
                message.setText(chatMessage.getMessageText());
                time.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        chatMessage.getMessageTime()));
                senderName.setText(chatMessage.getMessageSender());

                // get the corresponding user avatar
                final String avatarURL;
                database.child("users").child(chatMessage.getSenderId()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                onAvatarResult(dataSnapshot.getValue(User.class).getUserAvatarURL());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.w(TAG, "getAvatar:onCancelled", databaseError.toException());
                            }
                        }
                );
            }
        }

        public void onAvatarResult(String avatarURL){
            Glide.with(context).load(avatarURL).fitCenter().into(senderAvatar);
        }
    }
}
