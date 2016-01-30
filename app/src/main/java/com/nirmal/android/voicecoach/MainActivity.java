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
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*

http://www.java2s.com/Code/Android/Media/UsingAudioRecord.htm
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Voice_Activity";
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

    private static final CharSequence SEQ_OPTIONS[] = new CharSequence[] {
            "Off",
            "Sa Pa Sa",
            "Arohanam",
            "Avarohanam"
    };

    /*
    private Ragam[] mRagams = new Ragam[] {
            new Ragam("Mayamalava Gowla", new boolean[] {true, true, false, false, true, true, false, true, true, false, false, true}, new boolean[] {true, true, false, false, true, true, false, true, true, false, false, true}),
            new Ragam("Sankarabharanam",  new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true}, new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true}),
            new Ragam("Kalyani",          new boolean[] {true, false, true, false, true, false, true, true, false, true, false, true}, new boolean[] {true, false, true, false, true, false, true, true, false, true, false, true})
    };
*/
    Button startButton;
    Button stopButton;
    Button pitchButton;
    Button ragamButton;
    Button seqButton;
    EditText mThalamEditText;
    TextView mFrequencyTextView;
    PlotterView mFrequencyPlotterView;

    Button mLSaButton;
    Button mRiButton;
    Button mGaButton;
    Button mMaButton;
    Button mPaButton;
    Button mDaButton;
    Button mNiButton;
    Button mHSaButton;

    int sampleRateInHz = 44100; //11025;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean isRecording = false;
    RecordAudio recordTask;
    List<Ragam> mRagamList = new ArrayList<>();
    List<String> mRagamNames = new ArrayList<>();
    int mRagamIndex = 0;
    int mSequenceMode = 0;
    int mNumSamples = 1;

    private void populateRagams() {
        mRagamList.add(new Ragam("Mayamalava Gowla", new boolean[] {true, true, false, false, true, true, false, true, true, false, false, true, true}, new boolean[] {true, true, false, false, true, true, false, true, true, false, false, true, true}));
        mRagamList.add(new Ragam("Sankarabharanam",  new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true, true}, new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true, true}));
        mRagamList.add(new Ragam("Kalyani",          new boolean[] {true, false, true, false, true, false, true, true, false, true, false, true, true}, new boolean[] {true, false, true, false, true, false, true, true, false, true, false, true, true}));

        for (Ragam ragam : mRagamList) {
            mRagamNames.add(ragam.getName());
        }
    }

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
        ragamButton = (Button)this.findViewById(R.id.RagamButton);
        ragamButton.setOnClickListener(this);
        seqButton = (Button)this.findViewById(R.id.SeqButton);
        seqButton.setOnClickListener(this);
        mThalamEditText = (EditText)this.findViewById(R.id.ThalamEditText);

        mLSaButton = (Button)this.findViewById(R.id.LSaButton);
        mLSaButton.setOnClickListener(this);
        mRiButton = (Button)this.findViewById(R.id.RiButton);
        mRiButton.setOnClickListener(this);
        mGaButton = (Button)this.findViewById(R.id.GaButton);
        mGaButton.setOnClickListener(this);
        mMaButton = (Button)this.findViewById(R.id.MaButton);
        mMaButton.setOnClickListener(this);
        mPaButton = (Button)this.findViewById(R.id.PaButton);
        mPaButton.setOnClickListener(this);
        mDaButton = (Button)this.findViewById(R.id.DaButton);
        mDaButton.setOnClickListener(this);
        mNiButton = (Button)this.findViewById(R.id.NiButton);
        mNiButton.setOnClickListener(this);
        mHSaButton = (Button)this.findViewById(R.id.HSaButton);
        mHSaButton.setOnClickListener(this);


        mFrequencyPlotterView = (PlotterView)this.findViewById(R.id.plotter_view_frequency);
        mNumSamples = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfiguration, audioEncoding);

        populateRagams();

        if (mSequenceMode == 0) {
            mFrequencyPlotterView.drawReferenceLine(0);
        } else {
            mFrequencyPlotterView.drawAllReferenceLines(mRagamList.get(mRagamIndex).getArohanam());
        }

    }

    public void onClick(View v) {
        if (v == startButton) {
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            ragamButton.setEnabled(false);
            pitchButton.setEnabled(false);
            seqButton.setEnabled(false);
            mThalamEditText.setEnabled(false);
            int ms_per_beat = 1000;
            try {
                ms_per_beat = Integer.parseInt(mThalamEditText.getText().toString());
            } catch (Exception e) {
                ms_per_beat = 1000;
                Log.e(TAG, "NumberFormatException  - MS PER BEAT");
                mThalamEditText.setText("1000");
            }
            recordTask = new RecordAudio();
            recordTask.execute();
            if (mSequenceMode != 0) {
                int sampletime = (1000 * mNumSamples) / sampleRateInHz;
                int swarakalam = ms_per_beat / sampletime;
                Log.i(TAG, "ms_per_beat = " + ms_per_beat + "mNumSamples = " + mNumSamples + " Swarakalam = " + swarakalam);
                        mFrequencyPlotterView.startReferenceStair(mSequenceMode, swarakalam);
            }
        }
        if (v == stopButton) {
            ragamButton.setEnabled(true);
            pitchButton.setEnabled(true);
            seqButton.setEnabled(true);
            mThalamEditText.setEnabled(true);
            isRecording = false;
            mFrequencyPlotterView.stopReferenceStair();
        }
        if (v == pitchButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pitch");
            builder.setItems(PITCHES, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mFrequencyPlotterView.setPitch(which);
                }
            });
            builder.show();
        }
        if (v == ragamButton) {

            final CharSequence[] ragamNames = mRagamNames.toArray(new CharSequence[mRagamNames.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ragam");
            builder.setItems(ragamNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on pitch[which]
                    Log.i(TAG, "User selected ragam " + which);
                    mRagamIndex = which;
                    mFrequencyPlotterView.drawReferenceLine(0);
                }
            });
            builder.show();
        }
        if (v == seqButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Sequence");
            builder.setItems(SEQ_OPTIONS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mSequenceMode != which) {
                        mSequenceMode = which;
                        if (mSequenceMode == 0) {
                            mFrequencyPlotterView.drawReferenceLine(0);
                        } else {
                            Ragam r = mRagamList.get(mRagamIndex);
                            mFrequencyPlotterView.drawAllReferenceLines(r.getArohanam());
                        }
                    }
                }
            });
            builder.show();
        }

        if (v == mLSaButton) {
            mFrequencyPlotterView.drawReferenceLine(0);
        }
        if (v == mRiButton) {
            Ragam r = mRagamList.get(mRagamIndex);
            mFrequencyPlotterView.drawReferenceLine(r.getRiIndex());
        }
        if (v == mGaButton) {
            Ragam r = mRagamList.get(mRagamIndex);
            mFrequencyPlotterView.drawReferenceLine(r.getGaIndex());
        }
        if (v == mMaButton) {
            Ragam r = mRagamList.get(mRagamIndex);
            mFrequencyPlotterView.drawReferenceLine(r.getMaIndex());
        }
        if (v == mPaButton) {
            Ragam r = mRagamList.get(mRagamIndex);
            mFrequencyPlotterView.drawReferenceLine(r.getPaIndex());
        }
        if (v == mDaButton) {
            Ragam r = mRagamList.get(mRagamIndex);
            mFrequencyPlotterView.drawReferenceLine(r.getDaIndex());
        }
        if (v == mNiButton) {
            Ragam r = mRagamList.get(mRagamIndex);
            mFrequencyPlotterView.drawReferenceLine(r.getNiIndex());
        }
        if (v == mHSaButton) {
            mFrequencyPlotterView.drawReferenceLine(Ragam.MAX_NOTES-1);
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
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRateInHz,
                        channelConfiguration, audioEncoding, mNumSamples);

                short[] buffer = new short[mNumSamples];
                audioRecord.startRecording();

                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, mNumSamples);
                    frequency = FrequencyDetector_ZeroCrossing.calculateFrequency(sampleRateInHz, buffer);
                    //Log.i(TAG, "numsamples = " + buffer.length);
                    publishProgress(frequency);
                }
                audioRecord.stop();
                audioRecord.release();
            } catch (Throwable t) {
                Log.e(TAG, "Recording Failed - ", t);
            }
            return null;
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

