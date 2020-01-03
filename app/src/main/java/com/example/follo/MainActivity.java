package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private CircleImageView navProfileImage;
    private TextView navProfileUserName;
    private ImageButton addNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, query;


    String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance(); // get instance of the Firebase authentication called mAuth
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        query = FirebaseDatabase.getInstance().getReference().child("Posts");

        // Display the toolbar
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        addNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);

        // Setting up the home screen and the drawer that slides out from the left
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_closed);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        //assigns the layout navigation header into a variable called NavView
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("fullname")){
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        navProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);

                    }
                    else{
                        Toast.makeText(MainActivity.this, "Profile name does not exist", Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });
        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts(){
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(query, Posts.class).build();
        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Posts,PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder postsViewHolder, int position, @NonNull Posts posts) {
                postsViewHolder.setFullname(posts.getFullname());
                postsViewHolder.setDescription(posts.getDescription());
                postsViewHolder.setProfileImage(getApplicationContext(),posts.getProfileimage());
                postsViewHolder.setPostImage(getApplicationContext(),posts.getPostimage());
                postsViewHolder.setDate(posts.getDate());
                postsViewHolder.setTime(posts.getTime());
            }

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout,parent,false);
                return new PostsViewHolder(view);
            }
        };
        adapter.startListening();
        postList.setAdapter(adapter);

    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setFullname(String fullname) {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setDescription(String description) {
            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setProfileImage(Context applicationContext, String profileimage) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }

        public void setPostImage(Context applicationContext, String postimage) {
            ImageView postImages = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).into(postImages);

        }

        public void setDate(String date) {
            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
            postDate.setText(" "+date);
        }

        public void setTime(String time) {
            TextView postTime = (TextView) mView.findViewById(R.id.time);
            postTime.setText(" "+time);
        }
    }


    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }


    // THIS IS WHERE THE SUPPORTING METHODS START ##################################################

    //
    //When the MainActivity starts check if the user has info stored to the database.
    // If yes then stay on the mainActivity.  If No then send user back to SetUpActivity
    // If user is null then send them to the login screen
    @Override
    protected void onStart() {
        super.onStart();
        // If the current user is equal to null then call login method
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity(); // call method
        }
        else{
            CheckUserExistence();
        }
    }

    // Check the database to see is there is personal info stored in the database.  If there isn't
    // then send them back to the SetUp screen.  In case the user sets up an account and then
    // closes the app without filling out personal info.  Then when they return to the app it will
    // send them back to the setup screen before it will allow them to use the app.
    private void CheckUserExistence(){
        final String current_User_Id = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_User_Id)){ // does user have a user ID in the database?
                    // there is no user ID in the database then send user to setup database info "name, country, gender, etc..."
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Send the user to setup their personal info for database
    private void SendUserToSetupActivity(){
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    // Sends user to the Login Screen
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }

    // The hamburger on the top left makes the drawer slide out from the left
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // The menu that slides out from the left.  This handles the action when each of the items is pressed.
    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_post:
                SendUserToPostActivity();
                Toast.makeText(this,"Add New Post", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_profile:
                Toast.makeText(this,"Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this,"Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this,"Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this,"Find Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this,"Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this,"Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
                Toast.makeText(this,"Bye", Toast.LENGTH_SHORT).show();
                SendUserToLoginActivity();
                break;
        }
    }


}
