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


    /**
     * inserts a new user into the database
     *
     * @param username the username of the user to register
     * @param password plaintext password for the user to be registered
     * @return returns the row number of the newly inserted row or -1 if failed
     */
    public long registerUser(String username, String password) {

        //check login info first
        if(username == null || password == null || username.length() < 5) {
            return -1L;
        }

        //open db for writing
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        //FIXME:: change later for email and privilege support
        //email constant
        final String EMAIL = username + "@gmail.com";
        //admin constant
        final int IS_ADMIN = 1;

        String passwordHash = PasswordHasher.hashPassword(password);

        //prep row before insert
        ContentValues rowValues = new ContentValues(3);
        rowValues.put(UserContract.UserEntry.COLUMN_USERNAME, username);
        rowValues.put(UserContract.UserEntry.COLUMN_EMAIL, EMAIL);
        rowValues.put(UserContract.UserEntry.COLUMN_PASSWORD_HASH, passwordHash);
        rowValues.put(UserContract.UserEntry.COLUMN_PRIVILEGE, IS_ADMIN);


        //insert row into table
        //returns row number of newly inserted (long) or -1 if failed
        return db.insert(UserContract.UserEntry.TABLE_NAME, null, rowValues);
    }


    /**
     * checks to see if a user exists in the database
     *
     * @param username the username of the user to verify
     * @param password the password of the user to verify
     * @return returns true if the user exists
     */
    public boolean authenticate(String username, String password) {
        //open db for reading
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = UserContract.UserEntry.COLUMN_USERNAME + " = ?";

        String[] selectionArgs = {username};

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
        if (!exists) {
            cursor.close();
            return false;
        }

        cursor.moveToFirst();
        String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PASSWORD_HASH));
        cursor.close();
        return PasswordHasher.verifyPassword(password, storedHash);

    }

}
