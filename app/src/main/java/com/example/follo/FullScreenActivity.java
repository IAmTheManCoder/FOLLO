package com.example.follo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

public class FullScreenActivity extends AppCompatActivity {

    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        ImageView fullScreenImageView = (ImageView) findViewById(R.id.fullScreenView);

        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            imageUri = callingActivityIntent.getData();
            if (imageUri != null && fullScreenImageView != null) {
                Picasso.get().load(imageUri).into(fullScreenImageView);
            }
        }
        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
                Picasso.get().load(imageUri).into(photoView);
            }
        });
    }
}
