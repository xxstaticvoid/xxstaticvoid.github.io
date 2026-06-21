package com.boardsaver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * custom view class for camera border overlay
 * this indicates to users that the board should fit in outline
 * (makes it easier and faster for image processing)
 */
public class BoardOverlayView extends View {

    private final Paint borderPaint;
    private RectF guideRect;

    public BoardOverlayView(Context context, AttributeSet attributes) {
        super(context, attributes);

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(12f);
        borderPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth() * .8f;
        float height = width;
        float left = (getWidth() - width) / 2;
        float top = (getHeight() - height) / 2;

        guideRect = new RectF(left, top, left + width, top + height);

        canvas.drawRect(guideRect, borderPaint);
    }

    public RectF getGuideRect() {
        return guideRect;
    }

}
