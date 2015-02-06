package org.jaagrT.views;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jaagrT.R;
import org.jaagrT.helpers.AlertDialogs;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.FormValidators;
import org.jaagrT.helpers.Utilities;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPassword extends Activity {

    private static final String PLEASE_WAIT = "Please wait...";
    private static final String CHECK_INBOX = "Please check your Email for further instructions";
    private static final String OKAY = "Okay";
    private static final String SUCCESS = "Success";
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        setUpActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_screen, R.anim.push_screen_right);
    }

    private void setUpActivity() {
        activity = this;
        Button resetBtn = (Button) findViewById(R.id.resetPassword);
        final MaterialEditText emailBox = (MaterialEditText) findViewById(R.id.emailBox);
        emailBox.addValidator(new FormValidators.EmptyFieldValidator())
                .addValidator(new FormValidators.EmailValidator());
        final List<MaterialEditText> editTexts = new ArrayList<>();
        editTexts.add(emailBox);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Utilities.isEditBoxesValid(editTexts)) {
                    final SweetAlertDialog pDialog = AlertDialogs.showSweetProgress(activity);
                    pDialog.setTitleText(PLEASE_WAIT);
                    pDialog.show();
                    ParseUser.requestPasswordResetInBackground(emailBox.getText().toString(), new RequestPasswordResetCallback() {
                        @Override
                        public void done(ParseException e) {
                            pDialog.cancel();
                            if (e == null) {
                                showSuccessDialog();
                            } else {
                                ErrorHandler.handleError(activity, e);
                            }
                        }
                    });
                }
            }
        });
    }

    private void showSuccessDialog() {
        new MaterialDialog.Builder(activity)
                .title(SUCCESS)
                .titleColor(getResources().getColor(R.color.teal_400))
                .autoDismiss(false)
                .cancelable(false)
                .positiveText(OKAY)
                .positiveColor(getResources().getColor(R.color.teal_400))
                .content(CHECK_INBOX)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        activity.onBackPressed();
                    }
                })
                .build()
                .show();
    }
}
