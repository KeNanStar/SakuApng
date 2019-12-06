package com.apng.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.apng.ApngACTLChunk;
import com.apng.ApngFrame;
import com.apng.ApngFrameRender;
import com.apng.ApngReader;
import com.apng.entity.AnimParams;
import com.apng.utils.ApngUtils;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @author xing.hu
 * @since 2016/11/3, 下午3:07
 */
public class ApngImageView extends ImageView implements Animatable {
    public static final String TAG = ApngImageView.class.getSimpleName();
    private static final float DELAY_FACTOR = 1000F;
    public static boolean enableVerboseLog = false;
    public static int HALF_TRANSPARENT = Color.parseColor("#7F000000");

    // start a thread to play the Apng Animation
    private PlayThread mPlayThread;
    private AnimParams mAnimParams;

    private float mScale;

    private ApngFrameRender mFrameRender;


    private ApngFrame curFrame;

    private Bitmap curFrameBmp;

    private boolean mIsRunning = false;


    private ApngHandler mApngHandler;


    private static class ApngHandler extends Handler {
        private WeakReference<ApngImageView> mRef;


        public ApngHandler(ApngImageView apngImageView) {
            mRef = new WeakReference<>(apngImageView);

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }


    private volatile AnimationListener mListener;

    @Override
    public void start() {
        mIsRunning = true;
        mPlayThread = new PlayThread();
        mPlayThread.start();
    }

    @Override
    public void stop() {
        if (mPlayThread != null) {
            mPlayThread.interrupt();
            mPlayThread = null;
        }
        //clearCanvas();
        //notifyPlayCompeleted();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public interface AnimationListener {
        /**
         * call back when the anim plays complete
         */
        void onAnimationCompleted();
    }

    public void setAnimationListener(AnimationListener mListener) {
        this.mListener = mListener;
    }

    public ApngImageView(Context context) {
        super(context);
        init(context);

    }

    public ApngImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);


    }

    public ApngImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init(context);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mIsRunning
            && mFrameRender != null
             && curFrame != null
               && curFrameBmp != null){
            drawFrame(canvas);
        }



    }

    private void init(Context context) {

        mApngHandler = new ApngHandler(this);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        enableVerboseLog = true;


    }


    /**
     * set tha Apng Item to the queue
     */
    public void setApngForPlay(AnimParams animItem) {
        mAnimParams = animItem;
    }


    @Override
    protected void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow()");
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mPlayThread != null) {
            mPlayThread.interrupt();
            mPlayThread = null;
        }
    }

    private class PlayThread extends Thread {
        private static final int MAX_ZERO_NUM = 3;

        public PlayThread() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
        }

        @Override
        public void run() {
            Log.d(TAG, "PlayThread run()");
            mFrameRender = new ApngFrameRender();
            try {
                while (!isInterrupted()) {
                    try {
                        //  play it
                        playAnimation(mAnimParams);


                    } catch (InterruptedException e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                        break; // waiting in queue has been interrupted, finish play thread
                    }
                }
            } finally {
                mFrameRender.recycle();
            }
        }

        /**
         * play the apng animation
         */
        private void playAnimation(AnimParams animItem) throws InterruptedException {
            try {
                // step 1: prepare
                ApngReader reader = new ApngReader(animItem.imagePath);
                ApngACTLChunk actl = reader.getACTL();
                if (animItem.isHasBackground) setBgColor(true);
                // all loop count = apng_internal_loop_count x apng_play_times
                // if apng_internal_loop_count == 0 then set it to 1 (not support loop indefinitely)
                int loopCount = animItem.loopCount * (actl.getNumPlays() == 0 ? 1 : actl.getNumPlays());

                // step 2: draw frames
                boolean isLoop = loopCount == AnimParams.PLAY_4_LOOP;

                for (int lc = 0; lc < loopCount || isLoop; lc++) {
                    // reallocated to head again if loops more the one time
                    if (lc > 0 || isLoop) reader.reset();
                    for (int i = 0; i < actl.getNumFrames(); i++) {
                        long start = System.currentTimeMillis();
                        // get frame data
                        curFrame = reader.nextFrame();
                        if (curFrame == null) break; // if read next frame failed, break loop

                        byte[] data = readStream(curFrame.getImageStream());

                        if (data != null) {
                            //Bitmap frameBmp = BitmapFactory.decodeStream(frame.getImageStream());

                            curFrameBmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                            Log.d(TAG, "read the " + i + " frame:" + (System.currentTimeMillis() - start) + "ms");

                            // init the render and calculate scale rate
                            // at first time get the frame width and height
                            if (lc == 0 && i == 0) {
                                int imgW = curFrame.getWidth(), imgH = curFrame.getHeight();
                                mScale = calculateScale(animItem.scaleType, imgW, imgH, getWidth(), getHeight());
                                mFrameRender.prepare(imgW, imgH);
                            }


                            // draw frame
                            if (!isInterrupted()) {
                                index++;
                                ApngImageView.this.postInvalidate();
                            }

                            // delay
                            int waitMillis = Math.round(curFrame.getDelayNum() * DELAY_FACTOR / curFrame.getDelayDen())
                                    - (int) (System.currentTimeMillis() - start);
                            sleep(waitMillis > 0 ? waitMillis : 0);
                        }

                    }
                }

            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            } finally {
                if (animItem.isHasBackground) setBgColor(false);
            }
        }

        private void setBgColor(final boolean show) {
            ApngImageView.this.post(new Runnable() {
                @Override
                public void run() {
                    if (show)
                        ApngImageView.this.setBackgroundColor(HALF_TRANSPARENT);
                    else
                        ApngImageView.this.setBackgroundColor(Color.TRANSPARENT);
                }
            });
        }

        private void notifyPlayCompeleted() {
            if (mListener == null) return;
            ApngImageView.this.post(new Runnable() {
                @Override
                public void run() {
                    if (mListener != null)
                        mListener.onAnimationCompleted();
                }
            });
        }

        /*
         * get image byte stream
         * */
        private byte[] readStream(InputStream inStream) throws Exception {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;

            //fix bug: the end of inputStream while return 0 if the type of phone is Meizu MEIZU E3
            int numZero = 0;

            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
                if (len == 0) {
                    numZero++;
                    if (numZero >= MAX_ZERO_NUM) {
                        break;
                    }
                }
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        }

        public class PatchInputStream extends FilterInputStream {

            protected PatchInputStream(InputStream in) {
                super(in);
                // TODO Auto-generated constructor stub
            }

            public long skip(long n) throws IOException {
                long m = 0l;
                while (m < n) {
                    long _m = in.skip(n - m);
                    if (_m == 0l) {
                        break;
                    }
                    m += _m;
                }
                return m;
            }

        }

        /**
         * calculate the ratio of image width to canvas
         */
        private float calculateScale(int scaleType, int imgW, int imgH, int viewW, int viewH) {
            if (scaleType == AnimParams.WIDTH_SCALE_TYPE) {
                return ((float) viewW) / imgW;
            } else if (scaleType == AnimParams.HEIGHT_SCALE_TYPE) {
                return ((float) viewH) / imgH;
            } else if (scaleType == AnimParams.WIDTH_OR_HEIGHT_SCALE_TYPE) {
                float scalingByWidth = ((float) viewW) / imgW;
                float scalingByHeight = ((float) viewH) / imgH;
                return scalingByWidth <= scalingByHeight ? scalingByWidth : scalingByHeight;
            }
            return 1F;
        }

        int index = 0;


    }


    /**
     * draw the appointed frame
     */
    private void drawFrame(Canvas canvas) {
        //start to draw the frame
        try {
            Matrix matrix = new Matrix();
            matrix.setScale(mScale, mScale);
            Bitmap bmp = mFrameRender.render(curFrame, curFrameBmp);

            //saveBitmap(bmp, index);


            //anti-aliasing
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            float[] tranLeftAndTop = ApngUtils.getTranLeftAndTop(canvas, bmp, mAnimParams.align, mScale, mAnimParams.percent);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            matrix.postTranslate(tranLeftAndTop[0], tranLeftAndTop[1]);
            canvas.drawBitmap(bmp, matrix, null);
            curFrameBmp.recycle();
        } catch (Exception e) {
            Log.e(TAG, "draw error msg:" + Log.getStackTraceString(e));
        }
    }


    private void clearCanvas(Canvas canvas) {
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } catch (Exception e) {
            Log.e(TAG, "draw error msg:" + Log.getStackTraceString(e));
        }
    }

}
