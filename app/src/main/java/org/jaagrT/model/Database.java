package org.jaagrT.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import org.jaagrT.utilities.Utilities;

/**
 * Authored by vedhavyas on 10/12/14.
 * Project JaagrT
 */
public class Database extends SQLiteOpenHelper {

    public static final String USER_TABLE = "user_details";
    private static final String[] TABLES = {USER_TABLE};
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
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";
    private static Database dbFactory;
    private String tableName;

    private Database(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static Database getInstance(Context context) {
        if (dbFactory == null) {

            dbFactory = new Database(context);
        }

        return dbFactory;
    }

    public static Database getInstance(Context context, String tableName) {
        getInstance(context);
        dbFactory.setTableName(tableName);
        return dbFactory;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_USER_TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE + USER_TABLE);
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
        onCreate(db);
    }

    public long saveUser(User user) {
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = getContentValuesFromObject(user);

        result = db.insert(tableName, null, contentValues);
        return result;
    }

    public User getUser(int id) {
        if (id > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String sqlQuery = "SELECT * FROM " + tableName + " WHERE " + COLUMN_ID
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

        ContentValues contentValues = getContentValuesFromObject(user);

        try {
            result = db.update(tableName, contentValues, COLUMN_ID + " = " + user.getID(), null);
        } catch (SQLiteConstraintException e) {
            Utilities.logIt(e.getMessage());
            return result;
        }


        return result;
    }

    private ContentValues getContentValuesFromObject(User user) {
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

            String sqlQuery = "SELECT * FROM " + tableName + " WHERE " + COLUMN_ID
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


}
