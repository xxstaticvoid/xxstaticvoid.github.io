package com.boardsaver;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowMetrics;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.jspecify.annotations.NonNull;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executors;


public class CameraActivity extends AppCompatActivity {

    private ImageCapture imageCapture;
    private RectF previewBounds;
    private RectF guideRect;

    private BoardConverter boardConverter;
    private TemplateStorage templateStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        WindowMetrics metrics = getWindowManager().getCurrentWindowMetrics();
        previewBounds = new RectF(metrics.getBounds());
        templateStorage = new TemplateStorage(this);

        BoardOverlayView overlayView = findViewById(R.id.overlay_view);
        overlayView.post(() -> {
            guideRect = overlayView.getGuideRect();
        });

        //display camera preview
        startCamera();


        //build button listeners
        Button buttonCalibrate = findViewById(R.id.button_calibrate);
        buttonCalibrate.setOnClickListener(view -> {
            takePhoto(this.imageCapture, true);
        });

        Button buttonCapture = findViewById(R.id.button_capture);
        buttonCapture.setOnClickListener(view -> {
            takePhoto(this.imageCapture, false);
        });

    }


    /**
     *
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        //FIXME:: ADD build  preferences
        imageCapture = new ImageCapture.Builder()
                .build();

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                assert cameraProvider != null;
                Preview preview = new Preview.Builder()
                        .build();
                PreviewView previewView = findViewById(R.id.preview_view);
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                //select back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) this,
                        cameraSelector,
                        imageCapture,
                        preview
                );


            } catch (Exception e) {
                Log.e("CameraX", "Use case binding failed");
            }

        }, ContextCompat.getMainExecutor(this));
    }


    /**
     *
     *
     * @param imageCapture camera.core.ImageCapture / access to takePicture()
     * @param parseTemplates boolean / whether to parse templates or not (current mode)
     */
    private void takePhoto(ImageCapture imageCapture, boolean parseTemplates) {
        assert imageCapture != null;

        var cameraExecutor = Executors.newSingleThreadExecutor();

        //adjusted time format due to files not able to save with colon character
        var currTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                .format(System.currentTimeMillis());

        String imageFilename = "board_" + currTime + ".jpg";

        imageCapture.takePicture(
                cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        super.onCaptureSuccess(imageProxy);
                        Log.d("CameraX", "Photo captured successfully");

                        //get image rotation
                        int imageRotation = imageProxy.getImageInfo().getRotationDegrees();
                        Bitmap imageAsBitmap = imageProxy.toBitmap();

                        //rotate image if needed
                        Bitmap rotated = Bitmap.createBitmap(imageAsBitmap);
                        if(imageRotation != 0) {
                            rotated = rotateBitmap(imageAsBitmap, imageRotation);
                        }

                        //release image proxy
                        imageProxy.close();

                        String captureDebugString =
                                "Image: " + rotated.getWidth() + "x" + rotated.getHeight()
                                        + "\nPreview: " + previewBounds.width() + "x" + previewBounds.height()
                                        + "\nRotation: " + imageRotation;

                        Log.d("CameraX", captureDebugString);


                        //convert bitmap to opencv.mat
                        Mat rotatedMat = new Mat();
                        Utils.bitmapToMat(rotated, rotatedMat);

                        Mat croppedBoardMat = detectAndCropBoard(rotatedMat);

                        if (croppedBoardMat != null) {
                            Bitmap boardBitmap = Bitmap.createBitmap(
                                    croppedBoardMat.cols(),
                                    croppedBoardMat.rows(),
                                    Bitmap.Config.ARGB_8888
                            );

                            boardConverter = new BoardConverter(croppedBoardMat);

                            //if user selected 'calibrate' mode
                            if(parseTemplates) {
                                //save theme as piece templates
                                calibrateTemplates(croppedBoardMat);
                                cameraExecutor.shutdown();
                                return;
                            }

                            //saved cropped image
                            Utils.matToBitmap(croppedBoardMat, boardBitmap);
                            saveBitmap(boardBitmap, imageFilename);

                            //FIXME:: CREATE BOARD & ADD ENTRY IN DB


                            //FIXME:: UPDATE BOARD ADAPTER



                        } else {
                            Log.d("CameraX", "Could not detect board");
                        }


                        cameraExecutor.shutdown();
                        Log.d("CameraX", "Image Processing Finished");
                        Log.d("CameraX", "Photo saved to: " + imageFilename);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage());
                        cameraExecutor.shutdown();
                    }
                }
        );
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {

        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotated = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );

        return rotated;
    }


    private void calibrateTemplates(Mat croppedBoardMat) {
        CalibrationService calibrationService = new CalibrationService(
            templateStorage
        );
        calibrationService.calibrate(croppedBoardMat);
    }



    private Mat detectAndCropBoard(Mat rgbaMat) {
        Mat grayMat = new Mat();
        Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGBA2GRAY);

        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurredMat, new Size(5, 5), 0);

        Mat edgesMat = new Mat();
        Imgproc.Canny(blurredMat, edgesMat, 50, 150);

        Mat structuringElement = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                new Size(3, 3)
        );

        Mat closedEdgesMat = new Mat();
        Imgproc.morphologyEx(edgesMat, closedEdgesMat, Imgproc.MORPH_CLOSE, structuringElement);
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(
                closedEdgesMat,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
        );

        double imageArea = rgbaMat.width() * rgbaMat.height();

        MatOfPoint2f bestBoardCorners = null;
        double bestArea = 0;

        for (MatOfPoint contour : contours) {
            double contourArea = Imgproc.contourArea(contour);

            // Ignore tiny contours
            if (contourArea < imageArea * 0.05) {
                continue;
            }

            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            double perimeter = Imgproc.arcLength(contour2f, true);

            MatOfPoint2f approximatedContour = new MatOfPoint2f();
            Imgproc.approxPolyDP(
                    contour2f,
                    approximatedContour,
                    0.02 * perimeter,
                    true
            );

            // Chessboard outer boundary should be a quadrilateral
            if (approximatedContour.total() != 4) {
                continue;
            }

            MatOfPoint approxAsPoint = new MatOfPoint(approximatedContour.toArray());

            if (!Imgproc.isContourConvex(approxAsPoint)) {
                continue;
            }

            Rect boundingRect = Imgproc.boundingRect(approxAsPoint);
            double aspectRatio = (double) boundingRect.width / boundingRect.height;

            // Board should be close to square aka 1.0
            if (aspectRatio < 0.75 || aspectRatio > 1.25) {
                continue;
            }

            // Keep the largest valid square-ish quadrilateral
            if (contourArea > bestArea) {
                bestArea = contourArea;
                bestBoardCorners = approximatedContour;
            }
        }

        if (bestBoardCorners == null) {
            Log.d("CameraX", "No board candidate found");
            return null;
        }

        return warpBoardToSquare(rgbaMat, bestBoardCorners);
    }


    private Mat warpBoardToSquare(Mat rgbaMat, MatOfPoint2f boardCorners) {
        org.opencv.core.Point[] unorderedPoints = boardCorners.toArray();
        org.opencv.core.Point[] orderedPoints = orderBoardCorners(unorderedPoints);

        double topWidth = distance(orderedPoints[0], orderedPoints[1]);
        double bottomWidth = distance(orderedPoints[3], orderedPoints[2]);
        double leftHeight = distance(orderedPoints[0], orderedPoints[3]);
        double rightHeight = distance(orderedPoints[1], orderedPoints[2]);

        int outputSize = (int) Math.max(
                Math.max(topWidth, bottomWidth),
                Math.max(leftHeight, rightHeight)
        );

        // keep between 800 and 400
        outputSize = Math.max(outputSize, 500);

        MatOfPoint2f sourcePoints = new MatOfPoint2f(
                orderedPoints[0], // top left
                orderedPoints[1], // top right
                orderedPoints[2], // bottom right
                orderedPoints[3]  // bottom left
        );

        MatOfPoint2f destinationPoints = new MatOfPoint2f(
                new org.opencv.core.Point(0, 0),
                new org.opencv.core.Point(outputSize - 1, 0),
                new org.opencv.core.Point(outputSize - 1, outputSize - 1),
                new org.opencv.core.Point(0, outputSize - 1)
        );

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(
                sourcePoints,
                destinationPoints
        );

        Mat warpedBoardMat = new Mat();
        Imgproc.warpPerspective(
                rgbaMat,
                warpedBoardMat,
                perspectiveTransform,
                new Size(outputSize, outputSize)
        );

        return warpedBoardMat;

    }

    private Point[] orderBoardCorners(Point[] points) {
        ArrayList<Point> pointList = new ArrayList<>(Arrays.asList(points));

        // Sort points from top to bottom ( by y value)
        // this had to be implemented because image kept getting rotated and
        // horizontally flipped
        Collections.sort(pointList, (p1, p2) -> Double.compare(p1.y, p2.y));

        //separate top and bottom pairs
        Point topPointA = pointList.get(0);
        Point topPointB = pointList.get(1);
        Point bottomPointA = pointList.get(2);
        Point bottomPointB = pointList.get(3);

        Point topLeft;
        Point topRight;
        Point bottomLeft;
        Point bottomRight;

        if (topPointA.x < topPointB.x) {
            topLeft = topPointA;
            topRight = topPointB;
        } else {
            topLeft = topPointB;
            topRight = topPointA;
        }

        if (bottomPointA.x < bottomPointB.x) {
            bottomLeft = bottomPointA;
            bottomRight = bottomPointB;
        } else {
            bottomLeft = bottomPointB;
            bottomRight = bottomPointA;
        }

        return new Point[] {
                topLeft,
                topRight,
                bottomRight,
                bottomLeft
        };

    }

    private double distance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;

        //apply pythagorean theorem for point distance
        return Math.sqrt(dx * dx + dy * dy);
    }


    private void saveBitmap(Bitmap bitmap, String filename) {
        assert bitmap != null;
        File photoFile = new File(this.getExternalFilesDir(null), filename);
        try(FileOutputStream outputStream = new FileOutputStream(photoFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(photoFile));
        } catch (Exception e) {
            Log.e("CameraX", "Error saving bitmap: " + e.getMessage());
        }
    }

}
