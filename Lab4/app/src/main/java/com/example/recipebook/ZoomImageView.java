package com.example.recipebook;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ZoomImageView extends androidx.appcompat.widget.AppCompatImageView {
    private final Matrix imageMatrix = new Matrix();
    private final float[] matrixValues = new float[9];
    private final ScaleGestureDetector scaleDetector;
    private float baseScale = 1f;
    private float currentScale = 1f;
    private float lastX;
    private float lastY;
    private boolean dragging;

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(imageMatrix);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float factor = detector.getScaleFactor();
                float target = currentScale * factor;
                float minimum = baseScale;
                float maximum = baseScale * 5f;
                if (target < minimum) factor = minimum / currentScale;
                if (target > maximum) factor = maximum / currentScale;
                imageMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                getValues();
                currentScale = matrixValues[Matrix.MSCALE_X];
                clampTranslation();
                setImageMatrix(imageMatrix);
                return true;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        fitImage();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        post(this::fitImage);
    }

    public void resetZoom() {
        fitImage();
    }

    private void fitImage() {
        Drawable drawable = getDrawable();
        if (drawable == null || getWidth() <= 0 || getHeight() <= 0
                || drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) return;

        float viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        float viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        float scaleX = viewWidth / drawable.getIntrinsicWidth();
        float scaleY = viewHeight / drawable.getIntrinsicHeight();
        baseScale = Math.min(scaleX, scaleY);
        currentScale = baseScale;
        float dx = (viewWidth - drawable.getIntrinsicWidth() * baseScale) / 2f + getPaddingLeft();
        float dy = (viewHeight - drawable.getIntrinsicHeight() * baseScale) / 2f + getPaddingTop();
        imageMatrix.reset();
        imageMatrix.postScale(baseScale, baseScale);
        imageMatrix.postTranslate(dx, dy);
        setImageMatrix(imageMatrix);
        invalidate();
    }

    private void getValues() {
        imageMatrix.getValues(matrixValues);
    }

    private void clampTranslation() {
        Drawable drawable = getDrawable();
        if (drawable == null) return;
        RectF rect = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        imageMatrix.mapRect(rect);
        float dx = 0f;
        float dy = 0f;
        if (rect.width() <= getWidth()) {
            dx = (getWidth() - rect.width()) / 2f - rect.left;
        } else if (rect.left > 0) {
            dx = -rect.left;
        } else if (rect.right < getWidth()) {
            dx = getWidth() - rect.right;
        }
        if (rect.height() <= getHeight()) {
            dy = (getHeight() - rect.height()) / 2f - rect.top;
        } else if (rect.top > 0) {
            dy = -rect.top;
        } else if (rect.bottom < getHeight()) {
            dy = getHeight() - rect.bottom;
        }
        imageMatrix.postTranslate(dx, dy);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                dragging = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging && !scaleDetector.isInProgress() && currentScale > baseScale + 0.01f) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    imageMatrix.postTranslate(dx, dy);
                    clampTranslation();
                    setImageMatrix(imageMatrix);
                }
                lastX = event.getX();
                lastY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
            default:
                return true;
        }
    }
}
