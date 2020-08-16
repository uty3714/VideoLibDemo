package com.xiangzi.video;

import java.util.Map;

public interface IXzVideoPlayer {

    /**
     * 设置视频url
     * @param url
     * @param headers
     */
    void setUp(String url, Map<String, String> headers);

    /**
     * 开始播放
     */
    void start();

    /**
     * 从指定位置开始播放
     * @param pos
     */
    void start(int pos);

    /**
     * 重新播放
     */
    void restart();

    /**
     * 暂停
     */
    void pause();

    /**
     * seek到指定位置播放
     * @param pos
     */
    void seekTo(int pos);

    /**
     * 从上一次的位置继续播放
     * @param continueFromLastPosition
     */
    void continueFromLastPosition(boolean continueFromLastPosition);

    /*********************************
     * 以下9个方法是播放器在当前的播放状态
     **********************************/
    boolean isIdle();

    boolean isPreparing();

    boolean isPrepared();

    boolean isBufferingPlaying();

    boolean isBufferingPaused();

    boolean isPlaying();

    boolean isPaused();

    boolean isError();

    boolean isCompleted();

    /*********************************
     * 以下1个方法是播放器的模式
     **********************************/
    boolean isNormal();

    /**
     * 获取视频总时长
     * @return
     */
    int getDuration();

    /**
     * 获得视频当前播放的位置
     * @return
     */
    int getCurrentPosition();

    /**
     * 获取播放百分比
     * @return
     */
    int getPlayPercentage();

    /**
     * 获取缓存百分比
     * @return
     */
    int getBufferPercentage();

    /**
     * 释放播放器
     */
    void release();


}
