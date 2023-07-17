package com.humanebicycle.spirometer.fragments;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.humanebicycle.spirometer.AnalyticsActivity;
import com.humanebicycle.spirometer.Constants;
import com.humanebicycle.spirometer.OnOrientationChangeListener;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.TestManager;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.Acceleration;
import com.humanebicycle.spirometer.view.GyroscopeView;
import com.humanebicycle.spirometer.view.RecorderWaveformView;

import java.util.Timer;
import java.util.TimerTask;

public class TestFragment extends Fragment implements OnOrientationChangeListener {
    Button startRecordingButton;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    ShapeableImageView wrongAudioButton, correctAudioButton;
    RecorderWaveformView recorderWaveformView;
    TestManager mTestManager;

    @Override
    public void onDetach() {
        super.onDetach();
        //remove all listeners, avoid memory leak
        mTestManager = TestManager.getInstance(this);
        mTestManager.unRegisterAccelerometer();

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //attack all the listeners
        mTestManager = TestManager.getInstance(this);
        mTestManager.registerAccelerometer(getActivity());

    }

    TextView degreeTextView;
    GyroscopeView gyroscopeView;

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
        wrongAudioButton = view.findViewById(R.id.audio_wrong);
        correctAudioButton = view.findViewById(R.id.audio_correct);
        recorderWaveformView = view.findViewById(R.id.player_view_waveform);
        degreeTextView = view.findViewById(R.id.degree_text_view);
        gyroscopeView = view.findViewById(R.id.gyroscope_view);

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CheckPermissions()){
                    ActivityCompat.requestPermissions(getActivity(), new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
                    return;
                }

                if(mTestManager.getCurrentState()== TestManager.State.NOT_INITIALISED){
                    mTestManager.createTest(System.currentTimeMillis());
                }

                Log.d("abh", "onstart state:: "+mTestManager.getCurrentState());
                if(mTestManager.getCurrentState()== TestManager.State.PAUSED || mTestManager.getCurrentState()== TestManager.State.INITIALISED){
                    startRecording();
                    return;
                }

                if(mTestManager.getCurrentState()== TestManager.State.RECORDING){
                    pauseRecording();
                    return;
                }
            }
        });

        wrongAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTestManager.getCurrentState()== TestManager.State.RECORDING || mTestManager.getCurrentState()== TestManager.State.PAUSED){


                    mTestManager.discardTest();

                    correctAudioButton.setVisibility(View.GONE);
                    wrongAudioButton.setVisibility(View.GONE);
                    recorderWaveformView.setVisibility(View.GONE);

                    startRecordingButton.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.baseline_play_arrow_24),null,null,null);

                    recorderWaveformView.reset();
                    Toast.makeText(getContext(), "Recording is stopped!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        correctAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTestManager.getCurrentState()== TestManager.State.RECORDING || mTestManager.getCurrentState()== TestManager.State.PAUSED){
                    mTestManager.pauseTest();
                    startRecordingButton.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.baseline_play_arrow_24),null,null,null);


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
        startRecordingButton.setCompoundDrawablesWithIntrinsicBounds(getActivity().getDrawable(R.drawable.baseline_pause_24),null,null,null);
        correctAudioButton.setVisibility(View.VISIBLE);
        wrongAudioButton.setVisibility(View.VISIBLE);
        recorderWaveformView.setVisibility(View.VISIBLE);

        mTestManager.startTest(getActivity());
    }

    private void pauseRecording(){
        mTestManager.pauseTest();
        startRecordingButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getContext(),R.drawable.baseline_play_arrow_24),null,null,null);
    }

    private void animatePlayerView(){
        if(mTestManager.getCurrentState()== TestManager.State.RECORDING) {
            try {
                int amplitude = mTestManager.getmRecorder().getMaxAmplitude();
                recorderWaveformView.updateAmps(amplitude);
            }catch (Exception e){
                Log.e("abh", "isRecording reflected late. missed some seconds to plot waveform view" );
            }
        }
    }

    private void showSaveBottomSheet(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.save_audio_bottom_sheet);
        bottomSheetDialog.show();

        TextInputLayout nameET = bottomSheetDialog.findViewById(R.id.name_et_save_audio_bottom_sheet);
        Button positiveButton = bottomSheetDialog.findViewById(R.id.ok_button_save_bottom_sheet);
        Button negativeButton = bottomSheetDialog.findViewById(R.id.cancel_button_save_bottom_sheet);
        TextInputEditText ageET = bottomSheetDialog.findViewById(R.id.ageEt);

        AutoCompleteTextView smokeDropDown, genderDropDown;
        smokeDropDown = bottomSheetDialog.findViewById(R.id.smoke_exposed_dropdown);
        genderDropDown = bottomSheetDialog.findViewById(R.id.gender_exposed_dropdown);


        String[] genders = new String[]{"Male","Female","Other"};
        String[] smokingOptions = new String[]{"Yes","No"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_textview,genders);
        genderDropDown.setAdapter(genderAdapter);

        ArrayAdapter<String> smokingOptionAdapter = new ArrayAdapter<>(getActivity(),R.layout.dropdown_textview,smokingOptions);
        smokeDropDown.setAdapter(smokingOptionAdapter);

        nameET.setSelected(true);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nameET.getEditText().getText().toString().equals("")) {
                    Toast.makeText(getContext(), getResources().getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ageET.getText().toString().equals("")){
                    Toast.makeText(getContext(), getResources().getString(R.string.please_enter_age), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(smokeDropDown.getEditableText().toString().equals("")){
                    Toast.makeText(getContext(), getResources().getString(R.string.please_enter_smoking), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(genderDropDown.getEditableText().toString().equals("")){
                    Toast.makeText(getContext(), getResources().getString(R.string.please_enter_gender), Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("abh", "saving and adding audio to storgae: ");

                Toast.makeText(getContext(), "Recording Saved!", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getActivity(),AnalyticsActivity.class);
                i.putExtra(Constants.CURRENT_TEST,XStreamSerializer.getInstance().serialize(mTestManager.getCurrentTest()));
                startActivity(i);

                mTestManager.setTestName(nameET.getEditText().getText().toString());
                mTestManager.setTestAge(Integer.parseInt(ageET.getText().toString()));

                //TODO add smoke these to test and then add soo that they are exported while exporting csv

                mTestManager.saveTest(getContext());

                correctAudioButton.setVisibility(View.GONE);
                wrongAudioButton.setVisibility(View.GONE);
                recorderWaveformView.reset();

                bottomSheetDialog.cancel();
            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.cancel();
            }
        });
    }

    @Override
    public void onOrientationChange(Acceleration acceleration) {
        double accelerationMag = Math.sqrt(acceleration.X*acceleration.X+acceleration.Y*acceleration.Y+acceleration.Z*acceleration.Z);
        double cosAngleInRadian = Math.acos(acceleration.Z/accelerationMag);

        double cosAngleInDegree = Math.toDegrees(cosAngleInRadian);

        double x = -(acceleration.X/accelerationMag);
        double y = -(acceleration.Y/accelerationMag);

        gyroscopeView.updateGyroscopeView(x,y,cosAngleInDegree);

        degreeTextView.setText(String.valueOf(cosAngleInDegree));
    }
}