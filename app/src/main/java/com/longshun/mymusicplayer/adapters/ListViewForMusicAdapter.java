package com.longshun.mymusicplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.longshun.mymusicplayer.R;
import com.longshun.mymusicplayer.bean.Music;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by longShun on 2016/8/31.
 */
public class ListViewForMusicAdapter extends BaseAdapter {

    private Context context;

    private List<Music> musicList;

    public ListViewForMusicAdapter(Context context, List<Music> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @Override
    public int getCount() {
        return musicList == null ? 0 : musicList.size();
    }

    @Override
    public Object getItem(int i) {
        return musicList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_music_list,viewGroup,false);
            holder = new ViewHolder();
            holder.tvTitle = (TextView) view.findViewById(R.id.tv_title);
            holder.tvArtist = (TextView) view.findViewById(R.id.tv_artist);
            holder.tvDuration = (TextView) view.findViewById(R.id.tv_duration);
            holder.tvSize = (TextView) view.findViewById(R.id.tv_size);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        Music music = musicList.get(i);
        holder.tvTitle.setText(music.getTitle());
        holder.tvTitle.setSelected(true);//跑马灯效果需要
        holder.tvArtist.setText(music.getArtist());
        holder.tvArtist.setSelected(true);
        int duration = music.getDuration();
        int size = music.getSize();
        holder.tvDuration.setText("歌曲时长："+formatTime(duration)+"分钟");
        holder.tvSize.setText("歌曲大小："+ formatSize(size)+"M");
        return view;
    }

    private String formatSize(int size) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(size/1000/1000.0);
    }

    private String formatTime(int duration) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(duration/60/1000.0);
    }

    static class ViewHolder{
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;
        TextView tvSize;
    }

}
