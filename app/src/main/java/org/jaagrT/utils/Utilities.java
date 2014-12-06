package org.jaagrT.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.andreabaccega.widget.FormEditText;
import com.nispok.snackbar.Snackbar;

import org.jaagrT.R;

import java.io.ByteArrayOutputStream;

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
                .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                .show(activity);
    }

    public static byte[] getBlob(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] blob = null;
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            blob = stream.toByteArray();
        }

        return blob;
    }

    public static Bitmap getBitmapFromBlob(byte[] blob) {
        Bitmap bitmap = null;
        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }
        return bitmap;
    }

}
