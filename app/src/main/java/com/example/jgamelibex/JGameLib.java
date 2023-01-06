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
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class JGameLib extends View {
    boolean firstDraw = true;
    float pixelsW = 480, pixelsH = 800;
    float blocksW = 480, blocksH = 800;
    RectF screenRect;
    float blockSize = pixelsH / blocksH;
    int timerGap = 50;
    boolean needDraw = false;
    ArrayList<Image> images = new ArrayList();
    Image touchedImg = null;
    float touchX = 0;
    float touchY = 0;

    public JGameLib(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    void init(Canvas canvas) {
        pixelsW = canvas.getWidth();
        pixelsH = canvas.getHeight();
        screenRect = getScreenRect();
        blockSize = screenRect.width() / blocksW;
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

        for(Image img : images) {
            if(!img.visible) continue;
            RectF rect = null;
            if(img.dstRect != null) {
                rect = getRect(img);
            }
            drawBitmap(canvas, pnt, img.bmp, rect, img.srcRect);
        }
    }

    void drawBitmap(Canvas canvas, Paint pnt, Bitmap bmp, RectF rectDst, RectF rectSrc) {
        if(bmp == null) return;
        if(rectDst == null)
            rectDst = screenRect;
        if(rectSrc == null) {
            canvas.drawBitmap(bmp, null, rectDst, pnt);
            return;
        }
        float pixelW = bmp.getWidth();
        float pixelH = bmp.getHeight();
        float areaL = rectSrc.left / 100f * pixelW;
        float areaR = rectSrc.right / 100f * pixelW;
        float areaT = rectSrc.top / 100f * pixelH;
        float areaB = rectSrc.bottom / 100f * pixelH;
        Rect area = new Rect((int)areaL, (int)areaT, (int)areaR, (int)areaB);
        canvas.drawBitmap(bmp, area, rectDst, pnt);
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
        RectF rect = new RectF(0,0,0,0);
        if(img.dstRect == null) return rect;
        rect.left = screenRect.left + img.dstRect.left * blockSize;
        rect.right = screenRect.left + img.dstRect.right * blockSize;
        rect.top = screenRect.top + img.dstRect.top * blockSize;
        rect.bottom = screenRect.top + img.dstRect.bottom * blockSize;
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

    int indexOf(Image img) {
        for(int i = images.size()-1; i >= 0; i--) {
            if(images.get(i) == img)
                return i;
        }
        return -1;
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
        RectF srcRect = null;
        float unitSrcL=0, unitSrcT=0;
        float endSrcL, endSrcT;
        boolean visible = true;

        Image(int resid) {
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
                listener.onMoveEnded(this);
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
                listener.onResizeEnded(this);
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
                listener.onAnimationEnded(this);
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
                listener.onSourceAreaEnded(this);
        }

        // Image API start ====================================

        public void sourceArea(double l, double t, double w, double h) {
            RectF rect = new RectF((float)l, (float)t, (float)(l+w), (float)(t+h));
            sourceArea(rect);
        }

        public void sourceArea(RectF rect) {
            srcRect = rect;
            needDraw = true;
        }

        public void sourceArea(double l, double t, double time) {
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

        public void loadBmp() {
            bmp = loadBitmap(resids, idx);
        }

        public void move(double l, double t) {
            float width = this.dstRect.width(), height = this.dstRect.height();
            this.dstRect.left = (float)l;
            this.dstRect.top = (float)t;
            this.dstRect.right = (float)l + width;
            this.dstRect.bottom = (float)t + height;
            needDraw = true;
        }

        public void move(double l, double t, double time) {
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

        public void moveRelative(double gapH, double gapV) {
            move(this.dstRect.left+(float)gapH, this.dstRect.top+(float)gapV);
        }

        public void moveRelative(double gapH, double gapV, double time) {
            move(this.dstRect.left+(float)gapH, this.dstRect.top+(float)gapV, time);
        }

        public void resize(double width, double height) {
            this.dstRect.left = this.dstRect.centerX() - (float)(width / 2.);
            this.dstRect.right = this.dstRect.left + (float)width;
            this.dstRect.top = this.dstRect.centerY() - (float)(height / 2.);
            this.dstRect.bottom = this.dstRect.top + (float)height;
            needDraw = true;
        }

        public void resize(double width, double height, double time) {
            this.endW = (float)width;
            this.endH = (float)height;
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

        public void animation(double time) {
            if(this.resids.isEmpty()) return;
            animation(0, this.resids.size()-1, time);
        }

        public void animation(int start, int end, double time) {
            if(this.resids.isEmpty()) return;
            this.idx = start;
            this.endIdx = end;
            double frames = framesOfTime(time);
            if(frames != 0)
                this.unitIdx = (double)(end - start) / frames;
            else
                this.unitIdx = 0;
            this.loadBmp();
            needDraw = true;
        }

        public void setImageIndex(int idx) {
            animation(idx, idx, 0);
        }

        public void deleteImageResources() {
            //if(img == null) return;
            for(int i = this.resids.size()-1; i >= 0; i--) {
                this.resids.remove(i);
            }
        }

        // Image API end ====================================

    }

    // Inside Class end ====================================

    // API start ====================================

    public void setScreenAxis(float width, float height) {
        blocksW = width;
        blocksH = height;
    }

    public Image addImage(int resid)  {
        Image img = new Image(resid);
        images.add(img);
        needDraw = true;
        return img;
    }

    public Image addImage(int resid, double l, double t, double w, double h)  {
        Image img = addImage(resid);
        img.dstRect = new RectF((float)l, (float)t, (float)(l + w), (float)(t + h));
        return img;
    }

    public double framesOfTime(double time) {
        double miliTime = time * 1000.;
        return miliTime / timerGap;
    }

    public void clearMemory() {
        timer.removeMessages(0);
        deleteBGM();
        for(int i = images.size()-1; i >= 0; i--) {
            Image img = images.get(i);
            img.deleteImageResources();
            //deleteImageResources(img);
            images.remove(i);
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
        void onSourceAreaEnded(Image img);
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