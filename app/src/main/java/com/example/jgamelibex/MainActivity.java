package com.example.jgamelibex;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements JGameLib.GameEvent {
    JGameLib gameLib = null;
    JGameLib.Image imgHeart;
    JGameLib.Image gameBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameLib = findViewById(R.id.gameLib);

        initGame();
    }

    @Override
    protected void onDestroy() {
        if(gameLib != null)
            gameLib.clearMemory();
        super.onDestroy();
    }

    private void initGame() {
        gameLib.setScreenAxis(100,140);
        gameLib.listener(this);
        gameBackground = gameLib.addImage(R.drawable.anipang_standby);
        imgHeart = gameLib.addImage(R.drawable.icon_heart1, 34, 12, 9, 6);
        imgHeart.addResource(R.drawable.progressing00);
        imgHeart.addResource(R.drawable.progressing01);
        imgHeart.addResource(R.drawable.progressing02);
        imgHeart.addResource(R.drawable.progressing03);
        imgHeart.addResource(R.drawable.progressing04);
        imgHeart.addResource(R.drawable.progressing05);
        imgHeart.addResource(R.drawable.progressing06);
        imgHeart.addResource(R.drawable.progressing07);

        gameLib.playBGM(R.raw.morning);
    }

    // User Event start ====================================

    public void onBtn1(View v) {
        gameLib.move(imgHeart, 45, 95, 1.0);
    }

    public void onBtn2(View v) {
        gameBackground.sourceRect(0,0,100,100);
    }

    // User Event end ====================================

    // Game Event start ====================================

    @Override
    public void onMoveEnded(JGameLib.Image img) {
        if(img == imgHeart) {
            gameLib.resize(imgHeart, 25, 25, 0.8);
            gameLib.playAudioBeep(R.raw.fireworks_fire);
        }
    }

    @Override
    public void onResizeEnded(JGameLib.Image img) {
        if(img == imgHeart) {
            gameLib.animation(imgHeart, 1, 8, 1);
            gameLib.playAudioBeep(R.raw.fireworks_boom);
        }
    }

    @Override
    public void onAnimationEnded(JGameLib.Image img) {
        if(img == imgHeart) {
            gameLib.setImageIndex(imgHeart, 0);
            gameLib.resize(imgHeart, 9, 6);
            gameLib.move(imgHeart, 34, 12);
        }
    }

    @Override
    public void onGameTouchEvent(JGameLib.Image img, int action, float blockX, float blockY) {
        if(img == imgHeart && action == MotionEvent.ACTION_MOVE) {
            gameLib.moveRelative(img, blockX, blockY);
        }
    }

    @Override
    public void onAudioCompletion(int resid) {}

    // Game Event end ====================================

}