package org.jaagrT.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.jaagrT.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

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

    public static SweetAlertDialog errorDialog(Activity activity, String titleMessage, String errorMessage, String confirmText) {
        SweetAlertDialog warningDialog = new SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE);
        warningDialog.setContentText(errorMessage);
        warningDialog.setConfirmText(confirmText);
        warningDialog.setTitleText(titleMessage);
        warningDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.cancel();
            }
        });

        return warningDialog;
    }
}
