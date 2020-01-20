package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private CircleImageView userProfileImage;
    private Button sendFriendRequestioButton, declineFriendRequestionButton;

    private DatabaseReference friendRequestRef, usersRef;
    private FirebaseAuth mAuth;

    private String senderUserId, receiverUserId, CURRENT_STATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("postKey").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        InitializeFields();


        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    // Display this stuff to the screen
                    userName.setText("@"+ myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB: " + myDOB);
                    userCountry.setText("Country: "+ myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelation.setText("Relationship Status: " + myRelationStatus);

                    MaintenanceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequestionButton.setVisibility(View.INVISIBLE);
        declineFriendRequestionButton.setEnabled(false);

        // if the sender and the receiver are the same then disable the send Friend button
        if(!senderUserId.equals(receiverUserId)){

            sendFriendRequestioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestioButton.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){
                        SendFriendRequestToAPerson();
                    }
                }
            });
        }
        else{
            declineFriendRequestionButton.setVisibility(View.INVISIBLE);
            sendFriendRequestioButton.setVisibility(View.INVISIBLE);
        }

    }




    //################ This is where the supporting methods start ###########################################


    private void MaintenanceOfButtons() {

        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                sendFriendRequestioButton.setText("Cancel Friend Request");

                                declineFriendRequestionButton.setVisibility(View.INVISIBLE);
                                declineFriendRequestionButton.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    private void SendFriendRequestToAPerson() {
        friendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFriendRequestioButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendFriendRequestioButton.setText("Cancel Friend Request");

                                                declineFriendRequestionButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestionButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void InitializeFields() {
        userName = (TextView)  findViewById(R.id.person_username);
        userProfName = (TextView)  findViewById(R.id.person_full_name);
        userStatus = (TextView)  findViewById(R.id.person_profile_status);
        userCountry = (TextView)  findViewById(R.id.person_country);
        userGender = (TextView)  findViewById(R.id.person_gender);
        userRelation = (TextView)  findViewById(R.id.person_relationship_status);
        userDOB = (TextView)  findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);


        sendFriendRequestioButton = (Button) findViewById(R.id.person_send_friend_request);
        declineFriendRequestionButton = (Button) findViewById(R.id.person_decline_friend_request);

        CURRENT_STATE = "not_friends";
    }
}
