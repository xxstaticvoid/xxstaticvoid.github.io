package com.boardsaver;

public final class BoardContract {
    
    private BoardContract () {
        //hidden constructor, do nothing

    }
    
    public static class BoardEntry {

        public static final String TABLE_NAME = "saved_boards";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USER_ID = "user_id";

        public static final String COLUMN_FEN = "fen";
        public static final String COLUMN_IMAGE_PATH = "image_path";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_DESCRIPTION = "description";

    }
    
}



