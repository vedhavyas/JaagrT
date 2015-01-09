package org.jaagrT.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.model.UserContact;
import org.jaagrT.utilities.Constants;
import org.jaagrT.utilities.Utilities;

import java.util.ArrayList;
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

    public void saveCircles(List<ParseObject> parseObjects) {
        if (parseObjects.size() > 0) {
            List<User> circles = new ArrayList<>();
            for (ParseObject parseObject : parseObjects) {
                final User circle = new User();
                circle.setObjectID(parseObject.getObjectId());
                circle.setFirstName(parseObject.getString(Constants.USER_FIRST_NAME));
                circle.setLastName(parseObject.getString(Constants.USER_LAST_NAME));
                circle.setPhoneNumber(parseObject.getString(Constants.USER_PRIMARY_PHONE));
                circle.setPhoneVerified(parseObject.getBoolean(Constants.USER_PRIMARY_PHONE_VERIFIED));
                circle.setMemberOfMasterCircle(parseObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                circle.setEmail(parseObject.getString(Constants.USER_PRIMARY_EMAIL));
                if (parseObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE) != null) {
                    parseObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE)
                            .getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] thumbnailBytes, ParseException e) {
                                    if (e == null) {
                                        circle.setThumbnailPicture(Utilities.getBitmapFromBlob(thumbnailBytes));
                                    }
                                }
                            });
                }
                circles.add(circle);
            }

            db.saveCircles(circles);
        }
    }

    public List<User> getCircles() {
        return db.getCircles();
    }

    public User getCircle(int circleID) {
        return db.getCircle(circleID);
    }

}
