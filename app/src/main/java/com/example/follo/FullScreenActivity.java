package com.example.follo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

// This was my first Zoom attempt.  I am no longer using this class
// FullScreenZoom is the one that is in use now

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
