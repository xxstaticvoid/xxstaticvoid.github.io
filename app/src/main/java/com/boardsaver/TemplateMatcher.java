package com.boardsaver;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Map;

public class TemplateMatcher {
    private final TemplateStorage templateStorage;

    public TemplateMatcher(TemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }

    public char[][] classifyBoard(Mat normalizedBoardMat) {
        Map<String, ArrayList<Mat>> templates = templateStorage.loadTemplates();

        if (templates.isEmpty()) {
            throw new IllegalStateException("No templates loaded");
        }

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

    private String classifySquare(Mat squareMat, Map<String, ArrayList<Mat>> templates) {
        Mat candidateRegion = BoardConverter.getSquareCenter(squareMat);

        String bestLabel = "unknown";
        double bestScore = -1.0;

        for (String label : templates.keySet()) {
            for (Mat templateMat : templates.get(label)) {
                double score = compare(candidateRegion, templateMat);

                if (score > bestScore) {
                    bestScore = score;
                    bestLabel = label;
                }
            }
        }

        Log.d("TemplateMatcher", "Best label: " + bestLabel + ", score: " + bestScore);

        if (bestScore < 0.45) {
            return "unknown";
        }

        return bestLabel;
    }

    private double compare(Mat candidateMat, Mat templateMat) {
        Mat candidateProcessed = preprocessMat(candidateMat);
        Mat templateProcessed = preprocessMat(templateMat);

        Mat resultMat = new Mat();

        Imgproc.matchTemplate(
                candidateProcessed,
                templateProcessed,
                resultMat,
                Imgproc.TM_CCOEFF_NORMED
        );

        Core.MinMaxLocResult result = Core.minMaxLoc(resultMat);

        return result.maxVal;
    }

    private Mat preprocessMat(Mat inputMat) {
        Mat grayMat = new Mat();

        if (inputMat.channels() == 4) {
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_RGBA2GRAY);
        } else if (inputMat.channels() == 3) {
            Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        } else {
            grayMat = inputMat.clone();
        }

        Imgproc.GaussianBlur(grayMat, grayMat, new Size(3, 3), 0);

        Mat edgesMat = new Mat();
        Imgproc.Canny(grayMat, edgesMat, 50, 150);

        return edgesMat;
    }

}
