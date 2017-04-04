package com.maxcriser.cards.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.maxcriser.cards.R;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoView extends AppCompatActivity {

    private static final int MAXIMUM_SCALE = 20;
    private static final String IMAGE_PNG = "image/png";
    private static final String BITMAP_IMAGE = "BitmapImage";
    ImageView mImageView;
    PhotoViewAttacher mAttacher;
    String bitmapPath;

    public void onBackClicked(final View view) {
        onBackPressed();
    }

    public void onShareClicked(final View view) {
        if (bitmapPath != null) {
            final Uri bmpUri = Uri.parse(bitmapPath);
            final Intent bitmapIntent = new Intent(android.content.Intent.ACTION_SEND);
            bitmapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            bitmapIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            bitmapIntent.setType(IMAGE_PNG);
            startActivity(bitmapIntent);
        }
    }

    public void onInfoClicked(final View view) {
        if (bitmapPath != null) {
            // show information
        }
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        mImageView = (ImageView) findViewById(R.id.iv_photo);
        final Intent intent = getIntent();
        bitmapPath = intent.getStringExtra(BITMAP_IMAGE);
        Picasso.with(this).load(Uri.parse(bitmapPath)).into(mImageView);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setMaximumScale(MAXIMUM_SCALE);
        mAttacher.update();
    }
}
