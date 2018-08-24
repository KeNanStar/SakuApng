package com.apng;

import android.graphics.*;

import java.io.*;


/**
 * @author xing.hu@renren-inc.com
 * @since 2016/3/26, 16:38
 */
public class ApngImageLoader {


    private final String TAG = "ApngImageLoader";

    /**
     * 单例
     */
    private static class SingletonCreator {
        private static final ApngImageLoader instance = new ApngImageLoader();
    }

    public static ApngImageLoader getInstance() {
        return SingletonCreator.instance;
    }



    /**
     * 获取Apng每帧的Bitmap
     */
    public static Bitmap loadFrameImgSync(String filePath) {
        Bitmap bitmap = null;
        try {
            return decodeFrameToBitmap(filePath);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 将每一帧的文件转化成Bitmap
     * @return
     * @throws Throwable
     */
    private static Bitmap decodeFrameToBitmap(String filePath) throws Throwable {






        BitmapFactory.Options options = new BitmapFactory.Options();

        Bitmap decodeBitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);

                try {
                    decodeBitmap = BitmapFactory.decodeFileDescriptor(fis.getFD(), null, options);

                } catch (OutOfMemoryError e) {

                } catch (IllegalArgumentException e) {

                } catch (Exception e) {

                }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fis = null;
            }
        }
        return decodeBitmap;

    }

}
