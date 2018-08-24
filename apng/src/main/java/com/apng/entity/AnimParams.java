package com.apng.entity;


/**
 * @author xing.hu@renren-inc.com
 * @since 2016/5/24, 17:43
 */
public class AnimParams {

    /**
     * 等宽缩放
     */
    public static final int WIDTH_SCALE_TYPE = 0x0001;
    /**
     * 等高缩放
     */
    public static final int HEIGHT_SCALE_TYPE = 0x0010;
    /**
     *  按宽高比例较小的进行缩放
     */
    public static final int WIDTH_OR_HEIGHT_SCALE_TYPE = 0x0100;

    /**
     * 缩放比例
     */
    public int scaleType = WIDTH_SCALE_TYPE;

    /**
     * 对齐方式
     */
    public int align = 1;

    /**
     * 对齐百分比
     */
    public float percent = -1;

    /**
     *礼物展示权重
     */
    public int weight = 0;

    public boolean isHasBackground = false;

    /**
     * 动效的名字
     */
    public String name = "";

     /**
     * 礼物动画本地路径
     */
    public String imagePath;


    /**
     * 守护骑士分片特效执行次数
     */
    public int loopCount = 1;





}
