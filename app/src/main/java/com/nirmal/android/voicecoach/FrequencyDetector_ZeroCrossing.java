package com.nirmal.android.voicecoach;

/**
 * Created by AXS43 on 1/27/2016.
 */
public class FrequencyDetector_ZeroCrossing {

    public static int calculateFrequency(int sampleRate, short [] audioData){

        int zero_offset = 0;
        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples-1; p++)
        {
            if ((audioData[p] > zero_offset && audioData[p + 1] <= zero_offset) ||
                    (audioData[p] < zero_offset && audioData[p + 1] >= zero_offset))
            {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
        float numCycles = numCrossing/2;
        float frequency = numCycles/numSecondsRecorded;

        return (int)frequency;
    }
}
