package com.example.colortiles;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/** LinearLayout, у которого высота всегда равна ширине. Поле 4x4 = квадрат на всю ширину. */
public class SquareLayout extends LinearLayout {

    public SquareLayout(Context context) { super(context); }

    public SquareLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // В качестве высоты передаём ограничение ширины
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
