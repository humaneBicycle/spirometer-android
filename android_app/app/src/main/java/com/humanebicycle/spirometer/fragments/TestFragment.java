package com.humanebicycle.spirometer.fragments;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.humanebicycle.spirometer.AnalyticsActivity;
import com.humanebicycle.spirometer.MainActivity;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.Spirometer;
import com.humanebicycle.spirometer.helper.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.ui.RecorderWaveformView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class TestFragment extends Fragment {
    ImageButton startRecordingButton;
    ImageView micImage;
    Button viewResultButton;
    MediaRecorder mRecorder;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    boolean isRecording = false;
    String mFileName;
    ShapeableImageView wrongAudioButton, correctAudioButton;
    SpirometerTest test;
    RecorderWaveformView recorderWaveformView;


    public TestFragment() {
        // Required empty public constructor
    }

    public static TestFragment newInstance() {
        TestFragment fragment = new TestFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_test, container, false);

        startRecordingButton=view.findViewById(R.id.record_button);
        micImage = view.findViewById(R.id.record_audio_image);
        viewResultButton=view.findViewById(R.id.view_result);
        wrongAudioButton = view.findViewById(R.id.audio_wrong);
        correctAudioButton = view.findViewById(R.id.audio_correct);
        recorderWaveformView = view.findViewById(R.id.player_view_waveform);

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckPermissions() && !isRecording){
                    startRecording();
                }else if(isRecording){

                    pauseRecording();
                }else{

                    ActivityCompat.requestPermissions(getActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
                }
            }
        });

        viewResultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(test!=null){
                    String testString = XStreamSerializer.getInstance().serialize(test);
                    Intent intent = new Intent(getActivity(), AnalyticsActivity.class);
                    intent.putExtra("test",testString);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getActivity(), "Please record the audio first!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        wrongAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mRecorder!=null){
                    if(isRecording) {
                        mRecorder.stop();
                    }
                    mRecorder.release();
                    mRecorder=null;
                    isRecording=false;
                    test = null;
                    correctAudioButton.setVisibility(View.GONE);
                    wrongAudioButton.setVisibility(View.GONE);
                    startRecordingButton.setImageDrawable(getActivity().getDrawable(R.drawable.baseline_play_arrow_24));
                    recorderWaveformView.reset();
                    Toast.makeText(getContext(), "Recording is stopped!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        correctAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mRecorder!=null){
                    if(isRecording) {
                        mRecorder.stop();
                    }
                    startRecordingButton.setImageDrawable(getActivity().getDrawable(R.drawable.baseline_play_arrow_24));

                    isRecording=false;
                    showSaveBottomSheet();
                }
            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        //your method
                        animatePlayerView();
                    }
                }, 0, 100);//put here time 1000 milliseconds=1 second
            }
        }).run();

        return view;
    }

    public boolean CheckPermissions() {
        // this method is used to check permission
        int result = ContextCompat.checkSelfPermission(getContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // this method is called when user will
        // grant the permission for audio recording.
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION_CODE:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (permissionToRecord && permissionToStore) {
                        startRecording();
                    } else {
                        Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    private void startRecording(){
        micImage.setVisibility(View.GONE);
        isRecording=true;
        startRecordingButton.setImageDrawable(getActivity().getDrawable(R.drawable.baseline_pause_24));
        correctAudioButton.setVisibility(View.VISIBLE);
        wrongAudioButton.setVisibility(View.VISIBLE);
        recorderWaveformView.setVisibility(View.VISIBLE);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        long time = System.currentTimeMillis();

        mFileName = mFileName+"/"+String.valueOf(time)+".3gp";

        mRecorder = new MediaRecorder();

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        mRecorder.setOutputFile(mFileName);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
        mRecorder.start();


    }

    private void pauseRecording(){
        mRecorder.stop();
        isRecording=false;
        startRecordingButton.setImageDrawable(getActivity().getDrawable(R.drawable.baseline_play_arrow_24));
    }

    private void animatePlayerView(){
        if(isRecording) {
            try {
                int amplitude = mRecorder.getMaxAmplitude();
                recorderWaveformView.updateAmps(amplitude/10);

            }catch (Exception e){
                Log.e("abh", "isRecording reflected late. missed some seconds to plot waveform view" );
            }
        }
    }

    private void showSaveBottomSheet(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.save_audio_bottom_sheet);
        bottomSheetDialog.show();

        TextInputEditText nameET = bottomSheetDialog.findViewById(R.id.name_et_save_audio_bottom_sheet);
        Button positiveButton = bottomSheetDialog.findViewById(R.id.ok_button_save_bottom_sheet);
        Button negativeButton = bottomSheetDialog.findViewById(R.id.cancel_button_save_bottom_sheet);

        nameET.setSelected(true);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nameET.getText().toString().equals("")) {
                    Log.d("abh", "saving and adding audio to storgae: ");
                    test = new SpirometerTest(nameET.getText().toString(), System.currentTimeMillis(), mFileName, System.currentTimeMillis());
                    XStreamSerializer.getInstance().addTestToStorage(PreferenceManager.getDefaultSharedPreferences(getContext()), test);
                    Toast.makeText(getContext(), "Recording Saved!", Toast.LENGTH_SHORT).show();

                    correctAudioButton.setVisibility(View.GONE);
                    wrongAudioButton.setVisibility(View.GONE);
                    recorderWaveformView.reset();

                    bottomSheetDialog.cancel();
                }else{
                    Toast.makeText(getContext(), getResources().getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                }
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();
            }
        });
    }
}