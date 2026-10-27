package com.example.colortiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/** Ретро-"прогресс-бар" из 16 сегментов: жёлтые = светлые плитки, синие = тёмные */
public class SegmentBar extends View {

    private static final int TOTAL = 16;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int light, dark, ink;
    private final float d;
    private int lightCount = 8;

    public SegmentBar(Context context) {
        this(context, null);
    }

    public SegmentBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = getResources().getDisplayMetrics().density;
        light = ContextCompat.getColor(context, R.color.bright);
        dark = ContextCompat.getColor(context, R.color.dark);
        ink = ContextCompat.getColor(context, R.color.ink);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(ink);
    }

    public void setLightCount(int count) {
        lightCount = count;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas c) {
        float w = getWidth();
        float h = getHeight();
        float border = 2.5f * d;
        float inner = w - border * 2;
        float seg = inner / TOTAL;

        for (int i = 0; i < TOTAL; i++) {
            fill.setColor(i < lightCount ? light : dark);
            c.drawRect(border + i * seg, border, border + (i + 1) * seg, h - border, fill);
        }
        stroke.setStrokeWidth(1.5f * d);
        for (int i = 1; i < TOTAL; i++) {
            float x = border + i * seg;
            c.drawLine(x, border, x, h - border, stroke);
        }
        stroke.setStrokeWidth(border);
        c.drawRect(border / 2, border / 2, w - border / 2, h - border / 2, stroke);
    }
}
