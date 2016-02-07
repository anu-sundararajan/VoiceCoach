package com.nirmal.android.voicecoach;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

    final Context context = this;

    Button mStartStopButton;
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

    MenuItem mRagamItem;
    MenuItem mPitchItem;
    MenuItem mSeqItem;

    int sampleRateInHz = 44100; //11025;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    boolean mIsRecording = false;
    RecordAudio recordTask;
    List<Ragam> mRagamList = new ArrayList<>();
    List<String> mRagamNames = new ArrayList<>();
    int mNumSamples = 1;

    private void populateRagams() {
        mRagamList.add(new Ragam("Mayamalava Gowla", new boolean[] {true, true, false, false, true, true, false, true, true, false, false, true, true}, new boolean[] {true, true, false, false, true, true, false, true, true, false, false, true, true}));
        mRagamList.add(new Ragam("Sankarabharanam",  new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true, true}, new boolean[] {true, false, true, false, true, true, false, true, false, true, false, true, true}));
        mRagamList.add(new Ragam("Kalyani",          new boolean[] {true, false, true, false, true, false, true, true, false, true, false, true, true}, new boolean[] {true, false, true, false, true, false, true, true, false, true, false, true, true}));

        for (Ragam ragam : mRagamList) {
            mRagamNames.add(ragam.getName());
        }
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setIcon(R.mipmap.ic_launcher);
            Drawable d = ContextCompat.getDrawable(this, R.drawable.light_gold);
            actionBar.setBackgroundDrawable(d);
            actionBar.setTitle("  " + "Voice Coach");
        }
    }

    private void initViewElements() {
        mFrequencyTextView = (TextView)this.findViewById(R.id.text_view_frequency);
        mStartStopButton = (Button)this.findViewById(R.id.StartStopButton);
        mStartStopButton.setOnClickListener(this);
        mThalamEditText = (EditText)this.findViewById(R.id.ThalamEditText);

        mThalamEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i(TAG, "on editor action " + actionId + "  " + event);
                boolean handled = false;
                if ((actionId == KeyEvent.KEYCODE_ENTER)||
                (actionId == KeyEvent.KEYCODE_ENDCALL))
                {
                    int ms_per_beat = 1000;
                    try {
                        ms_per_beat = Integer.parseInt(mThalamEditText.getText().toString());
                    } catch (Exception e) {
                        ms_per_beat = 1000;
                        Log.e(TAG, "NumberFormatException  - MS PER BEAT");
                        mThalamEditText.setText("1000");
                    }
                    if ((ms_per_beat < 1000) || (ms_per_beat > 8000)) {
                        Toast.makeText(getApplicationContext(), "Thalam Range: 1000 to 8000 ms", Toast.LENGTH_SHORT).show();
                        if (ms_per_beat < 1000) ms_per_beat = 1000;
                        if (ms_per_beat > 8000) ms_per_beat = 8000;
                        mThalamEditText.setText(Integer.toString(ms_per_beat));
                        handled = true;
                    }
                }
                return handled;

            }
        });

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActionBar();
        initViewElements();

        mNumSamples = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfiguration, audioEncoding);
        Log.i(TAG, "mNumSamples = " + mNumSamples);

        populateRagams();

        mIsRecording = false;

        if (AppData.getSequence(context) == 0) {
            mFrequencyPlotterView.drawReferenceLine(0);
        } else {
            mFrequencyPlotterView.drawAllReferenceLines(mRagamList.get(AppData.getRagam(this)).getArohanam());
        }
    }

    public void onClick(View v) {
        if (v == mStartStopButton) {
            mIsRecording = !mIsRecording;
            setEnabled_ButtonsAndMenu(!mIsRecording);
            if (mIsRecording)
                startRecording();
            else stopRecording();
            return;
        }

        Ragam r = mRagamList.get(AppData.getRagam(this));
        if (v == mLSaButton) {
            mFrequencyPlotterView.drawReferenceLine(0);
        }
        if (v == mRiButton) {
            mFrequencyPlotterView.drawReferenceLine(r.getRiIndex());
        }
        if (v == mGaButton) {
            mFrequencyPlotterView.drawReferenceLine(r.getGaIndex());
        }
        if (v == mMaButton) {
            mFrequencyPlotterView.drawReferenceLine(r.getMaIndex());
        }
        if (v == mPaButton) {
            mFrequencyPlotterView.drawReferenceLine(r.getPaIndex());
        }
        if (v == mDaButton) {
            mFrequencyPlotterView.drawReferenceLine(r.getDaIndex());
        }
        if (v == mNiButton) {
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
        mPitchItem = menu.findItem(R.id.menu_item_pitch);
        mPitchItem.setTitle(PITCHES[AppData.getPitch(this)]);
        mRagamItem = menu.findItem(R.id.menu_item_ragam);
        mRagamItem.setTitle(mRagamNames.get(AppData.getRagam(this)));
        mSeqItem = menu.findItem(R.id.menu_item_seq);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        mRagamItem.setEnabled(!mIsRecording);
        mPitchItem.setEnabled(!mIsRecording);
        mSeqItem.setEnabled(!mIsRecording);
        super.onPrepareOptionsMenu(menu);
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
            Log.i(TAG, "Settings Pressed");
            return true;
        }

        if (id == R.id.menu_item_pitch) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pitch");
            builder.setItems(PITCHES, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AppData.setPitch(context, which);
                    mFrequencyPlotterView.setPitch(which);
                    invalidateOptionsMenu();
                }
            });
            builder.show();
            return true;
        }

        if (id == R.id.menu_item_ragam) {
            final CharSequence[] ragamNames = mRagamNames.toArray(new CharSequence[mRagamNames.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Ragam");
            builder.setItems(ragamNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on pitch[which]
                    Log.i(TAG, "User selected ragam " + which);
                    AppData.setRagam(context, which);
                    mFrequencyPlotterView.drawReferenceLine(0);
                    invalidateOptionsMenu();
                }
            });
            builder.show();
            return true;
        }

        if (id == R.id.menu_item_seq) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Sequence");
            builder.setItems(SEQ_OPTIONS, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (AppData.getSequence(context) != which) {
                        AppData.setSequence(context, which);
                        if (which == 0) {
                            mFrequencyPlotterView.drawReferenceLine(0);
                        } else {
                            Ragam r = mRagamList.get(AppData.getRagam(context));
                            mFrequencyPlotterView.drawAllReferenceLines(r.getArohanam());
                        }
                    }
                }
            });
            builder.show();
            return true;
        }

        if (id == R.id.menu_item_thalam) {
            Log.i(TAG, "Thalam Pressed");

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            toggleActionBar();
        }
        return true;
    }

    private void toggleActionBar() {
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            if(actionBar.isShowing()) {
                actionBar.hide();
            }
            else {
                actionBar.show();
            }
        }
    }

    private void startRecording() {
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
        if (AppData.getSequence(context) != 0) {
            int sampletime = (1000 * mNumSamples) / sampleRateInHz;
            int swarakalam = ms_per_beat / sampletime;
            Log.i(TAG, "ms_per_beat = " + ms_per_beat + "mNumSamples = " + mNumSamples + " Swarakalam = " + swarakalam);
            mFrequencyPlotterView.startReferenceStair(AppData.getSequence(context), swarakalam);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mStartStopButton.setText("Stop");
    }

    private void stopRecording() {
        mFrequencyPlotterView.stopReferenceStair();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStartStopButton.setText("Start");
    }

    private void setEnabled_ButtonsAndMenu(boolean enable) {
        mThalamEditText.setEnabled(enable);
        invalidateOptionsMenu();
    }

    private class RecordAudio extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Integer frequency = 0;
            try {
                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, sampleRateInHz,
                        channelConfiguration, audioEncoding, mNumSamples);

                FFT fft = new FFT();

                short[] buffer = new short[mNumSamples];
                audioRecord.startRecording();

                while (mIsRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, mNumSamples);
                    //frequency = FrequencyDetector_ZeroCrossing.calculateFrequency(sampleRateInHz, buffer);
                    frequency = fft.calculateFrequency(sampleRateInHz, buffer);
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
        protected void onPostExecute(Void result) {}

    }
}

