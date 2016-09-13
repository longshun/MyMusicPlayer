package com.longshun.mymusicplayer.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.longshun.mymusicplayer.R;
import com.longshun.mymusicplayer.adapters.ListViewForMusicAdapter;
import com.longshun.mymusicplayer.base.BaseActivity;
import com.longshun.mymusicplayer.bean.Music;
import com.longshun.mymusicplayer.utils.AudioUtils;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private List<Music> musicList;
    private ListViewForMusicAdapter musicAdapter;
    private ListView listViewMusics;

    private SwipeRefreshLayout refreshLayout;
    private AudioUtils audioUtils;

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AudioUtils.LOADED_MUSIC_LIST:
                    List<Music> musicList = (List<Music>) msg.obj;
                    updateMusicList(musicList);
                    //加载完数据，不刷新了
                    refreshLayout.setRefreshing(false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void findViews() {

    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void preOnCreate() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //首页不支持右滑退出
        swipeBackLayout.setEnableGesture(false);
        initData();
        //初始化控件
        initViews();
        initEvents();
        //为控件设置弹出菜单
        registerForContextMenu(listViewMusics);
    }

    private void initData() {
        //加载音乐列表
        audioUtils = AudioUtils.getInstance(this);
        musicList = audioUtils.getCurPlayMusicList();
    }

    private void initEvents() {
        listViewMusics.setOnItemClickListener(new MusicItemClickListener());
        refreshLayout.setOnRefreshListener(this);
    }

    private void initViews() {
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light
        );

        listViewMusics = (ListView) findViewById(R.id.list_music);

        //设置ListView为空时的布局
        View emptyView = LayoutInflater.from(this).inflate(R.layout.listview_empty, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        emptyView.setLayoutParams(params);
        Button btnReloadMusic = (Button) emptyView.findViewById(R.id.btn_reload_music);
        btnReloadMusic.setOnClickListener(this);
        ViewGroup viewParent = (ViewGroup) listViewMusics.getParent();
        viewParent.addView(emptyView);
        listViewMusics.setEmptyView(emptyView);

        musicAdapter = new ListViewForMusicAdapter(this, musicList);
        listViewMusics.setAdapter(musicAdapter);
    }

    /*重新加载音乐数据*/
    @Override
    public void onClick(View view) {
        if (view != null) {
            int id = view.getId();
            if (id == R.id.btn_reload_music) {
                audioUtils.getLocalMusicList(this,handler);
            }
        }
    }

    //---------------------下拉刷新回调接口
    @Override
    public void onRefresh() {
        audioUtils.getLocalMusicList(this,handler);
    }

    class MusicItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(MainActivity.this, PlayActivity.class);
            intent.putExtra("featurePosition", i);
            startActivity(intent);
        }
    }

    private void updateMusicList(List<Music> musics) {
        if (musics == null || musics.size() == 0 || musicList == null) {
            return;
        }
        musicList.clear();
        musicList.addAll(musics);
        musicAdapter.notifyDataSetChanged();
    }

    //-----------------------创建菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.list_music){
            getMenuInflater().inflate(R.menu.main,menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_exit:
                finish();
                break;
            case R.id.menu_donate:
                // TODO: 2016/9/12 支付宝或微信转账
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
}
