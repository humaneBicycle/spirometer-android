package com.humanebicycle.spirometer.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.helper.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PreviewAudioFragment extends Fragment {

    public static final String TEST = "preview_audio_test";
    SeekBar previewAudioSeekbar;
    ImageButton playPauseButton;
    boolean isPlaying=false;
    MediaPlayer mMediaPlayer;
    SpirometerTest test;
    TextView timeTextView, updatingTimeTextView;
    Timer seekbarTimer;
    TimerTask seekbarTimerTask;
    Thread updateTimeThread;

    public PreviewAudioFragment() {
        // Required empty public constructor
    }

    public static PreviewAudioFragment newInstance(SpirometerTest test) {
        PreviewAudioFragment fragment = new PreviewAudioFragment();
        Bundle args = new Bundle();
        args.putString(TEST, XStreamSerializer.getInstance().serialize(test));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            test  = (SpirometerTest) XStreamSerializer.getInstance().deSerialize(getArguments().getString(TEST));
        }
        Log.d("abh", "onCreate: "+test.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_audio, container, false);

        playPauseButton=view.findViewById(R.id.play_pause_preview_audio_fragment_button);
        previewAudioSeekbar = view.findViewById(R.id.preview_audio_seekbar);
        timeTextView = view.findViewById(R.id.seekbar_progress_tv);
        updatingTimeTextView = view.findViewById(R.id.seekbar_progress_tv_updating);

        if(mMediaPlayer==null){
            mMediaPlayer = MediaPlayer.create(getContext(), Uri.parse(test.getAudioAddress()));
            previewAudioSeekbar.setMax(mMediaPlayer.getDuration());

            int maxDuration = mMediaPlayer.getDuration();
            timeTextView.setText(formatMilliSecond(maxDuration));

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_play_arrow_24));

                }
            });
        }

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isPlaying){
                    isPlaying = true;
                    mMediaPlayer.start();
                    playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_pause_24));
                }else{
                    pauseMediaPlayer();
                }
            }
        });

        previewAudioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    seekBar.setProgress(i);
                    mMediaPlayer.seekTo(i);
                    updatingTimeTextView.setText(formatMilliSecond(i));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        seekbarTimerTask=null;
        seekbarTimerTask = new TimerTask() {
            @Override
            public void run() {
                if(mMediaPlayer!=null && isPlaying){
                    int pos = mMediaPlayer.getCurrentPosition();
                    previewAudioSeekbar.setProgress(pos);
                    Runnable updateCurrentUI = new Runnable() {
                        @Override
                        public void run() {
                            updatingTimeTextView.setText(formatMilliSecond(pos));
                        }
                    };
                    getActivity().runOnUiThread(updateCurrentUI);

                }

            }
        };

        updateTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                seekbarTimer = new Timer();
                seekbarTimer.scheduleAtFixedRate(seekbarTimerTask, 0, 1000);//put here time 1000 milliseconds=1 second
            }
        });
        updateTimeThread.run();

    }

    @Override
    public void onPause() {
        super.onPause();
        seekbarTimer.cancel();
        pauseMediaPlayer();
        updateTimeThread=null;
    }

    void pauseMediaPlayer(){
        isPlaying=false;
        mMediaPlayer.pause();
        playPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.baseline_play_arrow_24));
    }

    public static String formatMilliSecond(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }
}