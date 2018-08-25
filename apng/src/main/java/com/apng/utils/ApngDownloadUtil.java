package com.apng.utils;


import android.content.*;
import android.os.*;
import android.text.*;
import android.util.*;

import java.io.*;

/**
 * @author xing.hu
 * @since 2016/04/11, 14:25
 *  下载礼物动效apng类
 */
public class ApngDownloadUtil {

    private static final String TAG = "ApngDownloadUtil";


    /**
     * 获取Apng解压后的文件路径
     * @return
     */
    public static File getWorkingExactDir() {
        File workingDir = null;
        String apngCachePath = ("/sdcard/apng/.nomedia/exact");
        if (!TextUtils.isEmpty(apngCachePath)) {
            workingDir = new File(apngCachePath);
            if (!workingDir.exists()) {
                workingDir.mkdirs();
            }
        }
        return workingDir;
    }

    public static String getFileCachePath(String uri, Context context) {
        // 只有在存在sdcard时才下载Apng动画
        if (!haveExterStorage()) {// 没有sdcard
            return null;
        }
        String apngPath = getFileDirs("apng/.nomedia/", context);
        if (apngPath != null) {
            File file = new File(apngPath, String.format("%s.png", Md5.toMD5(uri)));
            return file.getAbsolutePath();
        }

        return null;
    }


    public static String getFileDirs(String dirName, Context context) {
        File externalCache = context.getExternalFilesDir(null);
        if (externalCache != null) {
            File cacheImg = new File(externalCache, dirName);
            if (!cacheImg.exists()) {
                cacheImg.mkdirs();
            }

            if (cacheImg.canRead() && cacheImg.canWrite()) {
                return cacheImg.getAbsolutePath();
            }

        }

        File innerCache = context.getFilesDir();
        if (innerCache != null) {
            File cacheImg = new File(innerCache, dirName);
            if (!cacheImg.exists()) {
                cacheImg.mkdirs();
            }

            if (cacheImg.canRead() && cacheImg.canWrite()) {
                return cacheImg.getAbsolutePath();
            }
        }

        // 如果files目录获取不到, 则获取cache目录
        return getCacheDirs(dirName, context);
    }

    public static String getCacheDirs(String dirName, Context context) {
        File externalCache =context.getExternalCacheDir();
        if (externalCache != null) {
            File cacheImg = new File(externalCache, dirName);
            if (!cacheImg.exists()) {
                cacheImg.mkdirs();
            }

            if (cacheImg.canRead() && cacheImg.canWrite()) {
                return cacheImg.getAbsolutePath();
            }

        }

        File innerCache = context.getCacheDir();
        if (innerCache != null) {
            File cacheImg = new File(innerCache, dirName);
            if (!cacheImg.exists()) {
                cacheImg.mkdirs();
            }

            if (cacheImg.canRead() && cacheImg.canWrite()) {
                return cacheImg.getAbsolutePath();
            }
        }
        return null;
    }

    public static boolean haveExterStorage() {

        if (true) {
            return isAvaiableSpace(200);
        }
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        // File sdcard = android.os.Environment.getExternalStorageDirectory();
        // if (sdcard == null || !sdcard.exists()) {
        // return false;
        // }
        return true;
    }

    public static boolean isAvaiableSpace(int sizekb) {
        boolean ishasSpace = false;
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                String sdcard = Environment.getExternalStorageDirectory().getPath();
                StatFs statFs = new StatFs(sdcard);
                long blockSize = statFs.getBlockSize();
                long blocks = statFs.getAvailableBlocks();
                long availableSpare = (blocks * blockSize) / (1024);
                Log.d("剩余空间", "availableSpare = " + availableSpare);
                if (availableSpare > sizekb) {
                    ishasSpace = true;
                }
            }
        }catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }

        return ishasSpace;
    }

}
