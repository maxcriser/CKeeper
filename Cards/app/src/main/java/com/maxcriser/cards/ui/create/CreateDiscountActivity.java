package com.maxcriser.cards.ui.create;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.maxcriser.cards.Core;
import com.maxcriser.cards.R;
import com.maxcriser.cards.async.OnResultCallback;
import com.maxcriser.cards.async.OwnAsyncTask;
import com.maxcriser.cards.async.task.BarcodeConverter;
import com.maxcriser.cards.async.task.RemovePhoto;
import com.maxcriser.cards.constant.Extras;
import com.maxcriser.cards.constant.ListConstants;
import com.maxcriser.cards.database.DatabaseHelper;
import com.maxcriser.cards.database.models.ModelDiscountCards;
import com.maxcriser.cards.ui.activities.PhotoEditorActivity;
import com.maxcriser.cards.utils.UniqueStringGenerator;
import com.maxcriser.cards.view.labels.RobotoRegular;
import com.squareup.picasso.Picasso;

import java.io.File;

import static android.view.View.GONE;
import static com.maxcriser.cards.constant.ListConstants.Requests.CAPTURE_IMAGE_BACK;
import static com.maxcriser.cards.constant.ListConstants.Requests.CAPTURE_IMAGE_FRONT;
import static com.maxcriser.cards.constant.ListConstants.Requests.EDIT_IMAGE_BACK;
import static com.maxcriser.cards.constant.ListConstants.Requests.EDIT_IMAGE_FRONT;
import static com.maxcriser.cards.constant.ListConstants.Requests.REQUEST_BACK_CAMERA;
import static com.maxcriser.cards.constant.ListConstants.Requests.REQUEST_FRONT_CAMERA;
import static com.maxcriser.cards.constant.ListConstants.Requests.REQUEST_WRITE_STORAGE_BACK;
import static com.maxcriser.cards.constant.ListConstants.Requests.REQUEST_WRITE_STORAGE_FRONT;
import static com.maxcriser.cards.constant.ListConstants.TAG_BARCODE;
import static com.maxcriser.cards.manager.StorageManager.isExternalStorageAvailable;

public class CreateDiscountActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText nameEditText;
    private String generateBarcode;
    public String photoFileNameFront;
    public String photoFileNameBack;
    private Uri editFrontUri;
    private Uri editBackUri;
    private ImageView frontPhoto;
    private ImageView backPhoto;
    private FrameLayout removeFront;
    private FrameLayout removeBack;
    private ScrollView mScrollView;
    private TextInputLayout nameLayout;
    private boolean statusSave;
    private OwnAsyncTask sync;

    public void onRemoveFrontClicked(final View view) {
        frontPhoto.setImageResource(R.drawable.camera_card_size);
        frontPhoto.setClickable(true);
        removeFront.setVisibility(GONE);
    }

    private boolean validateField(final EditText view, final TextInputLayout viewLayout) {
        if (view.getText().toString().isEmpty()) {
            viewLayout.setError("You must fill this field");
            requestFocus(view);
            return false;
        } else {
            viewLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(final View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void onRemoveBackClicked(final View view) {
        backPhoto.setImageResource(R.drawable.camera_card_size);
        backPhoto.setClickable(true);
        removeBack.setVisibility(GONE);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_discount);
        findViewById(R.id.search_image_toolbar).setVisibility(GONE);
        sync = new OwnAsyncTask();
        initViews();
    }

    @Override
    protected void onDestroy() {
        if (!statusSave) {
            if (editBackUri != null) {
                sync.execute(new RemovePhoto(), editBackUri, null);
            }
            if (editFrontUri != null) {
                sync.execute(new RemovePhoto(), editFrontUri, null);
            }
        }
        super.onDestroy();
    }

    private void initViews() {
        nameLayout = (TextInputLayout) findViewById(R.id.layout_name);
        final String uniqueString = UniqueStringGenerator.getUniqueString();
        photoFileNameFront = ListConstants.BEG_FILE_NAME_DISCOUNT + uniqueString + "front_photo.jpg";
        photoFileNameBack = ListConstants.BEG_FILE_NAME_DISCOUNT + uniqueString + "back_photo.jpg";
        frontPhoto = (ImageView) findViewById(R.id.front_photo);
        backPhoto = (ImageView) findViewById(R.id.back_photo);
        removeBack = (FrameLayout) findViewById(R.id.remove_back);
        removeFront = (FrameLayout) findViewById(R.id.remove_front);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        final RobotoRegular title = (RobotoRegular) findViewById(R.id.title_toolbar);
        nameEditText = (EditText) findViewById(R.id.id_edit_text_name_discount);
        db = ((Core) getApplication()).getDatabaseHelper(this);
        title.setText(getResources().getString(R.string.new_discount_title));
        final Intent barcodeIntent = getIntent();
        final String barcode = barcodeIntent.getStringExtra(TAG_BARCODE);
        final OwnAsyncTask barcodeGenerator = new OwnAsyncTask();

        barcodeGenerator.execute(new BarcodeConverter(barcode), null, new OnResultCallback<String, Void>() {

            @Override
            public void onSuccess(final String pS) {
                if (pS == null) {
                    Toast.makeText(CreateDiscountActivity.this, R.string.cannot_convert_barcode, Toast.LENGTH_SHORT).show();
                    generateBarcode = barcode;
                } else {
                    generateBarcode = pS;
                }
            }

            @Override
            public void onError(final Exception pE) {
                Toast.makeText(CreateDiscountActivity.this, R.string.cannot_convert_barcode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressChanged(final Void pVoid) {

            }
        });
    }

    public void onBackClicked(final View view) {
        super.onBackPressed();
    }

    public void onCreateCardClicked(final View view) {
        final String titleStr = nameEditText.getText().toString();
        if (!validateField(nameEditText, nameLayout)) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_LONG).show();
        } else if (removeFront.getVisibility() == View.GONE || removeBack.getVisibility() == View.GONE) {
            Toast.makeText(this, R.string.must_make_card_images, Toast.LENGTH_LONG).show();
        } else {
            final ContentValues cvNewDiscount = new ContentValues();
            cvNewDiscount.put(ModelDiscountCards.TITLE, titleStr);
            cvNewDiscount.put(ModelDiscountCards.BARCODE, generateBarcode);
            cvNewDiscount.put(ModelDiscountCards.PHOTO_FRONT, editFrontUri.toString());
            cvNewDiscount.put(ModelDiscountCards.PHOTO_BACK, editBackUri.toString());
            cvNewDiscount.put(ModelDiscountCards.ID, (Integer) null);

            db.insert(ModelDiscountCards.class, cvNewDiscount, new OnResultCallback<Long, Void>() {

                @Override
                public void onSuccess(final Long pLong) {
                    statusSave = true;
                    onBackClicked(null);
                }

                @Override
                public void onError(final Exception pE) {
                    Toast.makeText(CreateDiscountActivity.this, R.string.cannot_insert_card_to_database,
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onProgressChanged(final Void pVoid) {
                }
            });
        }
    }

    public void onToolbarBackClicked(final View view) {
    }

    public void onCancelClicked(final View view) {
        onBackClicked(null);
    }

    public void onBackPhotoClicked(final View view) {
        getPermission(REQUEST_BACK_CAMERA, Manifest.permission.CAMERA);
    }

    public void onFrontPhotoClicked(final View view) {
        getPermission(REQUEST_FRONT_CAMERA, Manifest.permission.CAMERA);
    }

    @TargetApi(23)
    private void getPermission(final byte CODE, final String PERMISSION) {
        if (ContextCompat.checkSelfPermission(this, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, CODE);
        } else {
            if (CODE == REQUEST_FRONT_CAMERA) {
                getPermission(REQUEST_WRITE_STORAGE_FRONT, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else if (CODE == REQUEST_BACK_CAMERA) {
                getPermission(REQUEST_WRITE_STORAGE_BACK, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else if (CODE == REQUEST_WRITE_STORAGE_FRONT) {
                startCameraForPhoto(CAPTURE_IMAGE_FRONT, photoFileNameFront);
            } else if (CODE == REQUEST_WRITE_STORAGE_BACK) {
                startCameraForPhoto(CAPTURE_IMAGE_BACK, photoFileNameBack);
            }
        }
    }

    private void startCameraForPhoto(final int code, final String fileName) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUri(fileName));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, code);
        }
    }

    public Uri getUri(final String fileName) {
        if (isExternalStorageAvailable()) {
            final File mediaStorageDir = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES), ListConstants.APP_TAG);
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Toast.makeText(this, ListConstants.APP_TAG + getString(R.string.filed_to_create_directory),
                        Toast.LENGTH_LONG).show();
            }
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == CAPTURE_IMAGE_FRONT ||
                requestCode == CAPTURE_IMAGE_BACK) {
            if (resultCode == RESULT_OK) {
                if (requestCode == CAPTURE_IMAGE_FRONT) {
                    final Uri takenPhotoUri = getUri(photoFileNameFront);
                    final Intent intent = new Intent(this, PhotoEditorActivity.class);
                    intent.putExtra(Extras.EXTRA_URI, takenPhotoUri.toString());
                    intent.putExtra(ListConstants.STATUS_PHOTOEDITOR, ListConstants.STATUS_PHOTOEEDITOR_CREDIT_CARD);
                    startActivityForResult(intent, EDIT_IMAGE_FRONT);
                } else {
                    final Uri takenPhotoUri = getUri(photoFileNameBack);
                    final Intent intent = new Intent(this, PhotoEditorActivity.class);
                    intent.putExtra(ListConstants.STATUS_PHOTOEDITOR, ListConstants.STATUS_PHOTOEEDITOR_CREDIT_CARD);
                    intent.putExtra(Extras.EXTRA_URI, takenPhotoUri.toString());
                    startActivityForResult(intent, EDIT_IMAGE_BACK);
                }
            } else {
                Toast.makeText(this, R.string.picture_wasnt_taken, Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == RESULT_OK) {
            if (requestCode == EDIT_IMAGE_FRONT) {
                editFrontUri = Uri.parse(data.getStringExtra(Extras.EXTRA_URI));
                Picasso.with(this).load(editFrontUri).placeholder(R.drawable.placeholder_wait_load).into(frontPhoto);
                /*
                Picasso.with(this).load(editFrontUri).into(new Target() {

                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        frontPhoto.setImageBitmap(bitmap);
                        removeFront.setVisibility(View.VISIBLE);
                        frontPhoto.setClickable(false);
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        Toast.makeText(CreateDiscountActivity.this, "Error load bitmap", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {

                    }
                });
                */
                removeFront.setVisibility(View.VISIBLE);
                frontPhoto.setClickable(false);
            } else if (requestCode == EDIT_IMAGE_BACK) {
                editBackUri = Uri.parse(data.getStringExtra(Extras.EXTRA_URI));
                Picasso.with(this).load(editBackUri).placeholder(R.drawable.placeholder_wait_load).into(backPhoto);
                /*
                Picasso.with(this).load(editBackUri).into(new Target() {

                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                        backPhoto.setImageBitmap(bitmap);
                        removeBack.setVisibility(View.VISIBLE);
                        backPhoto.setClickable(false);
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        Toast.makeText(CreateDiscountActivity.this, "Error load bitmap", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {

                    }
                });
                */
                removeBack.setVisibility(View.VISIBLE);
                backPhoto.setClickable(false);
            }
        } else {
            Toast.makeText(this, R.string.picture_wasnt_edited, Toast.LENGTH_SHORT).show();
        }
    }

}