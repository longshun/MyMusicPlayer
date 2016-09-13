package com.longshun.mymusicplayer.base;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.longshun.mymusicplayer.utils.AudioUtils;

/**
 * Created by longShun on 2016/9/5.
 */
public class MyApplication extends Application {

    private AudioUtils audioUtils;

    @Override
    public void onCreate() {
        super.onCreate();

        //1.获取加载音乐的单例对象
        audioUtils = AudioUtils.getInstance(this);
        //2.开始加载手机中的所有音乐
        audioUtils.getLocalMusicList(this);

        Fresco.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //unBind后台播放服务
        audioUtils.unBindMusicService();
    }
}
