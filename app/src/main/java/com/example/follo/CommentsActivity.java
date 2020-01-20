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

// Class CommentsActivity creates and opens up the Comments Activity.
public class CommentsActivity extends AppCompatActivity {

    // initiate some variable names
    private ImageButton postCommentButton;
    private EditText commentInputText;
    private RecyclerView commentList;

    private DatabaseReference usersRef, postsRef;
    private FirebaseAuth mAuth;

    private String post_Key, current_user_id, commentImageRef;

    // This is where class CommentsActivity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // get the random key of each post
        post_Key = getIntent().getExtras().get("postKey").toString();

        // Firebase references
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(post_Key).child("Commments");

        // create some rules for the layout of the comments on the recyclerView
        commentList = (RecyclerView) findViewById(R.id.comments_list);
        commentList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentList.setLayoutManager(linearLayoutManager);

        // assisn the items on the screen memory references
        commentInputText = (EditText) findViewById(R.id.comment_input);
        postCommentButton = (ImageButton) findViewById(R.id.post_comment_button);

        // when the user presses the post comment button validate the input and get the username
        // from Firebase and pass it to the ValidateComment method
        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);
                            // this wipes the EditText clean for a fresh start
                            commentInputText.setText("");
                            // Call onStart again so the list repopulates.  I wasn't seeing the
                            // top post until I called this again.
                            onStart();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

       // This is the "Users" DataSnapshot that gets the URL to the profile image
       // so the profile image of the user can be displayed on each post
        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    // get the reference to the profileimage and store it in commenImageRef
                    // It's used in the ValidateComment() method.
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

    // ##################### This is where the supporting methods start ########################################
    // The FirebaseRecyclerAdapter is placed in the onStart() method.  That way the comments list
    // will populate as soon as CommentsActivity starts.  It will automaticlly post all of the comments.
    protected void onStart(){
        super.onStart();

        // Create a query that orders the list by the timestamp that's placed as a child when posts
        // are created.
        Query query = postsRef.orderByChild("commenttimestamp");

        // Create the RecyclerAdapter options that sets the rules on how to populate the list.
        // This uses the query and Class Comments that have the getters and setters.
        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>().setQuery(query, Comments.class).build();

        // Creates the RecyclerAdapter.
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

                // This specifies which layout to post all of items to
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.comments_layout, parent, false);
                return new CommentsViewHolder(view);
            }
        };
        adapter.startListening();
        commentList.setAdapter(adapter);
    }

    // The ViewHolder method creates a reference to the item on the screen and then sets
    // the information stored in Firebase and sets it to the all_comments_layout to its specified location.
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

        // display the user profile image to the comment
        public void setCommentImage(Context applicationContext, String commentImage) {
            ImageView myCommentImage = (CircleImageView) mView.findViewById(R.id.comments_users_profile_image);
            Picasso.get().load(commentImage).placeholder(R.drawable.profile).into(myCommentImage);
        }

    }

    // When the user selects the post comment button, this method validates the information.
    // It gets the text input, then check to see if its empty or not.  If it's not empty create
    // custom key to post with info
    private void ValidateComment(String userName) {

        String commentText = commentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please Make A Comment...", Toast.LENGTH_SHORT).show();
        }
        else{
            // get the date
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            // get the time
            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss");
            final String saveCurrentTime = currentTime.format(calForDate.getTime());

            // create key by combining the userID + date + time and call it randomKey
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

    // Create the timestamp that the RecyclerAdapter uses to diplay the comments from newest to oldest
    private long getCurrentTimeStamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }


}
