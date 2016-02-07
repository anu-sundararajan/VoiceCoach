package com.nirmal.android.voicecoach;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by AXS43 on 2/3/2016.
 */
public class AppData {
    private static final String RAGAM = "RAGAM";
    private static final String THALAM = "THALAM";
    private static final String PITCH = "PITCH";
    private static final String SEQUENCE = "SEQUENCE";


    public static int getRagam(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(RAGAM, 0);
    }

    public static int getThalam(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(THALAM, 1000);
    }

    public static int getPitch(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PITCH, 0);
    }

    public static int getSequence(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(SEQUENCE, 0);
    }

    public static void setRagam(Context context, int r) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(RAGAM, r)
                .apply();
    }

    public static void setThalam(Context context, int t) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(THALAM, t)
                .apply();
    }

    public static void setPitch(Context context, int p) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PITCH, p)
                .apply();
    }

    public static void setSequence(Context context, int s) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(SEQUENCE, s)
                .apply();
    }
}
