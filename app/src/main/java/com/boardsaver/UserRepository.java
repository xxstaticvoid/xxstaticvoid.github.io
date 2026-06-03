package com.boardsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;

public class UserRepository {

    private final UserDbHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = UserDbHelper.getInstance(context);
    }

    public long registerUser(String username, String passwordHash) {

        //check login info first
        if(username.length() < 5) {
            return -1L;
        }


        //open db for writing
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        //FIXME:: change later for email and privilege support
        //email constant
        final String EMAIL = username + "@gmail.com";
        //admin constant
        final int IS_ADMIN = 1;


        //prep row before insert
        ContentValues rowValues = new ContentValues(3);
        rowValues.put(UserContract.UserEntry.COLUMN_USERNAME, username);
        rowValues.put(UserContract.UserEntry.COLUMN_EMAIL, EMAIL);
        rowValues.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, passwordHash);
        rowValues.put(UserContract.UserEntry.COLUMN_PRIVILEGE, IS_ADMIN);


        //insert row into table
        //returns row number of newly inserted (long)
        return db.insert(UserContract.UserEntry.TABLE_NAME, null, rowValues);
    }

    public boolean authenticate(String username, String passwordHash) {
        //open db for reading
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserContract.UserEntry.COLUMN_USERNAME + " = ? AND " +
                UserContract.UserEntry.COLUMN_PASSWORD_HASH + " = ?";

        String[] selectionArgs = {username, passwordHash};

        //run read query on db
        Cursor cursor = db.query(
                UserContract.UserEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;

    }

}
