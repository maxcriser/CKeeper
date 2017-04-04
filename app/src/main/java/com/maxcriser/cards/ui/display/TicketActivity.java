package com.maxcriser.cards.ui.display;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.maxcriser.cards.CoreApplication;
import com.maxcriser.cards.R;
import com.maxcriser.cards.async.OwnAsyncTask;
import com.maxcriser.cards.async.task.RemovePhoto;
import com.maxcriser.cards.database.DatabaseHelper;
import com.maxcriser.cards.database.models.ModelTickets;
import com.maxcriser.cards.date.DateToView;
import com.maxcriser.cards.dialog.ImageViewerDialogBuilder;
import com.maxcriser.cards.ui.activities.PhotoView;
import com.maxcriser.cards.view.labels.RobotoThin;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_CARDHOLDER;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_DATE;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_FIRST_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_ID;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_SECOND_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_TIME;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_TITLE;

public class TicketActivity extends Activity {

    private static final String BITMAP_IMAGE = "BitmapImage";
    private final int DELAY_MILLIS = 300;
    private FloatingActionMenu materialDesignFAM;
    private LinearLayout editLinear;
    private ScrollView mScrollView;
    private TextView editTitle;
    private EditText editName;
    private String editTitleStr;
    private LinearLayout linearFrameAction;
    private String idItem;
    private DatabaseHelper dbHelper;
    private Animation animScaleDown;
    private Animation animScaleUp;
    private OwnAsyncTask sync;
    private EditText editCardholder;
    private String firstPhoto;
    private String secondPhoto;
    private TextView date;
    private TextView time;
    private final Calendar calendar = Calendar.getInstance();
    private CardView mFrontCardPhoto;
    private CardView mBackCardPhoto;
    private Intent mCreditIntent;
    private String mTitleStr;
    private String mCardholderStr;
    private String mDateStr;
    private String mTimeStr;
    private ImageView mIvFrontPhoto;
    private ImageView mIvBackPhoto;
    private RobotoThin mDate;
    private RobotoThin mTime;
    private FloatingActionButton mFloatingActionButtonDelete;
    private FloatingActionButton mFloatingActionButtonEdit;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_ticket);
        findViewById(R.id.search_image_toolbar).setVisibility(GONE);
        sync = new OwnAsyncTask();
        initViews();
    }

    public void onDateClicked(final View view) {
        final DatePickerDialog tpd = new DatePickerDialog(this, dateCallBack,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        tpd.show();
    }

    public void onTimeClicked(final View view) {
        final TimePickerDialog tpd = new TimePickerDialog(this, timeCallBack,
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                true);
        tpd.show();
    }

    DatePickerDialog.OnDateSetListener dateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(final DatePicker view, final int year, final int month, final int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            DateToView.setDateToTicketView(date, calendar);
        }
    };

    TimePickerDialog.OnTimeSetListener timeCallBack = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(final TimePicker pTimePicker, final int hour, final int min) {
            calendar.set(Calendar.HOUR, hour);
            calendar.set(Calendar.MINUTE, min);
            DateToView.setTimeToView(time, calendar);
        }
    };

    private void initViews() {
        mFrontCardPhoto = (CardView) findViewById(R.id.firstCard);
        mBackCardPhoto = (CardView) findViewById(R.id.secondCard);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        mCreditIntent = getIntent();
        idItem = mCreditIntent.getStringExtra(EXTRA_TICKET_ID);
        mTitleStr = mCreditIntent.getStringExtra(EXTRA_TICKET_TITLE);
        mCardholderStr = mCreditIntent.getStringExtra(EXTRA_TICKET_CARDHOLDER);
        mDateStr = mCreditIntent.getStringExtra(EXTRA_TICKET_DATE);
        mTimeStr = mCreditIntent.getStringExtra(EXTRA_TICKET_TIME);
        firstPhoto = mCreditIntent.getStringExtra(EXTRA_TICKET_FIRST_PHOTO);
        secondPhoto = mCreditIntent.getStringExtra(EXTRA_TICKET_SECOND_PHOTO);

        mIvFrontPhoto = (ImageView) findViewById(R.id.front_photo);
        mIvBackPhoto = (ImageView) findViewById(R.id.back_photo);

        if (!firstPhoto.isEmpty()) {
            mFrontCardPhoto.setVisibility(VISIBLE);
            Picasso.with(this).load(Uri.parse(firstPhoto)).placeholder(R.drawable.camera_card_size_light).into(mIvFrontPhoto);
        }
        if (!secondPhoto.isEmpty()) {
            mBackCardPhoto.setVisibility(VISIBLE);
            Picasso.with(this).load(Uri.parse(secondPhoto)).placeholder(R.drawable.camera_card_size_light).into(mIvBackPhoto);
        }

        mDate = (RobotoThin) findViewById(R.id.date);
        mTime = (RobotoThin) findViewById(R.id.time);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        editTitle = (TextView) findViewById(R.id.title_show_discount);
        editCardholder = (EditText) findViewById(R.id.cardholder);
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        editLinear = (LinearLayout) findViewById(R.id.linear_edit_frame_title_discount);
        editName = (EditText) findViewById(R.id.rename_discount_title);
        mFloatingActionButtonDelete = (FloatingActionButton) findViewById(R.id.floating_delete_button);
        mFloatingActionButtonEdit = (FloatingActionButton) findViewById(R.id.floating_edit_button);
        linearFrameAction = (LinearLayout) findViewById(R.id.linear_frame_actions_discount);

        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down_floating);
        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up_floating);

        registerForContextMenu(materialDesignFAM);

        dbHelper = ((CoreApplication) getApplication()).getDatabaseHelper(this);

        mFloatingActionButtonDelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TicketActivity.this);
                alertDialogBuilder.setTitle(R.string.remove);
                alertDialogBuilder
                        .setMessage(R.string.are_you_sure_to_delete)
                        .setCancelable(false)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                dbHelper.delete(ModelTickets.class, null, ModelTickets.ID + " = ?", String.valueOf(idItem));
                                sync.execute(new RemovePhoto(), Uri.parse(firstPhoto), null);
                                sync.execute(new RemovePhoto(), Uri.parse(secondPhoto), null);
                                onBackClicked(null);
                            }
                        });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                materialDesignFAM.close(true);
            }
        });
        mFloatingActionButtonEdit.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
                editLinear.setVisibility(VISIBLE);
                editTitle.setVisibility(GONE);
                mDate.setClickable(true);
                mTime.setClickable(true);
                linearFrameAction.setVisibility(VISIBLE);
                editTitleStr = editTitle.getText().toString();
                editName.setText(editTitleStr);
                materialDesignFAM.close(true);
                editCardholder.setEnabled(true);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        materialDesignFAM.startAnimation(animScaleDown);
                        materialDesignFAM.setVisibility(GONE);
                    }
                }, DELAY_MILLIS);

            }
        });

        /*
        Picasso.with(this).load(Uri.parse(firstPhoto)).into(new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                ivFrontPhoto.setImageBitmap(bitmap);
                firstBitmap = bitmap;

            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {

            }
        });

        Picasso.with(this).load(Uri.parse(secondPhoto)).into(new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                ivBackPhoto.setImageBitmap(bitmap);
                secondBitmap = bitmap;
            }

            @Override
            public void onBitmapFailed(final Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(final Drawable placeHolderDrawable) {

            }
        });
        */
        setViews();
    }

    private void setViews() {
        editTitle.setText(mTitleStr);
        editCardholder.setText(mCardholderStr);
        mDate.setText(mDateStr);
        mTime.setText(mTimeStr);
    }

    public void onBackClicked(final View view) {
        super.onBackPressed();
    }

    public void onCancelClicked(final View view) {
        setViews();
        editCardholder.setEnabled(false);
        date.setClickable(false);
        time.setClickable(false);
        editLinear.setVisibility(GONE);
        editTitle.setVisibility(VISIBLE);
        linearFrameAction.setVisibility(GONE);
        materialDesignFAM.setVisibility(VISIBLE);
        materialDesignFAM.startAnimation(animScaleUp);
    }

    public void onCreateCardClicked(final View view) {
        editName.requestFocus();
        mTitleStr = editName.getText().toString();
        mCardholderStr = editCardholder.getText().toString();
        mDateStr = date.getText().toString();
        mTimeStr = time.getText().toString();
        if (!mTitleStr.isEmpty() && !mCardholderStr.isEmpty() && !mTimeStr.isEmpty() && !mDateStr.isEmpty()) {
            editTitle.setText(mTitleStr);
            editLinear.setVisibility(GONE);
            editTitle.setVisibility(VISIBLE);
            date.setClickable(false);
            time.setClickable(false);
            linearFrameAction.setVisibility(GONE);
            materialDesignFAM.setVisibility(VISIBLE);
            materialDesignFAM.startAnimation(animScaleUp);

            dbHelper.edit(ModelTickets.class, ModelTickets.TITLE, mTitleStr,
                    ModelTickets.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelTickets.class, ModelTickets.CARDHOLDER, mCardholderStr,
                    ModelTickets.ID, String.valueOf(idItem), null);
            editCardholder.setEnabled(false);
            dbHelper.edit(ModelTickets.class, ModelTickets.DATE, mDateStr,
                    ModelTickets.ID, String.valueOf(idItem), null);
            editCardholder.setEnabled(false);
            dbHelper.edit(ModelTickets.class, ModelTickets.TIME, mTimeStr,
                    ModelTickets.ID, String.valueOf(idItem), null);
            editCardholder.setEnabled(false);
        } else {
            mScrollView.fullScroll(ScrollView.FOCUS_UP);
            Toast.makeText(this, R.string.empty_card_name, Toast.LENGTH_LONG).show();
        }
    }

    public void onToolbarBackClicked(final View view) {

    }

    void showPhoto(final Bitmap bitmap) {
        final ImageViewerDialogBuilder dialog = new ImageViewerDialogBuilder(this, bitmap);
        dialog.startDialog();
    }

    public void onSecondPhotoClicked(final View view) {
        if (secondPhoto != null) {
//            ivBackPhoto.buildDrawingCache();
//            final Bitmap bitmap = ivBackPhoto.getDrawingCache();
            final Intent intent = new Intent(this, PhotoView.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(BITMAP_IMAGE, secondPhoto);
            startActivity(intent);
        }
    }

    public void onFirstPhotoClicked(final View view) {
        if (firstPhoto != null) {
            final Intent intent = new Intent(this, PhotoView.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.putExtra(BITMAP_IMAGE, firstPhoto);
            startActivity(intent);
        }
    }
}