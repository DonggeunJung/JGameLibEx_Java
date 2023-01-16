package com.example.jgamelibex;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements JGameLib.GameEvent {
    JGameLib gameLib = null;
    JGameLib.Card gameBackground;
    JGameLib.Card cardColor;
    JGameLib.Card cardHeart;

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

    void initGame() {
        gameLib.setScreenGrid(100,140);
        gameLib.listener(this);
        gameBackground = gameLib.addCard(R.drawable.anipang_standby);
        gameBackground.addImage(R.drawable.scroll_back_woods);
        cardColor = gameLib.addCardColor(Color.rgb(255,240,240), 80,110,20,20);
        cardHeart = gameLib.addCard(R.drawable.icon_heart1, 34, 12, 9, 6);
        cardHeart.addImage(R.drawable.explosion01);
        cardHeart.addImage(R.drawable.explosion02);
        cardHeart.addImage(R.drawable.explosion03);
        cardHeart.addImage(R.drawable.explosion04);
        cardHeart.addImage(R.drawable.explosion05);
        cardHeart.addImage(R.drawable.explosion06);

        gameLib.playBGM(R.raw.morning);
        gameLib.startSensorAccelerometer();
    }

    void scrollBackground() {
        gameBackground.sourceRect(0, 0, 30, 100);
        gameBackground.sourceRectIng(100, 0, 8);
    }

    // User Event start ====================================

    public void onBtn1(View v) {
        cardHeart.moving(45, 95, 1.0);
    }

    public void onBtn2(View v) {
        if(gameBackground.isSourceRectIng()) {
            gameBackground.stopSourceRectIng();
        } else {
            gameBackground.imageChange(1);
            scrollBackground();
        }
    }

    // User Event end ====================================

    // Game Event start ====================================

    @Override
    public void onGameWorkEnded(JGameLib.Card card, JGameLib.WorkType workType) {
        switch(workType) {
            case AUDIO_PLAY: {
                break;
            }
            case MOVE: {
                if(card == cardHeart) {
                    cardHeart.resizing(25, 25, 0.8);
                    gameLib.playAudioBeep(R.raw.fireworks_fire);
                }
                break;
            }
            case RESIZE: {
                if(card == cardHeart) {
                    cardHeart.imageChanging(1, 6, 1);
                    gameLib.playAudioBeep(R.raw.fireworks_boom);
                }
                break;
            }
            case IMAGE_CHANGE: {
                if(card == cardHeart) {
                    cardHeart.imageChange(0);
                    cardHeart.resize(9, 6);
                    cardHeart.move(34, 12);
                }
                break;
            }
            case SOURCE_RECT: {
                if(card == gameBackground) {
                    scrollBackground();
                }
                break;
            }
        }
    }

    @Override
    public void onGameTouchEvent(JGameLib.Card card, int action, float blockX, float blockY) {
        if(card == cardHeart && action == MotionEvent.ACTION_MOVE) {
            card.moveGap(blockX, blockY);
        }
    }

    @Override
    public void onGameSensor(int sensorType, float x, float y, float z) {
        if(sensorType == Sensor.TYPE_ACCELEROMETER) {
            float v1 = 0, v2 = 0, cut = 10, rate = 0.2f;
            if(Math.abs(x) > cut)
                v1 = (cut - Math.abs(x)) * rate;
            if(Math.abs(y) > cut)
                v2 = (cut - Math.abs(y)) * rate;
            cardColor.moveGap(v1, v2);
        }
    }

    @Override
    public void onGameCollision(JGameLib.Card card1, JGameLib.Card card2) {}

    @Override
    public void onGameTimer(int what) {}

    // Game Event end ====================================

}