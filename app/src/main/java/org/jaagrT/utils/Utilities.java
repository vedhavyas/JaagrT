package org.jaagrT.utils;

import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import com.andreabaccega.formedittextvalidator.Validator;
import com.andreabaccega.widget.FormEditText;
import com.nispok.snackbar.Snackbar;

import org.jaagrT.R;

/**
 * Authored by vedhavyas on 3/12/14.
 * Project JaagrT
 */

public class Utilities {


    public static Snackbar getSnackBar(Context context) {
        return Snackbar.with(context)
                .colorResource(R.color.white);
    }

    public static void logIt(String data) {
        Log.i("JaagrT", data);
    }

    public static boolean isEditBoxesValid(FormEditText[] editTexts) {
        boolean allValid = true;

        for (FormEditText editText : editTexts) {
            allValid = editText.testValidity() && allValid;
        }

        return allValid;
    }

    public static class PhoneNumberValidator extends Validator {

        public PhoneNumberValidator() {
            super("Phone Number is not valid");
        }

        @Override
        public boolean isValid(EditText editText) {
            return editText.getText().length() >= 10;
        }
    }

    public static class PasswordVerifyValidator extends Validator {

        private FormEditText passwordBox;

        public PasswordVerifyValidator(FormEditText passwordBox) {
            super("Passwords didn't match");
            this.passwordBox = passwordBox;
        }

        @Override
        public boolean isValid(EditText verifyPasswordBox) {
            return passwordBox.getText().toString().isEmpty() && !verifyPasswordBox.getText().toString().isEmpty() ||
                    verifyPasswordBox.getText().toString().equals(passwordBox.getText().toString());
        }
    }
}
