package com.audiorecordingplay;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AppUtils {

    public static String createAudioFileName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getCurrentTime() + ".3gp";
    }

    public static void shortToast(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    private static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ssSS");
        return simpleDateFormat.format(calendar.getTime());
    }

    public static String getAudioTime(int time) {
        long mins = TimeUnit.MILLISECONDS.toMinutes((long) time);
        long sec = TimeUnit.MILLISECONDS.toSeconds((long) time) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                        time));
        return mins + ":" + sec;
    }
}
