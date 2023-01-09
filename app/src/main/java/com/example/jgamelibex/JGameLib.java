/* JGameLib_Java : 2D Game library for education      */
/* Date : 2023.Jan.04 ~                               */
/* Author : Dennis (Donggeun Jung)                    */
/* Contact : topsan72@gmail.com                       */
package com.example.jgamelibex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import java.util.ArrayList;

public class JGameLib extends View implements SensorEventListener {
    boolean firstDraw = true;
    float totalPixelW = 480, totalPixelH = 800;
    float blocksW = 480, blocksH = 800;
    float blockSize = totalPixelH / blocksH;
    RectF screenRect;
    int timerGap = 50;
    boolean needDraw = false;
    ArrayList<Card> cards = new ArrayList();
    Card touchedCard = null;
    float touchX = 0;
    float touchY = 0;

    public JGameLib(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    void init(Canvas canvas) {
        totalPixelW = canvas.getWidth();
        totalPixelH = canvas.getHeight();
        screenRect = getScreenRect();
        blockSize = screenRect.width() / blocksW;
        timer.removeMessages(0);
        timer.sendEmptyMessageDelayed(0, timerGap);
    }

    RectF getScreenRect() {
        float pixelsRatio = totalPixelW / totalPixelH;
        float blocksRatio = blocksW / blocksH;
        RectF rect = new RectF();
        if(pixelsRatio > blocksRatio) {
            rect.top = 0;
            rect.bottom = totalPixelH;
            float screenW = totalPixelH * blocksRatio;
            rect.left = (totalPixelW - screenW) / 2.f;
            rect.right = rect.left + screenW;
        } else {
            rect.left = 0;
            rect.right = totalPixelW;
            float screenH = totalPixelW / blocksRatio;
            rect.top = (totalPixelH - screenH) / 2.f;
            rect.bottom = rect.top + screenH;
        }
        return rect;
    }

    void redraw() {
        this.invalidate();
    }

    public void onDraw(Canvas canvas) {
        if( firstDraw ) {
            firstDraw = false;
            init(canvas);
        }

        Paint pnt = new Paint();
        pnt.setStyle(Paint.Style.FILL);
        pnt.setAntiAlias(true);

        for(Card card : cards) {
            if(!card.visible) continue;
            RectF rect = screenRect;
            if(card.dstRect != null) {
                rect = getDstRect(card);
            }
            switch(card.backType) {
                case 0 : {
                    drawRect(canvas, pnt, card.backColor, rect);
                }
                case 1 : {
                    drawBitmap(canvas, pnt, card.bmp, rect, card.srcRect);
                }
            }
        }
    }

    void drawRect(Canvas canvas, Paint pnt, int color, RectF rectDst) {
        pnt.setColor(color);
        canvas.drawRect(rectDst, pnt);
    }

    void drawBitmap(Canvas canvas, Paint pnt, Bitmap bmp, RectF rectDst, RectF rectSrc) {
        if(bmp == null) return;
        if(rectSrc == null) {
            canvas.drawBitmap(bmp, null, rectDst, pnt);
            return;
        }
        float bmpPixelW = bmp.getWidth();
        float bmpPixelH = bmp.getHeight();
        float areaL = rectSrc.left / 100f * bmpPixelW;
        float areaR = rectSrc.right / 100f * bmpPixelW;
        float areaT = rectSrc.top / 100f * bmpPixelH;
        float areaB = rectSrc.bottom / 100f * bmpPixelH;
        Rect area = new Rect((int)areaL, (int)areaT, (int)areaR, (int)areaB);
        canvas.drawBitmap(bmp, area, rectDst, pnt);
    }

    Handler timer = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if(needDraw) {
                needDraw = false;
                for(Card card : cards) {
                    card.next();
                }
                redraw();
            }
            timer.sendEmptyMessageDelayed(0, timerGap);
            return false;
        }
    });

    private RectF getDstRect(Card card) {
        RectF rect = new RectF(0,0,0,0);
        if(card.dstRect == null) return rect;
        rect.left = screenRect.left + card.dstRect.left * blockSize;
        rect.right = screenRect.left + card.dstRect.right * blockSize;
        rect.top = screenRect.top + card.dstRect.top * blockSize;
        rect.bottom = screenRect.top + card.dstRect.bottom * blockSize;
        return rect;
    }

    private float getBlocksHorizontal(float pixelH) {
        return pixelH / blockSize;
    }

    private float getBlocksVertical(float pixelV) {
        return pixelV / blockSize;
    }

    Bitmap getBitmap(int resid) {
        return BitmapFactory.decodeResource(getResources(), resid);
    }

    Card findCard(float pixelX, float pixelY) {
        for(Card card : cards) {
            if(!card.visible) continue;
            RectF rect = getDstRect(card);
            if(rect.contains(pixelX, pixelY)) {
                return card;
            }
        }
        return null;
    }

    Bitmap loadBitmap(ArrayList<Integer> resids, double idx) {
        if(resids.isEmpty() || (int)idx < 0 || (int)idx >= resids.size())
            return null;
        int resid = resids.get((int)idx);
        return getBitmap(resid);
    }

    int indexOf(Card card) {
        for(int i = cards.size()-1; i >= 0; i--) {
            if(cards.get(i) == card)
                return i;
        }
        return -1;
    }

    // Inside Class start ====================================

    class Card {
        ArrayList<Integer> resids = new ArrayList();
        Bitmap bmp;
        double idx = -1;
        double unitIdx = 0, endIdx = 0;
        RectF dstRect = null;
        float unitL=0, unitT=0;
        float endL, endT;
        float unitW=0, unitH=0;
        float endW, endH;
        RectF srcRect = null;
        float unitSrcL=0, unitSrcT=0;
        float endSrcL, endSrcT;
        boolean visible = true;
        int backColor = 0x00000000;
        int backType = 1;

        Card(int clr, int type) {
            backType = type;
            this.backColor = clr;
        }

        Card(int resid) {
            resids.add(resid);
            idx = 0;
            loadBmp();
        }

        void next() {
            if(!visible) return;
            nextMove();
            nextResize();
            nextAnimation();
            nextSourceArea();
        }

        void nextMove() {
            if(unitL == 0 && unitT == 0) return;
            float currL = dstRect.left, currT = dstRect.top;
            float nextL = currL + unitL, nextT = currT + unitT;

            if((unitL != 0 && Math.min(currL,nextL) <= endL && endL <= Math.max(currL,nextL))
                    || (unitT != 0 && Math.min(currT,nextT) <= endT && endT <= Math.max(currT,nextT))) {
                unitL = unitT = 0;
                nextL = endL;
                nextT = endT;
            }
            move(nextL, nextT);
            if(unitL == 0 && unitT == 0 && listener != null)
                listener.onGameWorkEnded(this, WorkType.MOVE);
        }

        void nextResize() {
            if(unitW == 0 && unitH == 0) return;
            float currW = dstRect.width(), currH = dstRect.height();
            float nextW = currW + unitW, nextH = currH + unitH;
            if((unitW != 0 && Math.min(currW,nextW) <= endW && endW <= Math.max(currW,nextW))
                    || (unitH != 0 && Math.min(currW,nextW) <= endH && endH <= Math.max(currW,nextW))) {
                unitW = unitH = 0;
                nextW = endW;
                nextH = endH;
            }
            resize(nextW, nextH);
            if(unitW == 0 && unitH == 0 && listener != null)
                listener.onGameWorkEnded(this, WorkType.RESIZE);
        }

        void nextAnimation() {
            if(unitIdx == 0) return;
            double curridx = idx;
            double nextIdx = curridx + unitIdx;
            if(nextIdx >= endIdx || nextIdx >= resids.size()-1) {
                unitIdx = 0;
                nextIdx = Math.min((int)endIdx, resids.size()-1);
            }
            idx = nextIdx;
            if((int)nextIdx > (int)curridx) {
                loadBmp();
            }
            if(unitIdx == 0 && listener != null)
                listener.onGameWorkEnded(this, WorkType.ANIMATION);
            needDraw = true;
        }

        void nextSourceArea() {
            if(unitSrcL == 0 && unitSrcT == 0) return;
            float currL = srcRect.left, currT = srcRect.top;
            float nextL = currL + unitSrcL, nextT = currT + unitSrcT;

            if((unitSrcL != 0 && Math.min(currL,nextL) <= endSrcL && endSrcL <= Math.max(currL,nextL))
                    || (unitSrcT != 0 && Math.min(currT,nextT) <= endSrcT && endSrcT <= Math.max(currT,nextT))) {
                unitSrcL = unitSrcT = 0;
                nextL = endSrcL;
                nextT = endSrcT;
            }
            sourceArea(nextL, nextT, srcRect.width(), srcRect.height());
            if(unitSrcL == 0 && unitSrcT == 0 && listener != null)
                listener.onGameWorkEnded(this, WorkType.SOURCE_AREA);
        }

        // Card API start ====================================

        public RectF sourceArea() {
            return srcRect;
        }

        public void sourceArea(double l, double t, double w, double h) {
            RectF rect = new RectF((float)l, (float)t, (float)(l+w), (float)(t+h));
            sourceArea(rect);
        }

        public void sourceArea(RectF rect) {
            srcRect = rect;
            needDraw = true;
        }

        public void sourceAreaIng(double l, double t, double time) {
            this.endSrcL = (float)l;
            this.endSrcT = (float)t;
            float frames = (float)framesOfTime(time);
            if(frames != 0) {
                this.unitSrcL = (this.endSrcL - this.srcRect.left) / frames;
                this.unitSrcT = (this.endSrcT - this.srcRect.top) / frames;
            } else {
                this.unitSrcL = 0;
                this.unitSrcT = 0;
            }
            needDraw = true;
        }

        public void stopSourceAreaIng() {
            this.unitSrcL = 0;
            this.unitSrcT = 0;
        }

        public boolean isSourceAreaIng() {
            return unitSrcL != 0 || unitSrcT != 0;
        }

        public void addImage(int resid) {
            resids.add(resid);
        }

        public void removeImage(int idx) {
            if(idx >= resids.size()) return;
            resids.remove(idx);
        }

        public void visible(boolean s) {
            visible = s;
            needDraw = true;
        }

        public void loadBmp() {
            bmp = loadBitmap(resids, idx);
        }

        public RectF screenArea() {
            return this.dstRect;
        }

        public void screenArea(double l, double t, double w, double h) {
            this.dstRect.left = (float)l;
            this.dstRect.top = (float)t;
            this.dstRect.right = (float)l + (float)w;
            this.dstRect.bottom = (float)t + (float)h;
            needDraw = true;
        }

        public void screenAreaGap(double gapL, double gapT, double gapW, double gapH) {
            this.dstRect.left += (float)(gapL);
            this.dstRect.right += (float)(gapL + gapW);
            this.dstRect.top += (float)(gapT);
            this.dstRect.bottom += (float)(gapT + gapH);
            needDraw = true;
        }

        public void move(double l, double t) {
            float w = this.dstRect.width(), h = this.dstRect.height();
            screenArea(l, t, w, h);
        }

        public void moving(double l, double t, double time) {
            this.endL = (float)l;
            this.endT = (float)t;
            float frames = (float)framesOfTime(time);
            if(frames != 0) {
                this.unitL = (this.endL - this.dstRect.left) / frames;
                this.unitT = (this.endT - this.dstRect.top) / frames;
            } else {
                this.unitL = 0;
                this.unitT = 0;
            }
            needDraw = true;
        }

        public void stopMoving() {
            this.unitL = 0;
            this.unitT = 0;
        }

        public boolean isMoving() {
            return unitL != 0 || unitT != 0;
        }

        public void moveGap(double gapL, double gapT) {
            move(this.dstRect.left+(float)gapL, this.dstRect.top+(float)gapT);
        }

        public void moveGap(double gapL, double gapT, double time) {
            moving(this.dstRect.left+(float)gapL, this.dstRect.top+(float)gapT, time);
        }

        public void resize(double w, double h) {
            this.dstRect.left = this.dstRect.centerX() - (float)(w / 2.);
            this.dstRect.right = this.dstRect.left + (float)w;
            this.dstRect.top = this.dstRect.centerY() - (float)(h / 2.);
            this.dstRect.bottom = this.dstRect.top + (float)h;
            needDraw = true;
        }

        public void resizing(double w, double h, double time) {
            this.endW = (float)w;
            this.endH = (float)h;
            float frames = (float)framesOfTime(time);
            if(frames != 0) {
                this.unitW = (this.endW - this.dstRect.width()) / frames;
                this.unitH = (this.endH - this.dstRect.height()) / frames;
            } else {
                this.unitW = 0;
                this.unitH = 0;
            }
            needDraw = true;
        }

        public void resizeGap(double w, double h) {
            resize(this.dstRect.width()+w, this.dstRect.height()+h);
        }

        public void resizingGap(double w, double h, double time) {
            resizing(this.dstRect.width()+w, this.dstRect.height()+h, time);
        }

        public void stopResizing() {
            this.unitW = 0;
            this.unitH = 0;
        }

        public boolean isResizing() {
            return unitW != 0 || unitH != 0;
        }

        public void imageChange(int idx) {
            imageChanging(idx, idx, 0);
        }

        public void imageChanging(double time) {
            if(this.resids.isEmpty()) return;
            imageChanging(0, this.resids.size()-1, time);
        }

        public void imageChanging(int start, int end, double time) {
            if(this.resids.isEmpty()) return;
            if(this.idx != start) {
                this.idx = start;
                this.loadBmp();
            }
            this.endIdx = end;
            double frames = framesOfTime(time);
            if(frames != 0)
                this.unitIdx = (double)(end - start) / frames;
            else
                this.unitIdx = 0;
            needDraw = true;
        }

        public void stopImageChanging() {
            this.unitIdx = 0;
        }

        public boolean isImageChanging() {
            return unitIdx == 0;
        }

        public void deleteAllImages() {
            for(int i = this.resids.size()-1; i >= 0; i--) {
                this.resids.remove(i);
            }
        }

        // Card API end ====================================

    }

    // Inside Class end ====================================

    // API start ====================================

    public void setScreenGrid(float w, float h) {
        blocksW = w;
        blocksH = h;
        firstDraw = true;
    }

    public Card addCardColor(int clr) {
        Card card = new Card(clr, 0);
        addCard(card);
        return card;
    }

    public Card addCardColor(int clr, double l, double t, double w, double h) {
        Card card = new Card(clr, 0);
        addCard(card, l, t, w, h);
        return card;
    }

    public Card addCard(int resid)  {
        Card card = new Card(resid);
        addCard(card);
        return card;
    }

    public Card addCard(int resid, double l, double t, double w, double h) {
        Card card = new Card(resid);
        addCard(card, l, t, w, h);
        return card;
    }

    public void addCard(Card card, double l, double t, double w, double h) {
        addCard(card);
        card.dstRect = new RectF((float)l, (float)t, (float)(l + w), (float)(t + h));
    }

    public void addCard(Card card) {
        cards.add(card);
        needDraw = true;
    }

    public double framesOfTime(double time) {
        double miliTime = time * 1000.;
        return miliTime / timerGap;
    }

    public void clearMemory() {
        timer.removeMessages(0);
        deleteBGM();
        for(int i = cards.size()-1; i >= 0; i--) {
            Card card = cards.get(i);
            card.deleteAllImages();
            cards.remove(i);
        }
    }

    public void popupDialog(String title, String description, String btnText1) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        if(title != null && !title.isEmpty())
            dialog.setTitle(title);
        if(description != null && !description.isEmpty())
            dialog.setMessage(description);
        if(btnText1 != null && !btnText1.isEmpty())
            dialog.setPositiveButton("Close", null);
        dialog.show();
    }

    Vibrator vibrator;

    // To use Vibrator, add below Permission into AndroidManifest.xml
    // <uses-permission android:name="android.permission.VIBRATE"/>
    public void vibrate(double second) {
        if(vibrator == null)
            vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate((int)(second * 1000));
    }

    SensorManager sensorMgr = null;

    public void startSensorAccelerometer() {
        if(sensorMgr == null)
            sensorMgr = (SensorManager)getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorAcceler = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if( sensorAcceler != null )
            sensorMgr.registerListener(this, sensorAcceler, SensorManager.SENSOR_DELAY_UI);
    }

    public void stopSensorAccelerometer() {
        if(sensorMgr == null) return;
        sensorMgr.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {
        float v[] = event.values;
        switch( event.sensor.getType() ) {
            case Sensor.TYPE_ACCELEROMETER :
                if(listener != null) {
                    listener.onGameSensor(Sensor.TYPE_ACCELEROMETER, v[0], v[1], v[2]);
                }
                break;
        }
    }

    // API end ====================================

    // Event start ====================================

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float pixelX = event.getX();
        float pixelY = event.getY();
        float blockX = 0, blockY = 0;
        Card card = touchedCard;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                card = touchedCard = findCard(pixelX, pixelY);
                blockX = getBlocksHorizontal(pixelX);
                blockY = getBlocksVertical(pixelY);
                break;
            case MotionEvent.ACTION_MOVE :
                blockX = getBlocksHorizontal(pixelX - touchX);
                blockY = getBlocksVertical(pixelY - touchY);
                break;
            case MotionEvent.ACTION_UP :
                touchedCard = null;
                blockX = getBlocksHorizontal(pixelX);
                blockY = getBlocksVertical(pixelY);
                break;
        }
        if(listener != null) {
            listener.onGameTouchEvent(card, event.getAction(), blockX, blockY);
        }
        touchX = pixelX;
        touchY = pixelY;
        return true;
    }

    // Event end ====================================

    // Audio play start ====================================

    SoundPool soundPool = new SoundPool.Builder().build();
    int soundId = -1;

    public void playAudioBeep(int resid) {
        if(soundId >= 0) {
            soundPool.stop(soundId);
            soundPool = new SoundPool.Builder().build();
        }
        soundId = soundPool.load(this.getContext(), resid,1);
        soundPool.setOnLoadCompleteListener(
                new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int id, int status) {
                        soundPool.play(id, 1, 1, 1, 0, 1f);
                    }
                }
        );
    }

    MediaPlayer mPlayer = null;
    int audioSourceId = -1;
    boolean audioAutoReply = true;

    public void loadBGM(int resid) {
        audioSourceId = resid;
        stopBGM();
    }

    void loadBGM() {
        mPlayer = MediaPlayer.create(this.getContext(), audioSourceId);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if(audioAutoReply) {
                    stopBGM();
                    playBGM();
                }
                if(listener != null)
                    listener.onGameWorkEnded(null, WorkType.AUDIO_PLAY);
            }
        });
    }

    public void playBGM(int resid) {
        loadBGM(resid);
        playBGM();
    }

    public void playBGM() {
        mPlayer.start();
    }

    public void pauseBGM() {
        mPlayer.pause();
    }

    public void stopBGM() {
        deleteBGM();
        loadBGM();
    }

    void deleteBGM() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void audioAutoReplay(boolean autoPlay) {
        audioAutoReply = autoPlay;
    }

    // Audio play end ====================================

    // Interface start ====================================

    private GameEvent listener = null;

    public void listener(GameEvent lsn) { listener = lsn; }

    interface GameEvent {
        void onGameWorkEnded(Card card, WorkType workType);
        void onGameTouchEvent(Card card, int action, float blockX, float blockY);
        void onGameSensor(int sensorType, float x, float y, float z);
    }

    public enum WorkType {
        AUDIO_PLAY, MOVE, RESIZE, ANIMATION, SOURCE_AREA
    }

    // Interface end ====================================

}