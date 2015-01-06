package org.jaagrT.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import org.jaagrT.utilities.Utilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Authored by vedhavyas on 10/12/14.
 * Project JaagrT
 */
public class Database extends SQLiteOpenHelper {

    public static final String USER_TABLE = "user_details";
    public static final String CONTACTS_TABLE = "contacts_list";
    private static final String[] TABLES = {USER_TABLE, CONTACTS_TABLE};
    private static final String DB_NAME = "JaagrT.db";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_FIRST_NAME = "firstName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE_NUMBER = "phoneNumber";
    private static final String COLUMN_MEMBER_OF_MASTER_CIRCLE = "memberOfMasterCircle";
    private static final String COLUMN_PHONE_VERIFIED = "phoneVerified";
    private static final String COLUMN_PICTURE = "picture";
    private static final String COLUMN_THUMBNAIL_PICTURE = "thumbnailPicture";
    private static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_LAST_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_PHONE_NUMBER + " TEXT UNIQUE, "
            + COLUMN_MEMBER_OF_MASTER_CIRCLE + " INTEGER, "
            + COLUMN_PHONE_VERIFIED + " INTEGER, "
            + COLUMN_PICTURE + " BLOB ,"
            + COLUMN_THUMBNAIL_PICTURE + " BLOB)";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL_LIST = "emailList";
    private static final String SQL_CONTACT_TABLE_CREATE_QUERY = "CREATE TABLE " + CONTACTS_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_EMAIL_LIST + " TEXT,"
            + COLUMN_PICTURE + " BLOB)";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";
    private static final String SQL_SELECT_ALL_QUERY = "SELECT * FROM ";
    private static Database dbFactory;

    private Database(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static Database getInstance(Context context) {
        if (dbFactory == null) {

            dbFactory = new Database(context);
        }

        return dbFactory;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_USER_TABLE_CREATE_QUERY);
        db.execSQL(SQL_CONTACT_TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String table : TABLES) {
            db.execSQL(DROP_TABLE + table);
        }
        onCreate(db);
    }

    public void dropAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (String table : TABLES) {
            db.execSQL(DROP_TABLE + table);
        }
        onCreate(db);
    }

    public void dropTable(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DROP_TABLE + tableName);
        createDB(db, tableName);
    }

    private void createDB(SQLiteDatabase db, String tableName) {
        if (tableName.equalsIgnoreCase(USER_TABLE)) {
            db.execSQL(SQL_USER_TABLE_CREATE_QUERY);
        } else if (tableName.equalsIgnoreCase(CONTACTS_TABLE)) {
            db.execSQL(SQL_CONTACT_TABLE_CREATE_QUERY);
        }
    }


    public long saveUser(User user) {
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = getContentValuesFromUserObject(user);

        result = db.insert(USER_TABLE, null, contentValues);
        return result;
    }

    public User getUser(int id) {
        if (id > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String sqlQuery = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_ID
                    + " = " + String.valueOf(id);
            Cursor cursor = db.rawQuery(sqlQuery, null);

            if (cursor.moveToFirst()) {
                User user = new User();
                do {
                    user.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    user.setFirstName(cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME)));
                    user.setLastName(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                    user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    user.setMemberOfMasterCircleRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMBER_OF_MASTER_CIRCLE)));
                    user.setPhoneVerifiedRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_PHONE_VERIFIED)));
                    user.setThumbnailPicture(Utilities.getBitmapFromBlob(cursor.getBlob(cursor.getColumnIndex(COLUMN_THUMBNAIL_PICTURE))));
                } while (cursor.moveToNext());

                return user;
            }
            return null;

        } else {
            return null;
        }
    }

    public int updateUserData(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = -1;

        ContentValues contentValues = getContentValuesFromUserObject(user);

        try {
            result = db.update(USER_TABLE, contentValues, COLUMN_ID + " = " + user.getID(), null);
        } catch (SQLiteConstraintException e) {
            Utilities.logIt(e.getMessage());
            return result;
        }


        return result;
    }

    private ContentValues getContentValuesFromUserObject(User user) {
        ContentValues contentValues = new ContentValues();

        if (user.getFirstName() != null) {
            contentValues.put(COLUMN_FIRST_NAME, user.getFirstName());
        }

        if (user.getLastName() != null) {
            contentValues.put(COLUMN_LAST_NAME, user.getLastName());
        }

        if (user.getEmail() != null) {
            contentValues.put(COLUMN_EMAIL, user.getEmail());
        }

        if (user.getPhoneNumber() != null) {
            contentValues.put(COLUMN_PHONE_NUMBER, user.getPhoneNumber());
        }

        if (user.getPictureRaw() != null) {
            contentValues.put(COLUMN_PICTURE, user.getPictureRaw());
        }

        if (user.getThumbnailPictureRaw() != null) {
            contentValues.put(COLUMN_THUMBNAIL_PICTURE, user.getThumbnailPictureRaw());
        }

        contentValues.put(COLUMN_MEMBER_OF_MASTER_CIRCLE, user.isMemberOfMasterCircleRaw());
        contentValues.put(COLUMN_PHONE_VERIFIED, user.isPhoneVerifiedRaw());

        return contentValues;
    }

    public Bitmap getUserPicture(int id) {
        if (id > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String sqlQuery = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_ID
                    + " = " + String.valueOf(id);
            Cursor cursor = db.rawQuery(sqlQuery, null);

            if (cursor.moveToFirst()) {

                return Utilities.getBitmapFromBlob(cursor.getBlob(cursor.getColumnIndex(COLUMN_PICTURE)));
            }
            return null;

        } else {
            return null;
        }
    }

    public List<UserContact> getContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<UserContact> contacts = new ArrayList<>();

        String sqlQuery = SQL_SELECT_ALL_QUERY + CONTACTS_TABLE;
        Cursor cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                UserContact contact = new UserContact();
                contact.setID(String.valueOf(cursor.getInt(cursor.getColumnIndex(COLUMN_ID))));
                contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contact.setEmails(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL_LIST)));
                contact.setImage(cursor.getBlob(cursor.getColumnIndex(COLUMN_PICTURE)));
                contacts.add(contact);
            } while (cursor.moveToNext());

            return contacts;
        }

        return null;
    }

    public void saveContacts(List<UserContact> contacts) {
        if (contacts != null) {
            for (UserContact contact : contacts) {
                saveContact(contact);
            }
        }
    }

    private long saveContact(UserContact contact) {
        long result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValuesFromContactObject(contact);
        if (contentValues != null) {
            result = db.insert(CONTACTS_TABLE, null, contentValues);
        }
        return result;
    }

    private ContentValues getContentValuesFromContactObject(UserContact contact) {
        ContentValues contentValues = new ContentValues();

        if (contact.getName() != null) {
            contentValues.put(COLUMN_NAME, contact.getName());
        }

        if (contact.getEmails() != null) {
            contentValues.put(COLUMN_EMAIL_LIST, contact.getEmails());
        }

        if (contact.getImageBlob() != null) {
            contentValues.put(COLUMN_PICTURE, contact.getImageBlob());
        }

        if (contentValues.size() > 0) {
            return contentValues;
        } else {
            return null;
        }
    }


}
