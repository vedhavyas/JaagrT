package org.jaagrT.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.controller.LoginController;
import org.jaagrT.controller.SignUpController;
import org.jaagrT.listeners.BasicListener;
import org.jaagrT.services.ObjectService;
import org.jaagrT.utilities.AlertDialogs;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.FormValidators;
import org.jaagrT.utilities.Utilities;

import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Login extends Activity {

    private static final String LOGIN_SUCCESS = "Login Successful!!";
    private static final String LOGGING_IN = "Authenticating...";
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        activity = this;
        setUpActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private void setUpActivity() {

        Button fbBtn = (Button) findViewById(R.id.facebookBtn);
        final Button backBtn = (Button) findViewById(R.id.backBtn);
        Button loginBtn = (Button) findViewById(R.id.loginBtn);
        Button forgotPassBtn = (Button) findViewById(R.id.forgotPasswordBtn);
        final MaterialEditText emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        final MaterialEditText passwordBox = (MaterialEditText) findViewById(R.id.passwordBox);

        emailBox.addValidator(new FormValidators.EmptyFieldValidator());
        emailBox.addValidator(new FormValidators.EmailValidator());
        passwordBox.addValidator(new FormValidators.EmptyFieldValidator());
        final MaterialEditText[] editTexts = {emailBox, passwordBox};


        fbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                pDialog.setTitleText(Constants.CONNECTING_TO_FB);
                pDialog.show();
                ParseFacebookUtils.logIn(Arrays.asList(ParseFacebookUtils.Permissions.User.EMAIL), activity, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (e == null) {
                            if (parseUser == null) {
                                pDialog.cancel();
                                AlertDialogs.showErrorDialog(activity, Constants.ERROR, Constants.CONNECTION_FAILED, Constants.OKAY);
                            } else if (parseUser.isNew()) {
                                pDialog.setTitleText(Constants.CONNECTION_ESTABLISHED);
                                SignUpController signUpController = new SignUpController(activity, new BasicListener() {

                                    @Override
                                    public void onComplete() {
                                        startGetUserDetails();
                                    }
                                }, pDialog);
                                signUpController.facebookRegistration();
                            } else {
                                pDialog.setTitleText(Constants.CONNECTION_ESTABLISHED);
                                LoginController loginController = new LoginController(activity, new BasicListener() {
                                    @Override
                                    public void onComplete() {
                                        startMainActivity();
                                    }
                                });
                                loginController.getUserData(parseUser, pDialog);
                            }
                        } else {
                            pDialog.cancel();
                            if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                AlertDialogs.showErrorDialog(activity, Constants.ERROR, Constants.CHECK_INTERNET, Constants.OKAY);
                            } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                                AlertDialogs.showErrorDialog(activity, Constants.ERROR, Constants.INTER_SERVER_ERROR, Constants.OKAY);
                            } else {
                                AlertDialogs.showErrorDialog(activity, Constants.ERROR, e.getMessage(), Constants.OKAY);
                            }
                        }
                    }
                });
            }
        });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                    pDialog.setTitleText(LOGGING_IN);
                    pDialog.show();
                    ParseUser.logInInBackground(emailBox.getText().toString(), passwordBox.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {
                                pDialog.setTitleText(LOGIN_SUCCESS);
                                LoginController loginController = new LoginController(activity, new BasicListener() {
                                    @Override
                                    public void onComplete() {
                                        startMainActivity();
                                    }
                                });
                                loginController.getUserData(user, pDialog);
                            } else {
                                pDialog.cancel();
                                if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                    AlertDialogs.showErrorDialog(activity, Constants.ERROR, Constants.CHECK_INTERNET, Constants.OKAY);
                                } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                                    AlertDialogs.showErrorDialog(activity, Constants.ERROR, Constants.INTER_SERVER_ERROR, Constants.OKAY);
                                } else {
                                    AlertDialogs.showErrorDialog(activity, Constants.ERROR, e.getMessage(), Constants.OKAY);
                                }
                            }
                        }
                    });
                }
            }
        });

        forgotPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void startGetUserDetails() {
        startAppService();
        Intent intent = new Intent(this, UserAdditionalInfo.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private void startMainActivity() {
        startAppService();
        Intent mainActivityIntent = new Intent(activity, Main.class);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainActivityIntent);
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    private void startAppService() {
        Intent serviceIntent = new Intent(this, ObjectService.class);
        startService(serviceIntent);
    }
}
