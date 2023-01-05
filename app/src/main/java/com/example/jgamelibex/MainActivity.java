package com.example.jgamelibex;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements JGameLib.GameEvent {
    JGameLib gameLib = null;
    JGameLib.Image imgHeart;

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
        gameLib.setBackgroundResource(R.drawable.anipang_standby);
        gameLib.listener(this);
        imgHeart = gameLib.addImage(R.drawable.icon_heart1, 9, 0.8, 34, 8);
        gameLib.addResource(imgHeart, R.drawable.progressing00);
        gameLib.addResource(imgHeart, R.drawable.progressing01);
        gameLib.addResource(imgHeart, R.drawable.progressing02);
        gameLib.addResource(imgHeart, R.drawable.progressing03);
        gameLib.addResource(imgHeart, R.drawable.progressing04);
        gameLib.addResource(imgHeart, R.drawable.progressing05);
        gameLib.addResource(imgHeart, R.drawable.progressing06);
        gameLib.addResource(imgHeart, R.drawable.progressing07);

        gameLib.playBGM(R.raw.morning);
    }

    public void onBtn1(View v) {
        gameLib.move(imgHeart, 34, 67, 1.0);
    }

    // Event start ====================================

    @Override
    public void onMoveEnded(JGameLib.Image img) {
        if(img == imgHeart) {
            gameLib.resize(imgHeart, 23, 1.0, 0.8);
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
            gameLib.resize(imgHeart, 9, 0.8);
            gameLib.move(imgHeart, 34, 8);
        }
    }

    @Override
    public void onGameTouchEvent(JGameLib.Image img, int action, float rateH, float rateV) {
        if(img == imgHeart) {
            gameLib.move(img, img.left + rateH, img.top + rateV);
        }
    }

    @Override
    public void onAudioCompletion(int resid) {}

    // Event end ====================================

}