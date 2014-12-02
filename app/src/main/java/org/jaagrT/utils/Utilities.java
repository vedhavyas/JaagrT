package org.jaagrT.utils;

import android.content.Context;
import android.util.Log;

import com.nispok.snackbar.Snackbar;

import org.jaagrT.R;

/**
 * Authored by vedhavyas on 3/12/14.
 * Project JaagrT
 */

public class Utilities {


    public static Snackbar getSnackBar(Context context) {
        return Snackbar.with(context)
                .colorResource(R.color.orange_light);
    }

    public static void logIt(String data) {
        Log.i("JaagrT", data);
    }
}
