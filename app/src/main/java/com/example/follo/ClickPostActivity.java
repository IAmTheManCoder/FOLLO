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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class ClickPostActivity extends AppCompatActivity {

    private static final String TAG = "";
    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton, editPostButton;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String postKey, currentUserId, databaseUserId, description, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postKey = getIntent().getExtras().get("postKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);


        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        deletePostButton = (Button) findViewById(R.id.delete_post_button);
        editPostButton = (Button) findViewById(R.id.edit_post_button);

        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserId = dataSnapshot.child("uid").getValue().toString();

                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);
                    // if the current user is the originator of the post than show edit and delete buttons
                    if(currentUserId.equals(databaseUserId)){
                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);

                    }
                    editPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditCurrentPost(description);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deletePostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
                builder.setMessage("Are You Sure You Want To Delete This Post?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteCurrentPost();
                    }
                })
                        .setNegativeButton("Cancel", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(ClickPostActivity.this, FullScreenActivity.class);
                fullScreenIntent.setData(Uri.parse(image));
                startActivity(fullScreenIntent);

            }
        });
    }



    private void EditCurrentPost(String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeleteCurrentPost() {
        clickPostRef.removeValue();
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
