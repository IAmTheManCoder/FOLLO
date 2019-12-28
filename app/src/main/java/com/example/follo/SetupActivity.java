package com.example.follo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

public class SetupActivity extends AppCompatActivity {

    // Initialize variables
    private EditText username,fullName, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference userRef;
    private StorageReference userProfileImaageRef;

    String currentUserId;
    final static int gallery_Pick = 1;

    // Start Activity############################################################################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // setup Database variables
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileImaageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        // assign all of the text spaces, button, and profile pic to memory ref names
        username = (EditText) findViewById(R.id.setup_username);
        fullName = (EditText) findViewById(R.id.setup_full_name);
        countryName = (EditText) findViewById(R.id.setup_country_name);
        saveInformationButton = (Button) findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        // Click Listener for the Save button
        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, gallery_Pick);
            }
        });
    }

    //This is where the supporting methods start ####################################################


    @Override
   protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==gallery_Pick && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please Wait While We Are Updating Your Profile Image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true); // if user presses outside will not disappear until error occurred

                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImaageRef.child(currentUserId+ ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Profile Image Stored Successfully to Firebase...", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();

                            userRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile Image Stored To Firebase Successfully", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else{
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "ERROR Occurred " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }
                    }
                });
            }
            else{
                Toast.makeText(SetupActivity.this, "ERROR Occurred: Image Can't Be Cropped. TRY AGAIN. ", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    // When the Save button is pressed do some stuff.
    private void SaveAccountSetupInformation() {
        String usernam = username.getText().toString();
        String fullnam = fullName.getText().toString();
        String country = countryName.getText().toString();

        // if any of the text windows are empty show messages
        if(TextUtils.isEmpty(usernam)){
            Toast.makeText(this, "Please Write Your username...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullnam)){
            Toast.makeText(this, "Please Write Your Full Name...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please Write Your County...", Toast.LENGTH_SHORT).show();
        }
        // if the text windows are all full then do some stuff
        else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please Wait While We Are Creating Your New Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true); // if user presses outside will not disappear until error occurred

            // Temp HashMap to send some stuff to database when save button is pressed
            HashMap userMap = new HashMap();
            userMap.put("username", usernam);
            userMap.put("fullname", fullnam);
            userMap.put("country", country);
            userMap.put("status", "Hey there, I am using FOLLO and this app ROCKS!!!!");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");
            userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){ // if success then send user to main activity
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account Has Been Created Successfully.", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else{
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "ERROR Occurred " + message, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }

    // Sends user to main activity if the personal info is captured and stored to database correctly
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
