package com.audiorecordingplay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AudioRecordingActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btnStopRecording)
    Button btnStopRecording;
    @BindView(R.id.btnRecordingStart)
    Button btnRecordingStart;
    @BindView(R.id.tvRecordingTime)
    TextView tvRecordingTime;
    private MediaRecorder mediaRecorder = new MediaRecorder();
    private String localAudioPath = AppUtils.createAudioFileName(), displayTime;
    private Handler timeHandler = new Handler();
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updatedTime = 0L;
    private long startHTime = 0L;
    private boolean isFromPause = false;
    private Runnable recordTimeRunnable = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (tvRecordingTime != null) {
                displayTime = "" + String.format("%02d", mins) + ":"
                        + String.format("%02d", secs);
                tvRecordingTime.setText(displayTime);
            }
            timeHandler.postDelayed(this, 0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recording);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        askRecordPermission();
    }

    private void askRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.isAnyPermissionPermanentlyDenied()) {
                                showSettingsDialog();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    })
                    .withErrorListener(new PermissionRequestErrorListener() {
                        @Override
                        public void onError(DexterError error) {
                            AppUtils.shortToast(AudioRecordingActivity.this, getString(R.string.try_again));
                        }
                    }).check();
        }
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_title);
        builder.setMessage(R.string.permission_desc);
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 309);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 309 && resultCode == RESULT_CANCELED) {
            askRecordPermission();
        }
    }

    @OnClick(R.id.btnRecordingStart)
    public void startRecording() {
        btnRecordingStart.setVisibility(View.GONE);
        startHTime = SystemClock.uptimeMillis();
        prepareMediaRecorder(localAudioPath);
        timeHandler.postDelayed(recordTimeRunnable, 0);
        btnStopRecording.setVisibility(View.VISIBLE);
    }

    private void prepareMediaRecorder(String fileLocation) {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(fileLocation);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    @OnClick(R.id.btnStopRecording)
    public void stopRecording() {
        mediaRecorder.stop();
        Intent audioPlay = new Intent(this, AudioPlayActivity.class);
        audioPlay.putExtra("Audio", localAudioPath);
        startActivity(audioPlay);
        timeHandler.removeCallbacks(recordTimeRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timeHandler.removeCallbacks(recordTimeRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFromPause) {
            isFromPause = false;
            btnRecordingStart.setVisibility(View.VISIBLE);
            btnStopRecording.setVisibility(View.GONE);
            tvRecordingTime.setText("");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFromPause = true;
    }
}
