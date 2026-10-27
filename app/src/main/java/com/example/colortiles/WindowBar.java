package com.example.colortiles;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Заголовок ретро-окна: цветная полоса, три кружка слева,
 * название моноширинным шрифтом, кнопка [×] справа и жирная линия снизу.
 */
public class WindowBar extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float d;
    private final int ink, paper, coral, butter, mint;
    private String title;
    private int barColor;

    public WindowBar(Context context) {
        this(context, null);
    }

    public WindowBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = getResources().getDisplayMetrics().density;
        ink = ContextCompat.getColor(context, R.color.ink);
        paper = ContextCompat.getColor(context, R.color.paper);
        coral = ContextCompat.getColor(context, R.color.coral);
        butter = ContextCompat.getColor(context, R.color.butter);
        mint = ContextCompat.getColor(context, R.color.mint);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WindowBar);
        title = a.getString(R.styleable.WindowBar_barTitle);
        barColor = a.getColor(R.styleable.WindowBar_barColor, butter);
        a.recycle();
        if (title == null) title = "";

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(2 * d);
        stroke.setColor(ink);
        stroke.setStrokeCap(Paint.Cap.SQUARE);

        text.setColor(ink);
        text.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        text.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11,
                getResources().getDisplayMetrics()));
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas c) {
        float w = getWidth();
        float h = getHeight();
        float line = 3 * d;
        float barH = h - line;
        float cy = barH / 2f;

        // полоса и линия снизу
        fill.setColor(barColor);
        c.drawRect(0, 0, w, barH, fill);
        fill.setColor(ink);
        c.drawRect(0, barH, w, h, fill);

        // три кружка
        float r = 5 * d;
        int[] dots = {coral, butter, mint};
        for (int i = 0; i < 3; i++) {
            float cx = 12 * d + r + i * 16 * d;
            fill.setColor(dots[i]);
            c.drawCircle(cx, cy, r, fill);
            c.drawCircle(cx, cy, r, stroke);
        }

        // название
        Paint.FontMetrics fm = text.getFontMetrics();
        c.drawText(title, 12 * d + 3 * 16 * d + 8 * d, cy - (fm.ascent + fm.descent) / 2f, text);

        // кнопка [×]
        float box = 16 * d;
        float bx = w - 12 * d - box;
        float by = cy - box / 2f;
        fill.setColor(paper);
        c.drawRect(bx, by, bx + box, by + box, fill);
        c.drawRect(bx, by, bx + box, by + box, stroke);
        float p = 4.5f * d;
        c.drawLine(bx + p, by + p, bx + box - p, by + box - p, stroke);
        c.drawLine(bx + box - p, by + p, bx + p, by + box - p, stroke);
    }
}
