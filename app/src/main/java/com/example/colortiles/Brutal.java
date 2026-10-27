package com.example.colortiles;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;

import androidx.core.content.ContextCompat;

// Продолжаю нео-брутализм
/** Фоны: чёрный контур + жёсткая тень, смещённая под 45° */
public final class Brutal {

    private Brutal() { }

    /** Плашка с контуром 3dp и тенью. pressed = "вдавлена" в тень */
    public static LayerDrawable frame(Context c, int fill, float shadowDp, float radiusDp, boolean pressed) {
        float d = c.getResources().getDisplayMetrics().density;
        int ink = ContextCompat.getColor(c, R.color.ink);
        int s = Math.round(shadowDp * d);
        float r = radiusDp * d;

        GradientDrawable shadow = new GradientDrawable();
        shadow.setColor(ink);
        shadow.setCornerRadius(r);

        GradientDrawable face = new GradientDrawable();
        face.setColor(fill);
        face.setCornerRadius(r);
        face.setStroke(Math.round(3 * d), ink);

        LayerDrawable ld = new LayerDrawable(new Drawable[]{shadow, face});
        ld.setLayerInset(0, s, s, 0, 0);
        if (pressed) ld.setLayerInset(1, s, s, 0, 0);
        else ld.setLayerInset(1, 0, 0, s, s);
        return ld;
    }

    /** Кнопка: при нажатии вдавливается, тень исчезает */
    public static StateListDrawable button(Context c, int fill, float shadowDp) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_pressed}, frame(c, fill, shadowDp, 3, true));
        sld.addState(new int[]{}, frame(c, fill, shadowDp, 3, false));
        return sld;
    }

    /** Переключатель: выбран - цветной и вдавленный */
    public static StateListDrawable toggle(Context c, int onColor, int offColor) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_selected}, frame(c, onColor, 3, 3, true));
        sld.addState(new int[]{android.R.attr.state_pressed}, frame(c, offColor, 3, 3, true));
        sld.addState(new int[]{}, frame(c, offColor, 3, 3, false));
        return sld;
    }
}
