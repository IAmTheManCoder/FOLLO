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

public class FindFriendActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        mAuth = FirebaseAuth.getInstance(); // get instance of the Firebase authentication called mAuth
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = (ImageButton) findViewById(R.id.search_people_friends_button);
        searchInputText = (EditText)findViewById(R.id.search_box_input);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();

                SearchPeopleAndFriends(searchBoxInput);
            }
        });

    }

    private void SearchPeopleAndFriends(String searchBoxInput) {
        Query query = userRef.orderByChild("fullname").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options = new FirebaseRecyclerOptions.Builder<FindFriends>().setQuery(query,FindFriends.class).build();
        FirebaseRecyclerAdapter<FindFriends,FindFriendActivity.FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendActivity.FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendActivity.FindFriendViewHolder holder, int position, @NonNull FindFriends model) {
                final String postkey = getRef(position).getKey();
                holder.fullname.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);

                holder.itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                            public void onClick(View v){
                                Intent findFriends = new Intent(FindFriendActivity.this, FindFriendActivity.class);
                                findFriends.putExtra("PostKey", postkey);
                                startActivity(findFriends);
                    }
                });
            }

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

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView fullname, status;
        CircleImageView profileimage;

        public FindFriendViewHolder(@NonNull View itemView){
            super(itemView);
            fullname = itemView.findViewById(R.id.all_users_full_name);
            status = itemView.findViewById(R.id.all_users_status);
            profileimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
