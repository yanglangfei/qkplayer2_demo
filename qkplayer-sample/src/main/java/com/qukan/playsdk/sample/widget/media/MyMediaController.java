package com.qukan.playsdk.sample.widget.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qukan.playsdk.QLog;
import com.qukan.playsdk.sample.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by wsy on 2015/12/18.
 */
public class MyMediaController extends FrameLayout implements IMediaController
{
    private ActionBar mActionBar;
    private ArrayList<View> mShowOnceArray = new ArrayList<View>();

    private MediaController.MediaPlayerControl mPlayer;
    private Context mContext;
    private PopupWindow mWindow;
    private int mAnimStyle;
    private View mAnchor;
    private View mRoot;
    private ProgressBar mProgress;
    private TextView mEndTime, mCurrentTime;
    private TextView mFileName;
    private MyOutlineTextView mInfoView;
    private String mTitle;
    private long mDuration;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mInstantSeeking = true;
    private static final int sDefaultTimeout = 0;  // 如果设置为3000,表示3秒后进度条小时
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private boolean mFromXml = false;
    private ImageButton mPauseButton;

    private AudioManager mAM;

    // 进度条定时器
    private Timer myTimer;


    public MyMediaController(Context context)
    {
        super(context);
        if (!mFromXml && initController(context))
        {
            initFloatingWindow();
        }
    }

    public void setSupportActionBar(@Nullable ActionBar actionBar)
    {
        mActionBar = actionBar;
        if (isShowing())
        {
            actionBar.show();
        }
        else
        {
            actionBar.hide();
        }
    }


    public void showOnce(@NonNull View view)
    {
        mShowOnceArray.add(view);
        view.setVisibility(View.VISIBLE);
        show();
    }

    private boolean initController(Context context)
    {
        mContext = context;
        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return true;
    }

    @Override
    public void onFinishInflate()
    {
        super.onFinishInflate();

        if (mRoot != null)
        {
            initControllerView(mRoot);
        }
    }

    private void initFloatingWindow()
    {
        mWindow = new PopupWindow(mContext);
        mWindow.setFocusable(false);
        mWindow.setBackgroundDrawable(null);
        mWindow.setOutsideTouchable(true);
        mAnimStyle = android.R.style.Animation;
    }

    /**
     * Set the view that acts as the anchor for the control view. This can for
     * example be a VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(View view)
    {
        mAnchor = view;
        if (!mFromXml)
        {
            removeAllViews();
            mRoot = makeControllerView();
            mWindow.setContentView(mRoot);
            mWindow.setWidth(LayoutParams.MATCH_PARENT);
            mWindow.setHeight(LayoutParams.WRAP_CONTENT);
        }
        initControllerView(mRoot);
    }

    /**
     * Create the view that holds the widgets that control playback. Derived
     * classes can override this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView()
    {
        return ((LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.mediacontroller, this);
    }

    private void initControllerView(View v)
    {
        mPauseButton = (ImageButton) v
                .findViewById(R.id.mediacontroller_play_pause);
        if (mPauseButton != null)
        {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_seekbar);
        if (mProgress != null)
        {
            if (mProgress instanceof SeekBar)
            {
                SeekBar seeker = (SeekBar) mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
                seeker.setThumbOffset(1);
            }
            mProgress.setMax(1000);
        }

        mEndTime = (TextView) v.findViewById(R.id.mediacontroller_time_total);
        mCurrentTime = (TextView) v
                .findViewById(R.id.mediacontroller_time_current);
        mFileName = (TextView) v.findViewById(R.id.mediacontroller_file_name);
        if (mFileName != null)
        {
            mFileName.setText(mTitle);
        }
    }

    public void setMediaPlayer(MediaController.MediaPlayerControl player)
    {
        mPlayer = player;
        updatePausePlay();
    }

    public void show()
    {
        show(sDefaultTimeout);
    }

    /**
     * Set the content of the file_name TextView
     *
     * @param name
     */
    public void setFileName(String name)
    {
        mTitle = name;
        if (mFileName != null)
        {
            mFileName.setText(mTitle);
        }
    }

    /**
     * Set the View to hold some information when interact with the
     * MediaController
     *
     * @param v
     */
    public void setInfoView(MyOutlineTextView v)
    {
        mInfoView = v;
    }

    private void disableUnsupportedButtons()
    {
        try
        {
            if (mPauseButton != null && !mPlayer.canPause())
            {
                mPauseButton.setEnabled(false);
            }
        }
        catch (IncompatibleClassChangeError ex)
        {
        }
    }

    /**
     * <p>
     * Change the animation style resource for this controller.
     * </p>
     * <p/>
     * <p>
     * If the controller is showing, calling this method will take effect only
     * the next time the controller is shown.
     * </p>
     *
     * @param animationStyle animation style to use when the controller appears and
     *                       disappears. Set to -1 for the default animation, 0 for no
     *                       animation, or a resource identifier for an explicit animation.
     */
    public void setAnimationStyle(int animationStyle)
    {
        mAnimStyle = animationStyle;
    }

    /**
     * Show the controller on screen. It will go away automatically after
     * 'timeout' milliseconds of inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show the controller
     *                until hide() is called.
     */
    @SuppressLint("InlinedApi")
    public void show(int timeout)
    {
        if (!mShowing && mAnchor != null && mAnchor.getWindowToken() != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            {
                mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            if (mPauseButton != null)
            {
                mPauseButton.requestFocus();
            }
            disableUnsupportedButtons();

            if (mFromXml)
            {
                setVisibility(View.VISIBLE);
            }
            else
            {
                int[] location = new int[2];

                mAnchor.getLocationOnScreen(location);
                Rect anchorRect = new Rect(location[0], location[1],
                        location[0] + mAnchor.getWidth(), location[1]
                        + mAnchor.getHeight());

                mWindow.setAnimationStyle(mAnimStyle);
                mWindow.showAtLocation(mAnchor, Gravity.BOTTOM,
                        anchorRect.left, 0);
            }
            mShowing = true;
            if (mShownListener != null)
            {
                mShownListener.onShown();
            }
        }
        updatePausePlay();
        //mHandler.sendEmptyMessage(SHOW_PROGRESS);

        // 启动进度条定时器
        startMyTimer();

        if (timeout != 0)
        {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
        }

        if (mActionBar != null)
        {
            mActionBar.show();
        }
    }

    public boolean isShowing()
    {
        return mShowing;
    }

    @SuppressLint("InlinedApi")
    public void hide()
    {
        if (mAnchor == null)
        {
            return;
        }

        if (mShowing)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            {
                mAnchor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
            try
            {
                //mHandler.removeMessages(SHOW_PROGRESS);
                if (mFromXml)
                {
                    setVisibility(View.GONE);
                }
                else
                {
                    mWindow.dismiss();
                }
            }
            catch (IllegalArgumentException ex)
            {
                QLog.d("MediaController already removed");
            }
            mShowing = false;
            if (mHiddenListener != null)
            {
                mHiddenListener.onHidden();
            }
        }

        if (mActionBar != null)
        {
            mActionBar.hide();
        }
        for (View view : mShowOnceArray)
        {
            view.setVisibility(View.GONE);
        }
        mShowOnceArray.clear();

        // 关闭定时器
        stopMyTimer();
    }

    private void stopMyTimer()
    {
        // 取消进度条定时器
        if (myTimer != null)
        {
            myTimer.cancel();
            myTimer = null;
        }
    }

    private void startMyTimer()
    {
        stopMyTimer();

        myTimer = new Timer();
        myTimer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                mHandler.sendEmptyMessage(SHOW_PROGRESS);
            }
        }, 0, 1000);
    }

    public interface OnShownListener
    {
        public void onShown();
    }

    private OnShownListener mShownListener;

    public void setOnShownListener(OnShownListener l)
    {
        mShownListener = l;
    }

    public interface OnHiddenListener
    {
        public void onHidden();
    }

    private OnHiddenListener mHiddenListener;

    public void setOnHiddenListener(OnHiddenListener l)
    {
        mHiddenListener = l;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case FADE_OUT:
                {
                    hide();
                }
                break;

                case SHOW_PROGRESS:
                {
                    setProgress();

                    if (!mDragging && mShowing)
                    {
                        updatePausePlay();
                    }
                }
                break;
            }
        }
    };

    private long setProgress()
    {
        if (mPlayer == null || mDragging)
        {
            return 0;
        }

        int position = mPlayer.getCurrentPosition();
        int duration = mPlayer.getDuration();
        if (mProgress != null)
        {
            if (duration > 0)
            {
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = mPlayer.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        mDuration = duration;

        if (mEndTime != null)
        {
            mEndTime.setText(generateTime(mDuration));
        }
        if (mCurrentTime != null)
        {
            mCurrentTime.setText(generateTime(position));
        }

        return position;
    }

    private static String generateTime(long position)
    {
        int totalSeconds = (int) ((position / 1000.0) + 0.5);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0)
        {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds).toString();
        }
        else
        {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds).toString();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        show(sDefaultTimeout);
        return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev)
    {
        show(sDefaultTimeout);
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        int keyCode = event.getKeyCode();
        if (event.getRepeatCount() == 0
                && (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE))
        {
            doPauseResume();
            show(sDefaultTimeout);
            if (mPauseButton != null)
            {
                mPauseButton.requestFocus();
            }
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP)
        {
            if (mPlayer.isPlaying())
            {
                mPlayer.pause();
                updatePausePlay();
            }
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_MENU)
        {
            hide();
            return true;
        }
        else
        {
            show(sDefaultTimeout);
        }
        return super.dispatchKeyEvent(event);
    }

    private View.OnClickListener mPauseListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            doPauseResume();
            show(sDefaultTimeout);
        }
    };

    private void updatePausePlay()
    {
        if (mRoot == null || mPauseButton == null)
        {
            return;
        }

        if (mPlayer.isPlaying())
        {
            mPauseButton.setImageResource(R.drawable.mediacontroller_pause_button);
        }
        else
        {
            mPauseButton.setImageResource(R.drawable.mediacontroller_play_button);
        }
    }

    private void doPauseResume()
    {
        if (mPlayer.isPlaying())
        {
            mPlayer.pause();
        }
        else
        {
            mPlayer.start();
        }
        updatePausePlay();
    }

    private Runnable lastRunnable;
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener()
    {
        public void onStartTrackingTouch(SeekBar bar)
        {
            mDragging = true;
            show(3600000);

             mAM.setStreamMute(AudioManager.STREAM_MUSIC, true);

            if (mInfoView != null)
            {
                mInfoView.setText("");
                mInfoView.setVisibility(View.VISIBLE);
            }
        }

      

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser)
        {
            if (!fromuser)
            {
                return;
            }

            final long newposition = (mDuration * progress) / 1000;
            String time = generateTime(newposition);
            mPlayer.seekTo( (int) newposition);

            if (mInfoView != null)
            {
                mInfoView.setText(time);
            }
            if (mCurrentTime != null)
            {
                mCurrentTime.setText(time);
            }
        }
		
		  public void onStopTrackingTouch(SeekBar bar)
        {
            mDragging = false;
            setProgress();
            updatePausePlay();

            /*
            if (!mInstantSeeking)
            {
                mPlayer.seekTo((int) ((mDuration * bar.getProgress()) / 1000));
            }*/

            if (mInfoView != null)
            {
                mInfoView.setText("");
                mInfoView.setVisibility(View.GONE);
            }
            show(sDefaultTimeout);

            //mHandler.removeMessages(SHOW_PROGRESS);
            mAM.setStreamMute(AudioManager.STREAM_MUSIC, false);

            //mHandler.sendEmptyMessageDelayed(SHOW_PROGRESS, 1000);
         }
    };

    @Override
    public void setEnabled(boolean enabled)
    {
        if (mPauseButton != null)
        {
            mPauseButton.setEnabled(enabled);
        }
        if (mProgress != null)
        {
            mProgress.setEnabled(enabled);
        }
        disableUnsupportedButtons();
        super.setEnabled(enabled);
    }
}
