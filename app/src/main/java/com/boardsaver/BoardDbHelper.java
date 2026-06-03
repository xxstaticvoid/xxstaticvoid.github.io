package com.boardsaver;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


//FIXME:: CONVERT CLASS TO SINGLETON PATTERN
public class BoardDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "saved_boards.db";
    private static final int DB_VERSION = 5;
    private final Context appContext;

    private static volatile BoardDbHelper instance;

    //double checked locking
    public static BoardDbHelper getInstance(Context context) {
        if(instance == null) {
            synchronized (BoardDbHelper.class) {
                if(instance == null) {
                    instance = new BoardDbHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }


    private BoardDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.appContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_QUERY =
                "CREATE TABLE " + BoardContract.BoardEntry.TABLE_NAME + " ( " +
                        BoardContract.BoardEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        BoardContract.BoardEntry.COLUMN_USER_ID + " TEXT NOT NULL, " +
                        BoardContract.BoardEntry.COLUMN_FEN + " TEXT NOT NULL, " +
                        BoardContract.BoardEntry.COLUMN_IMAGE_PATH + " TEXT NOT NULL, " +
                        BoardContract.BoardEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                        BoardContract.BoardEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL) ";

        db.execSQL(SQL_CREATE_QUERY);
        loadBoards(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL_DROP_QUERY =
                "DROP TABLE IF EXISTS " + BoardContract.BoardEntry.TABLE_NAME;
        db.execSQL(SQL_DROP_QUERY);
        onCreate(db);
    }


    //method called once, only on Helper onCreate()
    private void loadBoards(SQLiteDatabase db) {
        //load items in

        //file located in app/res/raw/
        try (InputStream is = appContext.getAssets().open("starting_boards.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            //enable rollback if interrupted
            db.beginTransaction();

            String line;
            boolean first = true;

            while( (line = reader.readLine()) != null) {
                if(first) {
                    first = false;
                    continue;
                }

                String[] cols = line.split(",");

                for(int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].replace("\"", "").trim();
                }

                insertItem(
                        db,
                        cols[0],
                        cols[1],
                        cols[2],
                        cols[3],
                        cols[4],
                        cols[5]);
            }

            //file parsed successfully
            db.setTransactionSuccessful();

        } catch(Exception e) {
            System.out.println("Error loading data from file");
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    //only called on initial onCreate to load parts db
    private void insertItem(SQLiteDatabase db, String id, String userId, String fen, String imagePath, String date, String description) {
        ContentValues boardValues = new ContentValues();
        boardValues.put(BoardContract.BoardEntry.COLUMN_ID, id);
        boardValues.put(BoardContract.BoardEntry.COLUMN_USER_ID, userId);
        boardValues.put(BoardContract.BoardEntry.COLUMN_FEN, fen);
        boardValues.put(BoardContract.BoardEntry.COLUMN_IMAGE_PATH, imagePath);
        boardValues.put(BoardContract.BoardEntry.COLUMN_DATE, date);
        boardValues.put(BoardContract.BoardEntry.COLUMN_DESCRIPTION, description);

        db.insert(BoardContract.BoardEntry.TABLE_NAME, null, boardValues);
    }

}
