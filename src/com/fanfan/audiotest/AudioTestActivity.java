package com.fanfan.audiotest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import java.util.Random;

public class AudioTestActivity extends Activity
{
    private static final String TAG = "AudioTestActivity";
    private static final int SAMPLE_RATE_RECORD   = 44100;
    private static final int SAMPLE_RATE_PLAYBACK = 44100;
    private static final int MSG_UPDATE_WAVEFORM_VIEW = 1;

    private AudioRecord    mRecorder;
    private int            mMinBufSizeR;
    private RecordThread   mRecThread;

    private AudioTrack     mAudioTrack;
    private int            mMinBufSizeP;
    private PlaybackThread mPlayThread;

    private Button         mBtnStartStopRec;
    private Button         mBtnStartStopPlay;
    private Button         mBtnRecStartNoise;
    private WaveformView   mWaveformView;
    private Random         mRandom = new Random();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBtnStartStopRec  = (Button)findViewById(R.id.btn_startstop_record  );
        mBtnStartStopPlay = (Button)findViewById(R.id.btn_startstop_playback);
        mBtnRecStartNoise = (Button)findViewById(R.id.btn_test_record_start_noise);
        mWaveformView     = (WaveformView)findViewById(R.id.waveform_view   );
        mBtnStartStopRec .setOnClickListener(mOnClickListener);
        mBtnStartStopPlay.setOnClickListener(mOnClickListener);
        mBtnRecStartNoise.setOnClickListener(mOnClickListener);

        mMinBufSizeR = AudioRecord.getMinBufferSize(SAMPLE_RATE_RECORD,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        mRecorder    = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_RECORD,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            mMinBufSizeR * 2);

        mMinBufSizeP = AudioTrack.getMinBufferSize(SAMPLE_RATE_PLAYBACK,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack  = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_PLAYBACK,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            mMinBufSizeP * 2, AudioTrack.MODE_STREAM);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        mRecorder.stop();
        mRecorder.release();

        mAudioTrack.stop();
        mAudioTrack.release();

        super.onDestroy();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_startstop_record:
                if (mRecThread == null) {
                    mRecThread = new RecordThread();
                    mRecThread.start();
                    mBtnStartStopRec.setText(R.string.btn_stop_record);
                }
                else {
                    mRecThread.stopRecord();
                    mRecThread = null;
                    mBtnStartStopRec.setText(R.string.btn_start_record);
                }
                break;
            case R.id.btn_startstop_playback:
                if (mPlayThread == null) {
                    mPlayThread = new PlaybackThread();
                    mPlayThread.start();
                    mBtnStartStopPlay.setText(R.string.btn_stop_playback);
                }
                else {
                    mPlayThread.stopPlay();
                    mPlayThread = null;
                    mBtnStartStopPlay.setText(R.string.btn_start_playback);
                }
                break;
            case R.id.btn_test_record_start_noise:
                if (mRecThread == null) {
                    mRecThread = new RecordThread();
                    mRecThread.recordStartNoise();
                    try { mRecThread.join(); } catch (Exception e) {}
                    mRecThread = null;
                }
                break;
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_WAVEFORM_VIEW:
                mWaveformView.invalidate();
                break;
            }
        }
    };

    class RecordThread extends Thread
    {
        private boolean isRecording    = false;
        private boolean bRecStartNoise = false;

        @Override
        public void run() {
            isRecording = true;
            mRecorder.startRecording();

            while (isRecording) {
                int sampnum = mMinBufSizeR / 2;
                int offset  = 0;
                short[] buf = new short[sampnum];
                while (offset != sampnum) {
                    offset += mRecorder.read(buf, offset, sampnum);
                    Log.d(TAG, "read record data offset = " + offset);
                }
                mWaveformView.setAudioData(buf);
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_UPDATE_WAVEFORM_VIEW;
                mHandler.sendMessage(msg);
                if (bRecStartNoise) {
                    bRecStartNoise = false;
                    break;
                }
            }

            mRecorder.stop();
        }

        public void stopRecord() {
            isRecording = false;
        }

        public void recordStartNoise() {
            bRecStartNoise = true;
            start();
        }
    }

    class PlaybackThread extends Thread
    {
        private boolean isPlaying = false;

        @Override
        public void run() {
            isPlaying = true;
            mAudioTrack.play();

            while (isPlaying) {
                int sampnum = mMinBufSizeP / 2;
                int offset  = 0;
                short[] buf = new short[sampnum];
                for (int i=0; i<sampnum; i++) {
                    buf[i] = (short)mRandom.nextInt();
                }
                while (offset != sampnum) {
                    offset += mAudioTrack.write(buf, offset, sampnum);
                    Log.d(TAG, "write playback data offset = " + offset);
                }
            }

            mAudioTrack.stop();
        }

        public void stopPlay() {
            isPlaying = false;
        }
    }
}



