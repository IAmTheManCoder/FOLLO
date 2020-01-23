package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

// FindFriendActivity searches for all the users on Firebase and sorts them by alph.  This class
// contains the RecyclerAdapter that displays the info to the search_result_List Layout.

public class FindFriendActivity extends AppCompatActivity {

    // initiate some variable names
    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserId;

    // This is where FindFriend Activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        // firebase references
        mAuth = FirebaseAuth.getInstance(); // get instance of the Firebase authentication called mAuth
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // create the toolbar at the top of the screen that has the back arrow
        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find FriendsActivity");

        // intialize and create some rules for the search list
        searchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        // assign the button and EditText to variable names
        searchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        searchInputText = (EditText)findViewById(R.id.search_box_input);

        // when the user selects the search button get the user input in the EditText
        // and hand it to the method SearchPeopleAndFriends()
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();

                SearchPeopleAndFriends(searchBoxInput);
            }
        });

    }

    //#################### This is where the supporting methods start ###########################################

    // the Recycler Adapter that takes in the names fot the alpha range and query Firebase for the name range.
    // Then Displays the names and profile images to the FindFriendsActivity range.
    private void SearchPeopleAndFriends(String searchBoxInput) {
        Query query = userRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(query,FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends,FindFriendActivity.FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendActivity.FindFriendViewHolder>(options) {

            // get the info from firebase and set it to the proper holder
            @Override
            protected void onBindViewHolder(@NonNull FindFriendActivity.FindFriendViewHolder holder, final int position, @NonNull FindFriends model) {
                final String postkey = getRef(position).getKey();
                holder.fullname.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);


                holder.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                            public void onClick(View v){
                                Intent findFriends = new Intent(FindFriendActivity.this, PersonProfileActivity.class);
                                findFriends.putExtra("postKey",postkey);
                                startActivity(findFriends);
                    }
                });
            }

            // Specify the Layout List that this info gets displayed to
            @NonNull
            @Override
            public FindFriendActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);

                FindFriendActivity.FindFriendViewHolder viewHolder = new FindFriendActivity.FindFriendViewHolder(view);
                return viewHolder;
            }
        };
        searchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    // Set the items on screen to their variable names.
    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView fullname, status;
        CircleImageView profileimage;

        // assign the variable names to the items on the screen
        public FindFriendViewHolder(@NonNull View itemView){
            super(itemView);
            fullname = itemView.findViewById(R.id.all_users_full_name);
            status = itemView.findViewById(R.id.all_users_status);
            profileimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
