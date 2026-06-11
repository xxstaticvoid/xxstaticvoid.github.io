package com.boardsaver;

import org.opencv.core.Mat;

public class CalibrationService {

    private final TemplateStorage templateStorage;
    private final StartingPositionMapper startingPositionMapper = new StartingPositionMapper();

    public CalibrationService(TemplateStorage templateStorage) {
        this.templateStorage = templateStorage;
    }



    public void calibrate(Mat croppedBoardMat) {
        templateStorage.clearCurrentTemplates();

        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {

                Mat squareMat = BoardConverter.getSquare(croppedBoardMat, i, j);
                Mat templateMat = BoardConverter.getSquareCenter(squareMat);

                String label = startingPositionMapper.getLabelForStartingPosition(i, j);
                templateStorage.saveTemplate(label, templateMat, i, j);
            }
        }
    }

}
