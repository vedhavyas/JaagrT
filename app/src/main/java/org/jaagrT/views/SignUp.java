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
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.FormValidators;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.listeners.BasicListener;
import org.jaagrT.services.ObjectService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SignUp extends Activity {

    private static final String SIGNING_UP = "Signing up...";
    private MaterialEditText emailBox, passwordBox;
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
        overridePendingTransition(R.anim.push_right_screen, R.anim.push_screen_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void setUpActivity() {
        emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        passwordBox = (MaterialEditText) findViewById(R.id.passwordBox);
        emailBox.addValidator(new FormValidators.EmptyFieldValidator());
        emailBox.addValidator(new FormValidators.EmailValidator());
        passwordBox.addValidator(new FormValidators.EmptyFieldValidator());

        final List<MaterialEditText> editTexts = new ArrayList<>();
        editTexts.addAll(Arrays.asList(emailBox, passwordBox));

        Button signUpBtn = (Button) findViewById(R.id.signUpBtn);
        Button backBtn = (Button) findViewById(R.id.backBtn);
        final Button fbBtn = (Button) findViewById(R.id.facebookBtn);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                    pDialog.setTitleText(SIGNING_UP);
                    pDialog.show();
                    SignUpController signUpController = new SignUpController(activity, new BasicListener() {
                        @Override
                        public void onComplete() {
                            startGetUserDetails();
                        }
                    }, pDialog);
                    signUpController.registerUser(emailBox.getText().toString(), passwordBox.getText().toString());
                }

            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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
                                AlertDialogs.showErrorDialog(activity, ErrorHandler.ERROR, ErrorHandler.CONNECTION_FAILED, ErrorHandler.OKAY);
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
                            ErrorHandler.handleError(activity, e);
                        }
                    }
                });
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
