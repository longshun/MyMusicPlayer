package com.longshun.mymusicplayer.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.longshun.mymusicplayer.bean.Music;
import com.longshun.mymusicplayer.services.PlayMusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by longShun on 2016/8/31.
 */
public class AudioUtils implements ServiceConnection {

    //    private static final String TAG = "AudioUtils";
    private static Cursor cursor;
    private static AudioUtils audioUtils;
    public static final int LOADED_MUSIC_LIST = 0;
    public static boolean isLoadedLocalMusic;

    private Context context;
    private PlayMusicService.MusicController musicController;
    private List<Music> curPlayMusicList;//当前播放音乐列表
    private List<Music> loadedMusicList;//从系统中加载出来的音乐列表
    private List<Music> randomMusicList;//随机播放列表
    private int curPosition;//当前播放音乐在集合中的位置
    private Music curMusic;//当前播放音乐
    private int curSaveProgress = -1;//当前暂停音乐的进度
    private int curSaveMusicLength = -1;//当前播放音乐的长度
    private onFinishMusic onFinishMusic;
    private String playTitle;//当前播放音乐的标题

    //播放模式应该保存在本地
    private String curPlayMode = PlayMode.ORDER;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg != null) {
                int type = msg.what;
                switch (type) {
                    case AudioUtils.LOADED_MUSIC_LIST:
                        //加载完音乐数据
                        loadedMusicList = (List<Music>) msg.obj;
                        //设置默认播放列表集合
                        curPlayMusicList = loadedMusicList;
                        //初始歌曲
                        curMusic = curPlayMusicList.get(0);
                        //得到随机播放的音乐列表
                        randomMusicList = new ArrayList<>();
                        randomMusicList.addAll(loadedMusicList);
                        Collections.shuffle(randomMusicList);
                        //标记当前音乐数据已经加载完毕
                        isLoadedLocalMusic = true;
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private AudioUtils(Context context) {
        this.context = context;
        bindMusicService();//在application中bind服务,unbind服务
        setBackgroundMusicService(true);
    }

    public static AudioUtils getInstance(Context context) {
        if (audioUtils == null) {
            audioUtils = new AudioUtils(context);
        }
        return audioUtils;
    }

    public int getCurPlayMusicLength() {
        int len = -1;
        if (musicController != null) {
            len = musicController.getCurMusicLength();
        }
        return len;
    }

    public int getCurSaveMusicLength(){
        return curSaveMusicLength;
    }

    /*绑定音乐服务*/
    public void bindMusicService() {
        Intent musicServiceIntent = new Intent(context, PlayMusicService.class);
        context.bindService(musicServiceIntent, this, Context.BIND_AUTO_CREATE);
    }

    public void unBindMusicService() {
        context.unbindService(this);
        setBackgroundMusicService(false);
    }

    public String getPlayTitle() {
        return playTitle;
    }

    public void setPlayTitle(String playTitle) {
        this.playTitle = playTitle;
    }

    /*是否开启后台音乐播放服务*/
    public void setBackgroundMusicService(boolean allowBack) {
        Intent musicServiceIntent = new Intent(context, PlayMusicService.class);
        if (allowBack) {
            context.startService(musicServiceIntent);
        } else {
            context.stopService(musicServiceIntent);
        }
    }

    public void playMusic(int position) {
        if (musicController == null) {
            throw new RuntimeException("haven't connect music service!");
        }
        if (position < 0) {
            position = curPlayMusicList.size() - 1;
        }
        if (position >= curPlayMusicList.size()) {
            position = 0;
        }
        Music music = curPlayMusicList.get(position);
        //标记用来判断是否和上次播放的是同一首歌
        String url = music.getUrl();
        if (musicController.isPlayingBackground() && url.equals(musicController.getTag())) {
            return;
        }
        if (!musicController.isPlayingBackground() && url.equals(musicController.getTag())) {
            musicController.setCurProgress(curSaveProgress);
            Log.d("playProgress=", curSaveProgress + "");
            return;
        }
        musicController.playMusic(music);
        musicController.setTag(url);
        curPosition = position;
        curMusic = music;
        setPlayTitle(music.getTitle());
        curSaveMusicLength = musicController.getCurMusicLength();
    }

    public void pauseMusic() {
        if (musicController != null) {
            //暂停的时候保存此时的播放进度，下次再播放从此时开始
            this.curSaveProgress = musicController.getCurPosition();
            Log.d("pauseProgress=", curSaveProgress + "");
            musicController.pauseMusic();
        }
    }

    public boolean isPlayBackground() {
        boolean isPlayingBackground = false;
        if (musicController != null) {
            isPlayingBackground = musicController.isPlayingBackground();
        }
        return isPlayingBackground;
    }

    public void setCurProgressByRate(double rate) {
        if (musicController != null) {
            musicController.setCurProgressByRate(rate);
        }
    }

    public void setCurSaveProgress(int curSaveProgress) {
        this.curSaveProgress = curSaveProgress;
    }
    public int getCurSaveProgress(){
        return curSaveProgress;
    }

    public int getCurProgress() {
        int progress = 0;
        if (musicController != null) {
            progress = musicController.getCurPosition();
        }
        return progress;
    }

    public String getCurPlayMode() {
        return curPlayMode;
    }

    public void setCurPlayMode(String curPlayMode) {
        this.curPlayMode = curPlayMode;
        if (PlayMode.ORDER.equals(curPlayMode)) {
            setOrderList();
        }
        if (PlayMode.RANDOM.equals(curPlayMode)) {
            setCurPlayListToRandom();
        }
    }

    public void setCurPlayListToRandom() {
        curPlayMusicList = randomMusicList;
    }

    public List<Music> getRandomPlayList() {
        return randomMusicList;
    }

    public void setOrderList() {
        curPlayMusicList = loadedMusicList;
    }

    public class PlayMode {
        public static final String RANDOM = "随机";
        public static final String SINGLE = "单曲";
        public static final String ORDER = "顺序";
    }

    /*获取当前应用的音乐播放列表*/
    public List<Music> getCurPlayMusicList() {
        return curPlayMusicList;
    }

    public void setCurPlayMusicList(List<Music> musicList) {
        this.curPlayMusicList = musicList;
    }

    public void setCurMusicPosition(int position) {
        curPosition = position;
    }

    public int getCurPlayMusicPosition() {
        return curPosition;
    }

    public void setCurPlayMusic(Music music) {
        this.curMusic = music;
    }

    public Music getCurPlayMusic() {
        return curMusic;
    }

    //----------------------绑定音乐服务回调---------------
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        if (iBinder != null) {
            musicController = (PlayMusicService.MusicController) iBinder;
        }
        if (musicController.getOnCompleteListener() == null) {
            musicController.setOnCompleteListener(new PlayMusicService.OnCompleteListener() {
                @Override
                public void complete(PlayMusicService.MusicController musicController) {
                    //如果是单曲循环，则position位置不变
                    int position = getCurPlayMusicPosition();
                    if (PlayMode.SINGLE.equals(curPlayMode)) {
                        playMusic(position);
                    } else {
                        playMusic(position + 1);
                    }
                    onFinishMusic.finishMusic();
                }
            });
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    public interface onFinishMusic {
        void finishMusic();
    }

    public onFinishMusic getOnFinishMusicListener() {
        return onFinishMusic;
    }

    public void setOnFinishMusicListener(AudioUtils.onFinishMusic onFinishMusic) {
        this.onFinishMusic = onFinishMusic;
    }

    //------------------获取音乐数据---------------
    public void getLocalMusicList(Context context) {
        getLocalMusicList(context, null);
    }

    public void getLocalMusicList(Context context, Handler handler) {
        if (context != null) {
            if (handler != null) {
                mHandler = handler;
            }
            ContentResolver resolver = context.getContentResolver();
            cursor = getAudioCursor(resolver);
            Thread LoadMusicThread = new Thread(new LoadMusicTask());
            LoadMusicThread.start();
        }
    }

    private class LoadMusicTask implements Runnable {
        @Override
        public void run() {
            getLocalMusicsByCursor(cursor);
        }
    }

    private void getLocalMusicsByCursor(Cursor cursor) {
        if (cursor != null) {
            try {
                List<Music> musicList = new ArrayList<>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    int size = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    Music music = new Music();
                    music.setId(id);
                    music.setTitle(title);
                    music.setArtist(artist);
                    music.setDuration(duration);
                    music.setSize(size);
                    music.setUrl(url);
                    musicList.add(music);
//                Log.d(TAG,music.toString());
                }
                Message msg = new Message();
                msg.what = LOADED_MUSIC_LIST;
                msg.obj = musicList;
                mHandler.sendMessage(msg);
                Log.d("utils:", Thread.currentThread().getName());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                cursor.close();
            }
        }
    }

    private static Cursor getAudioCursor(ContentResolver resolver) {
        return resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    /*判断后台播放歌曲的服务是否存在*/
    private boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            String runningName = serviceList.get(i).service.getClassName();
            if (runningName.equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
