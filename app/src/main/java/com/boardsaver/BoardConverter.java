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

    public BoardConverter(Mat boardMat) {
        this.normalizedBoardMat = normalizeBoard(boardMat);
    }

    public Mat getNormalizedBoardMat() {
        return this.normalizedBoardMat;
    }


    public static Mat normalizeBoard(Mat boardMat) {
        Mat normalizedMat = new Mat();
        Imgproc.resize(boardMat, normalizedMat, new Size(BOARD_SIZE, BOARD_SIZE));
        return normalizedMat;
    }


    public static String boardStateToFen(char[][] boardState) {
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

    public static Mat preprocessForMatching(Mat inputMat) {
        Mat grayMat = new Mat();

        if (inputMat.channels() == 4) {
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY);
        } else if (inputMat.channels() == 3) {
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayMat = inputMat.clone();
        }

        Imgproc.GaussianBlur(grayMat, grayMat, new Size(3, 3), 0);

        Mat resizedMat = new Mat();
        Imgproc.resize(grayMat, resizedMat, new Size(64, 64));

        Mat edgesMat = new Mat();
        Imgproc.Canny(resizedMat, edgesMat, 50, 150);

        return edgesMat;
    }

    public static double getEdgeRatio(Mat squareMat) {
        Mat centerMat = getSquareCenter(squareMat);
        Mat edgesMat = preprocessForMatching(centerMat);

        int edgePixels = Core.countNonZero(edgesMat);
        int totalPixels = edgesMat.rows() * edgesMat.cols();

        return (double) edgePixels / totalPixels;
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
        int margin = squareMat.width() / 5;

        Rect centerRect = new Rect(
                margin,
                margin,
                squareMat.width() - (2 * margin),
                squareMat.height() - (2 * margin)
        );

        return new Mat(squareMat, centerRect);
    }

}
