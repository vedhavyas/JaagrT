package org.jaagrT;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.andreabaccega.widget.FormEditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.jaagrT.controller.LoginController;
import org.jaagrT.controller.RegistrationController;
import org.jaagrT.listeners.LoginListener;
import org.jaagrT.listeners.RegisterListener;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Utilities;

import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Register extends Activity {

    private FormEditText emailBox, passwordBox;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        activity = this;
        setUpActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void setUpActivity() {
        emailBox = (FormEditText) findViewById(R.id.emailBox);
        passwordBox = (FormEditText) findViewById(R.id.passwordBox);

        final FormEditText[] editTexts = {emailBox, passwordBox};

        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
        final Button fbBtn = (Button) findViewById(R.id.fbBtn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                    pDialog.setTitleText("Registering you now...");
                    pDialog.show();
                    RegistrationController registrationController = new RegistrationController(activity, new RegisterListener() {
                        @Override
                        public void onComplete() {
                            startGetUserDetails();
                        }
                    }, pDialog);
                    registrationController.registerUser(emailBox.getText().toString(), passwordBox.getText().toString());
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
                final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                pDialog.setTitleText("Connecting to Facebook...");
                pDialog.show();
                ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL), activity, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (e == null) {
                            if (parseUser == null) {
                                pDialog.cancel();
                                Utilities.snackIt(activity, "Failed to login with Facebook", "Okay");
                            } else if (parseUser.isNew()) {
                                pDialog.setTitleText("Connected to Facebook...");
                                RegistrationController registrationController = new RegistrationController(activity, new RegisterListener() {

                                    @Override
                                    public void onComplete() {
                                        startGetUserDetails();
                                    }
                                }, pDialog);
                                registrationController.facebookRegistration();
                            } else {
                                pDialog.setTitleText("Connected to Facebook...");
                                LoginController loginController = new LoginController(activity, new LoginListener() {
                                    @Override
                                    public void onComplete() {
                                        Utilities.snackIt(activity, "Download Complete", "Okay");
                                    }
                                });
                                loginController.getUserData(parseUser, pDialog);
                            }
                        } else {
                            pDialog.cancel();
                            AlertDialogs.showErrorDialog(activity, "Login Error", e.getMessage(), "Oops!");
                        }
                    }
                });
            }
        });

    }


    private void startGetUserDetails() {
        Intent intent = new Intent(this, GetUserDetails.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

}
