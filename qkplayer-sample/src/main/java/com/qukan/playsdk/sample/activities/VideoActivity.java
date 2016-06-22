package com.qukan.playsdk.sample.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Toast;

import com.qukan.playsdk.IMediaPlayer;
import com.qukan.playsdk.QkMediaPlayer;
import com.qukan.playsdk.misc.ITrackInfo;
import com.qukan.playsdk.sample.R;
import com.qukan.playsdk.sample.application.Settings;
import com.qukan.playsdk.sample.fragments.TracksFragment;
import com.qukan.playsdk.sample.widget.media.MyMediaController;
import com.qukan.playsdk.sample.widget.media.QkVideoView;

public class VideoActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "VideoActivity";

    private MyMediaController mMediaController;
    private QkVideoView mVideoView;
    private Settings mSettings;
    private boolean mBackPressed;
    private  String oriFlv="http://recordcdn.quklive.com/broadcast/activity/9458019977964845/20160524092410-20160524103102.m3u8";
    private String mVideoPath="http://hdl.quklive.com/broadcast/activity/9461574890504843.flv";



    private  String storePath="http://hdl.quklive.com/broadcast/activity/9458019977964845.flv";
    private  String storeLive="http://hls2.quklive.com:9080/broadcast/activity/9458019977964845/real.m3u8";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mSettings = new Settings(this);

        ActionBar actionBar = getSupportActionBar();
        mMediaController = new MyMediaController(this);
        QkMediaPlayer.loadLibrariesOnce(null);
        QkMediaPlayer.native_profileBegin("libqkplayer.so");

        mVideoView = (QkVideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setVideoPath(storePath);
        mVideoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                Toast.makeText(VideoActivity.this, "err", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        mVideoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                Toast.makeText(VideoActivity.this, "com", Toast.LENGTH_SHORT).show();

            }
        });

        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                Toast.makeText(VideoActivity.this, "info:", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        mVideoView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                Toast.makeText(VideoActivity.this, "pre", Toast.LENGTH_SHORT).show();

            }
        });
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
