/* JGameLib_Java : 2D Game library for education      */
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
    float pixelsW = 480, pixelsH = 800;
    float blocksW = 480, blocksH = 800;
    RectF scrRect;
    float blockSize = pixelsH / blocksH;
    int timerGap = 50;
    boolean needDraw = false;
    int backgroundResId = -1;
    Bitmap backgroundBmp = null;
    HashSet<Image> images = new HashSet();
    Image touchedImg = null;
    float touchX = 0;
    float touchY = 0;

    public JGameLib(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    void init(Canvas canvas) {
        pixelsW = canvas.getWidth();
        pixelsH = canvas.getHeight();
        scrRect = getScreenRect();
        blockSize = scrRect.width() / blocksW;
        timer.sendEmptyMessageDelayed(0, timerGap);
    }

    RectF getScreenRect() {
        float pixelsRatio = pixelsW / pixelsH;
        float blocksRatio = blocksW / blocksH;
        RectF rect = new RectF();
        if(pixelsRatio > blocksRatio) {
            rect.top = 0;
            rect.bottom = pixelsH;
            float screenW = pixelsH * blocksRatio;
            rect.left = (pixelsW - screenW) / 2.f;
            rect.right = rect.left + screenW;
        } else {
            rect.left = 0;
            rect.right = pixelsW;
            float screenH = pixelsW / blocksRatio;
            rect.top = (pixelsH - screenH) / 2.f;
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

        drawBitmap(canvas, pnt, backgroundBmp);
        for(Image img : images) {
            if(!img.visible) continue;
            RectF rect = getRect(img);
            drawBitmap(canvas, pnt, img.bmp, rect);
        }
    }
    void drawBitmap(Canvas canvas, Paint pnt, Bitmap bmp) {
        drawBitmap(canvas, pnt, bmp, scrRect);
    }

    void drawBitmap(Canvas canvas, Paint pnt, Bitmap bmp, RectF rectDst) {
        if(bmp == null) return;
        Rect rectSrc = new Rect(0,0, bmp.getWidth(), bmp.getHeight());
        drawBitmap(canvas, pnt, bmp, rectDst, rectSrc);
    }

    void drawBitmap(Canvas canvas, Paint pnt, Bitmap bmp, RectF rectDst, Rect rectSrc) {
        if(bmp == null) return;
        canvas.drawBitmap(bmp, rectSrc, rectDst, pnt);
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
        rect.left = scrRect.left + img.dstRect.left * blockSize;
        rect.right = scrRect.left + img.dstRect.right * blockSize;
        rect.top = scrRect.top + img.dstRect.top * blockSize;
        rect.bottom = scrRect.top + img.dstRect.bottom * blockSize;
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

    Image findImage(float pixelX, float pixelY) {
        for(Image img : images) {
            if(!img.visible) continue;
            RectF rect = getRect(img);
            if(rect.contains(pixelX, pixelY)) {
                return img;
            }
        }
        return null;
    }

    Bitmap loadBitmap(ArrayList<Integer> resids, double idx) {
        if(resids.isEmpty() || (int)idx >= resids.size()) return null;
        int resid = resids.get((int)idx);
        return getBitmap(resid);
    }

    // Inside Class start ====================================

    class Image {
        ArrayList<Integer> resids = new ArrayList();
        Bitmap bmp;
        double idx = 0;
        double unitIdx = 0, endIdx = 0;
        RectF dstRect = null;
        float unitL=0, unitT=0;
        float endL, endT;
        float unitW=0, unitH=0;
        float endW, endH;
        Rect srcRect = null;
        boolean visible = true;

        Image(int resid) {
            this(resid, 0, 0, blocksW, blocksH);
        }

        Image(int resid, double l, double t, double w, double h) {
            idx = 0;
            dstRect = new RectF((float)l, (float)t, (float)(l + w), (float)(t + h));
            resids.add(resid);
            loadBmp();
        }

        public void srcRect(Rect rect) {
            srcRect = rect;
        }

        public void addResource(int resid) {
            resids.add(resid);
        }

        public void removeResource(int idx) {
            if(idx >= resids.size()) return;
            resids.remove(idx);
        }

        public void visible(boolean s) {
            visible = s;
            needDraw = true;
        }

        public void next() {
            nextMove();
            nextResize();
            nextAnimation();
        }

        public void nextMove() {
            if(unitL == 0 && unitT == 0) return;
            float currL = dstRect.left, currT = dstRect.top;
            float nextL = currL + unitL, nextT = currT + unitT;

            if((unitL != 0 && Math.min(currL,nextL) <= endL && endL <= Math.max(currL,nextL))
                    || (unitT != 0 && Math.min(currT,nextT) <= endT && endT <= Math.max(currT,nextT))) {
                unitL = unitT = 0;
                nextL = endL;
                nextT = endT;
                if(listener != null) listener.onMoveEnded(this);
            }
            move(this, nextL, nextT);
        }

        public void nextResize() {
            if(unitW == 0 && unitH == 0) return;
            float currW = dstRect.width(), currH = dstRect.height();
            float nextW = currW + unitW, nextH = currH + unitH;
            if((unitW != 0 && Math.min(currW,nextW) <= endW && endW <= Math.max(currW,nextW))
                    || (unitH != 0 && Math.min(currW,nextW) <= endH && endH <= Math.max(currW,nextW))) {
                unitW = unitH = 0;
                nextW = endW;
                nextH = endH;
                if(listener != null) listener.onResizeEnded(this);
            }
            resize(this, nextW, nextH);
        }

        public void nextAnimation() {
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
                listener.onAnimationEnded(this);
            needDraw = true;
        }

        public void loadBmp() {
            bmp = loadBitmap(resids, idx);
        }
    }

    // Inside Class end ====================================

    // API start ====================================

    public void setBackground(int resid) {
        backgroundResId = resid;
        backgroundBmp = getBitmap(resid);
        needDraw = true;
    }

    public void setScreenAxis(float width, float height) {
        blocksW = width;
        blocksH = height;
    }

    public Image addImage(int resid, double l, double t, double w, double r)  {
        Image img = new Image(resid, (float)l, (float)t, (float)w, (float)r);
        images.add(img);
        needDraw = true;
        return img;
    }

    public void move(Image img, double l, double t) {
        float width = img.dstRect.width(), height = img.dstRect.height();
        img.dstRect.left = (float)l;
        img.dstRect.top = (float)t;
        img.dstRect.right = (float)l + width;
        img.dstRect.bottom = (float)t + height;
        needDraw = true;
    }

    public void move(Image img, double l, double t, double time) {
        img.endL = (float)l;
        img.endT = (float)t;
        float frames = (float)framesOfTime(time);
        if(frames != 0) {
            img.unitL = (img.endL - img.dstRect.left) / frames;
            img.unitT = (img.endT - img.dstRect.top) / frames;
        } else {
            img.unitL = 0;
            img.unitT = 0;
        }
        needDraw = true;
    }

    public void moveRelative(Image img, double gapH, double gapV) {
        move(img, img.dstRect.left+(float)gapH, img.dstRect.top+(float)gapV);
    }

    public void moveRelative(Image img, double gapH, double gapV, double time) {
        move(img, img.dstRect.left+(float)gapH, img.dstRect.top+(float)gapV, time);
    }

    public void resize(Image img, double width, double height) {
        img.dstRect.left = img.dstRect.centerX() - (float)(width / 2.);
        img.dstRect.right = img.dstRect.left + (float)width;
        img.dstRect.top = img.dstRect.centerY() - (float)(height / 2.);
        img.dstRect.bottom = img.dstRect.top + (float)height;
        needDraw = true;
    }

    public void resize(Image img, double width, double height, double time) {
        img.endW = (float)width;
        img.endH = (float)height;
        float frames = (float)framesOfTime(time);
        if(frames != 0) {
            img.unitW = (img.endW - img.dstRect.width()) / frames;
            img.unitH = (img.endH - img.dstRect.height()) / frames;
        } else {
            img.unitW = 0;
            img.unitH = 0;
        }
        needDraw = true;
    }

    public void addResource(Image img, int resid) {
        img.addResource(resid);
    }

    public void removeResource(Image img, int idx) {
        img.removeResource(idx);
    }

    public void animation(Image img, double time) {
        if(img.resids.isEmpty()) return;
        animation(img, 0, img.resids.size()-1, time);
    }

    public void animation(Image img, int start, int end, double time) {
        if(img.resids.isEmpty()) return;
        img.idx = start;
        img.endIdx = end;
        double frames = framesOfTime(time);
        if(frames != 0)
            img.unitIdx = (double)(end - start) / frames;
        else
            img.unitIdx = 0;
        img.loadBmp();
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
            for(int i = img.resids.size()-1; i >= 0; i--) {
                img.resids.remove(i);
            }
        }
    }

    public void clearMemory() {
        timer.removeMessages(0);
        deleteBGM();
        Iterator itr = images.iterator();
        while(itr.hasNext()) {
            Image img = (Image)itr.next();
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
        void onGameTouchEvent(Image img, int action, float blockX, float blockY);
        void onAudioCompletion(int resid);
    }

    // Interface end ====================================

    // Event start ====================================

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float pixelX = event.getX();
        float pixelY = event.getY();
        float blockX = 0, blockY = 0;
        Image img = touchedImg;

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                img = touchedImg = findImage(pixelX, pixelY);
                blockX = getBlocksHorizontal(pixelX);
                blockY = getBlocksVertical(pixelY);
                break;
            case MotionEvent.ACTION_MOVE :
                blockX = getBlocksHorizontal(pixelX - touchX);
                blockY = getBlocksVertical(pixelY - touchY);
                break;
            case MotionEvent.ACTION_UP :
                touchedImg = null;
                blockX = getBlocksHorizontal(pixelX);
                blockY = getBlocksVertical(pixelY);
                break;
        }
        if(listener != null) {
            listener.onGameTouchEvent(img, event.getAction(), blockX, blockY);
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