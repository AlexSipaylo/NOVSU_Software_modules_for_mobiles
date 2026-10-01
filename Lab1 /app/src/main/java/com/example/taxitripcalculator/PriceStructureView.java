package com.example.taxitripcalculator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class PriceStructureView extends View {
    private final Paint paint;
    private final Paint textPaint;
    private final RectF rect;
    private float[] parts = new float[0];
    private final boolean showLabels;
    private final float barHeight;

    public PriceStructureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(dpToPx(11));
        rect = new RectF();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PriceStructureView);
        barHeight = a.getDimension(R.styleable.PriceStructureView_barHeight, dpToPx(32));
        showLabels = a.getBoolean(R.styleable.PriceStructureView_showLabels, true);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int desiredW = dpToPxInt(300);
        int desiredH = dpToPxInt(80);
        int width = resolveSize(desiredW, widthSpec);
        int height = resolveSize(desiredH, heightSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (parts.length == 0) {
            return;
        }

        float total = 0;
        for (float p : parts) {
            total += p;
        }
        if (total <= 0) {
            return;
        }

        float left = 0;
        float top = (getHeight() - barHeight) / 2f;

        String[] labels = {"Подача", "Километры", "Минуты", "Опции"};

        for (int i = 0; i < parts.length; i++) {
            float width = getWidth() * parts[i] / total;
            paint.setColor(colorFor(i));
            rect.set(left, top, left + width, top + barHeight);
            canvas.drawRect(rect, paint);

            if (showLabels && width > dpToPx(55)) {
                textPaint.setColor(0xFFFFFFFF);
                canvas.drawText(labels[i], left + dpToPx(5), top + barHeight / 2f
                        + dpToPx(4), textPaint);
            }
            left += width;
        }
    }

    public void setData(double[] values) {
        parts = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            parts[i] = (float) values[i];
        }

        invalidate();
    }

    private int colorFor(int index) {
        switch (index) {
            case 0:
                return 0xFF800000;
            case 1:
                return 0xFF3F51B5;
            case 2:
                return 0xFF00897B;
            default:
                return 0xFFFF9800;
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private int dpToPxInt(float dp) {
        return (int) (dpToPx(dp) + 0.5f);
    }
}