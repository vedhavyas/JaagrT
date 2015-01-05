package org.jaagrT.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.utilities.Constants;

/**
 * Authored by vedhavyas on 14/12/14.
 * Project JaagrT
 */
public class BasicController {
    private static BasicController basicController;
    private Context context;
    private SharedPreferences prefs;

    private BasicController(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    public static BasicController getInstance(Context context) {
        if (basicController == null) {
            basicController = new BasicController(context);
        }

        return basicController;
    }

    public User getLocalUser() {
        Database db = Database.getInstance(context, Database.USER_TABLE);
        return db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public Bitmap getUserPicture() {
        Database db = Database.getInstance(context, Database.USER_TABLE);
        return db.getUserPicture(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public int saveUser(User user) {
        Database db = Database.getInstance(context, Database.USER_TABLE);
        return (int) db.saveUser(user);
    }

    public int updateUser(User user) {
        Database db = Database.getInstance(context, Database.USER_TABLE);
        return db.updateUserData(user);
    }

    public void dropAllTables() {
        Database db = Database.getInstance(context);
        db.dropAllTables();
    }
}
