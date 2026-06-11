package com.boardsaver;

public class StartingPositionMapper {

    private static final String[][] STARTING_POSITION = {
            {"black_rook", "black_knight", "black_bishop", "black_queen", "black_king", "black_bishop", "black_knight", "black_rook"},
            {"black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn", "black_pawn"},
            {"empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark"},
            {"empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light"},
            {"empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark"},
            {"empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light", "empty_dark", "empty_light"},
            {"white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn", "white_pawn"},
            {"white_rook", "white_knight", "white_bishop", "white_queen", "white_king", "white_bishop", "white_knight", "white_rook"}
    };

    public String getLabelForStartingPosition(int row, int col) {
        return STARTING_POSITION[row][col];
    }
}
