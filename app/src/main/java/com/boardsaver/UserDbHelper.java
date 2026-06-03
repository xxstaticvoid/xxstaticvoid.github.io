package com.boardsaver;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


//FIXME:: CONVERT CLASS TO SINGLETON PATTERN
public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "users.db";
    private static final int DB_VERSION = 2;

    private static volatile UserDbHelper instance;

    private UserDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static UserDbHelper getInstance(Context context) {
        if(instance == null) {
            instance = new UserDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

            // CREATE TABLE table_name (
            //     column1 datatype constraints,
            //     column2 datatype constraints,
            //     column3 datatype constraints,
            //     ...
            // );

        String SQL_CREATE_QUERY =
                "CREATE TABLE " + UserContract.UserEntry.TABLE_NAME + " ( " +
                        UserContract.UserEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        UserContract.UserEntry.COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                        UserContract.UserEntry.COLUMN_EMAIL + " TEXT NOT NULL, " +
                        UserContract.UserEntry.COLUMN_PASSWORD_HASH + " TEXT NOT NULL, " +
                        UserContract.UserEntry.COLUMN_PRIVILEGE + " TEXT NOT NULL)";
        db.execSQL(SQL_CREATE_QUERY);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DROP_QUERY =
                "DROP TABLE IF EXISTS " + UserContract.UserEntry.TABLE_NAME;
        db.execSQL(SQL_DROP_QUERY);
        onCreate(db);
    }
}
