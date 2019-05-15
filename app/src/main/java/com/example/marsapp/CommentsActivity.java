package com.example.marsapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private DatabaseReference UsersRef, PostRef;
    private FirebaseAuth mAuth;
    private String Postkey,current_user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        Postkey = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Postkey).child("Comments");

        CommentsList = findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = findViewById(R.id.comment_input);
        PostCommentButton = findViewById(R.id.post_comment_btn);


        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();
                            validateComments(userName);
                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void validateComments(String userName) {

        String commentText = CommentInputText.getText().toString();

        if (TextUtils.isEmpty(commentText)){
            Toast.makeText(this,"please write text to comment...",Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());

            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calFordDate.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime ;

            HashMap commentsMap = new HashMap();
                commentsMap.put("uid",current_user_id);
                commentsMap.put("comment",commentText);
                commentsMap.put("date",saveCurrentDate);
                commentsMap.put("time",saveCurrentTime);
                commentsMap.put("username",userName);


            PostRef.child(RandomKey).updateChildren(commentsMap)
            .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this,"you have Commented successfully...",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CommentsActivity.this,"Error Occured, try again...",Toast.LENGTH_SHORT).show();

                    }
                }
            });





        }
    }
}
