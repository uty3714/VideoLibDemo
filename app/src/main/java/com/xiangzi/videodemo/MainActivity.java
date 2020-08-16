package com.xiangzi.videodemo;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.xiangzi.video.controller.impl.XzVideoController;
import com.xiangzi.video.view.XzVideoPlayer;

public class MainActivity extends AppCompatActivity {


    private XzVideoPlayer mVideoPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoPlayer = findViewById(R.id.video_player);
        XzVideoController controller = new XzVideoController(this);
        controller.setOnPlayListener(new XzVideoController.OnPlayListener() {
            @Override
            public void play() {
                Log.i("TAG", "play: 开始播放");
            }

            @Override
            public void playProgress(int current, int total) {
                Log.i("TAG", "playProgress: 当前:"+current + " , 总共:"+ total);
            }

            @Override
            public void pause() {
                Log.i("TAG", "pause: 暂停播放");
            }

            @Override
            public void error() {
                Log.i("TAG", "error: 播放错误");
            }

            @Override
            public void playComplete() {
                Log.i("TAG", "playComplete: 播放完成");
            }

        });
        controller.setUrl("https://vdse.bdstatic.com/9712394c6705f0b81841b28590061766.mp4");
//        controller.setUrl("https://aweme.snssdk.com/aweme/v1/playwm/?video_id=v0200f490000bsbv2r9imdr83n6ff7b0&ratio=720p&line=0");
        mVideoPlayer.setController(controller);
        mVideoPlayer.start();

    }


    @Override
    protected void onPause() {
        super.onPause();
        mVideoPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoPlayer.restart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayer.release();
    }
}