package org.jaagrT;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.nispok.snackbar.Snackbar;

import org.jaagrT.utils.Utilities;


public class Signup extends Activity {

    private FormEditText nameBox, emailBox, passwordBox, verifyPasswordBox, phoneBox;
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

    private class SetUpUI extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Instantiate UI Elements
            nameBox = (FormEditText) findViewById(R.id.nameBox);
            emailBox = (FormEditText) findViewById(R.id.emailBox);
            passwordBox = (FormEditText) findViewById(R.id.passwordBox);
            verifyPasswordBox = (FormEditText) findViewById(R.id.verifyPasswordBox);
            phoneBox = (FormEditText) findViewById(R.id.phoneBox);

            //add validators
            phoneBox.addValidator(new Utilities.PhoneNumberValidator());
            verifyPasswordBox.addValidator(new Utilities.PasswordVerifyValidator(passwordBox));

            final FormEditText[] editTexts = {nameBox, emailBox, passwordBox, verifyPasswordBox, phoneBox};

            Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
            Button cancelBtn = (Button) findViewById(R.id.cancelBtn);

            //Setup Listeners
            signUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utilities.isEditBoxesValid(editTexts)) {
                        Snackbar snackbar = Utilities.getSnackBar(activity);
                        snackbar.text("Data validated")
                                .textColorResource(R.color.white)
                                .actionLabel("Okay")
                                .actionColorResource(R.color.blue)
                                .show(activity);
                    }

                }
            });

            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            return null;
        }
    }
}
