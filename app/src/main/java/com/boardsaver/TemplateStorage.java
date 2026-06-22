package com.boardsaver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TemplateStorage {

    private static final String TEMPLATE_DIRECTORY = "current_templates";
    private final Context context;

    public TemplateStorage(Context context) {
        this.context = context.getApplicationContext();
    }

    public void clearCurrentTemplates() throws RuntimeException {
        File templateDirectory = getTemplateDirectory();
        if (!templateDirectory.exists()) {
            templateDirectory.mkdirs();
            return;
        }

        File[] files = templateDirectory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("Error deleting file: " + file.getName());
            }
        }
    }

    public void saveTemplate(String label, Mat templateMat, int row, int col) {
        File templateDirectory = getTemplateDirectory();

        if (!templateDirectory.exists()) {
            templateDirectory.mkdirs();
        }

        String filename = label + "_" + row + "_" + col + ".png";
        File outputFile = new File(templateDirectory, filename);

        Bitmap bitmap = Bitmap.createBitmap(
                templateMat.cols(),
                templateMat.rows(),
                Bitmap.Config.ARGB_8888
        );

        Utils.matToBitmap(templateMat, bitmap);

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            Log.e("TemplateStorage", "Failed to save template: " + filename, e);
        }
    }


    public Map<String, ArrayList<Mat>> loadTemplates() {
        Map<String, ArrayList<Mat>> templates = new HashMap<>();

        File templateDirectory = getTemplateDirectory();

        if (!templateDirectory.exists()) {
            return templates;
        }

        File[] files = templateDirectory.listFiles();

        if (files == null) {
            return templates;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".png")) {
                continue;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            if (bitmap == null) {
                continue;
            }

            Mat templateMat = new Mat();
            Utils.bitmapToMat(bitmap, templateMat);

            String label = extractLabelFromFilename(file.getName());

            if (!templates.containsKey(label)) {
                templates.put(label, new ArrayList<>());
            }

            templates.get(label).add(templateMat);
        }

        return templates;
    }


    public boolean hasTemplates() {
        File templateDirectory = getTemplateDirectory();
        if (!templateDirectory.exists()) {
            return false;
        }

        File[] files = templateDirectory.listFiles();
        return files != null && files.length > 0;
    }

    private String extractLabelFromFilename(String filename) {
        // white_pawn_6_0.png  ->
        // We want: white_pawn

        String nameWithoutExtension = filename.replace(".png", "");
        String[] parts = nameWithoutExtension.split("_");


        //uh oh shaggy
        if (parts.length < 3) {
            return "unknown";
        }

        return parts[0] + "_" + parts[1];
    }


    private File getTemplateDirectory() {
        return new File(context.getFilesDir(), TEMPLATE_DIRECTORY);
    }
}
