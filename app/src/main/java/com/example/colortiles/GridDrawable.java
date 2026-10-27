package com.example.colortiles;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** "Миллиметровка": бумага + тонкая клетка */
public class GridDrawable extends Drawable {

    private final Paint line = new Paint();
    private final int paper;
    private final float cell;

    public GridDrawable(int paperColor, int lineColor, float cellPx, float strokePx) {
        paper = paperColor;
        cell = cellPx;
        line.setColor(lineColor);
        line.setStrokeWidth(strokePx);
    }

    @Override
    public void draw(@NonNull Canvas c) {
        Rect b = getBounds();
        c.drawColor(paper);
        for (float x = b.left; x <= b.right; x += cell) c.drawLine(x, b.top, x, b.bottom, line);
        for (float y = b.top; y <= b.bottom; y += cell) c.drawLine(b.left, y, b.right, y, line);
    }

    @Override public void setAlpha(int alpha) { }
    @Override public void setColorFilter(@Nullable ColorFilter cf) { }
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}
