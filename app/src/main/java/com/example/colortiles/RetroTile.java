package com.example.colortiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * Плитка-кнопка: чёрный контур, жёсткая тень под 45°,
 * пиксельный блик. Светлая - "солнечный" смайл, тёмная - "сонный" смайл.
 * При нажатии вдавливается в тень, при смене цвета переворачивается.
 */
public class RetroTile extends View {

    private boolean dark;
    private boolean hint;

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint solid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint zText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF face = new RectF();
    private final RectF tmp = new RectF();
    private final Path cursor = new Path();
    private final float d;
    private final int lightColor, darkColor, darkFace, ink;

    public RetroTile(Context context) {
        this(context, null);
    }

    public RetroTile(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = getResources().getDisplayMetrics().density;
        lightColor = ContextCompat.getColor(context, R.color.bright);
        darkColor = ContextCompat.getColor(context, R.color.dark);
        darkFace = ContextCompat.getColor(context, R.color.dark_face);
        ink = ContextCompat.getColor(context, R.color.ink);

        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(ink);
        stroke.setStrokeJoin(Paint.Join.ROUND);

        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);

        zText.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        zText.setColor(darkFace);

        setCameraDistance(12000 * d);
        setContentDescription(context.getString(R.string.cd_light));
    }

    // состояние
    public boolean isDark() {
        return dark;
    }

    public void setDark(boolean dark) {
        this.dark = dark;
        setContentDescription(getContext().getString(dark ? R.string.cd_dark : R.string.cd_light));
        invalidate();
    }

    public void setHint(boolean hint) {
        if (this.hint != hint) {
            this.hint = hint;
            invalidate();
        }
    }

    /** Переворот на ребро -> смена цвета -> возврат с лёгкой пружинкой */
    public void flipTo(boolean toDark, long delay, Runnable onLand) {
        animate().cancel();
        setRotationY(0f);

        animate().rotationY(90f)
                .setStartDelay(delay)
                .setDuration(130)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    setDark(toDark);
                    setRotationY(-90f);
                    if (onLand != null) onLand.run();
                    animate().rotationY(0f)
                            .setStartDelay(0)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator(1.4f))
                            .start();
                })
                .start();
    }

    /** При нажатии/отпускании перерисовываемся ("вдавливание") */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    // рисование
    @Override
    protected void onDraw(@NonNull Canvas c) {
        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float o = Math.max(2 * d, Math.min(w, h) * 0.05f);  // смещение тени
        float sw = Math.max(1.5f * d, Math.min(w, h) * 0.035f);
        float r = 3 * d;
        float off = isPressed() ? o : 0;  // нажата - "вдавлена"
        stroke.setStrokeWidth(sw);

        // 1. жёсткая тень
        solid.setColor(ink);
        tmp.set(o, o, w, h);
        c.drawRoundRect(tmp, r, r, solid);

        // 2. плитка
        face.set(off + sw / 2, off + sw / 2, w - o + off - sw / 2, h - o + off - sw / 2);
        fill.setColor(dark ? darkColor : lightColor);
        c.drawRoundRect(face, r, r, fill);
        c.drawRoundRect(face, r, r, stroke);

        float s = face.width();

        // 3. пиксельный блик в углу
        float u = s * 0.06f;
        solid.setColor(dark ? 0x55FFFFFF : 0xE6FFFFFF);
        c.drawRect(face.left + u, face.top + u, face.left + u * 3.2f, face.top + u * 1.9f, solid);
        c.drawRect(face.left + u, face.top + u, face.left + u * 1.9f, face.top + u * 3.2f, solid);

        // 4. смайл
        if (dark) drawSleepy(c, s);
        else drawSunny(c, s);

        // 5. подсказка - ретро-курсор "нажми сюда"
        if (hint) drawCursor(c, s);
    }

    private void drawSunny(Canvas c, float s) {
        float cx = face.centerX();
        float cy = face.centerY();
        solid.setColor(ink);

        // глаза-капсулы
        tmp.set(cx - s * 0.17f, cy - s * 0.17f, cx - s * 0.09f, cy - s * 0.01f);
        c.drawOval(tmp, solid);
        tmp.set(cx + s * 0.09f, cy - s * 0.17f, cx + s * 0.17f, cy - s * 0.01f);
        c.drawOval(tmp, solid);

        // улыбка
        line.setColor(ink);
        line.setStrokeWidth(s * 0.055f);
        tmp.set(cx - s * 0.2f, cy - s * 0.1f, cx + s * 0.2f, cy + s * 0.2f);
        c.drawArc(tmp, 25, 130, false, line);
    }

    private void drawSleepy(Canvas c, float s) {
        float cx = face.centerX();
        float cy = face.centerY();

        // закрытые глазки "∪"
        line.setColor(darkFace);
        line.setStrokeWidth(s * 0.05f);
        for (int side = -1; side <= 1; side += 2) {
            float ex = cx + side * s * 0.13f;
            float ey = cy - s * 0.09f;
            tmp.set(ex - s * 0.07f, ey - s * 0.05f, ex + s * 0.07f, ey + s * 0.05f);
            c.drawArc(tmp, 10, 160, false, line);
        }

        // ротик "o"
        c.drawCircle(cx, cy + s * 0.13f, s * 0.04f, line);

        // "z z"
        zText.setTextSize(s * 0.17f);
        c.drawText("z", face.right - s * 0.27f, face.top + s * 0.32f, zText);
        zText.setTextSize(s * 0.12f);
        c.drawText("z", face.right - s * 0.16f, face.top + s * 0.2f, zText);
    }

    /** Классическая стрелка-курсор: белая заливка, чёрный контур. */
    private void drawCursor(Canvas c, float s) {
        float k = s * 0.018f;  // масштаб курсора
        float x = face.right - s * 0.26f;   // кончик стрелки - в правом нижнем углу
        float y = face.bottom - s * 0.33f;
        cursor.reset();
        cursor.moveTo(x, y);
        cursor.lineTo(x, y + 13 * k);
        cursor.lineTo(x + 3.2f * k, y + 10 * k);
        cursor.lineTo(x + 5.6f * k, y + 15 * k);
        cursor.lineTo(x + 7.8f * k, y + 14 * k);
        cursor.lineTo(x + 5.4f * k, y + 9.3f * k);
        cursor.lineTo(x + 9.4f * k, y + 9.3f * k);
        cursor.close();

        solid.setColor(0xFFFFFFFF);
        c.drawPath(cursor, solid);
        stroke.setStrokeWidth(Math.max(1.2f * d, s * 0.02f));
        c.drawPath(cursor, stroke);
    }
}
