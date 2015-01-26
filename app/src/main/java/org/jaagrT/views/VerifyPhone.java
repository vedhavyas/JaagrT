package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseObject;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.BasicController;
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.FormValidators;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class VerifyPhone extends Activity {

    private static final String MESSAGE = "JaagrT Verification Code";
    private static final String WRONG_CODE = "Wrong Code!!";

    public static VerifyPhone verifyPhoneActivity;
    private Activity activity;
    private MaterialEditText phoneBox, verifyCodeBox;
    private User localUser;
    private ParseObject userDetailsObject;
    private String randomString;
    private String phoneNumber;
    private BasicController basicController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        activity = this;
        verifyPhoneActivity = this;
        setUpActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verifyPhoneActivity = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        verifyPhoneActivity = this;
    }

    @Override
    public void onBackPressed() {
        returnResult(Activity.RESULT_CANCELED);
    }

    private void setUpActivity() {
        ImageButton editPhoneBtn = (ImageButton) findViewById(R.id.editPhoneBtn);
        Button skipBtn = (Button) findViewById(R.id.cancelBtn);
        Button verifyAndUpdateBtn = (Button) findViewById(R.id.nextBtn);
        Button sendSMSBtn = (Button) findViewById(R.id.sendSMSBtn);
        verifyCodeBox = (MaterialEditText) findViewById(R.id.verificationCodeBox);
        phoneBox = (MaterialEditText) findViewById(R.id.phoneBox);

        verifyCodeBox.addValidator(new FormValidators.EmptyFieldValidator());
        phoneBox.addValidator(new FormValidators.EmptyFieldValidator());
        phoneBox.addValidator(new FormValidators.NumberValidator());
        phoneBox.setEnabled(false);

        final List<MaterialEditText> editTexts = new ArrayList<>();
        editTexts.add(phoneBox);

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
                returnResult(Activity.RESULT_CANCELED);
            }
        });

        verifyAndUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyAndUpdate();
            }
        });

        sendSMSBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    phoneBox.setEnabled(false);
                    generateRandomNumber();
                    phoneNumber = phoneBox.getText().toString();
                    showSMSWarning();
                }

            }
        });

        new GetDataIfPossible().execute();
    }

    private void verifyAndUpdate() {
        if (verifyCodeBox.getText().toString().equalsIgnoreCase(randomString)) {
            phoneBox.setText(phoneNumber);
            new UpdateUserData().execute();
        } else {
            Utilities.snackIt(activity, WRONG_CODE, ErrorHandler.OKAY);
        }
    }

    public void setVerificationCode(String phoneNumber, String messageBody) {
        String code = getCodeFromMessage(messageBody);

        if (phoneNumber.contains(phoneBox.getText().toString()) && code != null) {
            this.phoneNumber = phoneNumber;
            verifyCodeBox.setText(code);
            verifyAndUpdate();
        }
    }

    private void generateRandomNumber() {
        Random randomGenerator = new Random();
        int randomInt = 0;
        for (int i = 0; i < 4; i++) {
            randomInt = randomInt * 10 + randomGenerator.nextInt(10);
        }

        randomString = String.valueOf(randomInt);
    }

    private String getCodeFromMessage(String messageBody) {
        String[] data = messageBody.split(":");
        if (data.length == 2 && data[0].equalsIgnoreCase(MESSAGE)) {
            return data[1];
        }
        return null;
    }


    private void returnResult(int result) {
        Intent intent = new Intent();
        setResult(result, intent);
        finish();
    }

    private void showSuccessDialog() {
        new MaterialDialog.Builder(activity)
                .title("Success")
                .titleColor(getResources().getColor(R.color.teal_400))
                .content("SMS Sent!")
                .contentColor(getResources().getColor(R.color.teal_400))
                .positiveText("Okay")
                .positiveColor(getResources().getColor(R.color.teal_400))
                .show();
    }

    private void showSMSWarning() {
        new MaterialDialog.Builder(activity)
                .title("Warning")
                .titleColor(getResources().getColor(R.color.teal_400))
                .content("SMS will be sent to verify Phone Number. Standard SMS charges may apply.")
                .contentColor(getResources().getColor(R.color.teal_400))
                .positiveText("Send anyway")
                .positiveColor(getResources().getColor(R.color.teal_400))
                .negativeText("Don't send")
                .negativeColor(getResources().getColor(R.color.teal_400))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        new SendSMS().execute();
                    }
                }).show();
    }

    private class SendSMS extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneBox.getText().toString(), null, MESSAGE + ":" + randomString, null, null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showSuccessDialog();
        }
    }

    private class GetDataIfPossible extends AsyncTask<Void, Void, Void> {

        SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText(Constants.PLEASE_WAIT);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            basicController = BasicController.getInstance(activity);
            localUser = basicController.getLocalUser();
            userDetailsObject = ObjectService.getUserDetailsObject();
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
            pDialog = AlertDialogs.showSweetProgress(activity);
            pDialog.setTitleText(Constants.UPDATING);
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
            return basicController.updateUser(localUser);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            pDialog.cancel();
            if (result > 0) {
                returnResult(Activity.RESULT_OK);
            } else {
                returnResult(Activity.RESULT_CANCELED);
            }

        }
    }


}
