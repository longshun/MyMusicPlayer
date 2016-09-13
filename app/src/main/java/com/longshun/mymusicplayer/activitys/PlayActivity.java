package com.longshun.mymusicplayer.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.longshun.mymusicplayer.R;
import com.longshun.mymusicplayer.base.BaseActivity;
import com.longshun.mymusicplayer.bean.Music;
import com.longshun.mymusicplayer.utils.AudioUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends BaseActivity
        implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener,
        AudioUtils.onFinishMusic {

    private Button btnPlayNext;
    private Button btnPlayOrPause;
    private Button btnPlayPre;
    private Button btnPlayMode;
    private TextView tvMusicName;
    private SeekBar seekBarProgress;

    private AudioUtils audioUtils;

    /*用于更新进度条的计时器*/
    private Timer timer = new Timer();
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (audioUtils.isPlayBackground()) {
                int curPosition = audioUtils.getCurProgress();
                int curMusicLength = audioUtils.getCurPlayMusicLength();
                double rate = curPosition * 1.0 / curMusicLength;
                int max = seekBarProgress.getMax();
                int progress = (int) (max * rate);
                if (progress <= max) {
                    seekBarProgress.setProgress(progress);
                }
            }
        }
    };
    private Music curMusic;

    @Override
    protected void preOnCreate() {
    }

    @Override
    protected void findViews() {
    }

    @Override
    protected void initEvent() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        initData();
        initView();
        setViewData();
        initEvents();
//        audioUtils.playMusic(featurePosition);
        timer.schedule(timerTask, 0, 1000);

    }

    private void initData() {
        audioUtils = AudioUtils.getInstance(this);
        Intent intent = getIntent();
        int featurePosition = intent.getIntExtra("featurePosition", -1);
        //当前进入歌曲信息
        curMusic = audioUtils.getCurPlayMusicList().get(featurePosition);
        audioUtils.setCurMusicPosition(featurePosition);
    }

    private void initView() {
        btnPlayPre = (Button) findViewById(R.id.btn_play_pre);
        btnPlayNext = (Button) findViewById(R.id.btn_play_next);
        btnPlayOrPause = (Button) findViewById(R.id.btn_play_or_pause);
        btnPlayMode = (Button) findViewById(R.id.btn_play_mode);
        tvMusicName = (TextView) findViewById(R.id.tv_cur_music_name);
        seekBarProgress = (SeekBar) findViewById(R.id.seek_bar_progress);
    }

    private void setViewData() {
        if (audioUtils.isPlayBackground()){
            tvMusicName.setText(audioUtils.getPlayTitle());
            btnPlayOrPause.setBackgroundResource(R.mipmap.play);
        }else {
            tvMusicName.setText(curMusic.getTitle());
        }
        btnPlayMode.setText(audioUtils.getCurPlayMode());
        seekBarProgress.setMax(100);
        int getCurSaveProgress = audioUtils.getCurSaveProgress();
        Log.d("getCurSaveProgress=",getCurSaveProgress+"");
        if (getCurSaveProgress != -1 && curMusic.getTitle().equals(audioUtils.getPlayTitle())){
            int saveMusicLength = audioUtils.getCurSaveMusicLength();
            double rate = getCurSaveProgress*1.0/saveMusicLength;
            seekBarProgress.setProgress((int) (seekBarProgress.getMax()*rate));
        }
    }

    private void initEvents() {
        btnPlayNext.setOnClickListener(this);
        btnPlayPre.setOnClickListener(this);
        btnPlayOrPause.setOnClickListener(this);
        btnPlayMode.setOnClickListener(this);
        seekBarProgress.setOnSeekBarChangeListener(this);
        audioUtils.setOnFinishMusicListener(this);
    }


    /*播放，上一首，下一首点击事件*/
    @Override
    public void onClick(View view) {
        if (view != null) {
            int curPosition = audioUtils.getCurPlayMusicPosition();
            switch (view.getId()) {
                case R.id.btn_play_mode:
                    CharSequence text = btnPlayMode.getText();
                    //随机--》单曲--》顺序
                    if (AudioUtils.PlayMode.RANDOM.equals(text)) {
                        btnPlayMode.setText(AudioUtils.PlayMode.SINGLE);
                        audioUtils.setCurPlayMode(AudioUtils.PlayMode.SINGLE);
                    }
                    if (AudioUtils.PlayMode.SINGLE.equals(text)) {
                        btnPlayMode.setText(AudioUtils.PlayMode.ORDER);
                        audioUtils.setCurPlayMode(AudioUtils.PlayMode.ORDER);
                    }
                    if (AudioUtils.PlayMode.ORDER.equals(text)) {//顺序
                        btnPlayMode.setText(AudioUtils.PlayMode.RANDOM);
                        audioUtils.setCurPlayMode(AudioUtils.PlayMode.RANDOM);
                    }
                    break;
                case R.id.btn_play_pre:
                    audioUtils.playMusic(curPosition - 1);
                    updatePlayTitle();
                    break;
                case R.id.btn_play_or_pause:
                    if (audioUtils.isPlayBackground()) {
                        audioUtils.pauseMusic();
                        btnPlayOrPause.setBackgroundResource(R.mipmap.pause);
                    } else {
                        audioUtils.playMusic(curPosition);
                        btnPlayOrPause.setBackgroundResource(R.mipmap.play);
                    }
                    break;
                case R.id.btn_play_next:
                    audioUtils.playMusic(curPosition + 1);
                    updatePlayTitle();
                    break;
            }
        }
    }

    /*更新界面歌曲标题*/
    private void updatePlayTitle() {
        int appMusicPosition = audioUtils.getCurPlayMusicPosition();
        List<Music> appMusicList = audioUtils.getCurPlayMusicList();
        Music music = appMusicList.get(appMusicPosition);
        tvMusicName.setText(music.getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //SeekBar进度监听事件
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
        if (fromUser) {
            updatePlayProgress(i);
        }
    }

    /*更新播放进度*/
    private void updatePlayProgress(int i) {
        int max = seekBarProgress.getMax();
        double rate = i * 1.0 / max;
        if (audioUtils.isPlayBackground()) {
            //如果正在后台播放，更新当前播放位置
            audioUtils.setCurProgressByRate(rate);
        }else {
            //如果暂停状态下用户拖动播放进度条，更新上次播放音乐的进度
            int curMusicLength = audioUtils.getCurSaveMusicLength();
            Log.d("curMusicLength=",curMusicLength+"");
            int saveProgress = (int) (curMusicLength * rate);
            Log.d("saveProgress=",saveProgress+"");
            audioUtils.setCurSaveProgress(saveProgress);
        }
        Log.d("max,i,rate=", max + "--" + i + "--" + rate);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /*播放一首歌结束 后的操作*/
    @Override
    public void finishMusic() {
        tvMusicName.setText(audioUtils.getCurPlayMusic().getTitle());
    }
}
