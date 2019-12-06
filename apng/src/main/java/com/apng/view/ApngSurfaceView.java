package com.apng.view;

import android.content.*;
import android.graphics.*;
import android.os.Process;
import android.util.*;
import android.view.*;

import com.apng.ApngACTLChunk;
import com.apng.ApngFrame;
import com.apng.ApngFrameRender;
import com.apng.ApngReader;
import com.apng.entity.AnimParams;
import com.apng.utils.ApngUtils;


import java.io.*;
import java.util.concurrent.*;


/**
 * @author xing.hu
 * @since 2016/11/3, 下午3:07
 */
public class ApngSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = ApngSurfaceView.class.getSimpleName();
    private static final float DELAY_FACTOR = 1000F;
    public static boolean enableVerboseLog = false;
    public static int HALF_TRANSPARENT = Color.parseColor("#7F000000");

    // start a thread to play the Apng Animation
    private PlayThread mPlayThread;
    private final LinkedBlockingQueue<AnimParams> queue = new LinkedBlockingQueue<>();

    private volatile AnimationListener mListener;

    public interface AnimationListener {
        /**
         * call back when the anim plays complete
         */
        void onAnimationCompleted();
    }

    public void setAnimationListener(AnimationListener mListener) {
        this.mListener = mListener;
    }

    public ApngSurfaceView(Context context) {
        super(context);
        init(context);

    }

    public ApngSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);


    }

    public ApngSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init(context);

    }




    private void init(Context context) {
        setZOrderOnTop(true);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        enableVerboseLog = true;



    }

    /**
     * add tha Apng Item to the queue
     */
    public void addApngForPlay(AnimParams giftAnimItem) {
        queue.add(giftAnimItem);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mPlayThread.setSurfaceEnabled(false);
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        mPlayThread.setSurfaceEnabled(true);

    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    protected void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow()");
        super.onAttachedToWindow();
        mPlayThread = new PlayThread();
        mPlayThread.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayThread.interrupt();
        mPlayThread = null;
    }

    private class PlayThread extends Thread {
        private volatile boolean surfaceEnabled;
        private ApngFrameRender mFrameRender;
        private float mScale;
        private  static final int MAX_ZERO_NUM = 3;

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
                        // step1: fetch an animation object
                        AnimParams animItem = queue.take();

                        if(animItem.name.equals("fire")){
                            //mediaPlayer.start();
                        }

                        // step2: play it
                        playAnimation(animItem);


                        // clear canvas when played last apng
                        if (queue.isEmpty()) {
                            clearCanvas();
                            notifyPlayCompeleted();
                            //mediaPlayer.stop();
                        }
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
                    boolean isLoop = loopCount == AnimParams.PLAY_4_LOOP ;

                    for (int lc = 0; lc < loopCount || isLoop; lc++) {
                        // reallocated to head again if loops more the one time
                        if (lc > 0 || isLoop) reader.reset();
                        for (int i = 0; i < actl.getNumFrames(); i++) {
                            long start = System.currentTimeMillis();
                            // get frame data
                            ApngFrame frame = reader.nextFrame();
                            if (frame == null) break; // if read next frame failed, break loop

                            byte[] data = readStream(frame.getImageStream());

                            if(data!=null){
                                //Bitmap frameBmp = BitmapFactory.decodeStream(frame.getImageStream());

                                Bitmap frameBmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                                Log.d(TAG, "read the " + i + " frame:" + (System.currentTimeMillis() - start) + "ms");

                                // init the render and calculate scale rate
                                // at first time get the frame width and height
                                if (lc == 0 && i == 0) {
                                    int imgW = frame.getWidth(), imgH = frame.getHeight();
                                    mScale = calculateScale(animItem.scaleType, imgW, imgH, getWidth(), getHeight());
                                    mFrameRender.prepare(imgW, imgH);
                                }

                                // draw frame
                                drawFrame(animItem, frame, frameBmp);
                                frameBmp.recycle();

                                // delay
                                int waitMillis = Math.round(frame.getDelayNum() * DELAY_FACTOR / frame.getDelayDen())
                                        - (int) (System.currentTimeMillis() - start);
                                sleep(waitMillis > 0 ? waitMillis : 0);
                            }

                        }
                    }

            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            finally {
                if (animItem.isHasBackground) setBgColor(false);
            }
        }

        private void setBgColor(final boolean show) {
            ApngSurfaceView.this.post(new Runnable() {
                @Override
                public void run() {
                    if (show)
                        ApngSurfaceView.this.setBackgroundColor(HALF_TRANSPARENT);
                    else
                        ApngSurfaceView.this.setBackgroundColor(Color.TRANSPARENT);
                }
            });
        }

        private void notifyPlayCompeleted() {
            if (mListener == null) return;
            ApngSurfaceView.this.post(new Runnable() {
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
                if(len == 0 ) {
                    numZero++;
                    if(numZero >=  MAX_ZERO_NUM){
                        break;
                    }
                }
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        }

        public class PatchInputStream extends FilterInputStream{

            protected PatchInputStream(InputStream in) {
                super(in);
                // TODO Auto-generated constructor stub
            }

            public long skip(long n)throws IOException{
                long m=0l;
                while(m<n){
                    long _m=in.skip(n-m);
                    if(_m==0l){
                        break;
                    }
                    m+=_m;
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
        /**
         * draw the appointed frame
         */
        private void drawFrame(AnimParams animItem, ApngFrame frame, Bitmap frameBmp) {
            if (surfaceEnabled && !isInterrupted()) {
                //start to draw the frame
                try {
                    Matrix matrix = new Matrix();
                    matrix.setScale(mScale, mScale);
                    Bitmap bmp = mFrameRender.render(frame, frameBmp);

                    //saveBitmap(bmp, index);
                    index ++;

                    Canvas canvas = getHolder().lockCanvas();
                    //anti-aliasing
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    float[] tranLeftAndTop = ApngUtils.getTranLeftAndTop(canvas, bmp, animItem.align, mScale, animItem.percent);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                    matrix.postTranslate(tranLeftAndTop[0], tranLeftAndTop[1]);
                    canvas.drawBitmap(bmp, matrix, null);
                    getHolder().unlockCanvasAndPost(canvas); //  unlock the canvas
                } catch (Exception e) {
                    Log.e(TAG, "draw error msg:" + Log.getStackTraceString(e));
                }
            }
        }


        private void clearCanvas() {
            if (surfaceEnabled && !isInterrupted()) {
                try {
                    Canvas canvas = getHolder().lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    getHolder().unlockCanvasAndPost(canvas);//  unlock the canvas
                }  catch (Exception e) {
                    Log.e(TAG, "draw error msg:" + Log.getStackTraceString(e));
                }
            }
        }

        public void setSurfaceEnabled(boolean surfaceEnabled) {
            this.surfaceEnabled = surfaceEnabled;
        }
    }

}
