package com.boardsaver;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Map;

public class TemplateMatcher {

    private static final double EMPTY_EDGE_THRESHOLD = 0.012;
    private static final double MIN_TEMPLATE_SCORE = 0.3;

    private final TemplateStorage templateStorage;

    public TemplateMatcher(TemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }

    public String classifyBoardToFen(Mat croppedBoardMat) {
        char[][] boardState = classifyBoard(croppedBoardMat);
        return BoardConverter.boardStateToFen(boardState);
    }

    public char[][] classifyBoard(Mat croppedBoardMat) {
        Map<String, ArrayList<Mat>> templates = templateStorage.loadTemplates();

        if (templates.isEmpty()) {
            throw new IllegalStateException("No templates loaded");
        }

        Mat normalizedBoardMat = BoardConverter.normalizeBoard(croppedBoardMat);
        char[][] boardState = new char[8][8];


        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Mat squareMat = BoardConverter.getSquare(normalizedBoardMat, row, col);

                String label = classifySquare(squareMat, templates);

                ChessPiece piece = ChessPiece.fromTemplateLabel(label);

                boardState[row][col] = piece.getFenChar();
            }
        }

        return boardState;
    }

    private boolean isSquareEmpty(Mat squareMat) {
        Mat centerMat = BoardConverter.getSquareCenter(squareMat);

        Mat grayMat = new Mat();

        if (centerMat.channels() == 4) {
            Imgproc.cvtColor(centerMat, grayMat, Imgproc.COLOR_RGBA2GRAY);
        } else if (centerMat.channels() == 3) {
            Imgproc.cvtColor(centerMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayMat = centerMat.clone();
        }

        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stddev = new MatOfDouble();
        Core.meanStdDev(grayMat, mean, stddev);

        double contrast = stddev.toArray()[0];

        Mat edgesMat = new Mat();
        Imgproc.Canny(grayMat, edgesMat, 50, 150);

        double edgeRatio = (double) Core.countNonZero(edgesMat) /
                (edgesMat.rows() * edgesMat.cols());

        Log.d("SquareClassifyDebug", "edgeRatio=" + edgeRatio + " contrast=" + contrast);

        return edgeRatio < 0.005 && contrast < 4.0;
    }



    private String classifySquare(Mat squareMat, Map<String, ArrayList<Mat>> templates) {

        if(isSquareEmpty(squareMat)) {
            return "empty";
        }

        double edgeRatio = BoardConverter.getEdgeRatio(squareMat);

        if (edgeRatio < EMPTY_EDGE_THRESHOLD) {
            return "empty";
        }

        Mat candidateRegion = BoardConverter.getSquareCenter(squareMat);
        Mat candidateProcessedMat = BoardConverter.preprocessForMatching(candidateRegion);

        //set base values
        String bestLabel = "unknown";
        double bestScore = -1;

        for (String label : templates.keySet()) {
            if(label.startsWith("empty")) {
                continue;
            }

            ArrayList<Mat> templateList = templates.get(label);

            assert templateList != null;
            for (Mat templateMat : templateList) {
                double score = compare(candidateProcessedMat, templateMat);

                if (score > bestScore) {
                    bestScore = score;
                    bestLabel = label;
                }
            }

        }
        if (bestScore < MIN_TEMPLATE_SCORE) return "unknown";

        return bestLabel;

    }


    private double compare(Mat candidateProcessedMat, Mat rawTemplateMat) {
        Mat templateProcessed = BoardConverter.preprocessForMatching(rawTemplateMat);

        Mat resultMat = new Mat();

        Imgproc.matchTemplate(
                candidateProcessedMat,
                templateProcessed,
                resultMat,
                Imgproc.TM_CCOEFF_NORMED
        );

        Core.MinMaxLocResult result = Core.minMaxLoc(resultMat);

        return result.maxVal;
    }

}
