package com.example.wscdemo.base;

import android.app.Application;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class AppContext extends Application {

    /**
     * App全局上下文
     */
    private static AppContext mInstance;

    /**
     * 主线程池
     */
    public static Executor MAIN_EXECUTOR = Executors.newFixedThreadPool(5);


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    /**
     * 获取Application全局变量
     * @return
     */
    public static AppContext getAppContext() {
        return mInstance;
    }



}
