package com.apng.utils;

import android.graphics.*;

/**
 * @author xing.hu
 * @since 2016/11/9, 下午2:49
 * Apng动画播放工具类
 */
public class ApngUtils {
    public static final String TAG = "ApngUtils";
    //上对齐
    public static  final int APNG_ANIM_ALIGN_TOP = 1;
    //居中对齐
    public static  final int APNG_ANIM_ALIGN_MIDDLE = 2;
    //下对齐
    public static  final int APNG_ANIM_ALIGN_BOTTOM = 3;
    /**
     * 获取所绘制bitmap距离左边和顶部的距离
     * @param canvas:画布
     * @param bitmap:图片
     * @param align:上对齐、中对齐、下对齐
     * @return
     */
    public static float[] getLeftAndTop(Canvas canvas, Bitmap bitmap, int align){
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float left = 0;
        float top = 0;

        if(align == APNG_ANIM_ALIGN_TOP) {
            //默认距离顶部距离为0
            left = canvasWidth - bitmapWidth > 0 ? (float) (canvasWidth - bitmapWidth) / 2 : (float) (bitmapWidth - canvasWidth) / 2;
            top = 0;

        }
        else if(align == APNG_ANIM_ALIGN_MIDDLE){
            left = canvasWidth - bitmapWidth > 0 ? (float) (canvasWidth - bitmapWidth) / 2 : (float) (bitmapWidth - canvasWidth) / 2;
            top = canvasHeight - bitmapHeight > 0 ? (float) (canvasHeight - bitmapHeight) / 2 : (float) (bitmapHeight - canvasHeight) / 2;
        }
        else if(align == APNG_ANIM_ALIGN_BOTTOM){
            left = canvasWidth - bitmapWidth > 0 ? (float) (canvasWidth - bitmapWidth) / 2 : (float) (bitmapWidth - canvasWidth) / 2;
            top = canvasHeight - bitmapHeight > 0 ? (float) (canvasHeight - bitmapHeight): (float) (bitmapHeight - canvasHeight);
        }

        return  new float[]{left, top};

    }







    /**
     * 获取中心点的坐标
     * @param canvas
     * @param bitmap
     * @param align
     * @return
     */
    public static float[] getCenterCoordinate(Canvas canvas, Bitmap bitmap, int align, float mScaling){
        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();
        float bitmapWidth = bitmap.getWidth() * mScaling;
        float bitmapHeight = bitmap.getHeight() * mScaling;
        float postX = canvasWidth / 2;
        float postY = 0;


        if(align == APNG_ANIM_ALIGN_TOP) {
            postY = bitmapHeight / 2;

        }
        else if(align == APNG_ANIM_ALIGN_MIDDLE){
            postY = canvasHeight / 2;
        }
        else if(align == APNG_ANIM_ALIGN_BOTTOM){
            postY = canvasHeight - bitmapHeight / 2;
        }

        return  new float[]{postX, postY};

    }

    public static float[] getTranLeftAndTop(Canvas canvas, Bitmap bitmap, int align, float mScaling, float percent){
        float canvasWidth = canvas.getWidth();
        float canvasHeight = canvas.getHeight();
        float bitmapWidth = bitmap.getWidth() * mScaling;
        float bitmapHeight = bitmap.getHeight() * mScaling;

        //因为按宽的比例进行缩放,有可能图片的高度比Canvas的高度大
        bitmapWidth = bitmapWidth > canvasWidth ? canvasWidth:bitmapWidth;
        bitmapHeight = bitmapHeight > canvasHeight ? canvasHeight:bitmapHeight;

        float tranLeft = 0;
        float tranTop = 0;
        //不是按比例进行缩放
        if(percent == -1) {
            if (align == APNG_ANIM_ALIGN_TOP) {
                //默认距离顶部距离为0
                tranLeft = canvasWidth - bitmapWidth > 0 ? (canvasWidth - bitmapWidth) / 2 : (bitmapWidth - canvasWidth) / 2;
                tranTop = 0;

            } else if (align == APNG_ANIM_ALIGN_MIDDLE) {
                tranLeft = canvasWidth - bitmapWidth > 0 ? (canvasWidth - bitmapWidth) / 2 : (bitmapWidth - canvasWidth) / 2;
                tranTop = canvasHeight - bitmapHeight > 0 ? (canvasHeight - bitmapHeight) / 2 : (bitmapHeight - canvasHeight) / 2;
            } else if (align == APNG_ANIM_ALIGN_BOTTOM) {
                tranLeft = canvasWidth - bitmapWidth > 0 ? (canvasWidth - bitmapWidth) / 2 : (bitmapWidth - canvasWidth) / 2;
                tranTop = canvasHeight - bitmapHeight > 0 ? (canvasHeight - bitmapHeight) : (bitmapHeight - canvasHeight);
            }

        }
        else{
            if (align == APNG_ANIM_ALIGN_TOP) {
                tranLeft = canvasWidth - bitmapWidth > 0 ? (canvasWidth - bitmapWidth) / 2 : (bitmapWidth - canvasWidth) / 2;
                tranTop = canvasHeight * percent;

            } else if (align == APNG_ANIM_ALIGN_MIDDLE) {
                tranLeft = canvasWidth - bitmapWidth > 0 ? (canvasWidth - bitmapWidth) / 2 : (bitmapWidth - canvasWidth) / 2;
                tranTop =  canvasHeight * percent - bitmapHeight/ 2;
            } else if (align == APNG_ANIM_ALIGN_BOTTOM) {
                tranLeft = canvasWidth - bitmapWidth > 0 ? (canvasWidth - bitmapWidth) / 2 : (bitmapWidth - canvasWidth) / 2;
                tranTop = canvasHeight * percent - bitmapHeight;
            }
        }


        return  new float[]{tranLeft, tranTop};

    }




}
