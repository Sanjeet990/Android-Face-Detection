package com.spathak.facedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class FaceBoundOverlay extends View {
    private ArrayList<RectF> faceBoundList = new ArrayList<RectF>();
    private Paint boundsPaint = new Paint();

    public FaceBoundOverlay(Context context) {
        super(context);
        init(context);
    }

    public FaceBoundOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FaceBoundOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FaceBoundOverlay(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        boundsPaint.setColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(8f);
    }

    public void updateFaces(ArrayList<RectF> tempFaces) {
        faceBoundList.clear();
        faceBoundList.addAll(tempFaces);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Draw rectangle on all faces
        for (int i = 0; i < faceBoundList.size(); i++) {
            drawBounds(canvas, faceBoundList.get(i));
        }
    }

    private void drawBounds(Canvas canvas, RectF box) {
        //Drawing the rectable here
        canvas.drawRect(box, boundsPaint);
    }

}
