package com.xiangzi.video.manager;

import com.xiangzi.video.view.XzVideoPlayer;

public class XzVideoPlayerManager {

    private XzVideoPlayer mVideoPlayer;

    private XzVideoPlayerManager() {
    }

    private static XzVideoPlayerManager sInstance;

    public static synchronized XzVideoPlayerManager get() {
        if (sInstance == null) {
            sInstance = new XzVideoPlayerManager();
        }
        return sInstance;
    }

    public XzVideoPlayer getCurrentVideoPlayer() {
        return mVideoPlayer;
    }

    public void setCurrentVideoPlayer(XzVideoPlayer videoPlayer) {
        if (mVideoPlayer != videoPlayer) {
            releaseVideoPlayer();
            mVideoPlayer = videoPlayer;
        }
    }

    public void suspendNiceVideoPlayer() {
        if (mVideoPlayer != null && ((mVideoPlayer.isPlaying() || mVideoPlayer.isBufferingPlaying()))) {
            mVideoPlayer.pause();
        }
    }

    public void resumeVideoPlayer() {
        if (mVideoPlayer != null && (mVideoPlayer.isPaused() || mVideoPlayer.isBufferingPaused())) {
            mVideoPlayer.restart();
        }
    }

    public void releaseVideoPlayer() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
    }

}
