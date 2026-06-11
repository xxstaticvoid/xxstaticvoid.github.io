package com.boardsaver;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class BoardConverter {

    private static final int BOARD_SIZE = 800;
    private static final int SQUARE_SIZE = BOARD_SIZE / 8;

    private final Mat normalizedBoardMat;



    //used for testing fen conversions (DEBUG ONLY)
    public static final char[][] startingBoardState = {
            {'r','n','b','q','k','b','n','r'}, //black
            {'p','p','p','p','p','p','p','p'},
            {'1','1','1','1','1','1','1','1'},
            {'1','1','1','1','1','1','1','1'},
            {'1','1','1','1','1','1','1','1'},
            {'1','1','1','1','1','1','1','1'},
            {'P','P','P','P','P','P','P','P'},
            {'R','N','B','Q','K','B','N','R'} //white
    };

    public BoardConverter(Mat boardMat) {
        Mat normalizedMat = new Mat();
        Imgproc.resize(boardMat, normalizedMat, new Size(BOARD_SIZE, BOARD_SIZE));
        this.normalizedBoardMat = normalizedMat;
    }


    public String convert() {
        char[][] boardState = new char[8][8];

        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                //Log.d("CameraX", "Row: " + row + " Col: " + col);
                Mat squareMat = getSquare(this.normalizedBoardMat, row, col);
                if (isSquareOccupied(squareMat)) {
                    boardState[row][col] = '?';
                } else {
                    boardState[row][col] = '1';
                }

            }
        }

        return classifySquares(boardState);
    }


    public String classifySquares(char[][] boardState) {
        StringBuilder fenBuilder = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;

            for (int col = 0; col < 8; col++) {
                char piece = boardState[row][col];

                if (piece == '1' || piece == ' ') {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fenBuilder.append(emptyCount);
                        emptyCount = 0;
                    }

                    fenBuilder.append(piece);
                }
            }

            if (emptyCount > 0) {
                fenBuilder.append(emptyCount);
            }

            if (row < 7) {
                fenBuilder.append('/');
            }
        }

        return fenBuilder.toString();
    }

    private boolean isSquareOccupied(Mat squareMat) {
        Mat centerMat = getSquareCenter(squareMat);

        Mat grayMat = new Mat();
        Imgproc.cvtColor(centerMat, grayMat, Imgproc.COLOR_RGBA2GRAY);

        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurredMat, new Size(3, 3), 0);

        Mat edgesMat = new Mat();
        Imgproc.Canny(blurredMat, edgesMat, 50, 150);

        int edgePixels = Core.countNonZero(edgesMat);
        int totalPixels = edgesMat.rows() * edgesMat.cols();

        double edgeRatio = (double) edgePixels / totalPixels;

        //Log.d("CameraX",""+edgeRatio);
        return edgeRatio > 0.015; //[0.009 - .04]
    }


    public static Mat getSquare(Mat board, int row, int col) throws IllegalArgumentException {
        Rect squareRect = new Rect(
                col * SQUARE_SIZE,
                row * SQUARE_SIZE,
                SQUARE_SIZE,
                SQUARE_SIZE
        );

        return new Mat(board, squareRect);
    }


    public static Mat getSquareCenter(Mat squareMat) {
        final int margin = squareMat.width() / 5;

        Rect centerRect = new Rect(
                margin,
                margin,
                squareMat.width() - 2 * margin,
                squareMat.height() - 2 * margin
        );

        return new Mat(squareMat, centerRect);
    }

}
