package com.humanebicycle.spirometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.humanebicycle.spirometer.fragments.PreviewAudioFragment;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;

public class AnalyticsActivity extends AppCompatActivity {

    AutoCompleteTextView samplingRateDropDownView, filterDropDownView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        String testString = getIntent().getStringExtra(Constants.CURRENT_TEST);
        SpirometerTest test = (SpirometerTest) XStreamSerializer.getInstance().deSerialize(testString);
        Log.d("abh", "test is analytics: "+XStreamSerializer.getInstance().serialize(test));

        samplingRateDropDownView = findViewById(R.id.sample_rate_exposed_dropdown);
        filterDropDownView = findViewById(R.id.filter_exposed_dropdown);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.preview_audio_fragment_root, PreviewAudioFragment.newInstance(test)).commit();


        String[] sampleRates = new String[]{Constants.AUDIO_SAMPLING_RATE_4000,Constants.AUDIO_SAMPLING_RATE_44000,Constants.AUDIO_SAMPLING_RATE_44100};
        ArrayAdapter<String> samplingRateAdapter = new ArrayAdapter<>(this,R.layout.dropdown_textview,sampleRates);
        samplingRateDropDownView.setAdapter(samplingRateAdapter);

        String[] filteringOptions = new String[]{Constants.AUDIO_FILTER_HIGH_PASS,Constants.AUDIO_FILTER_LOW_PASS};
        ArrayAdapter<String> filteringOptionsAdapter = new ArrayAdapter<>(this,R.layout.dropdown_textview,filteringOptions);
        filterDropDownView.setAdapter(filteringOptionsAdapter);

        if(test.isProcessed()){
            displayResults(test);
        }else{
            process(test);
        }
    }

    private void displayResults(SpirometerTest test){

    }

    private void process(SpirometerTest test){

    }
}