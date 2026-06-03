package com.boardsaver;

public final class UserContract {

    private UserContract() {}

    public static class UserEntry {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PASSWORD_HASH = "password_hash";
        public static final String COLUMN_PRIVILEGE = "privilege";
    }

}
