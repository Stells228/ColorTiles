package com.example.colortiles;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    int darkColor;   // цвет тёмных плиток
    int brightColor; // цвет светлых плиток

    /** Массив плиток из заготовки: tiles[строка][столбец]. */
    RetroTile[][] tiles = new RetroTile[4][4];

    /** Модель поля: true — тёмная плитка, false — светлая. */
    boolean[][] dark = new boolean[4][4];

    private static final int HINT_NONE = 0, HINT_LIGHT = 1, HINT_DARK = 2;
    private static final long WAVE_STEP_MS = 50;   // задержка "волны" переворотов
    private static final long MOVE_LOCK_MS = 330;  // пока плитки переворачиваются, нажатия игнорируем

    private SparkleView sparkles;
    private SegmentBar segmentBar;
    private TextView movesText, loadingText, countText, hintLight, hintDark, btnNew, winBody;
    private View winDialog;
    private RetroTile winTile;

    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int moves;
    private boolean busy, won;
    private int hintTarget = HINT_NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        darkColor = ContextCompat.getColor(this, R.color.dark);
        brightColor = ContextCompat.getColor(this, R.color.bright);

        // Заполняем массив tiles ссылками на плитки
        int[][] ids = {
                {R.id.t00, R.id.t01, R.id.t02, R.id.t03},
                {R.id.t10, R.id.t11, R.id.t12, R.id.t13},
                {R.id.t20, R.id.t21, R.id.t22, R.id.t23},
                {R.id.t30, R.id.t31, R.id.t32, R.id.t33}
        };
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tiles[i][j] = findViewById(ids[i][j]);
            }
        }

        bindViews();
        setupStyles();
        newGame();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void bindViews() {
        sparkles = findViewById(R.id.sparkles);
        segmentBar = findViewById(R.id.segmentBar);
        movesText = findViewById(R.id.movesText);
        loadingText = findViewById(R.id.loadingText);
        countText = findViewById(R.id.countText);
        hintLight = findViewById(R.id.hintLight);
        hintDark = findViewById(R.id.hintDark);
        btnNew = findViewById(R.id.btnNew);
        winDialog = findViewById(R.id.winDialog);
        winTile = findViewById(R.id.winTile);
        winBody = findViewById(R.id.winBody);

        btnNew.setOnClickListener(v -> newGame());
        hintLight.setOnClickListener(v -> toggleHint(HINT_LIGHT));
        hintDark.setOnClickListener(v -> toggleHint(HINT_DARK));
        findViewById(R.id.winOk).setOnClickListener(v -> hideWinDialog());
        findViewById(R.id.winAgain).setOnClickListener(v -> newGame());
    }

    private void setupStyles() {
        int paper = color(R.color.paper);

        findViewById(R.id.badge).setBackground(Brutal.frame(this, color(R.color.sky), 3, 3, false));
        findViewById(R.id.statsWindow).setBackground(Brutal.frame(this, paper, 4, 3, false));
        winDialog.setBackground(Brutal.frame(this, paper, 5, 3, false));

        // Поле — бумага в клетку
        float d = getResources().getDisplayMetrics().density;
        findViewById(R.id.grid).setBackground(new LayerDrawable(new Drawable[]{
                new ColorDrawable(paper),
                new GridDrawable(paper, 0x1A000000, 14 * d, 1 * d)
        }));

        btnNew.setBackground(Brutal.button(this, color(R.color.coral), 4));
        hintLight.setBackground(Brutal.toggle(this, color(R.color.bright), paper));
        hintDark.setBackground(Brutal.toggle(this, color(R.color.sky), paper));
        findViewById(R.id.winOk).setBackground(Brutal.button(this, paper, 3));
        findViewById(R.id.winAgain).setBackground(Brutal.button(this, color(R.color.coral), 3));
    }

    // Игра
    /** Рандом стартовая позиция */
    private void newGame() {
        handler.removeCallbacksAndMessages(null);
        hideWinDialog();

        do {
            for (int i = 0; i < 4; i++)
                for (int j = 0; j < 4; j++)
                    dark[i][j] = random.nextBoolean();
        } while (allSame());

        moves = 0;
        won = false;
        busy = false;

        // плитки "выпрыгивают" волной
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                RetroTile t = tiles[i][j];
                t.animate().cancel();
                t.setRotationY(0f);
                t.setDark(dark[i][j]);
                t.setScaleX(0f);
                t.setScaleY(0f);
                t.animate().scaleX(1f).scaleY(1f)
                        .setStartDelay((i + j) * 40L)
                        .setDuration(320)
                        .setInterpolator(new OvershootInterpolator(1.8f))
                        .start();
            }
        }

        loadingText.setText(R.string.loading);
        updateStats();
        updateHints();
    }

    /** Обработчик нажатия на плитку */
    public void onClick(View v) {
        if (busy || won) return;

        // тэг вида "12": первая цифра — строка, вторая — столбец
        String tag = v.getTag().toString();
        int x = tag.charAt(0) - '0';
        int y = tag.charAt(1) - '0';

        hideHints();

        // строка x целиком (включая саму плитку)...
        for (int i = 0; i < 4; i++) {
            changeColor(x, i, WAVE_STEP_MS * Math.abs(i - y));
        }
        // ...и столбец y (саму плитку второй раз не трогаем)
        for (int i = 0; i < 4; i++) {
            if (i != x) changeColor(i, y, WAVE_STEP_MS * Math.abs(i - x));
        }

        sparkleAt(v, 7);

        moves++;
        busy = true;
        handler.postDelayed(() -> {
            busy = false;
            updateStats();
            checkWin();
            updateHints();
        }, MOVE_LOCK_MS);
    }

    /** Меняет цвет плитки на противоположный */
    public void changeColor(int i, int j, long delay) {
        dark[i][j] = !dark[i][j];
        tiles[i][j].flipTo(dark[i][j], delay, null);
    }

    private boolean allSame() {
        boolean first = dark[0][0];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (dark[i][j] != first) return false;
        return true;
    }

    /** Проверка выигрыша: все плитки одного цвета -> Toast + окно win.exe */
    private void checkWin() {
        if (!allSame()) return;
        won = true;
        boolean darkWon = dark[0][0];

        showWinToast(darkWon);

        loadingText.setText(R.string.loading_done);
        showWinDialog(darkWon);

        // искорки из каждой плитки
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                final RetroTile t = tiles[i][j];
                handler.postDelayed(() -> sparkleAt(t, 3), (i + j) * 60L);
            }
        }
    }

    /**
     * Toast о победе - маленькое ретро-окно notify.exe
     */
    @SuppressWarnings("deprecation")
    private void showWinToast(boolean darkWon) {
        View view = LayoutInflater.from(this).inflate(R.layout.toast_win, null);
        view.setBackground(Brutal.frame(this, color(R.color.paper), 4, 3, false));

        RetroTile icon = view.findViewById(R.id.toastTile);
        icon.setDark(darkWon);

        TextView title = view.findViewById(R.id.toastTitle);
        title.setText(darkWon ? R.string.toast_title_dark : R.string.toast_title_light);

        TextView text = view.findViewById(R.id.toastText);
        text.setText(getString(R.string.toast_text, moves, plural(moves)));

        Toast toast = new Toast(getApplicationContext());
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dp(96));
        toast.show();
    }

    /** "1 ход", "2 хода", "5 ходов": окончание к слову "ход". */
    private String plural(int n) {
        int n10 = n % 10, n100 = n % 100;
        if (n10 == 1 && n100 != 11) return "";
        if (n10 >= 2 && n10 <= 4 && (n100 < 12 || n100 > 14)) return "а";
        return "ов";
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private void showWinDialog(boolean darkWon) {
        winTile.setDark(darkWon);
        winBody.setText(getString(darkWon ? R.string.win_dark_body : R.string.win_light_body, moves));
        winDialog.setVisibility(View.VISIBLE);
        winDialog.setAlpha(0f);
        winDialog.setScaleX(0.7f);
        winDialog.setScaleY(0.7f);
        winDialog.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setStartDelay(250)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .start();
    }

    private void hideWinDialog() {
        winDialog.animate().cancel();
        winDialog.setVisibility(View.GONE);
    }

    private void sparkleAt(View t, int count) {
        int[] a = new int[2];
        int[] b = new int[2];
        t.getLocationInWindow(a);
        sparkles.getLocationInWindow(b);
        sparkles.burst(a[0] - b[0] + t.getWidth() / 2f, a[1] - b[1] + t.getHeight() / 2f, count);
    }

    private void updateStats() {
        int darkCount = 0;
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (dark[i][j]) darkCount++;
        int light = 16 - darkCount;
        movesText.setText(String.valueOf(moves));
        countText.setText(getString(R.string.counts, light, darkCount));
        segmentBar.setLightCount(light);
    }

    //  ПОДСКАЗКА
    //  На поле 4x4 из ЛЮБОЙ позиции можно прийти к ЛЮБОМУ цвету.
    //  Чтобы перевернуть одну плитку (r,c), надо нажать все 7 плиток её строки и столбца.
    //  Складывая это для всех "неправильных" плиток, получаем:
    //  плитку (i,j) нажать, если нечётно (неправильных в строке i) + (в столбце j) - (она сама)

    private void toggleHint(int target) {
        hintTarget = (hintTarget == target) ? HINT_NONE : target;
        hintLight.setSelected(hintTarget == HINT_LIGHT);
        hintDark.setSelected(hintTarget == HINT_DARK);
        updateHints();
    }

    private void updateHints() {
        if (hintTarget == HINT_NONE || won) {
            hideHints();
            return;
        }
        boolean targetDark = hintTarget == HINT_DARK;
        int[] row = new int[4];
        int[] col = new int[4];
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (dark[i][j] != targetDark) { row[i]++; col[j]++; }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int self = dark[i][j] != targetDark ? 1 : 0;
                tiles[i][j].setHint(((row[i] + col[j] - self) & 1) == 1);
            }
        }
    }

    private void hideHints() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                tiles[i][j].setHint(false);
    }

    private int color(int resId) {
        return ContextCompat.getColor(this, resId);
    }
}
