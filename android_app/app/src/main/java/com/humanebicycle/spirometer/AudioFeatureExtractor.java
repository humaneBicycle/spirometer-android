package com.humanebicycle.spirometer;

import android.util.Log;

import com.github.psambit9791.jdsp.transform.ShortTimeFourier;
import com.jlibrosa.audio.JLibrosa;

import org.apache.commons.math3.complex.Complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class AudioFeatureExtractor {
    //return the data that is directly fed in the model.
    float[] signal;
    JLibrosa jLibrosa;
    int sampleRate;

    public AudioFeatureExtractor(float[] signal, int sampleRate){
        this.signal=signal;
        this.sampleRate=sampleRate;
        jLibrosa = new JLibrosa();
    }

    public float[] getAudioFeatures(){
        float [] stftFeatures = getSTFTSpectrogramFeatures(signal);
        float [] melFeatures = getMELSpectrogramFeatures(signal);
        float [] mfccFeatures = getMFCCFeatures(signal);

        int size = stftFeatures.length+melFeatures.length+mfccFeatures.length;

        Log.d("abh size", "stft: "+stftFeatures.length+" mel:"+melFeatures.length+" mfcc:"+mfccFeatures.length);

        float [] toReturn=new float[size];
        for(int i =0;i<size;i++){
            if(i<stftFeatures.length-1){
                toReturn[i]=stftFeatures[i];
            }else if(i>stftFeatures.length && i<stftFeatures.length+melFeatures.length){
                toReturn[i]=melFeatures[i-stftFeatures.length];
            }else if(i>stftFeatures.length+melFeatures.length){
                toReturn[i]=mfccFeatures[i-stftFeatures.length-melFeatures.length];
            }

        }

        Log.d("abh", "getAudioFeatures: before size:"+size+" after size:"+toReturn.length);

        return toReturn;

    }

    private float[] getSTFTSpectrogramFeatures(float[] signal){
        double[] array = IntStream.range(0, signal.length).mapToDouble(i -> signal[i]).toArray();
        ShortTimeFourier shortTimeFourier = new ShortTimeFourier(array,241);
        shortTimeFourier.transform();
        shortTimeFourier.getComplex(false);
        Complex[][] complexes = shortTimeFourier.getComplex(false);
////using jlibrosa
//        Complex[][] complexes = jLibrosa.generateSTFTFeatures(signal,sampleRate,240);
        float sum[] = new float[242];
        for(int i =0;i<242;i++){
            float sum_r=0f;
            try {
                for (int j = 0; j < complexes[0].length; j++) {
                    try {
                        sum_r = sum_r + (float) complexes[i][j].abs();
                    } catch (NullPointerException e) {
                        Log.w("abh", "getSTFTSpectrogramFeatures: for index i " + i + " error" + e);
                    }
                }
            }catch (IndexOutOfBoundsException e){
                Log.w("abh", "getSTFTSpectrogramFeatures: "+e );
            }
            sum[i]=sum_r;
        }

        return sum;
    }

    private float[] getMELSpectrogramFeatures(float[] signal){
        double[][] MELSpectrogram = jLibrosa.generateMelSpectroGram(signal);
        float sum[] = new float[MELSpectrogram.length];
        for(int i =0;i<MELSpectrogram.length;i++){
            float sum_r = 0f;
            for(int j =0;j<MELSpectrogram[0].length;j++){
                sum_r=sum_r+(float)MELSpectrogram[i][j];
            }
            sum[i]=sum_r;
        }
        return sum;
    }

    private float[] getMFCCFeatures(float[] signal){
        float[][] MFCCFeatures = jLibrosa.generateMFCCFeatures(signal,sampleRate,5,480,16,240);
        float[] meanMFCCFeatures = jLibrosa.generateMeanMFCCFeatures(MFCCFeatures,5,480);
        return meanMFCCFeatures;
    }

}
