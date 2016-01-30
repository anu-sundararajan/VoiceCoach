package com.nirmal.android.voicecoach;

import android.util.Log;

/**
 * Created by AXS43 on 1/27/2016.
 */
public class Ragam {
    private static final String TAG = "Voice_Ragam";
    public static final int MAX_NOTES = 13;
    private String mName;
    private boolean[] mArohanam;
    private boolean[] mAvarohanam;

    public Ragam(String name, boolean arohanam[], boolean avarohanam[]) {
        mName = name;
        mArohanam = arohanam;
        mAvarohanam = avarohanam;
    }

    public String getName() {
        return mName;
    }

    public boolean[] getArohanam() {
        return mArohanam;
    }

    public boolean[] getAvarohanam() {
        return mAvarohanam;
    }

    public int getRiIndex() {
        if (mArohanam[1])
            return 1;
        else return 2;
    }

    public int getGaIndex() {
        if (mArohanam[3])
            return 3;
        else return 4;
    }

    public int getMaIndex() {
        if (mArohanam[5])
            return 5;
        else return 6;
    }

    public int getPaIndex() {
        return 7;
    }

    public int getDaIndex() {
        if (mArohanam[8])
            return 8;
        else return 9;
    }

    public int getNiIndex() {
        if (mArohanam[10])
            return 10;
        else return 11;
    }
}
