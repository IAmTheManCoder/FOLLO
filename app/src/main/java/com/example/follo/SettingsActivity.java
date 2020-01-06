package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText userName, userProfName, userStatus, userCountry, userGender, userRelation, userDOB;
    private Button updateAccountSettingsButton;
    private CircleImageView userProfImage;
    private ProgressDialog loadingBar;
    private DatabaseReference settingsUserRef;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImageRef;


    private String currentUserId;
    final static int Gallery_Pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        settingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Account Settings");// this is the back arrow bar

        userName = (EditText)  findViewById(R.id.settings_username);
        userProfName = (EditText)  findViewById(R.id.settings_profile_full_name);
        userStatus = (EditText)  findViewById(R.id.settings_status);
        userCountry = (EditText)  findViewById(R.id.settings_profile_country);
        userGender = (EditText)  findViewById(R.id.settings_gender);
        userRelation = (EditText)  findViewById(R.id.settings_relationship_status);
        userDOB = (EditText)  findViewById(R.id.settings_profile_dob);
        userProfImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        updateAccountSettingsButton = (Button) findViewById(R.id.update_account_settings_button);

        settingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText(myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText(myDOB);
                    userCountry.setText(myCountry);
                    userGender.setText(myGender);
                    userRelation.setText(myRelationStatus);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        updateAccountSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
            }
        });

        userProfImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // some conditions for the picture
        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();
            // crop the image
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        // Get the cropped image
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {       // store the cropped image into result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                // Get the result of the image and store it in resultUri
                Uri resultUri = result.getUri();
                // Get a reference to the storage in Firebase.  Its a filepath to the Firebase Storage
                // Create a child and store to the user with a file type .jpg
                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");
                // Store the cropped image "resultUri" into the profile image's folder
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                settingsUserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            //Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            //startActivity(selfIntent);
                                            Toast.makeText(SettingsActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }

                        });

                    }

                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void ValidateAccountInfo() {

        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please Write Your Username...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(profilename)){
            Toast.makeText(this, "Please Write Your Profile Name...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please Write Your Status...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please Write Your Date Of Birth...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please Write Your Country...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please Write Your Gender...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation)){
            Toast.makeText(this, "Please Write Your Relationship Status...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            updateAccountInfo(username, profilename, status, dob, country, gender, relation);
        }

    }

    private void updateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation) {
        HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", profilename  );
            userMap.put("status", status);
            userMap.put("dob", dob);
            userMap.put("country", country);
            userMap.put("gender", gender);
            userMap.put("relationshipstatus", relation);
            settingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SettingsActivity.this, "Account Settings Updated Successfully.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else{
                        Toast.makeText(SettingsActivity.this, "Error Occurred When Updating Account Settings Information", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
    }

    private void SendUserToMainActivity(){
        Intent mainInent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(mainInent);

    }

}
