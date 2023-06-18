package com.humanebicycle.spirometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.psambit9791.jdsp.filter.Butterworth;
import com.github.psambit9791.jdsp.transform.Hilbert;
import com.google.android.material.button.MaterialButton;
import com.humanebicycle.spirometer.fragments.PreviewAudioFragment;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.geometry.euclidean.twod.Line;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;


public class AnalyticsActivity extends AppCompatActivity {

//    AutoCompleteTextView samplingRateDropDownView, filterDropDownView;
    MaterialButton analyzeAudio;
    SpirometerTest test;
    float[] toPlotX;
    float[] toPlotY;
    ProgressBar progressBar;
    LinearLayout resultView;
    GraphView resultGraphView;
    int x;
    LineGraphSeries<DataPoint> y;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        String testString = getIntent().getStringExtra(Constants.CURRENT_TEST);
        SpirometerTest test = (SpirometerTest) XStreamSerializer.getInstance().deSerialize(testString);
        this.test=test;
        Log.d("abh", "test is analytics: "+XStreamSerializer.getInstance().serialize(test));

        analyzeAudio = findViewById(R.id.analyze_audio);
        progressBar = findViewById(R.id.analytics_progress_bar);
        resultView = findViewById(R.id.test_result_view);
        resultGraphView = findViewById(R.id.result_graph_view);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.preview_audio_fragment_root, PreviewAudioFragment.newInstance(test)).commit();

        resultGraphView.setTitle("Hilbert Envelope");
        resultGraphView.getViewport().setXAxisBoundsManual(true);
        resultGraphView.getViewport().setMinX(0);
        resultGraphView.getViewport().setMaxX(100000);
        resultGraphView.setHorizontalScrollBarEnabled(true);

//        samplingRateDropDownView = findViewById(R.id.sample_rate_exposed_dropdown);
//        filterDropDownView = findViewById(R.id.filter_exposed_dropdown);
//        String[] sampleRates = new String[]{Constants.AUDIO_SAMPLING_RATE_4000,Constants.AUDIO_SAMPLING_RATE_44000,Constants.AUDIO_SAMPLING_RATE_44100};
//        ArrayAdapter<String> samplingRateAdapter = new ArrayAdapter<>(this,R.layout.dropdown_textview,sampleRates);
//        samplingRateDropDownView.setAdapter(samplingRateAdapter);
//
//        String[] filteringOptions = new String[]{Constants.AUDIO_FILTER_HIGH_PASS,Constants.AUDIO_FILTER_LOW_PASS};
//        ArrayAdapter<String> filteringOptionsAdapter = new ArrayAdapter<>(this,R.layout.dropdown_textview,filteringOptions);
//        filterDropDownView.setAdapter(filteringOptionsAdapter);

//        if(test.isProcessed()){
//            displayResults();
//        }else{
//            processAudio();
//        }

        analyzeAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                processAudio();
            }
        });
    }

    private void displayResults(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                resultView.setVisibility(View.VISIBLE);
                resultGraphView.addSeries(y);
            }
        });

    }

    private void processAudio(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                com.jlibrosa.audio.JLibrosa jLibrosa = new com.jlibrosa.audio.JLibrosa();
                try {
                    Uri uri = Uri.parse(test.getAudioAddress());
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(AnalyticsActivity.this,uri);
                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    int millSecond = Integer.parseInt(durationStr);
                    /**
                     * 1. Get the audio.wav in array of float
                     * 2. filter using butter_and_pass
                     * 3. find hillbert transform
                     * 4. envolope_hat is absolute values of x_hillbert
                     */
                    //step 1
                    float[] audio = jLibrosa.loadAndRead(test.getAudioAddress(),8000,millSecond/60);
                    Log.d("abh", "run: audio loaded from jlibrosa");

                    //step 2
                    double[] band_pass_audio = butterBandpassFilter(audio,1500,2500,8000,10);
                    Log.d("abh", "run: butter filtering of data done!");

                    //step 3
                    Hilbert hilbert = new Hilbert(band_pass_audio);
                    hilbert.transform();
                    double[] envelopeHatHilbert = hilbert.getAmplitudeEnvelope();
                    Log.d("abh", "run: hilbert transform of data done!");

                    //step 4. take the abs of hilbert transform


                    //step 5display results
                    y=new LineGraphSeries<>();
                    for(int i =0;i<envelopeHatHilbert.length;i++){
                        y.appendData(new DataPoint(i+1,envelopeHatHilbert[i]),false,1000000);
                    }
                    Log.d("abh", "run: series to graph added!!");
                    displayResults();
                    Log.d("abh", "run: showing results!!");

                } catch (IOException e) {
                    Log.d("abh", "run: "+e);
                } catch (com.jlibrosa.audio.wavFile.WavFileException e) {
                    Log.d("abh", "run: "+e);

                } catch (com.jlibrosa.audio.exception.FileFormatNotSupportedException e) {
                    Log.d("abh", "run: "+e);
                }
            }
        };

        new Thread(runnable).start();
    }

    private double[] butterBandpassFilter(float[] data,int cutoffLow, int cutoffHigh, int sampleRate, int order){
        Butterworth butterworth = new Butterworth(sampleRate);

        double [] doubleSignal = IntStream.range(0, data.length).mapToDouble(i -> data[i]).toArray();

        //filtered data
        double [] butterBandPass = butterworth.bandPassFilter(doubleSignal,order,cutoffLow,cutoffHigh);

        return butterBandPass;

    }



}