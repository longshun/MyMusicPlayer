package com.longshun.mymusicplayer.activitys;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.longshun.mymusicplayer.R;
import com.longshun.mymusicplayer.utils.AudioUtils;

/*程序启动欢迎页面*/
public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        继承自Activity时这样写
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //--------------隐藏标题栏-----------------
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
        //---------------加载Gif动画----------------
        Uri uri = Uri.parse("res://" + this.getPackageName() + "/" + R.mipmap.splash);
        SimpleDraweeView gifView = (SimpleDraweeView) findViewById(R.id.sdv_gif);
        AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        gifView.setController(controller);

        delayToMainActivity();
    }

    /*延迟进入主页*/
    private void delayToMainActivity() {
        //所需加载大量数据时可以采用下面这种方法
        /*final MyApplication myApplication = (MyApplication) getApplication();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (myApplication.isLoadedLocalMusic()){
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
            }
        });
        thread.start();*/
        Handler handler = new Handler();
        long delay = 5000;
        handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (AudioUtils.isLoadedLocalMusic){
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            // TODO: 2016/9/5 3秒之后没有加载完数据执行的操作
                        }
                    }
                }
                , delay);
    }
}
