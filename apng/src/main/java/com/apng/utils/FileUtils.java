package com.apng.utils;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.text.*;
import ar.com.hjg.pngj.*;
import com.apng.utils.RecyclingUtils.*;

import java.io.*;
import java.net.*;

/**
 * @author xing.hu
 * @since 2016/3/26, 12:15
 */
public class FileUtils {
    public static String getBaseName(String filename) {
        return removeExtension(getName(filename));
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        } else {
            int index = indexOfLastSeparator(filename);
            return filename.substring(index + 1);
        }
    }

    public static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int lastUnixPos = filename.lastIndexOf(47);
            int lastWindowsPos = filename.lastIndexOf(92);
            return Math.max(lastUnixPos, lastWindowsPos);
        }
    }

    public static String removeExtension(String filename) {
        if (filename == null) {
            return null;
        } else {
            int index = indexOfExtension(filename);
            return index == -1 ? filename : filename.substring(0, index);
        }
    }

    public static int indexOfExtension(String filename) {
        if (filename == null) {
            return -1;
        } else {
            int extensionPos = filename.lastIndexOf(46);
            int lastSeparator = indexOfLastSeparator(filename);
            return lastSeparator > extensionPos ? -1 : extensionPos;
        }
    }

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        } else {
            int index = indexOfExtension(filename);
            return index == -1 ? "" : filename.substring(index + 1);
        }
    }

    public static boolean isApng(File file) {
        boolean isApng = false;

        try {
            PngReaderApng reader = new PngReaderApng(file);
            reader.end();

            //int apngNumFrames = reader.getApngNumFrames();

            //isApng = apngNumFrames > 1;
            isApng = reader.isApng();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isApng;
    }


    private static Bitmap decodeFile(String path, int maxWidth, int maxHeight) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(path, options);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
        }
        return bitmap;
    }

    public static int[] getApngWH(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new int[]{options.outWidth, options.outHeight};
    }

    /**
     * 把文件从Asset复制到缓存
     * @param imageUri
     * @return
     */
    public static File processApngFile(String imageUri, Context context) {
        if(TextUtils.isEmpty(imageUri)) return  null;
        String path = ApngDownloadUtil.getFileCachePath(imageUri, context);
        File cacheFile = null;
        if(!TextUtils.isEmpty(path)) {
            cacheFile = new File(path);
            if (!cacheFile.exists()) {
                Scheme scheme = Scheme.ofUri(imageUri);
                InputStream source;
                if (scheme == Scheme.ASSETS) {
                    try {
                        source = getStreamFromAssets(imageUri, context);
                        copyInputStreamToFile(source, cacheFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    source = null;
                    try {
                        URL source1 = new URL(imageUri);
                        InputStream e = source1.openStream();
                        copyInputStreamToFile(e, cacheFile);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NetworkOnMainThreadException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return cacheFile;
    }

    public static InputStream getStreamFromAssets(String imageUri, Context context) throws IOException {
        String filePath = RecyclingUtils.Scheme.ASSETS.crop(imageUri);
        return context.getAssets().open(filePath);
    }

    public static boolean copyInputStreamToFile(InputStream inputStream, File destination) {
        try {
            FileOutputStream e = new FileOutputStream(destination, false);
            byte[] bt = new byte[1024];

            int c;
            while((c = inputStream.read(bt)) > 0) {
                e.write(bt, 0, c);
            }

            e.close();
            inputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
