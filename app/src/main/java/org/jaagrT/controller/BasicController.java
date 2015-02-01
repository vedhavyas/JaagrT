package org.jaagrT.controller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import org.jaagrT.helpers.BitmapHolder;
import org.jaagrT.helpers.Constants;
import org.jaagrT.helpers.ErrorHandler;
import org.jaagrT.helpers.Utilities;
import org.jaagrT.model.Contact;
import org.jaagrT.model.Database;
import org.jaagrT.model.User;
import org.jaagrT.services.ObjectService;

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
    private BitmapHolder bitmapHolder;

    private BasicController(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(Constants.PREFERENCES_NAME, Activity.MODE_PRIVATE);
        this.db = Database.getInstance(context);
        this.bitmapHolder = BitmapHolder.getInstance(context);
    }

    public static BasicController getInstance(Context context) {
        if (basicController == null) {
            basicController = new BasicController(context);
        }

        return basicController;
    }

    public User getUser() {
        return db.getUser(prefs.getInt(Constants.LOCAL_USER_ID, -1));
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public int saveUser(User user) {
        return (int) db.saveUser(user);
    }

    public int updateUser(User user) {
        return db.updateUserData(user);
    }

    public void dropTable(String table) {
        db.dropTable(table);
    }

    public void saveContacts(List<Contact> contacts) {
        db.saveContacts(contacts);
    }

    public List<Contact> getContacts() {
        return db.getContacts();
    }

    public Contact getContact(int contactID) {
        return db.getContact(contactID);
    }

    public void saveCircles(List<ParseObject> parseObjects, Contact contact) {
        if (parseObjects.size() > 0) {
            List<User> circles = new ArrayList<>();
            for (final ParseObject parseObject : parseObjects) {
                final User circle = new User();
                circle.setObjectID(parseObject.getObjectId());
                if (parseObject.getString(Constants.USER_FIRST_NAME) == null) {
                    if (contact != null) {
                        circle.setFirstName(contact.getName());
                    } else {
                        String[] emailSet = parseObject.getString(Constants.USER_PRIMARY_EMAIL).split("@");
                        circle.setFirstName(emailSet[0]);
                    }
                } else {
                    circle.setFirstName(parseObject.getString(Constants.USER_FIRST_NAME));
                }
                circle.setLastName(parseObject.getString(Constants.USER_LAST_NAME));
                circle.setPhoneNumber(parseObject.getString(Constants.USER_PRIMARY_PHONE));
                circle.setPhoneVerified(parseObject.getBoolean(Constants.USER_PRIMARY_PHONE_VERIFIED));
                circle.setMemberOfMasterCircle(parseObject.getBoolean(Constants.USER_MEMBER_OF_MASTER_CIRCLE));
                circle.setEmail(parseObject.getString(Constants.USER_PRIMARY_EMAIL));
                if (parseObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE) != null) {
                    try {
                        Bitmap bitmap = Utilities.getBitmapFromBlob(parseObject.getParseFile(Constants.USER_THUMBNAIL_PICTURE).getData());
                        bitmapHolder.saveBitmapThumb(parseObject.getString(Constants.USER_PRIMARY_EMAIL), bitmap);
                    } catch (ParseException e) {
                        ErrorHandler.handleError(null, e);
                    }
                }
                if (parseObject.getParseFile(Constants.USER_PROFILE_PICTURE) != null) {
                    parseObject.getParseFile(Constants.USER_PROFILE_PICTURE).getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e == null) {
                                Bitmap bitmap = Utilities.getBitmapFromBlob(bytes);
                                bitmapHolder.saveBitmapImageAsync(parseObject.getString(Constants.USER_PRIMARY_EMAIL), bitmap);
                            } else {
                                ErrorHandler.handleError(null, e);
                            }
                        }
                    });
                }
                circles.add(circle);
            }

            db.saveCircles(circles);
        }
    }

    public void updateCirclesThroughObjects(List<ParseObject> circles) {
        db.dropTable(Database.CIRCLES_TABLE);
        saveCircles(circles, null);
    }

    public void updateCircles(List<User> circles) {
        db.dropTable(Database.CIRCLES_TABLE);
        db.saveCircles(circles);
    }

    public List<User> getCircles() {
        return db.getCircles();
    }

    public List<String> getCircleObjectIDs() {
        return db.getCircleObjectIDs();
    }

    public int getEntryCount(String tableName) {
        return db.getEntryCount(tableName);
    }

    public int deleteCircle(int circleID) {
        return db.deleteCircle(circleID);
    }

    public void deleteCircles(List<User> circles) {
        List<String> objectIDs = new ArrayList<>();
        if (circles != null) {
            for (User circle : circles) {
                objectIDs.add(circle.getObjectID());
                deleteCircle(circle.getID());
                bitmapHolder.deleteImageAsync(circle.getEmail());
            }
        }

        ObjectService.removeCircles(objectIDs);
    }

    public User getCircle(int circleID) {
        return db.getCircle(circleID);
    }
}
