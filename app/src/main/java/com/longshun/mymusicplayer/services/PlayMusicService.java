package com.longshun.mymusicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.longshun.mymusicplayer.bean.Music;
import com.longshun.mymusicplayer.media.MyMediaPlayer;

import java.io.IOException;

/*支只负责根据url播放音乐，不做其他处理*/
public class PlayMusicService extends Service {

    private MyMediaPlayer mediaPlayer;
    private OnCompleteListener completeListener;
    private MusicController musicController;

    /*提供给外界的音乐播放管理器*/
    public class MusicController extends Binder {

        private String tag;
        public MusicController() {
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public void playMusic(Music music) {
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(music.getUrl());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    this.tag = music.getUrl();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void pauseMusic() {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        }

        public boolean isPlayingBackground() {
            return mediaPlayer != null && mediaPlayer.isPlaying();
        }

        public void setOnCompleteListener(OnCompleteListener completeListener) {
            PlayMusicService.this.completeListener = completeListener;
        }

        public OnCompleteListener getOnCompleteListener(){
            return completeListener;
        }

        /*获取当前歌曲长度*/
        public int getCurMusicLength() {
            int duration = 0;
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                 duration = mediaPlayer.getDuration();
            }
            return duration;
        }
        /*获取当前播放的进度*/
        public int getCurPosition(){
            int curPosition = 0;
            if (mediaPlayer != null) {
                curPosition = mediaPlayer.getCurrentPosition();
            }
            return curPosition;
        }
        public void setCurProgressByRate(double rate) {
            if (mediaPlayer != null) {
                int duration = mediaPlayer.getDuration();
                int curPosition = (int) (duration*rate);
                Log.d("curPosition",curPosition+"");
                mediaPlayer.seekTo(curPosition);
            }
        }
        public void setCurProgress(int progress){
            if (mediaPlayer != null) {
                mediaPlayer.seekTo(progress);
                mediaPlayer.start();
            }
        }
    }


    public interface OnCompleteListener{
        void complete(MusicController musicController);
    }

    public PlayMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicController;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        musicController = new MusicController();
        //初始化音乐播放组件
        mediaPlayer = new MyMediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                completeListener.complete(musicController);
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        initData(intent);
        //意外退出服务，那么重新连接，但是不传递intent
        return START_STICKY;
    }

    /* private void initData(Intent intent) {
         if (intent != null) {
             currentMusicUrl = intent.getStringExtra("url");
         }
     }
 */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // TODO: 2016/9/1
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
