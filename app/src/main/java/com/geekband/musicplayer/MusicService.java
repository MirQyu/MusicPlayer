package com.geekband.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    private static final int NOTIFY_ID = 1;
    public static String TAG = MusicService.class.getSimpleName();

    // 连接器
    private final IBinder musicBind = new ServiceBinder();

    // media player
    private MediaPlayer mPlayer;
    // song list
    private ArrayList<Song> mSongs;
    //current position
    private int mSongIndex;

    private String songTitle = "";

    private boolean shuffle = false;
    private Random rand;


    public MusicService() {
    }

    public void setShuffle() {
        shuffle = !shuffle;
    }

    public void setSongs(ArrayList<Song> songs) {
        mSongs = songs;
    }

    public void setSongPosition(int songIndex) {
        this.mSongIndex = songIndex;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        // create the service
        super.onCreate();
        // create a player
        mPlayer = new MediaPlayer();
        // initialize the position
        mSongIndex = 0;

        initMusicPlayer();

        rand = new Random();
    }

    public void initMusicPlayer() {
        // set player properties
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(MusicService.this);
        mPlayer.setOnCompletionListener(MusicService.this);
        mPlayer.setOnErrorListener(MusicService.this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind");
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        stopForeground(true);
    }

    // inner class
    public class ServiceBinder extends Binder {
        MusicService getService() {
            Log.i(TAG, "--> getService");
            return MusicService.this;
        }
    }

    public void playSong() {
        Log.i(TAG, "--> playSong");
        // play a song
        mPlayer.reset();
        //get song
        Song playSong = mSongs.get(mSongIndex);
        songTitle = playSong.getTitle();
        // get id
        long currSong = playSong.getId();
        // set Uri
        Uri trackUri = ContentUris.withAppendedId
                (MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            mPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mPlayer.prepareAsync();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "--> onPrepared");
        mp.start();
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker("qyu")
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "-----> onCompletion\n" + mPlayer.getCurrentPosition() + "\n" + mPlayer.getDuration());
        if (mPlayer.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public int getPosn() {
        return mPlayer.getCurrentPosition();
    }

    public int getDur() {
        return mPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public void pausePlayer() {
        mPlayer.pause();
    }

    public void seek(int posn) {
        mPlayer.seekTo(posn);
    }

    public void go() {
        mPlayer.start();
    }

    public void playPrev() {
        mSongIndex--;
        if (mSongIndex < 0) mSongIndex = mSongs.size()-1;
        playSong();
    }

    public void playNext() {
        if (shuffle) {
            int newSong = mSongIndex;
            while (newSong == mSongIndex) {
                newSong = rand.nextInt(mSongs.size());
            }
            mSongIndex = newSong;
        } else {
            mSongIndex++;
            if (mSongIndex >= mSongs.size()) mSongIndex = 0;
        }
        playSong();
    }

}
