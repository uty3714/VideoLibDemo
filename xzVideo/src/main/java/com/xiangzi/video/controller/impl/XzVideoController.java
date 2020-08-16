package com.xiangzi.video.controller.impl;

import android.content.Context;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.xiangzi.video.IXzVideoPlayer;
import com.xiangzi.video.R;
import com.xiangzi.video.view.IXzVideoController;
import com.xiangzi.video.view.XzVideoPlayer;

public class XzVideoController extends IXzVideoController implements View.OnClickListener {

    private Context mContext;
    private String mVideoUrl;

    private boolean topBottomVisible;
    private CountDownTimer mDismissTopBottomCountDownTimer;
    private OnPlayListener mOnPlayListener;

    private ImageView mCoverImage;
    private LinearLayout mLoadingLayout;
    private LinearLayout mDisconnect;
    private TextView mRefresh;
    private SeekBar mSeek;


    public XzVideoController(@NonNull Context context) {
        super(context);
        mContext = context;
        initView();
    }

    @Override
    public void setVideoPlayer(IXzVideoPlayer videoPlayer) {
        super.setVideoPlayer(videoPlayer);
        mVideoPlayer.setUp(mVideoUrl,null);
    }

    private void initView(){

        LayoutInflater.from(mContext).inflate(R.layout.xz_video_palyer_view, this, true);
        mCoverImage = findViewById(R.id.cover_image);
        mLoadingLayout = findViewById(R.id.ll_loading);
        mDisconnect = findViewById(R.id.disconnect);
        mRefresh = findViewById(R.id.refresh);
        mSeek = findViewById(R.id.seek);

        mRefresh.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.refresh){
            mVideoPlayer.restart();
        }
    }

    @Override
    public void setUrl(String videoUrl) {
        this.mVideoUrl = videoUrl;
    }

    @Override
    public void setImage(String url) {

    }

    @Override
    public ImageView imageView() {
        return null;
    }

    @Override
    protected void onPlayStateChanged(int playState) {
        switch (playState) {
            case XzVideoPlayer.STATE_IDLE:
                break;
            case XzVideoPlayer.STATE_PREPARING:
                //准备
                mDisconnect.setVisibility(View.GONE);
                mCoverImage.setVisibility(View.VISIBLE);
                mLoadingLayout.setVisibility(View.VISIBLE);
                break;
            case XzVideoPlayer.STATE_PREPARED:
                //准备完成
                startUpdateProgressTimer();
                break;
            case XzVideoPlayer.STATE_PLAYING:
            case XzVideoPlayer.STATE_NOTE_4G:
                //开始播放
                mDisconnect.setVisibility(View.GONE);
                mCoverImage.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(View.GONE);
                setIsPlayingFlag();
                break;
            case XzVideoPlayer.STATE_PAUSED:
            case XzVideoPlayer.STATE_BUFFERING_PAUSED:
            case XzVideoPlayer.STATE_BUFFERING_PLAYING:
                //暂停
                mDisconnect.setVisibility(View.GONE);
                pauseUpdateProgressTimer();
                setIsPauseFlag();
                break;
            case XzVideoPlayer.STATE_ERROR:
                //错误
                mDisconnect.setVisibility(View.GONE);
                cancelUpdateProgressTimer();
                setIsPlayError();
                break;
            case XzVideoPlayer.STATE_COMPLETED:
                //播放完成
                mDisconnect.setVisibility(View.GONE);
                mLoadingLayout.setVisibility(GONE);
                cancelUpdateProgressTimer();
                setIsPlayComplete();
                break;
            case XzVideoPlayer.STATE_NOTE_DISCONNECT:
                //断网
                mDisconnect.setVisibility(View.VISIBLE);
                cancelUpdateProgressTimer();
                setIsPlayError();
                break;
        }
    }

    @Override
    protected void reset() {
        //重置
        mSeek.setProgress(0);
        mSeek.setSecondaryProgress(0);
        cancelUpdateProgressTimer();
    }

    @Override
    protected void updateProgress() {
        long position = mVideoPlayer.getCurrentPosition();
        long duration = mVideoPlayer.getDuration();
        int bufferPercentage = mVideoPlayer.getBufferPercentage();
        mSeek.setSecondaryProgress(bufferPercentage);
        int progress = (int) (100f * position / duration);
        mSeek.setProgress(progress);
        setPlayProgress(progress,100);
    }

    @Override
    protected void showChangePosition(long duration, int newPositionProgress) {
        mSeek.setProgress(newPositionProgress);
    }

    @Override
    protected void hideChangePosition() {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }


    private void setIsPauseFlag() {
        if (mOnPlayListener != null) {
            mOnPlayListener.pause();
        }
    }

    private void setIsPlayingFlag() {
        if (mOnPlayListener != null) {
            mOnPlayListener.play();
        }
    }

    public void setIsPlayError(){
        if(mOnPlayListener != null){
            mOnPlayListener.error();
        }
    }

    public void setIsPlayComplete(){
        if(mOnPlayListener != null){
            mOnPlayListener.playComplete();
        }
    }

    public void setPlayProgress(int current,int total){
        if(mOnPlayListener != null){
            mOnPlayListener.playProgress(current, total);
        }
    }

    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.mOnPlayListener = onPlayListener;
    }

    public interface OnPlayListener {
        void play();
        void playProgress(int current,int total);
        void pause();
        void error();
        void playComplete();
    }

}
