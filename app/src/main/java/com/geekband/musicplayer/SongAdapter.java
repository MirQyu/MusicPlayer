package com.geekband.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by MirQ on 16/7/21.
 */
public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> mSongs;
    private LayoutInflater songInf;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        mSongs = songs;
        songInf = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mSongs.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // map to song layout
        LinearLayout songLay = (LinearLayout) songInf.inflate
                (R.layout.song,parent, false);
        //
        TextView songTitleView = (TextView) songLay.findViewById(R.id.song_title);
        TextView songArtistView = (TextView) songLay.findViewById(R.id.song_artist);
        TextView songIdVeiw = (TextView) songLay.findViewById(R.id.song_id);
        Song currSong = mSongs.get(position);
        songTitleView.setText(currSong.getTitle());
        songArtistView.setText(currSong.getArtist());
        songIdVeiw.setText(String.valueOf(currSong.getId()));
        // set position as tag
        songLay.setTag(position);
        return songLay;
    }


    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
