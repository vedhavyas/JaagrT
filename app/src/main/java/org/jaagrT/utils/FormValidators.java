package org.jaagrT.utils;

import android.widget.EditText;

import com.andreabaccega.formedittextvalidator.Validator;
import com.andreabaccega.widget.FormEditText;

/**
 * Authored by vedhavyas on 5/12/14.
 * Project JaagrT
 */
public class FormValidators {

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
