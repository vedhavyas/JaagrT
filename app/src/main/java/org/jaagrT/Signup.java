package org.jaagrT;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.jaagrT.utils.AlertDialogs;
import org.jaagrT.utils.FormValidators;
import org.jaagrT.utils.Utilities;


public class Signup extends Activity {

    private FormEditText nameBox, emailBox, passwordBox, phoneBox;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        activity = this;
        new SetUpUI().execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private void registerUser() {
        final AlertDialog dialog = AlertDialogs.showProgressDialog(activity, "Signing up...");
        ParseUser newUser = new ParseUser();
        newUser.setUsername(emailBox.getText().toString());
        newUser.setPassword(passwordBox.getText().toString());
        newUser.setEmail(emailBox.getText().toString());
        newUser.put("name", nameBox.getText().toString());
        newUser.put("phone", phoneBox.getText().toString());

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                dialog.cancel();
                if (e == null) {
                    Utilities.snackIt(activity, "Registration Successful", "Okay");
                } else {
                    Utilities.logIt(e.getMessage());
                    AlertDialogs.errorDialog(activity, "Error", e.getMessage(), "okay").show();
                }
            }
        });
    }

    private class SetUpUI extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            nameBox = (FormEditText) findViewById(R.id.nameBox);
            emailBox = (FormEditText) findViewById(R.id.emailBox);
            passwordBox = (FormEditText) findViewById(R.id.passwordBox);
            FormEditText verifyPasswordBox = (FormEditText) findViewById(R.id.verifyPasswordBox);
            phoneBox = (FormEditText) findViewById(R.id.phoneBox);


            phoneBox.addValidator(new FormValidators.PhoneNumberValidator());
            verifyPasswordBox.addValidator(new FormValidators.PasswordVerifyValidator(passwordBox));

            final FormEditText[] editTexts = {nameBox, emailBox, passwordBox, verifyPasswordBox, phoneBox};

            Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
            Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
            Button fbBtn = (Button) findViewById(R.id.fbBtn);
            Button googleBtn = (Button) findViewById(R.id.googleBtn);


            signUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utilities.isEditBoxesValid(editTexts)) {
                        registerUser();
                    }

                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            fbBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            googleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return null;
        }
    }


}
