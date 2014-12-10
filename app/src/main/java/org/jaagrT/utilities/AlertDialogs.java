package org.jaagrT.utilities;

import android.app.Activity;
import android.graphics.Color;

import com.afollestad.materialdialogs.Alignment;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jaagrT.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Authored by vedhavyas on 5/12/14.
 * Project JaagrT
 */
public class AlertDialogs {


    public static void showErrorDialog(Activity activity, String title, String content, String negativeBtnText) {

        MaterialDialog.Builder errorDialog = new MaterialDialog.Builder(activity);
        errorDialog.title(title)
                .content(content)
                .negativeText(negativeBtnText)
                .titleColor(activity.getResources().getColor(R.color.red_dark))
                .negativeColor(activity.getResources().getColor(R.color.red_dark))
                .titleAlignment(Alignment.CENTER)
                .show();
    }

    public static SweetAlertDialog showSweetProgress(Activity activity) {
        SweetAlertDialog pDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setCancelable(false);
        return pDialog;
    }
}
