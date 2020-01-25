package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

// This Activity is activated when the user clicks on a post.  It takes you to where you can
// edit or delete the post if you are the originator of the post.  Click on this Activity and
// it then moves to the FullScreen Activity.

public class ClickPostActivity extends AppCompatActivity {

    // initiate some variable names
    private static final String TAG = "";
    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton, editPostButton;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;
    private FirebaseStorage mFirebaseStorage;

    private String postKey, currentUserId, databaseUserId, description, image, storageKey;


    // This is where the Activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        // Firebase references
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        postKey = getIntent().getExtras().get("postKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);
        mFirebaseStorage = FirebaseStorage.getInstance().getReference().getStorage();

        // assign the layout fields to their variable names
        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        deletePostButton = (Button) findViewById(R.id.delete_post_button);
        editPostButton = (Button) findViewById(R.id.edit_post_button);

        // Make the delete and edit button invisible
        // The app will make them re-appear if the user is the author of the post
        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        // reference to "Posts".child(postKey) for snapshot or changes
        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    // Get the post description, post pic, and user id
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();

                    // load the image to the imageView and description to the textView
                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);

                    // if the current user is the originator of the post than show edit and delete buttons
                    if(currentUserId.equals(databaseUserId)){
                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);
                    }

                    // if you are the originator of the post then this button will edit the post text
                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(description); // call this method
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        // if you are the originator of the post then you can delete it.
        deletePostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Create the "Are you sure" dialog box
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
                builder.setMessage("Are You Sure You Want To Delete This Post?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteCurrentPost();// When the user selects "Delete" call this method
                    }
                })
                        .setNegativeButton("Cancel", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        // Click on the image and start a Full Screen Activity.  The Full Screen has the zoom feature.
        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(ClickPostActivity.this, FullScreenZoom.class);
                fullScreenIntent.setData(Uri.parse(image));
                startActivity(fullScreenIntent);

            }
        });
    }

    // ######################## This is where the supporting methods begin ##########################################

    // Grab the Description of the post and edit the text.
    private void EditCurrentPost(String description) {

        // Create the dialog box to edit the post's text field
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        // create the Update button in the dialog box
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // update the description of the post
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this," Post Updated Successfully...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        // Make the dialog window dark green
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }


    // Delete the current post by the post's unique random key and also remove the post image
    // From storage
    private void DeleteCurrentPost() {

        // remove the post by key and all its children
        clickPostRef.removeValue();

        // "image" has the url to the post image's place in storage so remove that as well
        StorageReference photoRef = mFirebaseStorage.getReferenceFromUrl(image);
        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ClickPostActivity.this, "Post Image Removed From Storage", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ClickPostActivity.this, "Error on Post Image Deletion from Strorage", Toast.LENGTH_SHORT).show();
            }
        });

        // Send the user back to the main Activity after deleting the post
        SendUserToMainActivity();
        Toast.makeText(this, "Post Has Been Deleted", Toast.LENGTH_SHORT).show();
    }

    // Send user to Main Activity
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish(); // Once on the Main Activity if the back button is pressed then app will close
    }
}
