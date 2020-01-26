package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;

    private DatabaseReference friendsRef, usersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Firebase references
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // set some layout rules for the recyclerView that displays the list of friends
        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        // call the method that displays all friends
        DisplayAllFriends();
    }

    // This method creates the recyclerView rules and is responsible for querying the Firebase
    // database and displaying all the info
    private void DisplayAllFriends() {

        // query that decides how the friends are sorted
        Query query = friendsRef.orderByChild("date"); // haven't implemented a proper list sort yet.

        // build the options for the adapter
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();

        // start the adapter here
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull Friends friends) {

                // get the date to display how long 2 people have been friends
                friendsViewHolder.setDate(friends.getDate());
                final String usersIDs = getRef(position).getKey(); // get the key for each position
                // reference to "Users" on Firebase use the refe for each key
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // get the user's name and image to display on the Friends List along with date
                            final String username = dataSnapshot.child("fullname").getValue().toString();
                            final String profileimage = dataSnapshot.child("profileimage").getValue().toString();

                            friendsViewHolder.setFullName(username);// set the name
                            friendsViewHolder.setProfileImage(getApplicationContext(), profileimage); // set the image

                            // make each friend on the list clickable.
                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // create the two options for the dialog window below and call
                                    // it options
                                    CharSequence options[] = new CharSequence[]{
                                            username + "'s Profile",
                                            "Send Message"
                                    };
                                    // create the AlertDialog
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Options");
                                    // if user selects the friends username then start the Person profile intent
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0){
                                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                profileIntent.putExtra("postKey", usersIDs);
                                                startActivity(profileIntent);
                                            }// if user selects Send Message then start the message intent
                                            if (which == 1){
                                                // send this info to the ChatActivity.  It's the
                                                // message receiver's id and name
                                                Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                chatIntent.putExtra("postKey", usersIDs);
                                                chatIntent.putExtra("userName", username);
                                                startActivity(chatIntent);
                                            }
                                        }
                                    });
                                    builder.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }); // click listener for each friend on the list
                friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent friendProfile = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                        friendProfile.putExtra("postKey", usersIDs);
                        startActivity(friendProfile);
                    }
                });
            }

            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent ,false);
                return new FriendsViewHolder(view);
            }
        };
        adapter.startListening();
        myFriendList.setAdapter(adapter);
    }

        // Class that accesses the static class Friends and utilizes its methods.
        // In this case it's just get and set date
        public static class FriendsViewHolder extends RecyclerView.ViewHolder {

            View mView;

            public FriendsViewHolder(View itemView) {
                super(itemView);
                mView = itemView;
            }

            public void setProfileImage(Context applicationContext, String profileimage) {
                CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
                Picasso.get().load(profileimage).into(image);
            }

            public void setFullName(String fullName){
                TextView myName = (TextView) mView.findViewById(R.id.all_users_full_name);
                myName.setText(fullName);
            }

            public void setDate(String date){
                TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
                friendsDate.setText("Friends scince: " + date);
            }
        }
    }

