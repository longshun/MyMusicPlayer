package com.longshun.mymusicplayer.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public abstract class BaseActivity extends SwipeBackActivity implements View.OnClickListener {

    protected SwipeBackLayout swipeBackLayout;

    protected abstract void findViews();

    protected abstract void initEvent();

    protected abstract void preOnCreate();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        preOnCreate();
        super.onCreate(savedInstanceState);
        //初始化右划退出activity组件
        initSwipeActivity();

        findViews();
        initEvent();

    }

    public void initSwipeActivity() {
        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        Log.d("SwipeBackLayout getInstance=", "initSwipeActivity");
    }
}
