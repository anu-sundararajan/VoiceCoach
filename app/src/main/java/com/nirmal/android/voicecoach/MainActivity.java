package com.nirmal.android.voicecoach;

import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*

http://www.java2s.com/Code/Android/Media/UsingAudioRecord.htm
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VoiceCoach";
    private static final CharSequence PITCHES[] = new CharSequence[] {
            "Shruti A# / -0.5",
            "Shruti B / 0",
            "Shruti C / 1",
            "Shruti C# / Db / 1.5",
            "Shruti D / 2",
            "Shruti D# / Eb / 2.5",
            "Shruti E / 3",
            "Shruti F / 4",
            "Shruti F# / Gb / 4.5",
            "Shruti G / 5",
            "Shruti G# / Ab / 5.5",
            "Shruti A / 6",
            "Shruti A# / 6.5",
            "Shruti B / 7"
    };

    Button startButton;
    Button stopButton;
    Button pitchButton;
    TextView mFrequencyTextView;
    PlotterView mFrequencyPlotterView;

    int sampleRateInHz = 44100; //11025;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isRecording = false;
    RecordAudio recordTask;
    int mPitch = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrequencyTextView = (TextView)this.findViewById(R.id.text_view_frequency);
        startButton = (Button)this.findViewById(R.id.StartButton);
        startButton.setOnClickListener(this);
        stopButton = (Button)this.findViewById(R.id.StopButton);
        stopButton.setOnClickListener(this);
        stopButton.setEnabled(false);
        pitchButton = (Button)this.findViewById(R.id.PitchButton);
        pitchButton.setOnClickListener(this);
        mFrequencyPlotterView = (PlotterView)this.findViewById(R.id.plotter_view_frequency);
    }

    public void onClick(View v) {
        if (v == startButton) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            recordTask = new RecordAudio();
            recordTask.execute();
        }
        if (v == stopButton) {
            isRecording = false;
        }
        if (v == pitchButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pitch");
            builder.setItems(PITCHES, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on pitch[which]
                    Log.i(TAG, "User selected pitch " + which);
                    mPitch = which;
                }
            });
            builder.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class RecordAudio extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Integer frequency = 0;
            isRecording = true;
            try {
                int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,
                        channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRateInHz,
                        channelConfiguration, audioEncoding, bufferSize);

                short[] buffer = new short[bufferSize];
                audioRecord.startRecording();

                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    frequency = calculateFrequency(sampleRateInHz, buffer);
                    publishProgress(frequency);
                }
                audioRecord.stop();
                audioRecord.release();
            } catch (Throwable t) {
                Log.e(TAG, "Recording Failed - ", t);
            }
            return null;
        }

        private int calculateFrequency(int sampleRate, short [] audioData){

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

        @Override
        protected void onProgressUpdate(Integer... frequencies) {
            mFrequencyTextView.setText(frequencies[0].toString());
            mFrequencyPlotterView.plotFrequency(frequencies[0]);
        }

        @Override
        protected void onPostExecute(Void result) {
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
}

