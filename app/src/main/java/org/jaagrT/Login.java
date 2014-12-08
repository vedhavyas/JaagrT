package org.jaagrT;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.jaagrT.utils.AlertDialogs;
import org.jaagrT.utils.Utilities;


public class Login extends Activity {

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity = this;
        setUpActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void setUpActivity() {

        Button fbBtn = (Button) findViewById(R.id.fbBtn);
        final Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button forgotPassBtn = (Button) findViewById(R.id.forgotPasswordBtn);
        final FormEditText emailBox = (FormEditText) findViewById(R.id.emailBox);
        final FormEditText passwordBox = (FormEditText) findViewById(R.id.passwordBox);
        final FormEditText[] editTexts = {emailBox, passwordBox};


        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(activity, Signup.class);
                startActivity(signUpIntent);
                overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    final AlertDialog loginDialog = AlertDialogs.showProgressDialog(activity, "Logging you in...");
                    ParseUser.logInInBackground(emailBox.getText().toString(), passwordBox.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            loginDialog.cancel();
                            if (e == null) {
                                Utilities.snackIt(activity, "Login Successful", "Okay");
                            } else {
                                AlertDialogs.showErrorDialog(activity, "Login Error", e.getMessage(), "Oops!");
                            }
                        }
                    });
                }
            }
        });

        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cropIntent = new Intent(activity, UserProfileEdit.class);
                startActivity(cropIntent);
            }
        });
    }
}
