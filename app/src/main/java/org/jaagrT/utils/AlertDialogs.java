package org.jaagrT.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.Alignment;
import com.afollestad.materialdialogs.MaterialDialog;

import org.jaagrT.R;

/**
 * Authored by vedhavyas on 5/12/14.
 * Project JaagrT
 */
public class AlertDialogs {


    public static AlertDialog showProgressDialog(Activity activity, String progressText) {

        final AlertDialog.Builder progressDialog = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.progress_dialog, null);
        TextView progressTextView = (TextView) view.findViewById(R.id.progressText);
        progressTextView.setText(progressText);
        progressDialog.setView(view);
        progressDialog.setCancelable(false);
        return progressDialog.show();

    }

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
}
