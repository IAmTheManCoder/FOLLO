package com.example.follo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

// This is the Full screen activity in use
public class FullScreenZoom extends AppCompatActivity {

    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_zoom);

        // assign the pic to a reference
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);

        // this is the code that is designed for ChrisBanes Photoviewer
        Intent callingActivityIntent = getIntent();
        if (callingActivityIntent != null) {
            imageUri = callingActivityIntent.getData();
            if (imageUri != null && photoView != null) {
                Picasso.get().load(imageUri).into(photoView);
            }
        }
    }
}
