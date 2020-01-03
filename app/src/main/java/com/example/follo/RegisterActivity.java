package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    // initialize
    private EditText userEmail, userPassword, userConfirmationPassword;
    private Button createAccountButton;
    private ProgressDialog loadingBar;
    private ProgressBar loadBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Get instance of Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // assign the edit texts and create account button to some variable names
        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        userConfirmationPassword = (EditText) findViewById(R.id.register_confirm_password);
        createAccountButton = (Button) findViewById(R.id.register_create_account);
        loadingBar = new ProgressDialog(this);
        loadBar = new ProgressBar(this);

        // Create click listener for create account button
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    // gets the user input from the edit text boxes and creates account
    private void CreateNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmationPassword.getText().toString();
        // if email is empty show warning
        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please Write Your Email...", Toast.LENGTH_SHORT).show();
        }
        // if password is empty show warning
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Write Your Password...", Toast.LENGTH_SHORT).show();
        }
        // if confirm password is empty show warning
        else if (TextUtils.isEmpty(confirmPassword)){
            Toast.makeText(this, "Please Confirm Your Password...", Toast.LENGTH_SHORT).show();
        }
        // if password and confirmPassword do not match show warning
        else if(!password.equals(confirmPassword)){
                Toast.makeText(this, "Password and Confirm Password DO NOT MATCH...", Toast.LENGTH_SHORT).show();
            }
        // if instance comes back from Firebase with correct authentication than show Success message
        else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait While We Are Creating Your Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true); // if user presses outside will not disappear until error occurred

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task){

                            SendUserToSetupActivity(); // is successful send user to Setup Screen
                            if(task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Authentication Success...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                SendUserToLoginActivity();
                                String message = task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this, "ERROR Occurred:" + message + " Please Try Again...", Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }


                    });
        }

    }

    // After user creates account send user to set up screen
    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class); // in Register, send to Setup
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent); // activate intention
        finish(); // user is not allowed the register activity by pressing back button
    }

    // Send the user to the Register account screen
    private void SendUserToLoginActivity() {
        Intent registerIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(registerIntent);


    }
}
