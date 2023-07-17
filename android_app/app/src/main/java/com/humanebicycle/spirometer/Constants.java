package com.humanebicycle.spirometer;

public class Constants {
    public static final String AUDIO_SAMPLING_RATE_4000="audio_sampling_rate_4000";
    public static final String AUDIO_SAMPLING_RATE_44100="audio_sampling_rate_44100";
    public static final String AUDIO_SAMPLING_RATE_44000="audio_sampling_rate_4400";


    public static final String AUDIO_FILTER_LOW_PASS="audio_filer_low_pass";
    public static final String AUDIO_FILTER_HIGH_PASS="audio_filter_high_pass";
    public static final String SPIROMETER_TEST_LIST = "spirometer_test_list";
    public static final String CURRENT_TEST = "current_test_shared_pref";
    public static final int SAMPLE_RATE_8000=8000;
    public static final int SAMPLE_RATE_44100=44100;
    public static final int DEFAULT_SAMPLE_RATE=22050;
    private static int CURRENT_SAMPLE_RATE=0;

    public static int getSamplingRate(){
        if(CURRENT_SAMPLE_RATE==0){
            return DEFAULT_SAMPLE_RATE;
        }else{
            return CURRENT_SAMPLE_RATE;
        }
    }
    public static void setSampleRate(int sampleRate){
        CURRENT_SAMPLE_RATE=sampleRate;
    }

}
