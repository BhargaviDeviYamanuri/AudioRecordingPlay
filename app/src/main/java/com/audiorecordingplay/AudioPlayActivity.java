package com.audiorecordingplay;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AudioPlayActivity extends AppCompatActivity {
    @BindView(R.id.sbAudioStream)
    SeekBar sbAudioStream;
    @BindView(R.id.tvAudioDuration)
    TextView tvAudioDuration;
    @BindView(R.id.tvAudioPlayTime)
    TextView tvAudioPlayTime;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private String audioPath;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Handler playHandler = new Handler();
    private Runnable playTimeRunnable = new Runnable() {
        @Override
        public void run() {
            tvAudioPlayTime.setText(AppUtils.getAudioTime(mediaPlayer.getCurrentPosition()));
            sbAudioStream.setProgress(mediaPlayer.getCurrentPosition());
            playHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        audioPath = getIntent().getStringExtra("Audio");
        prepareAudio(audioPath);
        sbAudioStream.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void prepareAudio(String audioPath) {
        try {
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            sbAudioStream.setMax(mediaPlayer.getDuration());
            tvAudioDuration.setText(AppUtils.getAudioTime(mediaPlayer.getDuration()));
            tvAudioPlayTime.setText("0:00");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btnPlay)
    public void playAudio() {
        mediaPlayer.start();
        playHandler.postDelayed(playTimeRunnable, 0);
    }

    @OnClick(R.id.btnPause)
    public void pauseAudio() {
        mediaPlayer.pause();
        playHandler.removeCallbacks(playTimeRunnable);
    }
}
