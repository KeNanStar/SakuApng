package com.apng;

import android.graphics.*;

import static com.apng.ApngFCTLChunk.*;

/**
 * 帧图像合成器
 *
 * @author ltf
 * @since 16/12/2, 上午9:10
 */
public class ApngFrameRender {
    private Rect mFullRect = new Rect();

    private Bitmap mRenderFrame;
    private Canvas mRenderCanvas;

    private Bitmap mDisposedFrame;
    private Canvas mDisposeCanvas;
    private Rect mDisposeRect = new Rect();
    private byte mLastDisposeOp = APNG_DISPOSE_OP_NONE;

    /**
     * 渲染当前帧画面
     *
     * @param frame apng中当前帧
     * @return 渲染合成后的当前帧图像
     */
    public Bitmap render(ApngFrame frame, Bitmap frameBmp) {
        // 执行消除操作
        dispose(frame);
        // 合成当前帧
        blend(frame, frameBmp);
        return mRenderFrame;
    }

    /**
     * 首次使用或改变宽高时，要先调用本方法进行初始化
     */
    public void prepare(int width, int height) {
        if (mRenderFrame == null || mFullRect.width() != width || mFullRect.height() != height) {
            // recycle previous allocated resources
            recycle();
            // create new size cache
            mRenderFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mDisposedFrame = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mFullRect.set(0, 0, width, height);
            if (mRenderCanvas == null) {
                mRenderCanvas = new Canvas(mRenderFrame);
                mDisposeCanvas = new Canvas(mDisposedFrame);
            } else {
                mRenderCanvas.setBitmap(mRenderFrame);
                mDisposeCanvas.setBitmap(mDisposedFrame);
            }
        }
        mDisposeRect.set(0, 0, width, height);
        mLastDisposeOp = APNG_DISPOSE_OP_BACKGROUND;
    }

    /**
     * 不再使用时，回收资源
     */
    public void recycle() {
        if (mRenderFrame != null) {
            mRenderFrame.recycle();
            mDisposedFrame.recycle();
        }
    }

    /**
     * 帧图像析构消除 - 提交结果
     */
    private void dispose(ApngFrame frame) {
        // last frame dispose op
        switch (mLastDisposeOp) {
            case APNG_DISPOSE_OP_NONE:
                // no op
                break;

            case APNG_DISPOSE_OP_BACKGROUND:
                // clear rect
                mRenderCanvas.clipRect(mDisposeRect);
                mRenderCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mRenderCanvas.clipRect(mFullRect, Region.Op.REPLACE);
                break;

            case APNG_DISPOSE_OP_PREVIOUS:
                // swap work and cache bitmap
                Bitmap bmp = mRenderFrame;
                mRenderFrame = mDisposedFrame;
                mDisposedFrame = bmp;
                mRenderCanvas.setBitmap(mRenderFrame);
                mDisposeCanvas.setBitmap(mDisposedFrame);
                break;
        }

        // current frame dispose op
        mLastDisposeOp = frame.getDisposeOp();
        switch (mLastDisposeOp) {
            case APNG_DISPOSE_OP_NONE:
                // no op
                break;

            case APNG_DISPOSE_OP_BACKGROUND:
                // cache rect for next clear dispose
                int x = frame.getxOff();
                int y = frame.getyOff();
                mDisposeRect.set(x, y, x + frame.getWidth(), y + frame.getHeight());
                break;

            case APNG_DISPOSE_OP_PREVIOUS:
                // cache bmp for next restore dispose
                mDisposeCanvas.clipRect(mFullRect, Region.Op.REPLACE);
                mDisposeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                mDisposeCanvas.drawBitmap(mRenderFrame, 0, 0, null);
                break;
        }
    }

    /**
     * 帧图像合成
     */
    private void blend(ApngFrame frame, Bitmap frameBmp) {
        int xOff = frame.getxOff();
        int yOff = frame.getyOff();

        mRenderCanvas.clipRect(xOff, yOff, xOff + frame.getWidth(), yOff + frame.getHeight());
        if (frame.getBlendOp() == APNG_BLEND_OP_SOURCE) {
            mRenderCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        mRenderCanvas.drawBitmap(frameBmp, xOff, yOff, null);
        mRenderCanvas.clipRect(mFullRect, Region.Op.REPLACE);


    }


}
