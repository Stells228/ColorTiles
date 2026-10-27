package com.example.colortiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Прозрачный слой поверх экрана: при нажатии из плитки выскакивают
 * ретро-искорки, плюсики и кружки.
 * Нажатия не перехватывает.
 */
public class SparkleView extends View {

    private static final int STAR = 0, PLUS = 1, DOT = 2;

    private static class Spark {
        int type, color;
        float x, y, vx, vy, size, rot, vr, age, life;
    }

    private final ArrayList<Spark> sparks = new ArrayList<>();
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path star = new Path();
    private final Random rnd = new Random();
    private final float d;
    private final int[] palette;
    private long last = 0;

    public SparkleView(Context context) {
        this(context, null);
    }

    public SparkleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        d = getResources().getDisplayMetrics().density;
        palette = new int[]{
                ContextCompat.getColor(context, R.color.butter),
                ContextCompat.getColor(context, R.color.pink),
                ContextCompat.getColor(context, R.color.mint),
                ContextCompat.getColor(context, R.color.sky)
        };
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setColor(ContextCompat.getColor(context, R.color.ink));
        stroke.setStrokeJoin(Paint.Join.ROUND);
        stroke.setStrokeCap(Paint.Cap.ROUND);
        setFocusable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    public void burst(float x, float y, int count) {
        boolean idle = sparks.isEmpty();
        for (int i = 0; i < count; i++) {
            Spark p = new Spark();
            float r = rnd.nextFloat();
            p.type = r < 0.55f ? STAR : r < 0.8f ? PLUS : DOT;
            double a = rnd.nextFloat() * Math.PI * 2;
            float speed = (70 + rnd.nextFloat() * 120) * d;
            p.vx = (float) Math.cos(a) * speed;
            p.vy = (float) Math.sin(a) * speed;
            p.x = x + (float) Math.cos(a) * 18 * d;
            p.y = y + (float) Math.sin(a) * 18 * d;
            p.size = (p.type == STAR ? 7 + rnd.nextFloat() * 6 : 4 + rnd.nextFloat() * 3) * d;
            p.rot = rnd.nextFloat() * 90;
            p.vr = (rnd.nextFloat() - 0.5f) * 240;
            p.life = 0.5f + rnd.nextFloat() * 0.3f;
            p.color = palette[rnd.nextInt(palette.length)];
            sparks.add(p);
        }
        if (idle) last = 0;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(@NonNull Canvas c) {
        long now = System.nanoTime();
        float dt = last == 0 ? 0.016f : Math.min(0.033f, (now - last) / 1e9f);
        last = now;

        Iterator<Spark> it = sparks.iterator();
        while (it.hasNext()) {
            Spark p = it.next();
            p.age += dt;
            if (p.age >= p.life) {
                it.remove();
                continue;
            }
            float damp = Math.max(0f, 1f - 3f * dt);
            p.vx *= damp;
            p.vy *= damp;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.rot += p.vr * dt;

            float t = p.age / p.life;
            float scale = t < 0.25f ? t / 0.25f : 1f - (t - 0.25f) / 0.75f; // "выскок" и исчезание
            float sz = p.size * scale;
            if (sz <= 0.5f) continue;

            stroke.setStrokeWidth(1.8f * d);
            c.save();
            c.translate(p.x, p.y);
            c.rotate(p.rot);
            fill.setColor(p.color);
            switch (p.type) {
                case STAR: {
                    float k = sz * 0.16f;
                    star.reset();
                    star.moveTo(0, -sz);
                    star.quadTo(k, -k, sz, 0);
                    star.quadTo(k, k, 0, sz);
                    star.quadTo(-k, k, -sz, 0);
                    star.quadTo(-k, -k, 0, -sz);
                    star.close();
                    c.drawPath(star, fill);
                    c.drawPath(star, stroke);
                    break;
                }
                case PLUS:
                    stroke.setStrokeWidth(2.2f * d);
                    c.drawLine(-sz, 0, sz, 0, stroke);
                    c.drawLine(0, -sz, 0, sz, stroke);
                    break;
                default:
                    c.drawCircle(0, 0, sz * 0.7f, fill);
                    c.drawCircle(0, 0, sz * 0.7f, stroke);
                    break;
            }
            c.restore();
        }

        if (!sparks.isEmpty()) postInvalidateOnAnimation();
        else last = 0;
    }
}
