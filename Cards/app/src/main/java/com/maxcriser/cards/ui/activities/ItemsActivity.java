package com.maxcriser.cards.ui.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maxcriser.cards.Core;
import com.maxcriser.cards.R;
import com.maxcriser.cards.anim.FlipAnimation;
import com.maxcriser.cards.async.OnResultCallback;
import com.maxcriser.cards.async.OwnAsyncTask;
import com.maxcriser.cards.async.task.RemovePhoto;
import com.maxcriser.cards.constant.ListConstants;
import com.maxcriser.cards.constant.ListPreview;
import com.maxcriser.cards.database.DatabaseHelper;
import com.maxcriser.cards.database.models.ModelBankCards;
import com.maxcriser.cards.database.models.ModelDiscountCards;
import com.maxcriser.cards.database.models.ModelNFCItems;
import com.maxcriser.cards.database.models.ModelTickets;
import com.maxcriser.cards.dialog.EnterNfcNameDialogBuilder;
import com.maxcriser.cards.dialog.NfcInputDialogBuilder;
import com.maxcriser.cards.dialog.NfcOutputDialogBuilder;
import com.maxcriser.cards.listener.RecyclerItemClickListener;
import com.maxcriser.cards.loader.CardsCursorLoader;
import com.maxcriser.cards.model.PreviewColor;
import com.maxcriser.cards.ui.adapter.CardCursorAdapter;
import com.maxcriser.cards.ui.create.CreateBankActivity;
import com.maxcriser.cards.ui.create.CreateTicketActivity;
import com.maxcriser.cards.ui.display.BankCardActivity;
import com.maxcriser.cards.ui.display.DiscountCardActivity;
import com.maxcriser.cards.ui.display.TicketActivity;
import com.maxcriser.cards.view.labels.RobotoRegular;

import java.io.IOException;
import java.nio.charset.Charset;

import static android.view.View.GONE;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_BACK_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_BANK;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_CARDHOLDER;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_FRONT_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_ID;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_NUMBER;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_PIN;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_TYPE;
import static com.maxcriser.cards.constant.Extras.EXTRA_BANK_VALID;
import static com.maxcriser.cards.constant.Extras.EXTRA_CHECK_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_BACK;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_BARCODE;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_FRONT;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_ID;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_TITLE;
import static com.maxcriser.cards.constant.Extras.EXTRA_DISCOUNT_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_NFC_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKETS_TITLE_TO_ITEMS;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_CARDHOLDER;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_COLOR;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_DATE;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_FIRST_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_ID;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_SECOND_PHOTO;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_TIME;
import static com.maxcriser.cards.constant.Extras.EXTRA_TICKET_TITLE;
import static com.maxcriser.cards.constant.Extras.EXTRA_VERIFICATION_NUMBER_BANK;

public class ItemsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 1;
    private String typeItems;
    private FrameLayout progressBar;
    private FloatingActionButton newCard;
    private TextView noResultFor;
    private DatabaseHelper dbHelper;
    private CardCursorAdapter adapter;
    private RecyclerView recyclerItems;
    private CardView toolbarBack;
    private CardView toolbarSearch;
    private LinearLayout linearEmpty;
    private ImageView clearSearch;
    private EditText searchEdit;
    private String searchText = ListConstants.EMPTY_STRING;
    private Class ModelClass;
    private FlipAnimation mFlipAnimation;
    private NfcInputDialogBuilder nfcInputDialogBuilder;
    private NfcOutputDialogBuilder nfcOutputDialogBuilder;
    private EnterNfcNameDialogBuilder dialog;
    private NfcAdapter mNfcAdapter;
    private String writeNfcTag;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);
        final Intent intent = getIntent();
        typeItems = intent.getStringExtra(EXTRA_CHECK_ITEMS);
        initViews();
        initNFC();
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void showNfc(final Cursor pCursor) {
        nfcOutputDialogBuilder = new NfcOutputDialogBuilder(this);
        nfcOutputDialogBuilder.startDialog();
        writeNfcTag = pCursor.getString(pCursor.getColumnIndex(ModelNFCItems.TAG));
    }

    private void showTicket(final Cursor pCursor) {
        final String id = pCursor.getString(pCursor.getColumnIndex(ModelTickets.ID));
        final String nameTicket = pCursor.getString(pCursor.getColumnIndex(ModelTickets.TITLE));
        final String cardholderTicket = pCursor.getString(pCursor.getColumnIndex(ModelTickets.CARDHOLDER));
        final String dateTicket = pCursor.getString(pCursor.getColumnIndex(ModelTickets.DATE));
        final String timeTicket = pCursor.getString(pCursor.getColumnIndex(ModelTickets.TIME));
        final String color = pCursor.getString(pCursor.getColumnIndex(ModelTickets.BACKGROUND_COLOR));
        final String firstPhoto = pCursor.getString(pCursor.getColumnIndex(ModelTickets.PHOTO_FIRST));
        final String secondPhoto = pCursor.getString(pCursor.getColumnIndex(ModelTickets.PHOTO_SECOND));

        final Intent intent = new Intent(this, TicketActivity.class);
        intent.putExtra(EXTRA_TICKET_ID, id);
        intent.putExtra(EXTRA_TICKET_TITLE, nameTicket);
        intent.putExtra(EXTRA_TICKET_CARDHOLDER, cardholderTicket);
        intent.putExtra(EXTRA_TICKET_DATE, dateTicket);
        intent.putExtra(EXTRA_TICKET_TIME, timeTicket);
        intent.putExtra(EXTRA_TICKET_COLOR, color);
        intent.putExtra(EXTRA_TICKET_FIRST_PHOTO, firstPhoto);
        intent.putExtra(EXTRA_TICKET_SECOND_PHOTO, secondPhoto);
        startActivity(intent);

    }

    private void showDiscount(final Cursor pCursor) {
        final String cardID = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.ID));
        final String cardTitle = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.TITLE));
        final String cardBarcode = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.BARCODE));
        final String cardFront = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.PHOTO_FRONT));
        final String cardBack = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.PHOTO_BACK));

        final Intent intent = new Intent(this, DiscountCardActivity.class);
        intent.putExtra(EXTRA_DISCOUNT_ID, cardID);
        intent.putExtra(EXTRA_DISCOUNT_TITLE, cardTitle);
        intent.putExtra(EXTRA_DISCOUNT_BARCODE, cardBarcode);
        intent.putExtra(EXTRA_DISCOUNT_FRONT, cardFront);
        intent.putExtra(EXTRA_DISCOUNT_BACK, cardBack);
        startActivity(intent);
    }

    private void showBank(final Cursor pCursor) {
        final String id = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.ID));
        final String verNumber = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.VERIFICATION_NUMBER));
        final String bank = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.TITLE));
        final String cardholder = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.CARDHOLDER));
        final String number = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.NUMBER));
        final String pin = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.PIN));
        final String valid = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.VALID));
        final String type = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.TYPE));
        final String frontPhoto = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.PHOTO_FRONT));
        final String backPhoto = pCursor.getString(pCursor.getColumnIndex(ModelBankCards.PHOTO_BACK));

        final Intent intent = new Intent(this, BankCardActivity.class);
        intent.putExtra(EXTRA_BANK_ID, id);
        intent.putExtra(EXTRA_BANK_BANK, bank);
        intent.putExtra(EXTRA_BANK_CARDHOLDER, cardholder);
        intent.putExtra(EXTRA_VERIFICATION_NUMBER_BANK, verNumber);
        intent.putExtra(EXTRA_BANK_NUMBER, number);
        intent.putExtra(EXTRA_BANK_PIN, pin);
        intent.putExtra(EXTRA_BANK_VALID, valid);
        intent.putExtra(EXTRA_BANK_TYPE, type);
        intent.putExtra(EXTRA_BANK_FRONT_PHOTO, frontPhoto);
        intent.putExtra(EXTRA_BANK_BACK_PHOTO, backPhoto);
        startActivity(intent);
    }

    private void initViews() {
        progressBar = (FrameLayout) findViewById(R.id.frame_progressbar);
        newCard = (FloatingActionButton) findViewById(R.id.new_card);
        linearEmpty = (LinearLayout) findViewById(R.id.empty_page_id_fragment);
        noResultFor = (TextView) findViewById(R.id.frame_no_results_for);
        clearSearch = (ImageView) findViewById(R.id.clearSearch);
        searchEdit = (EditText) findViewById(R.id.search_edit);
        toolbarBack = (CardView) findViewById(R.id.card_view_toolbar_back);
        toolbarSearch = (CardView) findViewById(R.id.card_view_toolbar_search);
        final RobotoRegular title = (RobotoRegular) findViewById(R.id.title_toolbar);
        recyclerItems = (RecyclerView) findViewById(R.id.recycler_view_items);
        switch (typeItems) {
            case EXTRA_BANK_TITLE_TO_ITEMS:
                ModelClass = ModelBankCards.class;
                break;
            case EXTRA_DISCOUNT_TITLE_TO_ITEMS:
                ModelClass = ModelDiscountCards.class;
                break;
            case EXTRA_TICKETS_TITLE_TO_ITEMS:
                ModelClass = ModelTickets.class;
                break;
            default:
                ModelClass = ModelNFCItems.class;
                break;
        }
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        searchEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(final CharSequence pCharSequence, final int pI, final int pI1, final int pI2) {
            }

            @Override
            public void onTextChanged(final CharSequence pCharSequence, final int pI, final int pI1, final int pI2) {
            }

            @Override
            public void afterTextChanged(final Editable pEditable) {
                if (!pEditable.toString().isEmpty()) {
                    clearSearch.setVisibility(View.VISIBLE);
                } else {
                    clearSearch.setVisibility(View.GONE);
                }
                searchText = pEditable.toString();
                getSupportLoaderManager().restartLoader(LOADER_ID, null, ItemsActivity.this);
            }
        });
        title.setText(typeItems);

        dbHelper = ((Core) getApplication()).getDatabaseHelper(this);

        recyclerItems.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerItems.setLayoutManager(layoutManager);

        final ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder, final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ItemsActivity.this);
                alertDialogBuilder.setTitle(R.string.remove);
                alertDialogBuilder
                        .setMessage(R.string.are_you_sure_to_delete)
                        .setCancelable(false)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                getSupportLoaderManager().restartLoader(LOADER_ID, null, ItemsActivity.this);
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(final DialogInterface dialog, final int id) {
                                final TextView cardTitle = (TextView) viewHolder.itemView.findViewById(R.id.title_main_cards);
                                final Integer idDelete = (Integer) cardTitle.getTag();
                                dbHelper.query(new OnResultCallback<Cursor, Void>() {

                                    @Override
                                    public void onSuccess(final Cursor pCursor) {
                                        if (pCursor.moveToFirst()) {
                                            try {
                                                final String cardFront;
                                                final String cardBack;
                                                if (ModelClass == ModelTickets.class) {
                                                    cardFront = pCursor.getString(pCursor.getColumnIndex(ModelTickets.PHOTO_FIRST));
                                                    cardBack = pCursor.getString(pCursor.getColumnIndex(ModelTickets.PHOTO_SECOND));
                                                } else {
                                                    cardFront = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.PHOTO_FRONT));
                                                    cardBack = pCursor.getString(pCursor.getColumnIndex(ModelDiscountCards.PHOTO_BACK));
                                                }
                                                dbHelper.delete(ModelClass, null, ModelBankCards.ID + " = ?", String.valueOf(idDelete));
                                                final OwnAsyncTask sync = new OwnAsyncTask();
                                                sync.execute(new RemovePhoto(), Uri.parse(cardFront), null);
                                                sync.execute(new RemovePhoto(), Uri.parse(cardBack), null);
                                            } catch (final Exception e) {
                                                Toast.makeText(ItemsActivity.this, "Cannot remove photo", Toast.LENGTH_SHORT).show();
                                            }
                                            dbHelper.delete(ModelClass, null, ModelBankCards.ID + " = ?", String.valueOf(idDelete));
                                            // TODO: 21.03.2017 adapter.notifyItemRemoved(2);
                                            getSupportLoaderManager().restartLoader(LOADER_ID, null, ItemsActivity.this);
                                            dialog.cancel();
                                        }
                                    }

                                    @Override
                                    public void onError(final Exception pE) {
                                        Toast.makeText(ItemsActivity.this, R.string.connot_find_card, Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onProgressChanged(final Void pVoid) {
                                    }
                                }, "*", ModelClass, "WHERE "
                                        + ModelBankCards.ID + " = ?", String.valueOf(idDelete));
                            }
                        });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        };

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerItems);

        recyclerItems.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerItems, new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(final View view, final int position) {
                final TextView mTitle = (TextView) view.findViewById(R.id.title_main_cards);
                final int id = (Integer) mTitle.getTag();
                dbHelper.query(new OnResultCallback<Cursor, Void>() {

                    @Override
                    public void onSuccess(final Cursor pCursor) {
                        if (pCursor.moveToFirst()) {
                            switch (typeItems) {
                                case EXTRA_BANK_TITLE_TO_ITEMS:
                                    showBank(pCursor);
                                    break;
                                case EXTRA_DISCOUNT_TITLE_TO_ITEMS:
                                    showDiscount(pCursor);
                                    break;
                                case EXTRA_TICKETS_TITLE_TO_ITEMS:
                                    showTicket(pCursor);
                                    break;
                                default:
                                    showNfc(pCursor);
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onError(final Exception pE) {
                        Toast.makeText(ItemsActivity.this, R.string.connot_find_card, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {
                    }
                }, "*", ModelClass, "WHERE "
                        + ModelBankCards.ID + " = ?", String.valueOf(id));
            }

            @Override
            public void onItemLongClick(final View view, final int position) {
                final RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.relative_to_flip);
                if (relativeLayout != null) {
                    final CardView btnStart = (CardView) view.findViewById(R.id.front_cardview_to_flip);
                    final CardView btnFinish = (CardView) view.findViewById(R.id.back_cardview_to_flip);
                    if (btnStart.getVisibility() == View.VISIBLE) {
                        mFlipAnimation = new FlipAnimation(btnFinish, btnStart);
                    } else {
                        mFlipAnimation = new FlipAnimation(btnStart, btnFinish);
                    }
                    mFlipAnimation.setReverse();
                    relativeLayout.startAnimation(mFlipAnimation);
                }
            }
        }));
    }

    @Override
    public void onBackPressed() {
        if (toolbarSearch.getVisibility() == View.VISIBLE) {
            onBackSearchClicked(null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final LoaderManager supportLoaderManager = getSupportLoaderManager();
        if (supportLoaderManager.getLoader(LOADER_ID) != null) {
            supportLoaderManager.getLoader(LOADER_ID).forceLoad();
        }
        if (searchEdit != null) {
            if (toolbarSearch.getVisibility() == View.VISIBLE) {
                onBackSearchClicked(null);
            }
        }

        final IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        final IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        final IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        final IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        final PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
        }
    }

    public void onBackClicked(final View view) {
        super.onBackPressed();
    }

    @TargetApi(23)
    private void getPermission(final byte CODE, final String PERMISSION, final int INTENT) {
        if (ContextCompat.checkSelfPermission(this, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, CODE);
        } else {
            if (INTENT == ListConstants.Requests.REQUEST_CAMERA) {
                startBarcodeReader();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.permission_has_not_been_granted, Toast.LENGTH_SHORT).show();
        } else if (requestCode == ListConstants.Requests.REQUEST_CAMERA) {
            startBarcodeReader();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startBarcodeReader() {
        final Intent intent = new Intent(this, BarcodeScannerActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    public void onAddNewClicked(final View view) {
        switch (typeItems) {
            case EXTRA_BANK_TITLE_TO_ITEMS:
                startActivity(new Intent(this, CreateBankActivity.class));
                break;
            case EXTRA_DISCOUNT_TITLE_TO_ITEMS:
                getPermission(ListConstants.Requests.REQUEST_CAMERA, Manifest.permission.CAMERA, ListConstants.Requests.REQUEST_CAMERA);
                break;
            case EXTRA_TICKETS_TITLE_TO_ITEMS:
                startActivity(new Intent(this, CreateTicketActivity.class));
                break;
            default:
                nfcInputDialogBuilder = new NfcInputDialogBuilder(this);
                nfcInputDialogBuilder.startDialog();
                break;
        }
    }

    public void onBackSearchClicked(final View view) {
        toolbarBack.setVisibility(View.VISIBLE);
        toolbarSearch.setVisibility(GONE);
        searchEdit.setText(ListConstants.EMPTY_STRING);
        searchText = searchEdit.getText().toString();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newCard.getWindowToken(), 0);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final Class clazz;
        switch (typeItems) {
            case EXTRA_BANK_TITLE_TO_ITEMS:
                clazz = ModelBankCards.class;
                break;
            case EXTRA_DISCOUNT_TITLE_TO_ITEMS:
                clazz = ModelDiscountCards.class;
                break;
            case EXTRA_TICKETS_TITLE_TO_ITEMS:
                clazz = ModelTickets.class;
                break;
            default:
                clazz = ModelNFCItems.class;
                break;
        }
        return new CardsCursorLoader(this, searchText, clazz, getApplication());
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        progressBar.setVisibility(GONE);
        if (data.getCount() == 0) {
            if (!searchText.isEmpty()) {
                final Spannable text = new SpannableString(getString(R.string.no_result_for) + " '" + searchText + "'");
                text.setSpan(new StyleSpan(Typeface.BOLD), text.length() - searchText.length() - 1, text.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                noResultFor.setText(text);
                noResultFor.setVisibility(View.VISIBLE);
            } else {
                linearEmpty.setVisibility(View.VISIBLE);
            }
            recyclerItems.setVisibility(GONE);
        } else {
            noResultFor.setVisibility(GONE);
            linearEmpty.setVisibility(GONE);
            recyclerItems.setVisibility(View.VISIBLE);
        }

        Object layout = R.layout.item_bank;
        switch (typeItems) {
            case EXTRA_BANK_TITLE_TO_ITEMS:
                layout = R.layout.item_bank;
                break;
            case EXTRA_DISCOUNT_TITLE_TO_ITEMS:
                layout = R.layout.item_discount;
                break;
            case EXTRA_TICKETS_TITLE_TO_ITEMS:
                layout = R.layout.item_ticket;
                break;
            case EXTRA_NFC_TITLE_TO_ITEMS:
                layout = R.layout.item_nfc;
                break;
        }
        adapter = new CardCursorAdapter(data, this, layout);
//        recyclerItems.setAdapter(adapter);
        recyclerItems.swapAdapter(adapter, true);
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
//        recyclerItems.setAdapter(null);
        recyclerItems.swapAdapter(null, true);
    }

    public void onToolbarBackClicked(final View view) {
        if (adapter.getItemCount() != 0) {
            recyclerItems.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    public void onSearchClicked(final View view) {
        toolbarSearch.setVisibility(View.VISIBLE);
        toolbarBack.setVisibility(GONE);
        searchEdit.setText(ListConstants.EMPTY_STRING);
        searchEdit.clearFocus();
        searchEdit.requestFocus();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    public void onClearSearchClicked(final View view) {
        searchEdit.setText(ListConstants.EMPTY_STRING);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            final Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                if (nfcInputDialogBuilder.isShowing()) {
                    if (nfcInputDialogBuilder != null) {
                        nfcInputDialogBuilder.cancelDialog();
                    }
                    dialog = new EnterNfcNameDialogBuilder(this);
                    dialog.startDialog();
                    try {
                        writeNfcToDatabase(ndef);
                    } catch (final IOException pE) {
                        Toast.makeText(this, R.string.cannot_write_nfc_tag_to_db, Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (nfcOutputDialogBuilder.isShowing()) {
                        if (nfcOutputDialogBuilder != null) {
                            nfcOutputDialogBuilder.cancelDialog();
                        }
                        try {
                            writeNfc(ndef, writeNfcTag);
                        } catch (final Exception pE) {
                            Toast.makeText(this, R.string.cannot_write_nfc, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else {
                if (nfcInputDialogBuilder != null) {
                    nfcInputDialogBuilder.cancelDialog();
                }
                if (nfcOutputDialogBuilder != null) {
                    nfcOutputDialogBuilder.cancelDialog();
                }
                Toast.makeText(this, getString(R.string.cannot_scanning_type_nfc), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void writeNfc(final Ndef ndef1, final String message) throws Exception {
        if (ndef1 != null) {
            try {
                ndef1.connect();
                final NdefRecord mimeRecord = NdefRecord.createMime("text/plain", message.getBytes(Charset.forName("US-ASCII")));
                ndef1.writeNdefMessage(new NdefMessage(mimeRecord));
            } catch (IOException | FormatException e) {
                Toast.makeText(this, R.string.cannot_connet_to_ndef, Toast.LENGTH_LONG).show();
            } finally {
                try {
                    ndef1.close();
                    if (nfcOutputDialogBuilder != null) {
                        nfcOutputDialogBuilder.cancelDialog();
                    }
                } catch (final IOException pE) {
                    throw new Exception(pE);
                }
            }
        } else {
            Toast.makeText(this, R.string.ndef_not_available, Toast.LENGTH_LONG).show();
            if (nfcOutputDialogBuilder != null) {
                nfcOutputDialogBuilder.cancelDialog();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void writeNfcToDatabase(final Ndef ndef1) throws IOException {
        final String message;
        final String title = dialog.titleField.getText().toString();
        if (!title.isEmpty()) {
            try {
                ndef1.connect();
                final NdefMessage ndefMessage = ndef1.getNdefMessage();
                message = new String(ndefMessage.getRecords()[0].getPayload());
                final PreviewColor listPreviewColor = ListPreview.colors.get(0);
                final ContentValues cv = new ContentValues();
                cv.put(ModelNFCItems.BACKGROUND_COLOR, listPreviewColor.getCodeColorCards());
                cv.put(ModelNFCItems.TITLE, title);
                cv.put(ModelNFCItems.ID, (Integer) null);
                cv.put(ModelNFCItems.TAG, message);

                dbHelper.insert(ModelNFCItems.class, cv, new OnResultCallback<Long, Void>() {

                    @Override
                    public void onSuccess(final Long pLong) {
                        getSupportLoaderManager().restartLoader(LOADER_ID, null, ItemsActivity.this);
                    }

                    @Override
                    public void onError(final Exception pE) {
                        Toast.makeText(ItemsActivity.this, R.string.cannot_insert_card_to_database, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onProgressChanged(final Void pVoid) {
                    }
                });

            } catch (IOException | FormatException e) {
                Toast.makeText(this, R.string.cannot_save_nfc_item, Toast.LENGTH_LONG).show();
            } finally {
                if (nfcInputDialogBuilder != null) {
                    nfcInputDialogBuilder.cancelDialog();
                }
                ndef1.close();
            }
        } else {
            Toast.makeText(this, R.string.cannot_save_nfc_title_isempty, Toast.LENGTH_LONG).show();
        }
    }
}