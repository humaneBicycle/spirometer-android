package com.humanebicycle.spirometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.media.AudioFormat;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar ;
import android.widget.TextView;


import com.github.psambit9791.jdsp.filter.Butterworth;
import com.google.android.material.button.MaterialButton;
import com.humanebicycle.spirometer.fragments.PreviewAudioFragment;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.pdfbox.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxTensorLike;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ca.yorku.lab.quantimb.Hilbert;
import umontreal.ssj.util.sort.HilbertCurveMap;


public class AnalyticsActivity extends AppCompatActivity {

//    AutoCompleteTextView `sampling`RateDropDownView, filterDropDownView;
    MaterialButton analyzeAudio;
    SpirometerTest test;
    float[] toPlotX;
    float[] toPlotY;
    ProgressBar progressBar;
    LinearLayout resultView;
//    GraphView resultGraphView;
    int x;
    LineGraphSeries<DataPoint> y;
    float predictedFVC, predictedFEV1;
    TextView predictedFVCTextView, predictedFEV1TextView;


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
//        resultGraphView = findViewById(R.id.result_graph_view);
        predictedFEV1TextView = findViewById(R.id.predicted_fev1_value);
        predictedFVCTextView = findViewById(R.id.predicted_fvc_value);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.preview_audio_fragment_root, PreviewAudioFragment.newInstance(test)).commit();


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
//                resultGraphView.addSeries(y);

                predictedFEV1TextView.setText(String.valueOf(predictedFEV1));
                predictedFVCTextView.setText(String.valueOf(predictedFVC));
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
//                   set the global sampling rate
                    MediaExtractor mex = new MediaExtractor();
                    try {
                        mex.setDataSource(test.getAudioAddress());// the adresss location of the sound on sdcard.
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    MediaFormat mf = mex.getTrackFormat(0);

                    int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    Constants.setSampleRate(sampleRate);
                    Log.d("abh", "local sample rate: "+sampleRate+" global:"+Constants.getSamplingRate());

                    float[] audio = jLibrosa.loadAndRead(test.getAudioAddress(),Constants.getSamplingRate(),millSecond/60);
                    Log.d("abh", "run: audio loaded from jlibrosa");

                    //step 2
//                    double[] band_pass_audio = butterBandpassFilter(audio,3000,5000,Constants.getSamplingRate(),5);
                    Log.d("abh", "run: butter filtering of data done!");

                    //step 3


//                    final float[] toPlot = calculateEnvelope(envelope);

                    Log.d("abh", "run: hilbert transform of data done!");


                    //step plotting envelope graph
                    y=new LineGraphSeries<>();
//                    for(int i =0;i<envelope.length;i++){
//                        y.appendData(new DataPoint(i+1,envelope[i]),true,envelope.length);
//                    }


//                    float[] float_band_pass_audio = new float[band_pass_audio.length];
//                    for (int i = 0 ; i < band_pass_audio.length; i++)
//                    {
//                        float_band_pass_audio[i] = (float) band_pass_audio[i];
//                    }

                    AudioFeatureExtractor audioFeatureExtractor = new AudioFeatureExtractor(audio,Constants.getSamplingRate(),test);
                    float[] features = audioFeatureExtractor.getAudioFeatures();
                    Log.d("abh", "run: extracted the features");



                    predictFEV1(features,createFEV1ORTSession(OrtEnvironment.getEnvironment()),OrtEnvironment.getEnvironment());
                    Log.d("abh", "run: predicted the fev1");

                    predictFVC(features,createFVCORTSession(OrtEnvironment.getEnvironment()),OrtEnvironment.getEnvironment());
                    Log.d("abh", "run: predicted the fvc");

                    Log.d("abh", "run: series to graph added!!");
                    displayResults();
                    Log.d("abh", "run: showing results!!");

                } catch (IOException e) {
                    Log.d("abh", "run: "+e);
                } catch (com.jlibrosa.audio.wavFile.WavFileException e) {
                    Log.d("abh", "run: "+e);

                } catch (com.jlibrosa.audio.exception.FileFormatNotSupportedException e) {
                    Log.d("abh", "run: "+e);
                }catch (Exception e){
                    Log.w("abh", "run: "+e );
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

    private OrtSession createFEV1ORTSession(OrtEnvironment ortEnvironment) throws NullPointerException{
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.rf_fev1);
            byte[] fev1_model_array = IOUtils.toByteArray(inputStream);
            return ortEnvironment.createSession(fev1_model_array);
        }catch (Exception e){
            Log.w("abh", "createORTSession: "+e );
        }
        return null;
    }

    private OrtSession createFVCORTSession(OrtEnvironment ortEnvironment) throws NullPointerException{
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.rf_fvc);
            byte[] fev1_model_array = IOUtils.toByteArray(inputStream);
            return ortEnvironment.createSession(fev1_model_array);
        }catch (Exception e){
            Log.w("abh", "createORTSession: "+e );
        }
        return null;
    }

    private void predictFEV1(float[] input,OrtSession ortSession, OrtEnvironment ortEnvironment){
        String inputName = ortSession.getInputNames().iterator().next();

        FloatBuffer floatBuffer = FloatBuffer.wrap(input);

        long[] longOfArray = {1,1025};

        try {
            OnnxTensorLike onnxTensorLike = OnnxTensor.createTensor(ortEnvironment, floatBuffer, longOfArray);

            HashMap<String, OnnxTensorLike> map = new HashMap<>();
            map.put(inputName, onnxTensorLike);

            OrtSession.Result result = ortSession.run(map);
            float[][] resultArray = (float[][])result.get(0).getValue();
            predictedFEV1=resultArray[0][0];
            Log.d("abh", "result predictFEV1 value:" + resultArray[0][0]);
        }catch (Exception e){
            Log.w("abh", "predictFEV1: "+e);
        }

    }

    private void predictFVC(float[] input, OrtSession ortSession, OrtEnvironment ortEnvironment){
        String inputName = ortSession.getInputNames().iterator().next();
        FloatBuffer floatBuffer = FloatBuffer.wrap(input);

        long[] longOfArray = {1,1025};

        try{
            OnnxTensorLike onnxTensorLike = OnnxTensor.createTensor(ortEnvironment,floatBuffer,longOfArray);

            HashMap<String,OnnxTensorLike> map = new HashMap<>();
            map.put(inputName,onnxTensorLike);

            OrtSession.Result result = ortSession.run(map);
            float [][] resultArray = (float[][]) result.get(0).getValue();
            predictedFVC=resultArray[0][0];

        }catch (Exception e){
            Log.e("abh", "predictFVC: "+e );
        }
    }

    private float[] calculateEnvelope(double[] buffer,int window){
        float[] toPlot = new float[buffer.length];


        return toPlot;
    }

//    public double[] extract(File inputFile) {
//        AudioInputStream in = null;
//        try {
//            in = AudioSystem.getAudioInputStream(inputFile);
//        } catch (Exception e) {
//            System.out.println("Cannot read audio file");
//            return new double[0];
//        }
//        AudioFormat format = in.getFormat();
//        byte[] audioBytes = readBytes(in);
//
//        int[] result = null;
//        if (format.getSampleSizeInBits() == 16) {
//            int samplesLength = audioBytes.length / 2;
//            result = new int[samplesLength];
//            if (format.isBigEndian()) {
//                for (int i = 0; i < samplesLength; ++i) {
//                    byte MSB = audioBytes[i * 2];
//                    byte LSB = audioBytes[i * 2 + 1];
//                    result[i] = MSB << 8 | (255 & LSB);
//                }
//            } else {
//                for (int i = 0; i < samplesLength; i += 2) {
//                    byte LSB = audioBytes[i * 2];
//                    byte MSB = audioBytes[i * 2 + 1];
//                    result[i / 2] = MSB << 8 | (255 & LSB);
//                }
//            }
//        } else {
//            int samplesLength = audioBytes.length;
//            result = new int[samplesLength];
//            if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
//                for (int i = 0; i < samplesLength; ++i) {
//                    result[i] = audioBytes[i];
//                }
//            } else {
//                for (int i = 0; i < samplesLength; ++i) {
//                    result[i] = audioBytes[i] - 128;
//                }
//            }
//        }
//
//        return result;
//    }


}