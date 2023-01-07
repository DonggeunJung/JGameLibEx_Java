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
        imgHeart.move(45, 95, 1.0);
    }

    public void onBtn2(View v) {
        gameBackground.addResource(R.drawable.scroll_back_woods);
        gameBackground.setImageIndex(1);
        gameBackground.sourceArea(0, 0, 30, 100);
        gameBackground.sourceArea(70, 0, 4);
    }

    // User Event end ====================================

    // Game Event start ====================================

    @Override
    public void onGameWorkEnded(JGameLib.Image img, JGameLib.WorkType workType) {
        switch(workType) {
            case AUDIO_PLAY: {
                break;
            }
            case MOVE: {
                if(img == imgHeart) {
                    imgHeart.resize(25, 25, 0.8);
                    gameLib.playAudioBeep(R.raw.fireworks_fire);
                }
                break;
            }
            case RESIZE: {
                if(img == imgHeart) {
                    imgHeart.animation(1, 8, 1);
                    gameLib.playAudioBeep(R.raw.fireworks_boom);
                }
                break;
            }
            case ANIMATION: {
                if(img == imgHeart) {
                    imgHeart.setImageIndex(0);
                    imgHeart.resize(9, 6);
                    imgHeart.move(34, 12);
                }
                break;
            }
            case SOURCE_AREA: {
                if(img == gameBackground) {
                    gameBackground.sourceArea(0, 0, 30, 100);
                    gameBackground.sourceArea(70, 0, 4);
                }
                break;
            }
        }
    }

    @Override
    public void onGameTouchEvent(JGameLib.Image img, int action, float blockX, float blockY) {
        if(img == imgHeart && action == MotionEvent.ACTION_MOVE) {
            img.moveRelative(blockX, blockY);
        }
    }

    // Game Event end ====================================

}