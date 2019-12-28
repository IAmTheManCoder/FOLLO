package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Set;

public class SetupActivity extends AppCompatActivity {

    private EditText username,fullName, countryName;
    private Button saveInformationButton;
    private CircleImageView profileImage;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference userRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);


        username = (EditText) findViewById(R.id.setup_username);
        fullName = (EditText) findViewById(R.id.setup_full_name);
        countryName = (EditText) findViewById(R.id.setup_country_name);
        saveInformationButton = (Button) findViewById(R.id.setup_information_button);
        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });
    }

    private void SaveAccountSetupInformation() {
        String usernam = username.getText().toString();
        String fullnam = fullName.getText().toString();
        String country = countryName.getText().toString();

        if(TextUtils.isEmpty(usernam)){
            Toast.makeText(this, "Please Write Your username...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullnam)){
            Toast.makeText(this, "Please Write Your Full Name...", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please Write Your County...", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please Wait While We Are Creating Your New Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true); // if user presses outside will not disappear until error occurred

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
                    if(task.isSuccessful()){
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account Has Been Created Successfully.", Toast.LENGTH_LONG).show();
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

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
