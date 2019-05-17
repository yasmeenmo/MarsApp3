package com.example.marsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    public void updateUserStatus(String state)
    {

        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, YYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();

        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        UsersRef.child(online_user_id).child("userState")
                .updateChildren(currentStateMap);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserStatus("online");
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void DisplayAllFriends() {
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                (

                        Friends.class,
                        R.layout.all_users_display_layout,
                        FriendsViewHolder.class,
                        FriendsRef
                ) {

            @Override
            protected void populateViewHolder(final FriendsViewHolder viewholder, Friends model, int position)
            {
                viewholder.setDate(model.getDate());


                final String usersIDs = getRef(position).getKey();


                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final String userName = dataSnapshot.child("fullnamel").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                            final String type;

                            if(dataSnapshot.hasChild("userState"))
                            {
                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                if(type.equals("online"))
                                {
                                    viewholder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    viewholder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            viewholder.setFullname(userName);
                            viewholder.setProfileimage(getApplicationContext(), profileImage);

                            viewholder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CharSequence options[] = new CharSequence[]
                                            {
                                              userName + "'s Profile ",
                                                    "Send massege"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if (which == 0)
                                            {
                                                Intent profileintent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                profileintent.putExtra("visit_user_id",usersIDs);
                                                startActivity(profileintent);
                                            }

                                            if (which == 1)
                                            {
                                                Intent chatintent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                chatintent.putExtra("visit_user_id",usersIDs);
                                                chatintent.putExtra("userName",userName);
                                                startActivity(chatintent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView onlineStatusView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_users_online_icon);


        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname){
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }

        public void setDate(String date){
            TextView friendsDate = mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends since : " + date);
        }
    }

}