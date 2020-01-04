package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // initialize variables
    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView needNewAccountLink;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Start the app.  This screen is called upon start up.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // assign the edit texts and button to some variables
        needNewAccountLink = (TextView) findViewById(R.id.register_account_link);
        userEmail = (EditText) findViewById(R.id.login_email);
        userPassword = (EditText) findViewById(R.id.login_password);
        loginButton = (Button) findViewById(R.id.login_button);
        loadingBar = new ProgressDialog(this);

        // if the user selects "don't have an account?" then send them to the register screen
        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
        // if the select the login button than call the Allowing User To Login method
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowingUserToLogin();
            }
        });
    }

    // This onStart method triggers when the app starts up.  If the current user logging in
    // has an account an is currently authenticated then send them right to the Main Activity
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            SendUserToMainActivity();
        }
    }

    // When the user inputs their login id and password and then presses the Log in button
    // gather the input in the text screen and attempt to authenticate with Firebase.
    private void AllowingUserToLogin(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)) { // if empty give warning
            Toast.makeText(this, "Please Write Your Email...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){ // if empty give warning
            Toast.makeText(this, "Please Write Your Password...", Toast.LENGTH_SHORT).show();
        }
        // start the login bar
        else {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please Wait While We Log You In...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true); // if user presses outside will not disappear until error occurred

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){ // if user authenticates then send them to Main Activity
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "You Have Logged In Successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{ // if user does not authenticate display warning message from Firebase
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "ERROR Occurred " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    // Send user to Main Activity
    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish(); // Once on the Main Activity if the back button is pressed then app will close

    }

    // Send the user to the Register account screen
    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);

    }
}
