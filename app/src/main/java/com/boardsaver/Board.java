package com.boardsaver;
import java.time.LocalDateTime;

import androidx.annotation.NonNull;


public class Board {
    private final int id;
    private final String userId;
    private final String FEN;
    private final String imagePath;
    private final LocalDateTime date;
    private String description;


    public static String createImagePath(String imageName) {
        return "R.drawable." + imageName;
    }

    public Board(int id, String userId, String FEN, String imageName, LocalDateTime date, String description) {
        this.id = id;
        this.userId = userId;
        this.FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

        this.imagePath = imageName;
        this.date = date;
        this.description = description;
    }




    //== GETTERS ==//

    public int getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getState() {
        return this.FEN;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public String getDescription() {
        return this.description;
    }

    //== SETTERS ==//
    public void setDescription(String description) {
        this.description = description;
    }



    @NonNull
    @Override
    public String toString() {
        return this.FEN + "\n" + this.date + "\n" + this.description;
    }

}
