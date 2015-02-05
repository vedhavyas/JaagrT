package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.FormValidators;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.listeners.BitmapGetListener;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Profile extends ActionBarActivity {

    private static final int EMAIL = 1, PHONE = 2;
    private static final int NORMAL_MODE = 0;
    private int whichMode = NORMAL_MODE;
    private static final int EDIT_MODE = 1;
    private static final int PICK_PICTURE = 2;
    private static final int VERIFY_PHONE = 3;
    private static final String PHONE_NOT_VERIFIED = "Phone not verified";
    private static final String EMAIL_NOT_VERIFIED = "Email not verified";
    private static final String UPDATING = "Updating...";
    private Activity activity;
    private LinearLayout secondaryEmailContainer, secondaryPhoneContainer;
    private List<MaterialEditText> primaryDetailBoxes = new ArrayList<>(), secondaryEmailBoxes = new ArrayList<>(), secondaryPhoneBoxes = new ArrayList<>();
    private User user;
    private ParseUser parseUser;
    private Handler handler;
    private BasicController basicController;
    private BitmapHolder bitmapHolder;
    private MaterialEditText emailBox, firstNameBox, lastNameBox, phoneBox;
    private Button verifyPhoneBtn;
    private CardView secondaryEmailsCard, secondaryPhonesCard;
    private Toolbar profilePicView;
    private ImageButton addSecondaryEmailField, addSecondaryPhoneField;
    private List<ImageButton> deleteButtons = new ArrayList<>();
    private boolean profileMode;
    private int circleID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        circleID = getIntent().getIntExtra(Constants.CIRCLE_ID, -1);
        profileMode = circleID <= 0;
        setUpActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PICK_PICTURE && resultCode == RESULT_OK) {
            getUserPicture();
        } else if (requestCode == Constants.VERIFY_PHONE && resultCode == RESULT_OK) {
            user = basicController.getUser();
            getUserAndUpdateFields();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!profileMode) {
            super.onBackPressed();
            overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
        } else {
            startMainActivity();
        }
    }

    private void setUpActivity() {
        activity = this;
        handler = new Handler();
        basicController = BasicController.getInstance(activity);
        bitmapHolder = BitmapHolder.getInstance(activity);

        if (profileMode) {
            user = basicController.getUser();
            parseUser = ParseUser.getCurrentUser();
        } else {
            user = basicController.getCircle(circleID);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ObservableScrollView scrollView = (ObservableScrollView) findViewById(R.id.scrollView);
        final FloatingActionButton editBtn = (FloatingActionButton) findViewById(R.id.editBtn);
        editBtn.attachToScrollView(scrollView);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (whichMode == NORMAL_MODE) {
                    editBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_save_profile));
                    whichMode = EDIT_MODE;
                    changeFieldMode(true);
                } else {
                    if (Utilities.isEditBoxesValid(primaryDetailBoxes) && Utilities.isEditBoxesValid(secondaryEmailBoxes) && Utilities.isEditBoxesValid(secondaryPhoneBoxes)) {
                        new UpdateUser().execute();
                    }
                }
            }
        });
        if (!profileMode) {
            editBtn.setVisibility(View.GONE);
        }

        profilePicView = (Toolbar) findViewById(R.id.profileView);
        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profileMode) {
                    startResultActivity(PICK_PICTURE);
                }
            }
        });
        emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        firstNameBox = (MaterialEditText) findViewById(R.id.firstNameBox);
        firstNameBox.addValidator(new FormValidators.EmptyFieldValidator());
        lastNameBox = (MaterialEditText) findViewById(R.id.lastNameBox);
        lastNameBox.addValidator(new FormValidators.EmptyFieldValidator());
        phoneBox = (MaterialEditText) findViewById(R.id.phoneBox);
        phoneBox.addValidator(new FormValidators.EmptyFieldValidator())
                .addValidator(new FormValidators.DigitValidator())
                .addValidator(new FormValidators.NumberValidator());

        primaryDetailBoxes.addAll(Arrays.asList(firstNameBox, lastNameBox, phoneBox));
        verifyPhoneBtn = (Button) findViewById(R.id.verifyPhoneBtn);
        verifyPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startResultActivity(VERIFY_PHONE);
            }
        });
        secondaryEmailContainer = (LinearLayout) findViewById(R.id.secondaryEmailsLayout);
        secondaryPhoneContainer = (LinearLayout) findViewById(R.id.secondaryPhonesLayout);
        secondaryEmailsCard = (CardView) findViewById(R.id.secondaryEmailsCard);
        secondaryPhonesCard = (CardView) findViewById(R.id.secondaryPhonesCard);
        addSecondaryEmailField = (ImageButton) findViewById(R.id.addEmailFieldBtn);
        addSecondaryEmailField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewField(null, EMAIL);
            }
        });

        addSecondaryPhoneField = (ImageButton) findViewById(R.id.addPhoneFieldBtn);
        addSecondaryPhoneField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewField(null, PHONE);
            }
        });

        getUserAndUpdateFields();
        getUserPicture();
    }


    private void addNewField(String data, final int which) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final ImageButton deleteField;
        final MaterialEditText editText;
        View view;

        if (which == EMAIL) {
            view = inflater.inflate(R.layout.secondary_email_field, null);
            deleteField = (ImageButton) view.findViewById(R.id.deleteField);
            editText = (MaterialEditText) view.findViewById(R.id.emailBox);
            editText.addValidator(new FormValidators.EmptyFieldValidator())
                    .addValidator(new FormValidators.EmailValidator());
            secondaryEmailBoxes.add(editText);
            secondaryEmailContainer.addView(view, secondaryEmailContainer.getChildCount());
        } else {
            view = inflater.inflate(R.layout.secondary_phone_field, null);
            deleteField = (ImageButton) view.findViewById(R.id.deleteField);
            editText = (MaterialEditText) view.findViewById(R.id.phoneBox);
            editText.addValidator(new FormValidators.EmptyFieldValidator())
                    .addValidator(new FormValidators.DigitValidator())
                    .addValidator(new FormValidators.NumberValidator());
            secondaryPhoneBoxes.add(editText);
            secondaryPhoneContainer.addView(view, secondaryPhoneContainer.getChildCount());
        }

        if (data != null) {
            editText.setText(data);
        }

        deleteField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (which == EMAIL) {
                    secondaryEmailBoxes.remove(editText);
                    secondaryEmailContainer.removeView((View) v.getParent());
                } else {
                    secondaryPhoneBoxes.remove(editText);
                    secondaryPhoneContainer.removeView((View) v.getParent());
                }

                deleteButtons.add(deleteField);
            }
        });

        deleteButtons.add(deleteField);
    }

    private void getUserAndUpdateFields() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        emailBox.setText(user.getEmail());
                        emailBox.setEnabled(false);
                        if (profileMode && !parseUser.getBoolean(Constants.EMAIL_VERIFIED)) {
                            emailBox.setError(EMAIL_NOT_VERIFIED);
                        }
                        if (user.getFirstName() != null) {
                            firstNameBox.setText(user.getFirstName());
                        }

                        if (user.getLastName() != null) {
                            lastNameBox.setText(user.getLastName());
                        }

                        if (user.getPhoneNumber() != null) {
                            phoneBox.setText(user.getPhoneNumber());
                            if (user.isPhoneVerified()) {
                                verifyPhoneBtn.setVisibility(View.GONE);
                            } else if (!profileMode) {
                                phoneBox.setError(PHONE_NOT_VERIFIED);
                                verifyPhoneBtn.setVisibility(View.GONE);
                            } else {
                                phoneBox.setError(PHONE_NOT_VERIFIED);
                            }
                        } else {
                            verifyPhoneBtn.setVisibility(View.GONE);
                        }

                        if (user.getSecondaryEmailsRaw() != null && !user.getSecondaryEmailsRaw().isEmpty()) {
                            List<String> emails = user.getSecondaryEmails();
                            for (String email : emails) {
                                addNewField(email, EMAIL);
                            }
                        } else {
                            secondaryEmailsCard.setVisibility(View.GONE);
                        }

                        if (user.getSecondaryPhonesRaw() != null && !user.getSecondaryPhonesRaw().isEmpty()) {
                            List<String> phones = user.getSecondaryPhones();
                            for (String phone : phones) {
                                addNewField(phone, PHONE);
                            }
                        } else {
                            secondaryPhonesCard.setVisibility(View.GONE);
                        }

                        changeFieldMode(false);
                    }
                });
            }
        }).start();
    }

    private void getUserPicture() {
        bitmapHolder.getBitmapImageAsync(user.getEmail(), new BitmapGetListener() {
            @Override
            public void onGet(Bitmap bitmap) {
                if (bitmap != null) {
                    Drawable userDrawable = Utilities.getBitmapDrawable(activity, bitmap);
                    if (userDrawable != null) {
                        profilePicView.setBackground(userDrawable);
                    }
                }
            }
        });
    }

    private void changeFieldMode(boolean mode) {
        int visibility;
        if (mode) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        firstNameBox.setEnabled(mode);
        lastNameBox.setEnabled(mode);
        phoneBox.setEnabled(mode);

        if (mode) {
            secondaryEmailsCard.setVisibility(visibility);
            secondaryPhonesCard.setVisibility(visibility);
        }
        addSecondaryEmailField.setVisibility(visibility);
        addSecondaryPhoneField.setVisibility(visibility);

        for (ImageButton button : deleteButtons) {
            button.setVisibility(visibility);
        }

        for (MaterialEditText editText : secondaryEmailBoxes) {
            editText.setEnabled(mode);
        }

        for (MaterialEditText editText : secondaryPhoneBoxes) {
            editText.setEnabled(mode);
        }

    }

    private void startResultActivity(int whichActivity) {
        if (whichActivity == PICK_PICTURE) {
            Intent intent = new Intent(activity, PickPicture.class);
            startActivityForResult(intent, Constants.PICK_PICTURE);
        } else if (whichActivity == VERIFY_PHONE) {
            Intent intent = new Intent(activity, VerifyPhone.class);
            startActivityForResult(intent, Constants.VERIFY_PHONE);
        }
    }


    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(activity, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private class UpdateUser extends AsyncTask<Void, Void, Void> {

        private SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText(UPDATING);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ParseObject userDetailsObject = ObjectService.getUserDetailsObject();
            user = basicController.getUser();
            user.setFirstName(firstNameBox.getText().toString());
            user.setLastName(lastNameBox.getText().toString());
            if (!user.getPhoneNumber().contains(phoneBox.getText().toString())) {
                user.setPhoneVerified(false);
            }
            user.setPhoneNumber(phoneBox.getText().toString());

            List<String> secondaryEmails = new ArrayList<>();
            if (secondaryEmailBoxes.size() > 0) {
                for (MaterialEditText editText : secondaryEmailBoxes) {
                    secondaryEmails.add(editText.getText().toString());
                }
            }
            user.setSecondaryEmails(secondaryEmails);

            List<String> secondaryPhones = new ArrayList<>();
            if (secondaryPhoneBoxes.size() > 0) {
                for (MaterialEditText editText : secondaryPhoneBoxes) {
                    secondaryPhones.add(editText.getText().toString());
                }
            }
            user.setSecondaryPhones(secondaryPhones);

            if (userDetailsObject != null) {
                userDetailsObject.put(Constants.USER_FIRST_NAME, firstNameBox.getText().toString());
                userDetailsObject.put(Constants.USER_LAST_NAME, lastNameBox.getText().toString());
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE, phoneBox.getText().toString());
                userDetailsObject.remove(Constants.USER_SECONDARY_EMAILS);
                userDetailsObject.remove(Constants.USER_SECONDARY_PHONES);
                userDetailsObject.addAll(Constants.USER_SECONDARY_EMAILS, secondaryEmails);
                userDetailsObject.addAll(Constants.USER_SECONDARY_PHONES, secondaryPhones);
                userDetailsObject.saveEventually();
            }

            basicController.updateUser(user);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.cancel();
            startMainActivity();
        }
    }

}
