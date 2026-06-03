package com.boardsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;



//FIXME:: ADD SEPARATE THREAD FROM UI FOR RESPONSIVENESS
// THIS WOULD BE DONE USING java.util.concurrent.Executors

public class BoardRepository {

    private final BoardDbHelper dbHelper;

    public BoardRepository(Context context) {
        this.dbHelper = BoardDbHelper.getInstance(context);
    }


    // CREATE
    public boolean addBoard(Board board) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues boardValues = packIntoValues(board);
        long row = db.insert(BoardContract.BoardEntry.TABLE_NAME, null, boardValues);
        return row >= 0;
    }

    // DELETE
    public boolean deleteBoard(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = BoardContract.BoardEntry.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        int rowsAffected = db.delete(BoardContract.BoardEntry.TABLE_NAME, where, whereArgs );
        return rowsAffected > 0;
    }


    // UPDATE
    public boolean updateItem(Board board) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues updatedValues = packIntoValues(board);
        String where = BoardContract.BoardEntry.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(board.getId())};

        int rowsAffected = db.update(BoardContract.BoardEntry.TABLE_NAME, updatedValues, where, whereArgs);
        return rowsAffected > 0;
    }

    // COUNT
    public int getNumOfRows() {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + BoardContract.BoardEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        int rowCount = 0;
        if(cursor.moveToFirst()) {
            //get count, only 1 row returned from COUNT()
            rowCount = cursor.getInt(0);
        }
        cursor.close();
        return rowCount;

    }

    // READ
    //used for syncing to db to get current items
    public ArrayList<Board> getAllItems() throws IllegalArgumentException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                BoardContract.BoardEntry.TABLE_NAME,
                null, //all columns
                null, //no selection
                null,
                null,
                null,
                null

        );


        ArrayList<Board> currItemList = new ArrayList<>();

        //loop through db items (cursor)
        if(cursor.moveToFirst()) {
            do {
                Board currBoard = new Board(
                        cursor.getInt( cursor.getColumnIndexOrThrow( BoardContract.BoardEntry.COLUMN_ID )),
                        cursor.getString( cursor.getColumnIndexOrThrow( BoardContract.BoardEntry.COLUMN_USER_ID )),
                        cursor.getString( cursor.getColumnIndexOrThrow( BoardContract.BoardEntry.COLUMN_FEN )),
                        cursor.getString( cursor.getColumnIndexOrThrow( BoardContract.BoardEntry.COLUMN_IMAGE_PATH )),
                        LocalDateTime.parse(cursor.getString( cursor.getColumnIndexOrThrow( BoardContract.BoardEntry.COLUMN_DATE ))),
                        cursor.getString( cursor.getColumnIndexOrThrow( BoardContract.BoardEntry.COLUMN_DESCRIPTION ))
                );
                currItemList.add(currBoard);

            } while (cursor.moveToNext());
        }

        cursor.close();

        //return list of all SpareParts
        return currItemList;

    }

    private ContentValues packIntoValues(Board board) {
        ContentValues values = new ContentValues();
        values.put(BoardContract.BoardEntry.COLUMN_ID, board.getId());
        values.put(BoardContract.BoardEntry.COLUMN_USER_ID, board.getUserId());
        values.put(BoardContract.BoardEntry.COLUMN_FEN, board.getState());
        values.put(BoardContract.BoardEntry.COLUMN_IMAGE_PATH, board.getImagePath());
        values.put(BoardContract.BoardEntry.COLUMN_DATE, board.getDate().toString());
        values.put(BoardContract.BoardEntry.COLUMN_DESCRIPTION, board.getDescription());
        return values;
    }


}
