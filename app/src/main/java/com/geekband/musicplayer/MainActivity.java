package com.geekband.musicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl{


    public static final String SERVICE_CONNECTION = " ---->ServiceConnection";
    public static String TAG = MainActivity.class.getSimpleName();

    // 歌曲列表
    private ArrayList<Song> mSongList;
    private ListView mSongView;
    // 后台服务
    private MusicService mMusicService;
    // 开启&关闭后台服务的Intent
    private Intent mPlayIntent;
    // 标志该Activity是否与Service绑定
    private boolean mMusicBound = false;

    private MusicController mController;

    private boolean paused = false, playbackPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        Log.i(TAG, "onCreate");

        /**
         * 主要任务, 初始化SongList, 并获取设备外存上的歌曲, ID, TITLE, ARTIST存入SongList
         */

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (myToolbar != null ){

            // set title
            myToolbar.setTitle("My Title");

            // set subtitle
            myToolbar.setSubtitle("sub title");

            // Navigation Icon 要設定在 setSupportActionBar 才有作用
            // 否則會出現 back button
            myToolbar.setNavigationIcon(R.drawable.play);

            // Menu item click 的監聽事件一樣要設定在 setSupportActionBar 才有
            myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_shuffle:
                            //shuffle
                            mMusicService.setShuffle();
                            break;
                        case R.id.action_end:
                            unbindService(mServiceConnection);
                            stopService(mPlayIntent);
                            mMusicService = null;
                            System.exit(0);
                            break;
                    }
                    return false;
                }
            });
        }

        mSongView = (ListView) findViewById(R.id.song_list);
        mSongList = new ArrayList<>();

        getSongList();

        Collections.sort(mSongList, new Comparator<Song>() {
            @Override
            public int compare(Song lhs, Song rhs) {
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(MainActivity.this, mSongList);
        mSongView.setAdapter(songAdt);

        setController();
    }

    private void getSongList() {
        // retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 算不算耗时操作???
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            // get Columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            // add
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                mSongList.add(new Song(thisId, thisTitle, thisArtist));
            } while (musicCursor.moveToNext());
        }
    }

    /**
     * 设置 MusicController
     */
    private void setController() {
        // set the mController
        mController = new MusicController(this);

        View.OnClickListener nexLst = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        };
        View.OnClickListener preLst = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        };
        mController.setPrevNextListeners(nexLst, preLst);
        mController.setMediaPlayer(this);
        mController.setAnchorView(findViewById(R.id.song_list));
        mController.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG + SERVICE_CONNECTION, "onServiceConnected");
            MusicService.ServiceBinder serviceBinder = (MusicService.ServiceBinder) service;
            mMusicService = serviceBinder.getService();
            mMusicService.setSongs(mSongList);
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG + SERVICE_CONNECTION, "onServiceDisconnected");
            mMusicBound = false;
        }
    };

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        mPlayIntent = new Intent(MainActivity.this, MusicService.class);
        bindService(mPlayIntent, mServiceConnection, BIND_AUTO_CREATE);
        startService(mPlayIntent);
    }

    // paused 是否离开APP

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        mController.hide();
        super.onStop();
    }

    public void songPicked(View view) {
        mMusicService.setSongPosition(Integer.valueOf(view.getTag().toString()));
        mMusicService.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        mController.show(0);
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        unbindService(mServiceConnection);
        stopService(mPlayIntent);
        mMusicService = null;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        super.onBackPressed();
    }


    private void playPrev() {
        mMusicService.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        mController.show(0);
    }

    private void playNext() {
        mMusicService.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        mController.show(0);
    }

    @Override
    public void start() {
        if (mMusicService != null && mMusicBound)
            mMusicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        if (mMusicService != null && mMusicBound)
            mMusicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (mMusicService != null && mMusicBound) {
            return mMusicService.getDur();
        }
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {

        if (mMusicService != null && mMusicBound && mMusicService.isPlaying()) {
            return mMusicService.getPosn();
        }
        else
            return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (mMusicService != null && mMusicBound)
            mMusicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMusicService !=null && mMusicBound && mMusicService.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
