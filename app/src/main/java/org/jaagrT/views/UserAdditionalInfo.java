package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.ParseObject;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.ObjectRetriever;
import org.jaagrT.listeners.ParseListener;
import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.FormValidators;
import org.jaagrT.utilities.Utilities;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class UserAdditionalInfo extends Activity {

    private MaterialEditText firstNameBox, lastNameBox, phoneBox;
    private User localUser;
    private ParseObject userDetailsObject;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_additional_info);
        activity = this;
        setUpActivity();
    }

    private void setUpActivity() {

        Button nextBtn = (Button) findViewById(R.id.nextBtn);
        Button skipBtn = (Button) findViewById(R.id.skipBtn);

        firstNameBox = (MaterialEditText) findViewById(R.id.firstNameBox);
        lastNameBox = (MaterialEditText) findViewById(R.id.lastNameBox);
        phoneBox = (MaterialEditText) findViewById(R.id.phoneBox);

        firstNameBox.addValidator(new FormValidators.EmptyFieldValidator());
        lastNameBox.addValidator(new FormValidators.EmptyFieldValidator());
        phoneBox.addValidator(new FormValidators.EmptyFieldValidator());
        phoneBox.addValidator(new FormValidators.PhoneNumberValidator());

        final MaterialEditText[] editTexts = {firstNameBox, lastNameBox, phoneBox};

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    new UpdateUserDetails().execute();
                }
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMainActivity();
            }
        });

        new GetObjects().execute();
    }

    private void fillBoxesIfPossible() {
        if (localUser != null) {
            if (localUser.getFirstName() != null) {
                firstNameBox.setText(localUser.getFirstName());
            }

            if (localUser.getLastName() != null) {
                lastNameBox.setText(localUser.getLastName());
            }

            if (localUser.getPhoneNumber() != null) {
                phoneBox.setText(localUser.getPhoneNumber());
            }
        }
    }

    private void startVerifyPhoneActivity() {
        Intent phoneActivityIntent = new Intent(activity, VerifyPhone.class);
        phoneActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        phoneActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneActivityIntent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private void startMainActivity() {
        Intent mainActivityIntent = new Intent(activity, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private class GetObjects extends AsyncTask<Void, Void, Void> {
        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText("Please wait...");
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ObjectRetriever retriever = ObjectRetriever.getInstance(activity);
            localUser = retriever.getLocalUser();
            userDetailsObject = retriever.getUserDetailsObject(new ParseListener() {
                @Override
                public void onComplete(ParseObject parseObject) {
                    userDetailsObject = parseObject;
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            fillBoxesIfPossible();
            pDialog.cancel();
        }
    }

    private class UpdateUserDetails extends AsyncTask<Void, Void, Integer> {

        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText("Updating...");
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (userDetailsObject != null) {
                userDetailsObject.put(Constants.USER_FIRST_NAME, firstNameBox.getText().toString());
                userDetailsObject.put(Constants.USER_LAST_NAME, lastNameBox.getText().toString());
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE, phoneBox.getText().toString());
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE_VERIFIED, false);
                userDetailsObject.saveEventually();
            }

            Database db = Database.getInstance(activity, Database.USER_TABLE);
            localUser.setFirstName(firstNameBox.getText().toString());
            localUser.setLastName(lastNameBox.getText().toString());
            localUser.setPhoneNumber(phoneBox.getText().toString());
            localUser.setPhoneVerified(false);
            return db.updateUserData(localUser);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                Utilities.snackIt(activity, "Info updated", "Okay");
            } else {
                Utilities.snackIt(activity, "Failed to update your Info", "Okay");
            }

            startVerifyPhoneActivity();
        }
    }

}
