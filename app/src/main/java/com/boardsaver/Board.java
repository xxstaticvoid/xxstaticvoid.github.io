package com.boardsaver;
import java.time.LocalDateTime;

import androidx.annotation.NonNull;


public class Board {
    private int id;
    private final String userId;
    private final String FEN;
    private final String imagePath;
    private final LocalDateTime date;
    private String description;


    public Board(int id, String userId, String FEN, String imageName, LocalDateTime date, String description) {
        this.id = id;
        this.userId = userId;
        this.FEN = FEN;

        this.imagePath = imageName;
        this.date = date;
        this.description = description;
    }

    //Constructor for NEW items that are not in the db
    public Board(String userId, String FEN, String imageName, LocalDateTime date, String description) {
        this.id = -1;
        this.userId = userId;
        this.FEN = FEN;

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

    public void setId(int id) { this.id = id; }

    @NonNull
    @Override
    public String toString() {
        return this.FEN + "\n" + this.date + "\n" + this.description;
    }

}
