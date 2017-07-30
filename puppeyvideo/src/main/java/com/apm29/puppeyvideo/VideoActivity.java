package com.apm29.puppeyvideo;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apm29.puppeyvideo.utils.CommonUtils;
import com.apm29.puppeyvideo.utils.T;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.VideoView;
import master.flame.danmaku.ui.widget.DanmakuView;

public class VideoActivity extends AppCompatActivity {

    @InjectView(R.id.tv_uuid)
    TextView tvUuid;
    @InjectView(R.id.surface_view)
    VideoView mVideoView;
    @InjectView(R.id.sv_danmaku)
    DanmakuView mDanmakuView;
    @InjectView(R.id.loading_image)
    ImageView mLoadingImage;
    @InjectView(R.id.loading_tv)
    TextView loadingTv;
    @InjectView(R.id.loading_LinearLayout)
    LinearLayout mLoadingLayout;
    @InjectView(R.id.edt_url)
    EditText edtUrl;
    @InjectView(R.id.btn_watch)
    Button btnWatch;
    @InjectView(R.id.iv_help)
    ImageView ivHelp;
    @InjectView(R.id.rl_controller)
    RelativeLayout rlController;
    private String mVideoPath;
    private CustomMediaController mediaController;

    private ObjectAnimator mOjectAnimator;
    private long currentPosition = 0;
    private boolean needResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.inject(this);
        tvUuid.setText(String.format("您的设备唯一标志:%s", CommonUtils.getMyUUID(this)));
    }


    @OnClick({R.id.btn_watch, R.id.surface_view, R.id.iv_help, R.id.rl_video})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_watch:
                getUrl();
                prepareVideo();
                preparePlayVideo();
                break;
            case R.id.rl_video:
                if (mVideoPath==null)return;
                Intent intent = new Intent(this, VideoFullScreenActivity.class);
                intent.putExtra(VideoFullScreenActivity.VIDEO_PATH,mVideoPath);
                startActivity(intent);
                boolean playing = mVideoView.isPlaying();
                if (playing)
                    stopPlay();
                else
                    startPlay();
                break;
            case R.id.iv_help:
                Toast.makeText(this, "help me", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void prepareVideo() {
        mVideoView.requestFocus();
        mVideoView.setBufferSize(1024 * 1024);
        mVideoView.setVideoChroma(MediaPlayer.VIDEOCHROMA_RGB565);
        mVideoView.setMediaController(null);
        mVideoView.setVideoPath(mVideoPath);
    }

    private void getUrl() {
        //url
        mVideoPath = edtUrl.getText().toString().trim();
    }

/*
    public Bitmap getCurrentFrame() {
        if (mVideoView != null) {
            MediaPlayer mediaPlayer = mVideoView.getmMediaPlayer();
            return mediaPlayer.getCurrentFrame();
        }
        return null;
    }

    public void reverseVideo() {
        if (mVideoView != null) {
            long duration = mVideoView.getDuration();
            long currentPosition = mVideoView.getCurrentPosition();
            long goalduration = currentPosition - duration / 10;
            if (goalduration <= 0) {
                mVideoView.seekTo(0);
            } else {
                mVideoView.seekTo(goalduration);
            }
            T.showToastMsgShort(this, StringUtils.generateTime(goalduration));
        }
    }

    public void speedVideo() {
        if (mVideoView != null) {
            long duration = mVideoView.getDuration();
            long currentPosition = mVideoView.getCurrentPosition();
            long goalduration = currentPosition + duration / 10;
            if (goalduration >= duration) {
                mVideoView.seekTo(duration);
            } else {
                mVideoView.seekTo(goalduration);
            }
            T.showToastMsgShort(this, StringUtils.generateTime(goalduration));
        }
    }

    public void setVideoPageSize(int mCurrentPageSize) {
        if (mVideoView != null) {
            mVideoView.setVideoLayout(mCurrentPageSize, 0);
        }
    }
*/

    @NonNull
    private void startLoadingAnimator() {
        if (mOjectAnimator == null) {
            mOjectAnimator = ObjectAnimator.ofFloat(mLoadingImage, "rotation", 0f, 360f);
        }
        mLoadingLayout.setVisibility(View.VISIBLE);

        mOjectAnimator.setDuration(1000);
        mOjectAnimator.setRepeatCount(-1);
        mOjectAnimator.start();
    }

    private void stopLoadingAnimator() {
        mLoadingLayout.setVisibility(View.GONE);
        mOjectAnimator.cancel();
    }

    private void startPlay() {
        mVideoView.start();
    }

    private void stopPlay() {
        mVideoView.pause();
    }

    public void onPause() {
        super.onPause();
        currentPosition = mVideoView.getCurrentPosition();
        mVideoView.pause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPosition = mVideoView.getCurrentPosition();
        mVideoView.pause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    private void preparePlayVideo() {
        startLoadingAnimator();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // TODO Auto-generated method stub
                stopLoadingAnimator();

                if (currentPosition > 0) {
                    mVideoView.seekTo(currentPosition);
                } else {
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
                startPlay();
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
                switch (arg1) {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        //开始缓存，暂停播放
                        startLoadingAnimator();
                        if (mVideoView.isPlaying()) {
                            stopPlay();
                            needResume = true;
                        }
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        //缓存完成，继续播放
                        stopLoadingAnimator();
                        if (needResume) startPlay();
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED:
                        //显示 下载速度
                        Log.i("Speed", "download rate:" + arg2);
                        break;
                }
                return true;
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
//                LogUtils.i(LogUtils.LOG_TAG, "what=" + what);
                return false;
            }
        });
        mVideoView.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
            }
        });
        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Log.i("Percent", "percent" + percent);
            }
        });
    }
}
