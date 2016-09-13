package com.longshun.mymusicplayer.media;

import android.media.MediaPlayer;

/**
 * Created by longShun on 2016/9/2.
 */
public class MyMediaPlayer extends MediaPlayer {
    private String tag;

    public MyMediaPlayer() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
