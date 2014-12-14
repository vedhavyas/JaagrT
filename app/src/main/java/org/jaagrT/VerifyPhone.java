package org.jaagrT;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.parse.ParseObject;

import org.jaagrT.controller.ObjectRetriever;
import org.jaagrT.listeners.ParseListener;
import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.FormValidators;
import org.jaagrT.utilities.Utilities;

import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class VerifyPhone extends Activity {

    private static final String MESSAGE = "The verification code is:";
    public static Activity phoneVerifyActivity;
    private FormEditText phoneBox, verifyCodeBox;
    private User localUser;
    private ParseObject userDetailsObject;
    private String randomString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        phoneVerifyActivity = this;
        setUpActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        phoneVerifyActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        phoneVerifyActivity = this;
    }

    private void setUpActivity() {
        Button editPhoneBtn = (Button) findViewById(R.id.editPhoneBtn);
        Button skipBtn = (Button) findViewById(R.id.skipBtn);
        Button updateBtn = (Button) findViewById(R.id.updateBtn);
        Button verifyBtn = (Button) findViewById(R.id.verifyBtn);
        verifyCodeBox = (FormEditText) findViewById(R.id.verifyCodeBox);
        phoneBox = (FormEditText) findViewById(R.id.phoneBox);
        phoneBox.addValidator(new FormValidators.PhoneNumberValidator());
        phoneBox.setEnabled(false);

        final FormEditText[] editTexts = {phoneBox};

        editPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneBox.setEnabled(true);
                phoneBox.setFocusable(true);
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAndUpdate();
            }
        });

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    phoneBox.setEnabled(false);
                    generateRandomNumber();
                    new SendSMS().execute();
                }

            }
        });

        new GetDataIfPossible().execute();
    }

    private void verifyAndUpdate() {
        if (verifyCodeBox.getText().toString().equalsIgnoreCase(randomString)) {
            new UpdateUserData().execute();
        } else {
            Utilities.snackIt(phoneVerifyActivity, "Wrong Code", "Oops!");
        }
    }

    public void setVerificationCode(String code) {
        verifyCodeBox.setText(code);
        verifyAndUpdate();
    }

    private void generateRandomNumber() {
        Random randomGenerator = new Random();
        int randomInt = 0;
        for (int i = 0; i < 4; i++) {
            randomInt = randomInt * 10 + randomGenerator.nextInt(10);
        }

        randomString = String.valueOf(randomInt);
    }

    private class SendSMS extends AsyncTask<Void, Void, Void> {
        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(phoneVerifyActivity);
            pDialog.setTitleText("Sending SMS...");
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneBox.getText().toString(), null, MESSAGE + randomString, null, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pDialog.cancel();
            AlertDialogs.showPositiveDialog(phoneVerifyActivity, "Success", "SMS triggered!");
        }
    }

    private class GetDataIfPossible extends AsyncTask<Void, Void, Void> {

        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(phoneVerifyActivity);
            pDialog.setTitleText("Please wait...");
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            ObjectRetriever retriever = ObjectRetriever.getInstance(phoneVerifyActivity);
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
            pDialog.cancel();
            if (localUser != null) {
                if (localUser.getPhoneNumber() != null) {
                    phoneBox.setText(localUser.getPhoneNumber());
                }
            }
        }
    }

    private class UpdateUserData extends AsyncTask<Void, Void, Integer> {

        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(phoneVerifyActivity);
            pDialog.setTitleText("Saving...");
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (userDetailsObject != null) {
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE, phoneBox.getText().toString());
                userDetailsObject.put(Constants.USER_PRIMARY_PHONE_VERIFIED, true);
                userDetailsObject.saveEventually();
            }

            localUser.setPhoneNumber(phoneBox.getText().toString());
            localUser.setPhoneVerified(true);
            Database db = Database.getInstance(phoneVerifyActivity, Database.USER_TABLE);
            return db.updateUserData(localUser);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                Utilities.snackIt(phoneVerifyActivity, "Save successful", "Okay");
            } else {
                Utilities.snackIt(phoneVerifyActivity, "Failed to save", "Okay");
            }
        }
    }


}
