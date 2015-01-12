package org.jaagrT.helpers;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.parse.ParseException;

/**
 * Authored by vedhavyas on 10/1/15.
 * Project JaagrT
 */
public class ErrorHandler {

    public static final String ERROR = "Error";
    public static final String OKAY = "Okay";
    public static final String ERROR_UNKNOWN = "Unknown Error!!";
    public static final String CHECK_INTERNET = "Connection timed-out. Please check your Internet connection.";
    public static final String ACCOUNT_ALREADY_LINKED = "Account already linked";
    public static final String EMAIL_ALREADY_TAKEN = "Email already exists";
    public static final String EMAIL_NOT_FOUND = "Email not found";
    public static final String EMAIL_MISSING = "Email missing";
    public static final String EMAIL_INVALID = "Invalid Email";
    public static final String CONNECTION_FAILED = "Connection failed!!";

    public static void handleError(Activity activity, ParseException e) {

        if (activity != null && e != null) {
            if (e.getCode() == ParseException.ACCOUNT_ALREADY_LINKED) {
                AlertDialogs.showErrorDialog(activity, ERROR, ACCOUNT_ALREADY_LINKED, OKAY);
            } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                AlertDialogs.showErrorDialog(activity, ERROR, CHECK_INTERNET, OKAY);
            } else if (e.getCode() == ParseException.EMAIL_TAKEN) {
                AlertDialogs.showErrorDialog(activity, ERROR, EMAIL_ALREADY_TAKEN, OKAY);
            } else if (e.getCode() == ParseException.EMAIL_NOT_FOUND) {
                AlertDialogs.showErrorDialog(activity, ERROR, EMAIL_NOT_FOUND, OKAY);
            } else if (e.getCode() == ParseException.EMAIL_MISSING) {
                AlertDialogs.showErrorDialog(activity, ERROR, EMAIL_MISSING, OKAY);
            } else if (e.getCode() == ParseException.INTERNAL_SERVER_ERROR) {
                AlertDialogs.showErrorDialog(activity, ERROR, CHECK_INTERNET, OKAY);
            } else if (e.getCode() == ParseException.INVALID_EMAIL_ADDRESS) {
                AlertDialogs.showErrorDialog(activity, ERROR, EMAIL_INVALID, OKAY);
            } else if (e.getCode() == ParseException.TIMEOUT) {
                AlertDialogs.showErrorDialog(activity, ERROR, CHECK_INTERNET, OKAY);
            } else if (e.getCode() == ParseException.USERNAME_TAKEN) {
                AlertDialogs.showErrorDialog(activity, ERROR, EMAIL_ALREADY_TAKEN, OKAY);
            } else {
                AlertDialogs.showErrorDialog(activity, ERROR, e.getMessage(), OKAY);
            }
        } else if (activity == null && e != null) {
            Utilities.logData(e.getMessage(), Log.ERROR);
        }

    }

    public static void handleError(Activity activity, Exception e) {
        if (activity == null && e != null) {
            Utilities.logData(e.getMessage(), Log.ERROR);
        } else if (activity != null && e != null) {
            AlertDialogs.showErrorDialog(activity, ERROR, e.getMessage(), OKAY);
        }
    }

    public static void handleError(SQLiteException e) {
        if (e != null) {
            Utilities.logData(e.getMessage(), Log.ERROR);
        }
    }
}
