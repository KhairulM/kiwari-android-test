package com.khairulm.simplechatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int SIGN_IN_REQUEST_CODE = 123;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;

    private RecyclerView mRecycler;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<ChatMessage> messages;

    private String username;
    private final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //seedDatabase();
        messages = new ArrayList<>();

        // add fab on click listener
        FloatingActionButton sendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageBox = (EditText) findViewById(R.id.messageBox);

                ChatMessage newMessage = new ChatMessage(username,
                        messageBox.getText().toString(),
                        mUser.getUid().toString());
                mDatabase.child("messages").push().setValue(newMessage);

                messageBox.setText("");
            }
        });

        // Setup RecyclerView
        mRecycler = (RecyclerView) findViewById(R.id.messageRecycler);
        mRecycler.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mLayoutManager);

        mAdapter = new ChatMessageAdapter(new ArrayList<ChatMessage>(), mDatabase);
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Check if user is authenticated
        if (mFirebaseAuth.getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE);
        } else {
            mUser = mFirebaseAuth.getCurrentUser();
            displayChatMessages();
            Toast.makeText(this,
                    "Welcome " + username,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mUser = mFirebaseAuth.getCurrentUser();
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    });
        }
        return true;
    }

    private void displayChatMessages() {
        // attach a username listener
        mDatabase.child("users").child(mUser.getUid().toString()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        onUsernameResult(user.getUserName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w(TAG, "FABonClick:onCancelled", databaseError.toException());
                    }
                }
        );

        // get all chat message
        mDatabase.child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = new ChatMessage(
                            messageSnapshot.child("messageSender").getValue(String.class),
                            messageSnapshot.child("messageText").getValue(String.class),
                            messageSnapshot.child("senderId").getValue(String.class),
                            messageSnapshot.child("messageTime").getValue(long.class));

                    onChatMessageResult(message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "displayChatMessages:onCancelled", databaseError.toException());
            }
        });

        // get new messages
        mDatabase.child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatMessage message = new ChatMessage(
                        dataSnapshot.child("messageSender").getValue(String.class),
                        dataSnapshot.child("messageText").getValue(String.class),
                        dataSnapshot.child("senderId").getValue(String.class),
                        dataSnapshot.child("messageTime").getValue(long.class));

                onChatMessageResult(message);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "displayChatMessages:onCancelled", databaseError.toException());
            }
        });
    }

    private void onUsernameResult(String username) {
        this.username = username;
    }

    private void onChatMessageResult(ChatMessage chatMessage) {
        Log.e(TAG, "onChatMessageResult: "+chatMessage.getMessageSender());
        messages.add(chatMessage);
        mAdapter = new ChatMessageAdapter(messages, mDatabase);
        mRecycler.setAdapter(mAdapter);
        mRecycler.scrollToPosition(messages.size()-1);
    }

    private void seedDatabase() {
        // adding hardcoded user
        User jarjit = new User("Jarjit Singh",
                "https://api.adorable.io/avatars/160/jarjit@mail.com.png",
                "Raw1KyfiwjgpIFvYElFE6DH0wsE2");
        User ismail = new User("Ismail bin Mail",
                "https://api.adorable.io/avatars/160/ismail@mail.com.png",
                "M2Qn42Ip5DMCYp4Txjecp4T4ox53");

        // writing hardcoded user to database
        mDatabase.child("users").child(jarjit.getUserId()).setValue(jarjit);
        mDatabase.child("users").child(ismail.getUserId()).setValue(ismail);

        // adding harcoded message
    }
}
