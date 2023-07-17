package com.humanebicycle.spirometer;

import android.util.Log;

import com.github.psambit9791.jdsp.transform.ShortTimeFourier;
import com.github.psambit9791.jdsp.windows.Rectangular;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.utils.CSVUtil;
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
    SpirometerTest test;

    public AudioFeatureExtractor(float[] signal, int sampleRate,SpirometerTest test){
        this.signal=signal;
        this.sampleRate=sampleRate;
        jLibrosa = new JLibrosa();
        this.test = test;
    }

    public float[] getAudioFeatures(){
        float [] stftFeatures = getSTFTSpectrogramFeatures(signal);
//        float [] melFeatures = getMELSpectrogramFeatures(signal);
//        float [] mfccFeatures = getMFCCFeatures(signal);

//        int size = stftFeatures.length+melFeatures.length+mfccFeatures.length;
//
//        Log.d("abh size", "stft: "+stftFeatures.length+" mel:"+melFeatures.length+" mfcc:"+mfccFeatures.length);
//
//        float [] toReturn=new float[size];
//        /**
//         * these 3 features are in order:
//         * 1. stft
//         * 2. mel
//         * 3. mfcc
//         */
//        for(int i =0;i<size;i++){
//            if(i<stftFeatures.length-1){
//                toReturn[i]=stftFeatures[i];
//            }else if(i>stftFeatures.length && i<stftFeatures.length+melFeatures.length){
//                toReturn[i]=melFeatures[i-stftFeatures.length];
//            }else if(i>stftFeatures.length+melFeatures.length){
//                toReturn[i]=mfccFeatures[i-stftFeatures.length-melFeatures.length];
//            }
//
//        }
//
//        Log.d("abh", "getAudioFeatures: before size:"+size+" after size:"+toReturn.length);
////        for(int i =0;i<toReturn.length;i++){
////            Log.d("abh", "getAudioFeatures: "+toReturn[i]);
////        }

        return stftFeatures;

    }

    private float[] getSTFTSpectrogramFeatures(float[] signal){
//        double[] array = IntStream.range(0, signal.length).mapToDouble(i -> signal[i]).toArray();
//        ShortTimeFourier shortTimeFourier = new ShortTimeFourier(array,1024);
//        shortTimeFourier.transform();
//        Complex[][] complexes = shortTimeFourier.getComplex(false);

////using jlibrosa
        Complex[][] complexes = jLibrosa.generateSTFTFeatures(signal,sampleRate,40);
        if(CSVUtil.exportAudioSTFT(complexes,test)){
            Log.d("abh vvi", "exported audio stft ");
        }


        Log.d("vvi abh", "getSTFTSpectrogramFeatures: length complexes[0].length "+complexes[1].length+" complexes length:"+complexes.length);

        for(int i =0;i<complexes.length;i++){
            String str = new String();
            for(int j =0;j<complexes[i].length;j++){
                str = str+ "complex at i: "+i+" j:"+j+" is "+ complexes[i][j]+"\t";
            }
            Log.d("abh", str);
        }

        float sum[] = new float[complexes.length];
        for(int i =0;i<complexes.length;i++){
            float sum_r=0f;
            String str=new String();
            try {
                for (int j = 0; j < complexes[i].length; j++) {
                    try {
                        sum_r = sum_r + (float) complexes[i][j].abs();

                    } catch (NullPointerException e) {
                        Log.w("abh", "getSTFTSpectrogramFeatures: for index i " + i + " error" + e);
                    }
                }
                str = str + "at i = "+i+" sum is "+sum_r/complexes[i].length + " ";
                Log.d("abh", str);
            }catch (IndexOutOfBoundsException e){
                Log.w("abh", "getSTFTSpectrogramFeatures: "+e );
            }
            sum[i]=sum_r/complexes[i].length;
        }

        return sum;
    }

}
