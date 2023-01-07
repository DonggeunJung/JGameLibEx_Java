package com.example.jgamelibex;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity implements JGameLib.GameEvent {
    JGameLib gameLib = null;
    JGameLib.Card cardHeart;
    JGameLib.Card gameBackground;

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
        gameBackground = gameLib.addCard(R.drawable.anipang_standby);
        gameBackground.addImage(R.drawable.scroll_back_woods);
        cardHeart = gameLib.addCard(R.drawable.icon_heart1, 34, 12, 9, 6);
        cardHeart.addImage(R.drawable.progressing00);
        cardHeart.addImage(R.drawable.progressing01);
        cardHeart.addImage(R.drawable.progressing02);
        cardHeart.addImage(R.drawable.progressing03);
        cardHeart.addImage(R.drawable.progressing04);
        cardHeart.addImage(R.drawable.progressing05);
        cardHeart.addImage(R.drawable.progressing06);
        cardHeart.addImage(R.drawable.progressing07);

        gameLib.playBGM(R.raw.morning);
    }

    // User Event start ====================================

    public void onBtn1(View v) {
        cardHeart.moving(45, 95, 1.0);
    }

    public void onBtn2(View v) {
        if(gameBackground.isSourceAreaIng()) {
            gameBackground.stopSourceAreaIng();
        } else {
            gameBackground.imageChange(1);
            gameBackground.sourceArea(0, 0, 30, 100);
            gameBackground.sourceAreaIng(70, 0, 4);
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
                    cardHeart.imageChanging(1, 8, 1);
                    gameLib.playAudioBeep(R.raw.fireworks_boom);
                }
                break;
            }
            case ANIMATION: {
                if(card == cardHeart) {
                    cardHeart.imageChange(0);
                    cardHeart.resize(9, 6);
                    cardHeart.move(34, 12);
                }
                break;
            }
            case SOURCE_AREA: {
                if(card == gameBackground) {
                    gameBackground.sourceArea(0, 0, 30, 100);
                    gameBackground.sourceAreaIng(70, 0, 4);
                }
                break;
            }
        }
    }

    @Override
    public void onGameTouchEvent(JGameLib.Card card, int action, float blockX, float blockY) {
        if(card == cardHeart && action == MotionEvent.ACTION_MOVE) {
            card.relativeMove(blockX, blockY);
        }
    }

    // Game Event end ====================================

}