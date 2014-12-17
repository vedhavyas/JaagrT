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
                                        startPickPictureActivity();
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
                    final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                    pDialog.setTitleText("Logging you in...");
                    pDialog.show();
                    ParseUser.logInInBackground(emailBox.getText().toString(), passwordBox.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {
                                pDialog.setTitleText("Logged in...");
                                LoginController loginController = new LoginController(activity, new LoginListener() {
                                    @Override
                                    public void onComplete() {
                                        Utilities.snackIt(activity, "Download Complete", "Okay");
                                    }
                                });
                                loginController.getUserData(user, pDialog);
                            } else {
                                pDialog.cancel();
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

    private void startPickPictureActivity() {
        Intent pickPictureIntent = new Intent(this, PickPicture.class);
        pickPictureIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        pickPictureIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(pickPictureIntent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }
}
