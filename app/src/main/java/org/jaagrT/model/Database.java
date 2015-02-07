package org.jaagrT.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jaagrT.helpers.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Authored by vedhavyas on 10/12/14.
 * Project JaagrT
 */
public class Database extends SQLiteOpenHelper {

    public static final String USER_TABLE = "user_details";
    public static final String CONTACTS_TABLE = "contacts_list";
    public static final String CIRCLES_TABLE = "user_circles_list";
    public static final String INVITATION_TABLE = "invitation_table";
    private static final String[] TABLES = {USER_TABLE, CONTACTS_TABLE, CIRCLES_TABLE, INVITATION_TABLE};
    private static final String DB_NAME = "JaagrT.db";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_OBJECT_ID = "objectID";
    private static final String COLUMN_FIRST_NAME = "firstName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_EMAIL = "email";
    private static final String SQL_INVITATION_TABLE_CREATE_QUERY = "CREATE TABLE " + INVITATION_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMAIL + " TEXT UNIQUE)";
    private static final String COLUMN_PHONE_NUMBER = "phoneNumber";
    private static final String COLUMN_MEMBER_OF_MASTER_CIRCLE = "memberOfMasterCircle";
    private static final String COLUMN_PHONE_VERIFIED = "phoneVerified";
    private static final String COLUMN_SECONDARY_EMAILS = "secondaryEmails";
    private static final String COLUMN_SECONDARY_PHONES = "secondaryPhones";
    private static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OBJECT_ID + " TEXT,"
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_LAST_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_SECONDARY_EMAILS + " TEXT,"
            + COLUMN_SECONDARY_PHONES + " TEXT,"
            + COLUMN_PHONE_NUMBER + " TEXT UNIQUE, "
            + COLUMN_MEMBER_OF_MASTER_CIRCLE + " INTEGER, "
            + COLUMN_PHONE_VERIFIED + " INTEGER)";
    private static final String SQL_CIRCLES_TABLE_CREATE_QUERY = "CREATE TABLE " + CIRCLES_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OBJECT_ID + " TEXT,"
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_LAST_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_SECONDARY_EMAILS + " TEXT,"
            + COLUMN_SECONDARY_PHONES + " TEXT,"
            + COLUMN_PHONE_NUMBER + " TEXT UNIQUE, "
            + COLUMN_MEMBER_OF_MASTER_CIRCLE + " INTEGER, "
            + COLUMN_PHONE_VERIFIED + " INTEGER)";
    private static final String COLUMN_CONTACT_ID = "contactID";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL_LIST = "emailList";
    private static final String SQL_CONTACT_TABLE_CREATE_QUERY = "CREATE TABLE " + CONTACTS_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CONTACT_ID + " TEXT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_EMAIL_LIST + " TEXT)";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";


    private static Database dbFactory;
    private Cursor cursor;

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
        db.execSQL(SQL_CIRCLES_TABLE_CREATE_QUERY);
        db.execSQL(SQL_INVITATION_TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
        } else if (tableName.equalsIgnoreCase(CIRCLES_TABLE)) {
            db.execSQL(SQL_CIRCLES_TABLE_CREATE_QUERY);
        } else if (tableName.equalsIgnoreCase(INVITATION_TABLE)) {
            db.execSQL(SQL_INVITATION_TABLE_CREATE_QUERY);
        }
    }


    public long saveUser(User user) {
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = getContentValuesFromUserObject(user);

        result = db.insert(USER_TABLE, null, contentValues);
        return result;
    }

    public User getUser(int userID) {
        if (userID > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String selectQuery = "SELECT " + COLUMN_ID
                    + "," + COLUMN_OBJECT_ID
                    + "," + COLUMN_FIRST_NAME
                    + "," + COLUMN_LAST_NAME
                    + "," + COLUMN_EMAIL
                    + "," + COLUMN_PHONE_NUMBER
                    + "," + COLUMN_MEMBER_OF_MASTER_CIRCLE
                    + "," + COLUMN_PHONE_VERIFIED
                    + "," + COLUMN_SECONDARY_EMAILS
                    + "," + COLUMN_SECONDARY_PHONES
                    + " FROM "
                    + USER_TABLE
                    + " WHERE "
                    + COLUMN_ID
                    + " = "
                    + String.valueOf(userID);

            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                User user = new User();
                do {
                    user.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    user.setObjectID(cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID)));
                    user.setFirstName(cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME)));
                    user.setLastName(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                    user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    user.setMemberOfMasterCircleRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMBER_OF_MASTER_CIRCLE)));
                    user.setPhoneVerifiedRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_PHONE_VERIFIED)));
                    user.setSecondaryEmailsRaw(cursor.getString(cursor.getColumnIndex(COLUMN_SECONDARY_EMAILS)));
                    user.setSecondaryPhonesRaw(cursor.getString(cursor.getColumnIndex(COLUMN_SECONDARY_PHONES)));
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
            ErrorHandler.handleError(e);
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

        if (user.getObjectID() != null) {
            contentValues.put(COLUMN_OBJECT_ID, user.getObjectID());
        }

        if (user.getSecondaryEmailsRaw() != null) {
            contentValues.put(COLUMN_SECONDARY_EMAILS, user.getSecondaryEmailsRaw());
        }

        if (user.getSecondaryPhonesRaw() != null) {
            contentValues.put(COLUMN_SECONDARY_PHONES, user.getSecondaryPhonesRaw());
        }

        contentValues.put(COLUMN_MEMBER_OF_MASTER_CIRCLE, user.isMemberOfMasterCircleRaw());
        contentValues.put(COLUMN_PHONE_VERIFIED, user.isPhoneVerifiedRaw());

        return contentValues;
    }

    public List<Contact> getContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Contact> contacts = new ArrayList<>();

        String sqlQuery = "SELECT * FROM " + CONTACTS_TABLE;
        cursor = db.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                contact.setContactID(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_ID)));
                contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contact.setEmails(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL_LIST)));
                contacts.add(contact);
            } while (cursor.moveToNext());

            return contacts;
        }

        return null;
    }

    public void saveContacts(List<Contact> contacts) {
        if (contacts != null) {
            for (Contact contact : contacts) {
                saveContact(contact);
            }
        }
    }

    private long saveContact(Contact contact) {
        long result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValuesFromContactObject(contact);
        if (contentValues != null) {
            result = db.insert(CONTACTS_TABLE, null, contentValues);
        }
        return result;
    }

    private ContentValues getContentValuesFromContactObject(Contact contact) {
        ContentValues contentValues = new ContentValues();

        if (contact.getName() != null) {
            contentValues.put(COLUMN_NAME, contact.getName());
        }

        if (contact.getContactID() != null) {
            contentValues.put(COLUMN_CONTACT_ID, contact.getContactID());
        }

        if (contact.getEmails() != null) {
            contentValues.put(COLUMN_EMAIL_LIST, contact.getEmails());
        }

        if (contentValues.size() > 0) {
            return contentValues;
        } else {
            return null;
        }
    }

    public Contact getContact(int contactID) {
        if (contactID > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String sqlQuery = "SELECT * FROM " + CONTACTS_TABLE + " WHERE " + COLUMN_ID
                    + " = " + String.valueOf(contactID);
            cursor = db.rawQuery(sqlQuery, null);

            if (cursor.moveToFirst()) {
                Contact contact = new Contact();
                do {
                    contact.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    contact.setContactID(cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_ID)));
                    contact.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                    contact.setEmails(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL_LIST)));
                } while (cursor.moveToNext());

                return contact;
            }
            return null;

        } else {
            return null;
        }

    }

    public void saveCircles(List<User> circles) {
        if (circles != null) {
            for (User circle : circles) {
                saveCircle(circle);
            }
        }
    }

    public long saveCircle(User circle) {
        long result = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValuesFromUserObject(circle);
        if (contentValues != null) {
            result = db.insert(CIRCLES_TABLE, null, contentValues);
        }
        return result;
    }

    public List<User> getCircles() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> circles = new ArrayList<>();

        String selectQuery = "SELECT " + COLUMN_ID
                + "," + COLUMN_OBJECT_ID
                + "," + COLUMN_FIRST_NAME
                + "," + COLUMN_LAST_NAME
                + "," + COLUMN_EMAIL
                + "," + COLUMN_PHONE_NUMBER
                + "," + COLUMN_MEMBER_OF_MASTER_CIRCLE
                + "," + COLUMN_PHONE_VERIFIED
                + "," + COLUMN_SECONDARY_EMAILS
                + "," + COLUMN_SECONDARY_PHONES
                + " FROM "
                + CIRCLES_TABLE;

        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                User circle = new User();
                circle.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                circle.setObjectID(cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID)));
                circle.setFirstName(cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME)));
                circle.setLastName(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME)));
                circle.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                circle.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                circle.setMemberOfMasterCircleRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMBER_OF_MASTER_CIRCLE)));
                circle.setPhoneVerifiedRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_PHONE_VERIFIED)));
                circle.setSecondaryEmailsRaw(cursor.getString(cursor.getColumnIndex(COLUMN_SECONDARY_EMAILS)));
                circle.setSecondaryPhonesRaw(cursor.getString(cursor.getColumnIndex(COLUMN_SECONDARY_PHONES)));
                circles.add(circle);
            } while (cursor.moveToNext());
        }

        return circles;
    }

    public List<String> getCircleObjectIDs() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> objectIDs = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_OBJECT_ID
                + " FROM "
                + CIRCLES_TABLE;

        cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                objectIDs.add(cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID)));
            } while (cursor.moveToNext());
        }

        return objectIDs;
    }

    public User getCircle(int circleID) {
        if (circleID > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String selectQuery = "SELECT " + COLUMN_ID
                    + "," + COLUMN_OBJECT_ID
                    + "," + COLUMN_FIRST_NAME
                    + "," + COLUMN_LAST_NAME
                    + "," + COLUMN_EMAIL
                    + "," + COLUMN_PHONE_NUMBER
                    + "," + COLUMN_MEMBER_OF_MASTER_CIRCLE
                    + "," + COLUMN_PHONE_VERIFIED
                    + "," + COLUMN_SECONDARY_EMAILS
                    + "," + COLUMN_SECONDARY_PHONES
                    + " FROM "
                    + CIRCLES_TABLE
                    + " WHERE "
                    + COLUMN_ID
                    + " = "
                    + String.valueOf(circleID);
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                User circle = new User();
                do {
                    circle.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    circle.setObjectID(cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID)));
                    circle.setFirstName(cursor.getString(cursor.getColumnIndex(COLUMN_FIRST_NAME)));
                    circle.setLastName(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_NAME)));
                    circle.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                    circle.setPhoneNumber(cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER)));
                    circle.setMemberOfMasterCircleRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_MEMBER_OF_MASTER_CIRCLE)));
                    circle.setPhoneVerifiedRaw(cursor.getInt(cursor.getColumnIndex(COLUMN_PHONE_VERIFIED)));
                    circle.setSecondaryEmailsRaw(cursor.getString(cursor.getColumnIndex(COLUMN_SECONDARY_EMAILS)));
                    circle.setSecondaryPhonesRaw(cursor.getString(cursor.getColumnIndex(COLUMN_SECONDARY_PHONES)));
                } while (cursor.moveToNext());

                return circle;
            }
            return null;

        } else {
            return null;
        }
    }

    public int getEntryCount(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COLUMN_ID
                + " FROM "
                + tableName;
        return db.rawQuery(selectQuery, null).getCount();
    }

    public int deleteCircle(int circleID) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(CIRCLES_TABLE, COLUMN_ID + " = " + circleID, null);
    }

    public void saveInvitationList(String[] invitations) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues;
        for (String invitation : invitations) {
            contentValues = new ContentValues();
            contentValues.put(COLUMN_EMAIL, invitation);
            db.insert(INVITATION_TABLE, null, contentValues);
        }
    }

    public String[] getInvitations() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> list = new ArrayList<>();
        String selectQuery = "SELECT " + COLUMN_EMAIL
                + " FROM "
                + INVITATION_TABLE;
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
            } while (cursor.moveToNext());
        }

        String[] invitations = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            invitations[i] = list.get(i);
        }

        return invitations;
    }
}