/* JGameLib_Java : 2D Game library for education      */
/* Author : Dennis (Donggeun Jung)                    */
/* Contact : topsan72@gmail.com                       */
package com.example.jgamelibex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class JGameLib extends View {
    boolean firstDraw = true;
    float scrWidth = 480, scrHeight = 800;
    int timerGap = 50;
    boolean needDraw = false;
    HashSet<Image> images = new HashSet();
    Image touchedImg = null;
    float touchX = 0;
    float touchY = 0;

    public JGameLib(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(Canvas canvas) {
        scrWidth = canvas.getWidth();
        scrHeight = canvas.getHeight();
        timer.sendEmptyMessageDelayed(0, timerGap);
    }

    public void redraw() {
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

        for(Image img : images) {
            RectF rect = getRect(img);
            canvas.drawBitmap(img.bmp, null, rect, pnt);
        }
    }

    Handler timer = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            if(needDraw) {
                needDraw = false;
                for(Image img : images) {
                    img.next();
                }
                redraw();
            }
            timer.sendEmptyMessageDelayed(0, timerGap);
            return false;
        }
    });

    private RectF getRect(Image img) {
        RectF rect = new RectF();
        rect.left = img.left / 100f * scrWidth;
        rect.top = img.top / 100f * scrHeight;
        float width = img.width / 100f * scrWidth;
        rect.right = rect.left + width;
        float height = width * img.ratio;
        rect.bottom = rect.top + height;
        return rect;
    }

    private float getRateHorizontal(float pixelH) {
        return pixelH * 100f / scrWidth;
    }

    private float getRateVertical(float pixelV) {
        return pixelV * 100f / scrHeight;
    }

    Bitmap getBitmap(int resid) {
        return BitmapFactory.decodeResource(getResources(), resid);
    }

    Image findImage(float x, float y) {
        for(Image img : images) {
            RectF rect = getRect(img);
            if(rect.contains(x, y)) {
                return img;
            }
        }
        return null;
    }

    // Inside Class start ====================================

    class Image {
        Image(int resid, double w, double r, double l, double t) {
            idx = 0;
            width = (float)w; ratio = (float)r;
            left = (float)l; top = (float)t;
            resources.add(resid);
            loadBitmap();
        }

        ArrayList<Integer> resources = new ArrayList();
        double idx = 0;
        double unitIdx = 0, endIdx = 0;
        float width, ratio;
        float unitW=0, unitR=0;
        float endW, endR;
        float left, top;
        float unitL=0, unitT=0;
        float endL, endT;
        boolean visible = true;
        Bitmap bmp;

        public void addResource(int resid) {
            resources.add(resid);
        }

        public void removeResource(int idx) {
            if(idx >= resources.size()) return;
            resources.remove(idx);
        }

        public void visible(boolean s) {
            visible = s;
            needDraw = true;
        }

        public void next() {
            nextResize();
            nextMove();
            nextAnimation();
        }

        public void nextResize() {
            if(unitW == 0 && unitR == 0) return;
            width += unitW;
            ratio += unitR;
            if((unitW != 0 && Math.min(width,width-unitW) <= endW && endW <= Math.max(width,width-unitW))
                    || (unitR != 0 && Math.min(ratio,ratio-unitR) <= endR && endR <= Math.max(ratio,ratio-unitR))) {
                unitW = 0;
                unitR = 0;
                width = endW;
                ratio = endR;
                if(listener != null) listener.onResizeEnded(this);
            }
            needDraw = true;
        }

        public void nextMove() {
            if(unitL == 0 && unitT == 0) return;
            left += unitL;
            top += unitT;
            if((unitL != 0 && Math.min(left,left-unitL) <= endL && endL <= Math.max(left,left-unitL))
                    || (unitT != 0 && Math.min(top,top-unitT) <= endT && endT <= Math.max(top,top-unitT))) {
                unitL = 0;
                unitT = 0;
                left = endL;
                top = endT;
                if(listener != null) listener.onMoveEnded(this);
            }
            needDraw = true;
        }

        public void nextAnimation() {
            if(unitIdx == 0) return;
            double nextIdx = idx + unitIdx;
            if(nextIdx > endIdx || nextIdx >= resources.size()) {
                unitIdx = 0;
                nextIdx = Math.min(endIdx, resources.size()-1);
                if(listener != null) listener.onAnimationEnded(this);
            }
            if((int)nextIdx > (int)idx) {
                loadBitmap();
            }
            idx = nextIdx;
            needDraw = true;
        }

        public void loadBitmap() {
            if(resources.isEmpty() || idx >= resources.size()) return;
            int resid = resources.get((int)idx);
            bmp = getBitmap(resid);
        }
    }

    // Inside Class end ====================================

    // API start ====================================

    public Image addImage(int resid, double w, double r, double l, double t)  {
        Image img = new Image(resid, (float)w, (float)r, (float)l, (float)t);
        images.add(img);
        needDraw = true;
        return img;
    }

    public void move(Image img, double l, double t) {
        img.left = (float)l;
        img.top = (float)t;
        needDraw = true;
    }

    public void move(Image img, double l, double t, double time) {
        img.endL = (float)l;
        img.endT = (float)t;
        float frames = (float)framesOfTime(time);
        img.unitL = (img.endL - img.left) / frames;
        img.unitT = (img.endT - img.top) / frames;
        needDraw = true;
    }

    public void resize(Image img, double w, double r) {
        img.width = (float)w;
        img.ratio = (float)r;
        needDraw = true;
    }

    public void resize(Image img, double w, double r, double time) {
        img.endW = (float)w;
        img.endR = (float)r;
        float frames = (float)framesOfTime(time);
        img.unitW = (img.endW - img.width) / frames;
        img.unitR = (img.endR - img.ratio) / frames;

        float centerH = img.left + (img.width / 2f);
        float endL = centerH - (img.endW / 2f);
        float centerV = img.top + (img.width * img.ratio / 2f);
        float endT = centerV - (img.endW * img.endR / 2f);
        move(img, endL, endT, time);
    }

    public void addResource(Image img, int resid) {
        img.addResource(resid);
    }

    public void removeResource(Image img, int idx) {
        img.removeResource(idx);
    }

    public void animation(Image img, double time) {
        if(img.resources.isEmpty()) return;
        animation(img, 0, img.resources.size()-1, time);
    }

    public void animation(Image img, int start, int end, double time) {
        if(img.resources.isEmpty()) return;
        img.idx = start;
        img.endIdx = end;
        double frames = framesOfTime(time);
        img.unitIdx = (double)(end - start) / frames;
        img.loadBitmap();
        needDraw = true;
    }

    public void setImageIndex(Image img, int idx) {
        animation(img, idx, idx, 0);
    }

    public double framesOfTime(double time) {
        double miliTime = time * 1000.;
        return miliTime / timerGap;
    }

    public void deleteImageResources(Image img) {
        if(images.contains(img)) {
            for(int i = img.resources.size()-1; i >= 0; i--) {
                img.resources.remove(i);
            }
            images.remove(img);
        }
    }

    public void clearMemory() {
        deleteBGM();
        Iterator itrt = images.iterator();
        while(itrt.hasNext()) {
            Image img = (Image)itrt.next();
            deleteImageResources(img);
            images.remove(img);
        }
    }

    // API end ====================================

    // Interface start ====================================

    private GameEvent listener = null;

    public void listener(GameEvent lsn) { listener = lsn; }

    interface GameEvent {
        void onMoveEnded(Image img);
        void onResizeEnded(Image img);
        void onAnimationEnded(Image img);
        void onGameTouchEvent(Image img, int action, float rateH, float rateV);
        void onAudioCompletion(int resid);
    }

    // Interface end ====================================

    // Event start ====================================

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x1 = event.getX();
        float y1 = event.getY();
        float rateH = 0, rateV = 0;
        Image img = touchedImg;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                img = touchedImg = findImage(x1, y1);
                break;
            case MotionEvent.ACTION_MOVE :
                rateH = getRateHorizontal(x1 - touchX);
                rateV = getRateVertical(y1 - touchY);
                break;
            case MotionEvent.ACTION_UP :
                touchedImg = null;
                break;
        }
        if(listener != null) {
            listener.onGameTouchEvent(img, event.getAction(), rateH, rateV);
        }
        touchX = x1;
        touchY = y1;
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
                    listener.onAudioCompletion(audioSourceId);
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

}