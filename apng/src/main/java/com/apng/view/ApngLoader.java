package com.apng.view;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xing.hu
 * @since 2019-12-06, 16:23
 * Apng Loader
 *
 */
public class ApngLoader {



    private ScheduledThreadPoolExecutor  mExecutors;

    private ApngLoader(){
        mExecutors =  new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());

    }

    private static class Holder {
        private static ApngLoader apngLoader = new ApngLoader();

    }

    public static ApngLoader getInstance() {
        return Holder.apngLoader;

    }


    public  ScheduledThreadPoolExecutor getExecutor(){
        return mExecutors;
    }


    public static  class  Const{
        public static final int STEP_DEFAULT = 0;
        // clear ImageView canvas
        public static final  int STEP_CLEAR_CANVAS = 1;
        // draw frame on ImageView canvas
        public static final int STEP_DRAW_FRAME = 2;

    }


}
