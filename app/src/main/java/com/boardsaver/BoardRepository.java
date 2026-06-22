package com.boardsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


//FIXME:: ADD SEPARATE THREAD FROM UI FOR RESPONSIVENESS
// THIS WOULD BE DONE USING java.util.concurrent.Executors

public class BoardRepository {

    private final BoardDbHelper dbHelper;
    private AtomicInteger nextId;

    public BoardRepository(Context context) {
        this.dbHelper = BoardDbHelper.getInstance(context);
        this.nextId = new AtomicInteger(getNumOfRows() + 1);
    }


    /**
     * CREATE - adds board to db
     *
     * @param board board data type that is to be added to db
     * @return true if successful, false otherwise.
     */
    public boolean addBoard(Board board) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues boardValues = packIntoValues(board);
        long row = db.insert(BoardContract.BoardEntry.TABLE_NAME, null, boardValues);
        if (row >= 0) nextId.incrementAndGet();
        return row >= 0;
    }

    /**
     * DELETE - removes matching board from db
     *
     * @param id board id that is to be matched and removed from db
     * @return true if successful, false otherwise.
     */
    public boolean deleteBoard(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = BoardContract.BoardEntry.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        int rowsAffected = db.delete(BoardContract.BoardEntry.TABLE_NAME, where, whereArgs );
        if (rowsAffected > 0) nextId.decrementAndGet();
        return rowsAffected > 0;
    }


    /**
     * UPDATE - updates an existing board in db with new values. Matches by Board ID.
     *
     * @param board board data type that is to be updated in db. Matches the Board ID.
     * @return true if successful, false otherwise.
     */
    public boolean updateItem(Board board) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues updatedValues = packIntoValues(board);
        updatedValues.remove(BoardContract.BoardEntry.COLUMN_ID);

        String where = BoardContract.BoardEntry.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(board.getId())};

        int rowsAffected = db.update(BoardContract.BoardEntry.TABLE_NAME, updatedValues, where, whereArgs);
        return rowsAffected > 0;
    }


    /**
     * READ - retrieves all board entries from db. used for syncing to db to get current items
     *
     * @return ArrayList of Board data type. this will be all items in the db.
     * @throws IllegalArgumentException will be thrown if column names are invalid. may also through SQLiteException if if the database cannot be opened
     */
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


    /**
     * COUNT - gets number of rows in db
     *
     * @return number of rows in db
     *
     */
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


    /**
     * stores board data into ContentValues object for convenient handling.
     *
     * @return ContentValues object containing board data
     *
     */
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

    public synchronized int getNextId() {
        return nextId.get();
    }

}
