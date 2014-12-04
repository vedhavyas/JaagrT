package org.jaagrT.utils;

import android.app.Activity;
import android.util.Log;

import com.andreabaccega.widget.FormEditText;
import com.nispok.snackbar.Snackbar;

import org.jaagrT.R;

/**
 * Authored by vedhavyas on 3/12/14.
 * Project JaagrT
 */

public class Utilities {


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

    public static void snackIt(Activity activity, String text, String actionLabel) {

        Snackbar snackbar = Snackbar.with(activity);
        snackbar.text(text)
                .textColorResource(R.color.white)
                .actionLabel(actionLabel)
                .actionColorResource(R.color.blue)
                .show(activity);
    }

}
