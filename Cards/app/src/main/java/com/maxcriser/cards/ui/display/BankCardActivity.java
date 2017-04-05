package com.maxcriser.cards.ui.display;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.maxcriser.cards.Core;
import com.maxcriser.cards.R;
import com.maxcriser.cards.async.OwnAsyncTask;
import com.maxcriser.cards.async.task.RemovePhoto;
import com.maxcriser.cards.constant.ListConstants;
import com.maxcriser.cards.database.DatabaseHelper;
import com.maxcriser.cards.database.models.ModelBankCards;
import com.maxcriser.cards.date.DateToView;
import com.maxcriser.cards.dialog.ImageViewerDialogBuilder;
import com.maxcriser.cards.ui.activities.PhotoView;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_BACK_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_BANK;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_CARDHOLDER;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_FRONT_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_ID;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_NUMBER;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_PIN;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_TYPE;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_VALID;
import static com.maxcriser.cards.constant.Extras.EXTRA_VERIFICATION_NUMBER_BANK;

public class BankCardActivity extends Activity {

    private static final String BITMAP_IMAGE = "BitmapImage";
    private final int DELAY_MILLIS = 300;
    private FloatingActionMenu materialDesignFAM;
    private LinearLayout editLinear;
    private ScrollView mScrollView;
    private final Calendar calendar = Calendar.getInstance();
    String firstPhoto;
    String secondPhoto;
    private Boolean showPin = false;
    private OwnAsyncTask sync;
    private TextView editBank;
    private EditText editName;
    private String editNameString;
    private TextView valid;
    private LinearLayout linearFrameAction;
    private String idItem;
    private EditText editPin;
    private ImageView eye;
    private DatabaseHelper dbHelper;
    private Animation animScaleDown;
    private Animation animScaleUp;
    EditText editVerificationNumber;
    EditText editCardholder;
    EditText editNumber;
    ImageView ivFrontPhoto;
    ImageView ivBackPhoto;
    private Intent mCreditIntent;
    String mBank;
    private String mVerNumber;
    private String mCardholder;
    private String mNumber;
    private String mPin;
    private String mValid;
    private String mType;
    private ImageView mEditType;
    private FloatingActionButton mFloatingActionButtonDelete;
    private FloatingActionButton mFloatingActionButtonEdit;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bank);
        findViewById(R.id.search_image_toolbar).setVisibility(GONE);
        sync = new OwnAsyncTask();
        initViews();
    }

    DatePickerDialog.OnDateSetListener dateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(final DatePicker view, final int year, final int month, final int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            DateToView.setDateToCreditView(valid, calendar);
        }
    };

    public void onDateClicked(final View view) {
        final DatePickerDialog tpd = new DatePickerDialog(this, dateCallBack,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        tpd.show();
    }

    private void initViews() {
        mCreditIntent = getIntent();
        idItem = mCreditIntent.getStringExtra(EXTRA_BANK_ID);
        mBank = mCreditIntent.getStringExtra(EXTRA_BANK_BANK);
        mVerNumber = mCreditIntent.getStringExtra(EXTRA_VERIFICATION_NUMBER_BANK);
        mCardholder = mCreditIntent.getStringExtra(EXTRA_BANK_CARDHOLDER);
        mNumber = mCreditIntent.getStringExtra(EXTRA_BANK_NUMBER);
        mPin = mCreditIntent.getStringExtra(EXTRA_BANK_PIN);
        mValid = mCreditIntent.getStringExtra(EXTRA_BANK_VALID);
        mType = mCreditIntent.getStringExtra(EXTRA_BANK_TYPE);
        firstPhoto = mCreditIntent.getStringExtra(EXTRA_BANK_FRONT_PHOTO);
        secondPhoto = mCreditIntent.getStringExtra(EXTRA_BANK_BACK_PHOTO);
        valid = (TextView) findViewById(R.id.date);
        editVerificationNumber = (EditText) findViewById(R.id.ver_number);
        editBank = (TextView) findViewById(R.id.title_show_discount);
        editCardholder = (EditText) findViewById(R.id.cardholder);
        mEditType = (ImageView) findViewById(R.id.type_card);
        editNumber = (EditText) findViewById(R.id.number);
        editPin = (EditText) findViewById(R.id.pin);
        editLinear = (LinearLayout) findViewById(R.id.linear_edit_frame_title_discount);
        editName = (EditText) findViewById(R.id.rename_discount_title);

        setViews();

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        ivFrontPhoto = (ImageView) findViewById(R.id.front_photo);
        ivBackPhoto = (ImageView) findViewById(R.id.back_photo);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        eye = (ImageView) findViewById(R.id.eye);
        mFloatingActionButtonDelete = (FloatingActionButton) findViewById(R.id.floating_delete_button);
        mFloatingActionButtonEdit = (FloatingActionButton) findViewById(R.id.floating_edit_button);
        linearFrameAction = (LinearLayout) findViewById(R.id.linear_frame_actions_discount);
        animScaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down_floating);
        animScaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up_floating);

        registerForContextMenu(materialDesignFAM);

        dbHelper = ((Core) getApplication()).getDatabaseHelper(this);

        Picasso.with(this).load(Uri.parse(firstPhoto)).placeholder(R.drawable.camera_card_size_light).into(ivFrontPhoto);
        Picasso.with(this).load(Uri.parse(secondPhoto)).placeholder(R.drawable.camera_card_size_light).into(ivBackPhoto);

        mFloatingActionButtonDelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BankCardActivity.this);
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
                                dbHelper.delete(ModelBankCards.class, null, ModelBankCards.ID + " = ?", String.valueOf(idItem));
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
                // newer
                editCardholder.setEnabled(true);
                editNumber.setEnabled(true);
                editPin.setEnabled(true);
                valid.setClickable(true);
                editVerificationNumber.setEnabled(true);
                // end newer
                mScrollView.fullScroll(ScrollView.FOCUS_UP);
                editLinear.setVisibility(VISIBLE);
                editBank.setVisibility(GONE);
                linearFrameAction.setVisibility(VISIBLE);
                editNameString = editBank.getText().toString();
                editName.setText(editNameString);
                materialDesignFAM.close(true);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        materialDesignFAM.startAnimation(animScaleDown);
                        materialDesignFAM.setVisibility(GONE);
                    }
                }, DELAY_MILLIS);
            }
        });
    }

    private void setViews() {
        editBank.setText(mBank);
        editCardholder.setText(mCardholder);
        editNumber.setText(mNumber);
        editPin.setText(mPin);
        editVerificationNumber.setText(mVerNumber);
        valid.setText(mValid);
        final Integer typeID;
        switch (mType) {
            case ListConstants.Cards.VISA:
                typeID = R.drawable.type_visa;
                break;
            case ListConstants.Cards.MASTERCARD:
                typeID = R.drawable.type_mastercard;
                break;
            case ListConstants.Cards.AMEX:
                typeID = R.drawable.type_amex;
                break;
            case ListConstants.Cards.MAESTRO:
                typeID = R.drawable.type_maestro;
                break;
            case ListConstants.Cards.WESTERN_UNION:
                typeID = R.drawable.type_western_union;
                break;
            case ListConstants.Cards.JCB:
                typeID = R.drawable.type_jcb;
                break;
            case ListConstants.Cards.DINERS_CLUB:
                typeID = R.drawable.type_diners_club;
                break;
            default:
                typeID = R.drawable.type_belcard;
                break;
        }
        mEditType.setBackgroundResource(typeID);
    }

    public void onBackClicked(final View view) {
        super.onBackPressed();
    }

    public void onCancelClicked(final View view) {
        setViews();
        valid.setClickable(false);
        editCardholder.setEnabled(false);
        editBank.setEnabled(false);
        editNumber.setEnabled(false);
        editPin.setEnabled(false);
        editVerificationNumber.setEnabled(false);
        editLinear.setVisibility(GONE);
        editBank.setVisibility(VISIBLE);
        linearFrameAction.setVisibility(GONE);
        materialDesignFAM.setVisibility(VISIBLE);
        materialDesignFAM.startAnimation(animScaleUp);
    }

    public void onCreateCardClicked(final View view) {
        editName.requestFocus();
        mBank = editName.getText().toString();
        mCardholder = editCardholder.getText().toString();
        mNumber = editNumber.getText().toString();
        mPin = editPin.getText().toString();
        mValid = valid.getText().toString();
        mVerNumber = editVerificationNumber.getText().toString();
        if (!mBank.isEmpty() && !mCardholder.isEmpty() && !mNumber.isEmpty()
                && !mPin.isEmpty() && !mValid.isEmpty() && !mVerNumber.isEmpty()) {
            editBank.setText(mBank);
            editLinear.setVisibility(GONE);
            editBank.setVisibility(VISIBLE);
            linearFrameAction.setVisibility(GONE);
            materialDesignFAM.setVisibility(VISIBLE);
            materialDesignFAM.startAnimation(animScaleUp);

            dbHelper.edit(ModelBankCards.class, ModelBankCards.TITLE,
                    mBank, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.CARDHOLDER,
                    mCardholder, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.NUMBER,
                    mNumber, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.PIN,
                    mPin, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.VALID,
                    mValid, ModelBankCards.ID, String.valueOf(idItem), null);
            dbHelper.edit(ModelBankCards.class, ModelBankCards.VERIFICATION_NUMBER,
                    mVerNumber, ModelBankCards.ID, String.valueOf(idItem), null);
            editCardholder.setEnabled(false);
            editNumber.setEnabled(false);
            editPin.setEnabled(false);
            valid.setClickable(false);
            editVerificationNumber.setEnabled(false);
        } else {
            mScrollView.fullScroll(ScrollView.FOCUS_UP);
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_LONG).show();
        }
    }

    public void onToolbarBackClicked(final View view) {
    }

    public void onShowPinClicked(final View view) {
        showPin = !showPin;
        if (showPin) {
            editPin.setInputType(TYPE_TEXT_VARIATION_PASSWORD);
            eye.setImageResource(R.drawable.eye_off);
        } else {
            editPin.setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
            eye.setImageResource(R.drawable.eye_on);
        }
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