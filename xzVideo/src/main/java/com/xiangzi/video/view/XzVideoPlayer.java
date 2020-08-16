package com.xiangzi.video.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xiangzi.video.IXzVideoPlayer;
import com.xiangzi.video.manager.XzVideoPlayerManager;
import com.xiangzi.video.receiver.XzNetworkChangeReceiver;
import com.xiangzi.video.utils.XzNetworkStatus;
import com.xiangzi.video.view.surface.XzTextureView;

import java.io.IOException;
import java.util.Map;

public class XzVideoPlayer extends FrameLayout implements IXzVideoPlayer,
        TextureView.SurfaceTextureListener,
        XzNetworkChangeReceiver.XzNetworkChangeListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener {

    public static final int STATE_ERROR = -1;               //播放错误
    public static final int STATE_IDLE = 0;                 //播放未开始
    public static final int STATE_PREPARING = 1;            //播放准备中
    public static final int STATE_PREPARED = 2;             //播放准备就绪
    public static final int STATE_PLAYING = 3;              //正在播放
    public static final int STATE_PAUSED = 4;               //暂停播放
    public static final int STATE_BUFFERING_PLAYING = 5;    //正在缓冲
    public static final int STATE_BUFFERING_PAUSED = 6;     //正在缓冲 播放器暂时
    public static final int STATE_COMPLETED = 7;            //播放完成
    public static final int STATE_NOTE_4G = 8;              //提示4G
    public static final int STATE_NOTE_DISCONNECT = 9;      //提示断网

    public static final int MODE_NORMAL = 10;               //普通模式


    private int mCurrentState = STATE_IDLE;                     //播放状态(暂停 开始...)
    private int mCurrentMode = MODE_NORMAL;                     //播放模式(全屏 小屏...)
    private int mCurrentNetworkState = XzNetworkStatus.CONNECTED_WIFI;  //网络状态(wifi 4G 断网)
    private Context mContext;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private FrameLayout mContainer;
    private XzTextureView mTextureView;
    private Surface mSurface;
    private String mUrl;
    private Map<String, String> mHeaders;
    private IXzVideoController mController;
    private int mBufferPercentage;
    private SurfaceTexture mSurfaceTexture;
    private boolean continueFromLastPosition;
    private int skipToPosition;
    public static boolean allow4GFlag = false;
    private XzNetworkChangeReceiver mNetworkChangeReceiver;

    public XzVideoPlayer(@NonNull Context context) {
        this(context, null);
    }

    public XzVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XzVideoPlayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        if (mNetworkChangeReceiver == null) {
            mNetworkChangeReceiver = new XzNetworkChangeReceiver(this);
        }
        allow4GFlag = false;
        init();
    }

    private void init() {
        mContainer = new FrameLayout(mContext);
        mContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mContainer, params);
    }

    @Override
    public void setUp(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }

    public void setController(IXzVideoController controller) {
        mContainer.removeView(mController);
        mController = controller;
        mController.reset();
        mController.setVideoPlayer(this);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer.addView(mController, params);
    }

    @Override
    public void start() {
        if (mCurrentState == STATE_IDLE) {
            XzVideoPlayerManager.get().setCurrentVideoPlayer(this);
            initAudioManager();
            initMediaPlayer();
            initTextureView();
            addTextureView();
        }
    }

    private void initAudioManager() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    private void initTextureView() {
        if (mTextureView == null) {
            mTextureView = new XzTextureView(mContext);
            mTextureView.setSurfaceTextureListener(this);//此时回调onSurfaceTextureAvailable
        }
    }

    private void addTextureView() {
        mContainer.removeView(mTextureView);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mContainer.addView(mTextureView, 0, params);
    }


    @Override
    public void start(int pos) {
        skipToPosition = pos;
        start();
    }

    @Override
    public void restart() {
        //先判断网络状态
        mCurrentNetworkState = XzNetworkChangeReceiver.getNetworkStatus();
        if (mCurrentNetworkState == XzNetworkStatus.CONNECTED_4G && !allow4GFlag) {//4G
            mCurrentState = STATE_NOTE_4G;
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentNetworkState == XzNetworkStatus.NOT_CONNECTED) {//断网
            mCurrentState = STATE_NOTE_DISCONNECT;
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentState == STATE_PAUSED || (mCurrentState == STATE_NOTE_4G && allow4GFlag) || mCurrentState == STATE_NOTE_DISCONNECT) {
            if (mMediaPlayer == null) {
                mCurrentState = STATE_IDLE;
                start();
            } else {
                mMediaPlayer.start();
                mCurrentState = STATE_PLAYING;
                mController.onPlayStateChanged(mCurrentState);
            }
        } else if (mCurrentState == STATE_BUFFERING_PAUSED) {
            mMediaPlayer.start();
            mCurrentState = STATE_BUFFERING_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentState == STATE_COMPLETED) {
            mMediaPlayer.reset();
            openMediaPlayer();
        } else if (mCurrentState == STATE_ERROR) {
            mMediaPlayer.reset();
            mController.onPlayStateChanged(mCurrentState);
        } else if (mCurrentState == STATE_IDLE) {
            start();
        } else {
        }
    }

    @Override
    public void pause() {
        if (mCurrentState == STATE_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_PAUSED;
            mController.onPlayStateChanged(mCurrentState);
        }
        if (mCurrentState == STATE_BUFFERING_PLAYING) {
            mMediaPlayer.pause();
            mCurrentState = STATE_BUFFERING_PAUSED;
            mController.onPlayStateChanged(mCurrentState);
        }
    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(pos);
        }
    }

    @Override
    public void continueFromLastPosition(boolean continueFromLastPosition) {
        this.continueFromLastPosition = continueFromLastPosition;
    }

    @Override
    public boolean isIdle() {
        return mCurrentState == STATE_IDLE;
    }

    @Override
    public boolean isPreparing() {
        return mCurrentState == STATE_PREPARING;
    }

    @Override
    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED;
    }

    @Override
    public boolean isBufferingPlaying() {
        return mCurrentState == STATE_BUFFERING_PLAYING;
    }

    @Override
    public boolean isBufferingPaused() {
        return mCurrentState == STATE_BUFFERING_PAUSED;
    }

    @Override
    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mCurrentState == STATE_PAUSED;
    }

    @Override
    public boolean isError() {
        return mCurrentState == STATE_ERROR;
    }

    @Override
    public boolean isCompleted() {
        return mCurrentState == STATE_COMPLETED;
    }

    @Override
    public boolean isNormal() {
        return mCurrentMode == MODE_NORMAL;
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    @Override
    public int getCurrentPosition() {
        int currentPosition = mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
        return currentPosition;
    }

    @Override
    public int getPlayPercentage() {
        long position = getCurrentPosition();
        long duration = getDuration();
        try {
            int progress = (int) (100f * position / duration);
            return progress;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int getBufferPercentage() {
        return mBufferPercentage;
    }

    @Override
    public void release() {
        //解除网络监听广播
        if (mNetworkChangeReceiver != null) {
            mNetworkChangeReceiver.unRegisterNetworkChangeBroadcast();
        }
        //TODO 保存播放位置

        mCurrentMode = MODE_NORMAL;

        // 释放播放器
        releasePlayer();

        // 恢复控制器
        if (mController != null) {
            mController.reset();
        }
        Runtime.getRuntime().gc();
    }

    private void releasePlayer() {
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(null);
            mAudioManager = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mContainer.removeView(mTextureView);
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCurrentState = STATE_IDLE;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        mBufferPercentage = percent;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mCurrentState = STATE_COMPLETED;
        mController.onPlayStateChanged(mCurrentState);
        // 清除屏幕常亮
        mContainer.setKeepScreenOn(false);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        // 直播流播放时去调用mediaPlayer.getDuration会导致-38和-2147483648错误，忽略该错误
        mCurrentState = STATE_ERROR;
        mController.onPlayStateChanged(mCurrentState);
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {

        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            // 播放器渲染第一帧
            mCurrentState = STATE_PLAYING;
            mController.onPlayStateChanged(mCurrentState);
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            // MediaPlayer暂时不播放，以缓冲更多的数据
            if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_BUFFERING_PAUSED;
            } else {
                mCurrentState = STATE_BUFFERING_PLAYING;
            }
            mController.onPlayStateChanged(mCurrentState);
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            // 填充缓冲区后，MediaPlayer恢复播放/暂停
            if (mCurrentState == STATE_BUFFERING_PLAYING) {
                mCurrentState = STATE_PLAYING;
                mController.onPlayStateChanged(mCurrentState);
            }
            if (mCurrentState == STATE_BUFFERING_PAUSED) {
                mCurrentState = STATE_PAUSED;
                mController.onPlayStateChanged(mCurrentState);
            }
        } /*else if (what == MediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                // 视频旋转了extra度，需要恢复
                if (mTextureView != null) {
                    mTextureView.setRotation(extra);
                    LogUtil.d("视频旋转角度：" + extra);
                }
            } */ else {
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mCurrentState = STATE_PREPARED;
        mController.onPlayStateChanged(mCurrentState);
        mediaPlayer.start();
//        // 从上次的保存位置播放
//        if (continueFromLastPosition) {
//            int savedPlayPosition = 0;
//            mediaPlayer.seekTo(savedPlayPosition);
//        }
//        // 跳到指定位置播放
//        if (skipToPosition != 0) {
//            mediaPlayer.seekTo(skipToPosition);
//        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        mTextureView.adaptVideoSize(width, height);
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture;
            openMediaPlayer();
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    private void openMediaPlayer() {
        // 屏幕常亮
        mContainer.setKeepScreenOn(true);
        // 设置监听
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mCurrentNetworkState = XzNetworkChangeReceiver.getNetworkStatus();
        mNetworkChangeReceiver.registerNetworkChangeBroadcast();

        // 设置dataSource
        try {
            mMediaPlayer.setDataSource(mUrl);
            if (mSurface == null) {
                mSurface = new Surface(mSurfaceTexture);
            }
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            mController.onPlayStateChanged(mCurrentState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onNetworkChange(int networkStatus) {
        switch (networkStatus) {
            case XzNetworkStatus.NOT_CONNECTED:
                if (isPlaying() || isBufferingPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentState = STATE_NOTE_DISCONNECT;
                    mController.onPlayStateChanged(mCurrentState);
                }
                mCurrentNetworkState = XzNetworkStatus.NOT_CONNECTED;
                break;
            case XzNetworkStatus.CONNECTED_WIFI:
                mCurrentNetworkState = XzNetworkStatus.CONNECTED_WIFI;
                break;
            case XzNetworkStatus.CONNECTED_4G:
                if (isPlaying() || isBufferingPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentState = STATE_NOTE_4G;
                    mController.onPlayStateChanged(mCurrentState);
                }
                mCurrentNetworkState = XzNetworkStatus.CONNECTED_4G;
                break;
        }
    }
}
