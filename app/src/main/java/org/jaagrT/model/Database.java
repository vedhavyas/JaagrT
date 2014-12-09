package org.jaagrT.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Authored by vedhavyas on 10/12/14.
 * Project JaagrT
 */
public class Database extends SQLiteOpenHelper {

    private static final String DB_NAME = "JaagrT.db";
    private static final String USER_TABLE = "user_details";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_FIRST_NAME = "firstName";
    private static final String COLUMN_LAST_NAME = "lastName";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE_NUMBER = "phoneNumber";
    private static final String COLUMN_MEMBER_OF_MASTER_CIRCLE = "memberOfMasterCircle";
    private static final String COLUMN_PICTURE = "picture";
    private static final String COLUMN_THUMBNAIL_PICTURE = "thumbnailPicture";
    private static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FIRST_NAME + " TEXT,"
            + COLUMN_LAST_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE,"
            + COLUMN_PHONE_NUMBER + " TEXT UNIQUE, "
            + COLUMN_MEMBER_OF_MASTER_CIRCLE + " INTEGER, "
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

    public long registerUserTODB(User user) {
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

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

        result = db.insert(tableName, null, contentValues);
        return result;
    }


}
