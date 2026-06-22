package com.boardsaver;

import android.util.Log;

import org.opencv.core.Mat;

public class CalibrationService {

    private final TemplateStorage templateStorage;
    private final StartingPositionMapper startingPositionMapper;

    public CalibrationService(TemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
        this.startingPositionMapper = new StartingPositionMapper();
    }

    public void calibrate(Mat croppedBoardMat) {
        Mat normalizedBoardMat = BoardConverter.normalizeBoard(croppedBoardMat);
        templateStorage.clearCurrentTemplates();


        //loop through board, if square not empty, which piece is it?
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {

                Mat squareMat = BoardConverter.getSquare(normalizedBoardMat, i, j);
                Mat templateMat = BoardConverter.getSquareCenter(squareMat);

//                Log.d("SquareClassifyDebug",
//                        "row=" + i + " col=" + j + " edgeRatio=" + BoardConverter.getEdgeRatio(squareMat)
//                );

                String label = startingPositionMapper.getLabelForStartingPosition(i, j);
                if (!label.equals("empty")) {
                    templateStorage.saveTemplate(label, templateMat, i, j);
                }
            }
        }
    }

}
