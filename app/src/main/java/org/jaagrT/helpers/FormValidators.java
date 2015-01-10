package org.jaagrT.helpers;

import android.support.annotation.NonNull;

import com.rengwuxian.materialedittext.validation.METValidator;
import com.rengwuxian.materialedittext.validation.RegexpValidator;

/**
 * Authored by vedhavyas on 5/12/14.
 * Project JaagrT
 */
public class FormValidators {

    private static final String EMAIL_NOT_VALID = "Not a valid Email";
    private static final String EMAIL_REGEX = "[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}";

    private static final String FIELD_CANNOT_BE_EMPTY = "Field cannot be Empty";

    private static final String PHONE_NUMBER_NOT_VALID = "Not a valid Phone Number";
    private static final String PHONE_NUMBER_REGEX = "(\\+[0-9]+[\\- \\.]*)?(\\([0-9]+\\)[\\- \\.]*)?([0-9][0-9\\- \\.][0-9\\- \\.]+[0-9])";

    public static class EmailValidator extends RegexpValidator {

        public EmailValidator() {
            super(EMAIL_NOT_VALID, EMAIL_REGEX);
        }
    }

    public static class EmptyFieldValidator extends METValidator {

        public EmptyFieldValidator() {
            super(FIELD_CANNOT_BE_EMPTY);
        }

        @Override
        public boolean isValid(@NonNull CharSequence charSequence, boolean result) {
            return !result;
        }
    }

    public static class PhoneNumberValidator extends RegexpValidator {


        public PhoneNumberValidator() {
            super(PHONE_NUMBER_NOT_VALID, PHONE_NUMBER_REGEX);
        }
    }
}
