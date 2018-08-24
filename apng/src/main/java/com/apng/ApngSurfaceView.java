package com.apng;

import android.content.*;
import android.graphics.*;
import android.media.*;
import android.os.Process;
import android.util.*;
import android.view.*;

import com.apng.entity.AnimParams;
import com.apng.utils.ApngUtils;


import java.io.*;
import java.util.concurrent.*;


/**
 * @author xing.hu@renren-inc.com
 * @since 2016/11/3, 下午3:07
 */
public class ApngSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    public static final String TAG = "ApngSurfaceView";
    private static final float DELAY_FACTOR = 1000F;
    public static boolean enableVerboseLog = false;
    public static int HALF_TRANSPARENT = Color.parseColor("#7F000000");

    // SurfaceView通常需要自己单独的线程来播放动画
    private PlayThread mPlayThread;
    private final LinkedBlockingQueue<AnimParams> queue = new LinkedBlockingQueue<>();

    private volatile AnimationListener mListener;

    public interface AnimationListener {
        /**
         * 动画结束的回调
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
     * 添加要播放Apng动画到队列，会依次播放（本方法线程安全）
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
        // 这里是SurfaceView发生变化的时候触发的部分
    }

    @Override
    protected void onAttachedToWindow() {
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

        public PlayThread() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);
        }

        @Override
        public void run() {
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
                try {
                    if (animItem.isHasBackground) setBgColor(true);
                    // all loop count = apng_internal_loop_count x apng_play_times
                    // if apng_internal_loop_count == 0 then set it to 1 (not support loop indefinitely)
                    int loopCount = animItem.loopCount * (actl.getNumPlays() == 0 ? 1 : actl.getNumPlays());

                    // step 2: draw frames
                    for (int lc = 0; lc < loopCount; lc++) {
                        // reallocated to head again if loops more the one time
                        if (lc > 0) reader.reset();
                        for (int i = 0; i < actl.getNumFrames(); i++) {
                            long start = System.currentTimeMillis();
                            // get frame data
                            ApngFrame frame = reader.nextFrame();
                            if (frame == null) break; // if read next frame failed, break loop
                            Bitmap frameBmp = BitmapFactory.decodeStream(frame.getImageStream());
                            //saveBitmap(frameBmp, i);
                            Log.d(TAG, "读取第" + i + "帧所用的时间:" + (System.currentTimeMillis() - start) + "ms");

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
                } finally {
                    if (animItem.isHasBackground) setBgColor(false);
                }
            } catch (IOException e) {
            } catch (FormatNotSupportException e) {
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

        /**
         * 计算图片宽度跟画布宽度的比例(等宽缩放)
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
         * 绘制指定帧
         */
        private void drawFrame(AnimParams animItem, ApngFrame frame, Bitmap frameBmp) {
            if (surfaceEnabled && !isInterrupted()) {
                //开始绘制每一帧
                try {
                    Matrix matrix = new Matrix();
                    matrix.setScale(mScale, mScale);
                    Bitmap bmp = mFrameRender.render(frame, frameBmp);

                    //saveBitmap(bmp, index);
                    index ++;

                    Canvas canvas = getHolder().lockCanvas();
                    //消除锯齿
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    float[] tranLeftAndTop = ApngUtils.getTranLeftAndTop(canvas, bmp, animItem.align, mScale, animItem.percent);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                    matrix.postTranslate(tranLeftAndTop[0], tranLeftAndTop[1]);
                    canvas.drawBitmap(bmp, matrix, null);
                    getHolder().unlockCanvasAndPost(canvas); // 释放锁并提交画布进行重绘
                } catch (Exception e) {
                    Log.d(TAG, "绘制出错");
                }
            }
        }


        private void clearCanvas() {
            if (surfaceEnabled && !isInterrupted()) {
                try {
                    Canvas canvas = getHolder().lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    getHolder().unlockCanvasAndPost(canvas);//解锁画布，提交画好的图像
                } catch (Exception e) {
                    Log.d(TAG, "绘制出错");
                }
            }
        }

        public void setSurfaceEnabled(boolean surfaceEnabled) {
            this.surfaceEnabled = surfaceEnabled;
        }
    }


    /** 保存方法 */
    public void saveBitmap(Bitmap bm, int index) {
        Log.e(TAG, "保存图片");
        File f = new File("/sdcard/gen/", index + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
