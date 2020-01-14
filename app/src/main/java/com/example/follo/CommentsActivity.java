package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
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

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText commentInputText;
    private RecyclerView commentList;

    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;

    private String post_Key, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_Key = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key).child("Commments");

        commentList = (RecyclerView) findViewById(R.id.comments_list);
        commentList .setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);

        commentInputText = (EditText) findViewById(R.id.comment_input);
        postCommentButton = (ImageButton) findViewById(R.id.post_comment_button);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);

                            commentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    private void ValidateComment(String userName) {

        String commentText = commentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please Make A Comment...", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
            final String saveCurrentTime = currentTime.format(calForDate.getTime());

            final String randomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
                commentsMap.put("uid", current_user_id);
                commentsMap.put("comment", commentText);
                commentsMap.put("date", saveCurrentDate);
                commentsMap.put("time", saveCurrentTime);
                commentsMap.put("username", userName);
            postsRef.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "Your Comment Was Added Successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(CommentsActivity.this, "ERROR OCCURRED, Please Try Again", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
