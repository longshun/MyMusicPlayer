package com.longshun.mymusicplayer.bean;

import java.io.Serializable;

/**
 * Created by longShun on 2016/8/31.
 */
public class Music implements Serializable {



    private int id;
    /*歌曲名称*/
    private String title;
    private String album;
    /*歌手*/
    private String artist;
    /*歌曲地址*/
    private String url;
    /*歌曲时长*/
    private int duration;
    /*歌曲占用存储空间*/
    private int size;

    public Music() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", url='" + url + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }
}
