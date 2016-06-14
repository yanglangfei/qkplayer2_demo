package com.qukan.playsdk.sample.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.qukan.playsdk.QkMediaPlayer;
import com.qukan.playsdk.misc.ITrackInfo;
import com.qukan.playsdk.sample.R;
import com.qukan.playsdk.sample.application.Settings;
import com.qukan.playsdk.sample.fragments.TracksFragment;
import com.qukan.playsdk.sample.widget.media.MyMediaController;
import com.qukan.playsdk.sample.widget.media.QkVideoView;

public class VideoActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "VideoActivity";

    private String mVideoPath;
    private MyMediaController mMediaController;
    private QkVideoView mVideoView;
    private Settings mSettings;
    private boolean mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mSettings = new Settings(this);
         mVideoPath="http://recordcdn.quklive.com:80/broadcast/activity/9461574890504843/record.m3u8";
        ActionBar actionBar = getSupportActionBar();
        mMediaController = new MyMediaController(this);
        QkMediaPlayer.loadLibrariesOnce(null);
        QkMediaPlayer.native_profileBegin("libqkplayer.so");

        mVideoView = (QkVideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoPath(mVideoPath);
        mVideoView.start();
    }

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        QkMediaPlayer.native_profileEnd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }


    @Override
    public ITrackInfo[] getTrackInfo() {
        if (mVideoView == null)
            return null;

        return mVideoView.getTrackInfo();
    }

    @Override
    public void selectTrack(int stream) {
        mVideoView.selectTrack(stream);
    }

    @Override
    public void deselectTrack(int stream) {
        mVideoView.deselectTrack(stream);
    }

    @Override
    public int getSelectedTrack(int trackType) {
        if (mVideoView == null)
            return -1;

        return mVideoView.getSelectedTrack(trackType);
    }
}
