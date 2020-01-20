package com.example.follo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ResetPasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button resetPasswordSendEmailButton;
    private EditText resetEmailInput;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_possword);

        mToolbar = (Toolbar) findViewById(R.id.forot_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        resetPasswordSendEmailButton = (Button) findViewById(R.id.reset_password_email_button);
        resetEmailInput = (EditText) findViewById(R.id.reset_password_email);

        resetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = resetEmailInput.getText().toString();

                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPasswordActivity.this, "Please Write A Valid Email Address First...", Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "Please Check Your Email Account...", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            }
                            else {
                                String message = task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "ERROR OCCURRED..." + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
}

