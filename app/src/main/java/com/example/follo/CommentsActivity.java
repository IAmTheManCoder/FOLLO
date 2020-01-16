package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText commentInputText;
    private RecyclerView commentList;

    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;

    private String post_Key, current_user_id, commentImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        post_Key = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key).child("Commments");
        //commentImageRef = usersRef.child("profileimage").toString();

        commentList = (RecyclerView) findViewById(R.id.comments_list);
        commentList.setHasFixedSize(true);
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
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);

                            commentInputText.setText("");
                            onStart();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

            usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        commentImageRef = dataSnapshot.child("profileimage").getValue().toString();

                    }
                    else{
                        Toast.makeText(CommentsActivity.this, "Comment Image Failed To Load", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    protected void onStart(){
        super.onStart();

        Query query = postsRef.orderByChild("commenttimestamp");

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(query, Comments.class).build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int position, @NonNull Comments comments) {
                final String postKey = getRef(position).getKey();
                commentsViewHolder.setUsername(comments.getUsername());
                commentsViewHolder.setDate(comments.getDate());
                commentsViewHolder.setTime(comments.getTime());
                commentsViewHolder.setCommentImage(getApplicationContext(),comments.getCommentImage());
                commentsViewHolder.setComment(comments.getComment());
            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false);
                return new CommentsViewHolder(view);
            }
        };
        adapter.startListening();
        commentList.setAdapter(adapter);
    }




    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public CommentsViewHolder (View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setUsername(String username){
            TextView myUsername = (TextView) mView.findViewById(R.id.comment_username);
            myUsername.setText(username);
        }

        public void setComment(String comment) {
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }

        public void setDate(String date) {
            TextView myDate = (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText(date);
        }

        public void setTime(String time) {
            TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText(time);
        }

        public void setCommentImage(Context applicationContext, String commentImage) {
            ImageView myCommentImage = (CircleImageView) mView.findViewById(R.id.comments_users_profile_image);
            Picasso.get().load(commentImage).placeholder(R.drawable.profile).into(myCommentImage);
        }



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
                commentsMap.put("commentImage", commentImageRef);
                commentsMap.put("commenttimestamp", getCurrentTimeStamp());
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

    //############## This is where the supporting methods start ######################################################

    private long getCurrentTimeStamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }


}
