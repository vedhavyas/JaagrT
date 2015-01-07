package org.jaagrT.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.model.UserContact;
import org.jaagrT.utilities.Constants;

import java.util.List;

/**
 * Authored by vedhavyas on 14/12/14.
 * Project JaagrT
 */
public class BasicController {
    private static BasicController basicController;
    private Context context;
    private SharedPreferences prefs;
    private Database db;

    private BasicController(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        this.db = Database.getInstance(context);
    }

    public static BasicController getInstance(Context context) {
        if (basicController == null) {
            basicController = new BasicController(context);
        }

        return basicController;
    }

    public User getLocalUser() {
        return db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public Bitmap getUserPicture() {
        return db.getUserPicture(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public int saveUser(User user) {
        return (int) db.saveUser(user);
    }

    public int updateUser(User user) {
        return db.updateUserData(user);
    }

    public void dropAllTables() {
        db.dropAllTables();
    }

    public void dropTable(String table) {
        db.dropTable(table);
    }

    public void saveContacts(List<UserContact> contacts) {
        db.saveContacts(contacts);
    }

    public List<UserContact> getContacts() {
        return db.getContacts();
    }

    public UserContact getContact(int contactID) {
        return db.getContact(contactID);
    }
}
