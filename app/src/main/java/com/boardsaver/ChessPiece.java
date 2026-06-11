package com.boardsaver;



public enum ChessPiece {
    EMPTY('1'),

    WHITE_PAWN('P'),
    WHITE_KNIGHT('N'),
    WHITE_BISHOP('B'),
    WHITE_ROOK('R'),
    WHITE_QUEEN('Q'),
    WHITE_KING('K'),

    BLACK_PAWN('p'),
    BLACK_KNIGHT('n'),
    BLACK_BISHOP('b'),
    BLACK_ROOK('r'),
    BLACK_QUEEN('q'),
    BLACK_KING('k'),

    UNKNOWN('?');

    private final char fenChar;

    ChessPiece(char fenChar) {
        this.fenChar = fenChar;
    }

    public char getFenChar() {
        return fenChar;
    }

    public static ChessPiece fromTemplateLabel(String label) {
        switch (label) {
            case "white_pawn":
                return WHITE_PAWN;
            case "white_knight":
                return WHITE_KNIGHT;
            case "white_bishop":
                return WHITE_BISHOP;
            case "white_rook":
                return WHITE_ROOK;
            case "white_queen":
                return WHITE_QUEEN;
            case "white_king":
                return WHITE_KING;

            case "black_pawn":
                return BLACK_PAWN;
            case "black_knight":
                return BLACK_KNIGHT;
            case "black_bishop":
                return BLACK_BISHOP;
            case "black_rook":
                return BLACK_ROOK;
            case "black_queen":
                return BLACK_QUEEN;
            case "black_king":
                return BLACK_KING;

            case "empty_light":
            case "empty_dark":
            case "empty":
                return EMPTY;

            default:
                return UNKNOWN;
        }
    }
}