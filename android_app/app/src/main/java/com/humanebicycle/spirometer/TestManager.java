package com.humanebicycle.spirometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.humanebicycle.spirometer.model.Acceleration;
import com.humanebicycle.spirometer.utils.FileUtil;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;

import java.io.File;
import java.io.IOException;

public class TestManager {

    static SpirometerTest test;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorEventListener;
    MediaRecorder mRecorder;
    String mFileName;

    OnOrientationChangeListener onOrientationChangeListener;

    /**
     * These are the states for the test.
     */

    State STATE = State.NOT_INITIALISED;
    public enum State{
        NOT_INITIALISED,
        INITIALISED,
        RECORDING,
        PAUSED
    }

    public TestManager(OnOrientationChangeListener onOrientationChangeListener) {
        this.onOrientationChangeListener=onOrientationChangeListener;
    }

    public State getCurrentState(){
        return this.STATE;
    }

    public static TestManager getInstance(OnOrientationChangeListener onOrientationChangeListener){
        return new TestManager(onOrientationChangeListener);
    }

    //start, pause, discard, save

    public void createTest(long time){
        STATE = State.INITIALISED;

        String audioAddress = FileUtil.getAppStorageDirectoryForAudio() + String.valueOf(time)+".3gp";
        mFileName = audioAddress;
        Log.d("abh", "audio address: "+audioAddress);
        test= new SpirometerTest(time,audioAddress);
    }

    public void startTest(Activity activity){
        if(test==null){
            throw new IllegalStateException("Can't start a test without creating it. current state: "+STATE);
        }

        if(STATE==State.INITIALISED){
            mRecorder = new MediaRecorder();

            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mRecorder.setOutputFile(mFileName);
            Log.d("abh", "outsourcefile: "+mFileName);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e("TAG", "prepare() failed");
                return;
            }
            mRecorder.start();
            registerAccelerometer(activity);

            STATE = State.RECORDING;
            return;
        }

        if(STATE==State.PAUSED){
            mRecorder.resume();
            registerAccelerometer(activity);
            STATE=State.RECORDING;
            return;
        }

    }

    public void pauseTest(){
        if(STATE==State.RECORDING){
            unRegisterAccelerometer();
            mRecorder.pause();
            STATE=State.PAUSED;
        }
    }

    public void discardTest(){
        unRegisterAccelerometer();
        if(STATE== TestManager.State.RECORDING) {
            mRecorder.stop();
        }
        mRecorder.release();
        File file = new File(test.getAudioAddress());
        if(!file.delete()){
            Log.d("abh", "discardTest: t can't delete discarded audio file!");
        }
        mRecorder=null;
        test = null;
        STATE = State.NOT_INITIALISED;
    }

    public void saveTest(Context context){
        XStreamSerializer.getInstance().addTestToStorage(PreferenceManager.getDefaultSharedPreferences(context), test);
        if(STATE==State.RECORDING){
            throw new IllegalStateException("Manager must be paused by calling pauseTest() before saving. current state: "+STATE);
        }
        mRecorder.release();
        mRecorder=null;
        test = null;
        STATE = State.NOT_INITIALISED;
    }

    public SpirometerTest getCurrentTest(){
        return test;
    }

    public void registerAccelerometer(Activity activity){
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Acceleration a = new Acceleration(event.values, System.currentTimeMillis());
                onOrientationChangeListener.onOrientationChange(a);
                if(STATE==State.RECORDING && test!=null) {
                    test.getAccelerationList().add(a);
                    Log.d("abh", "onSensorChanged: added " + a + " to database.");
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        sensorManager.registerListener(sensorEventListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void setTestName(String name){
        test.setName(name);
    }

    public void unRegisterAccelerometer(){
        try {
            sensorManager.unregisterListener(sensorEventListener);
        }catch (NullPointerException e){
            Log.w("abh", "unRegisterAccelerometer: unregistering sensor failed. null pointer" +e);
        }
    }


    public MediaRecorder getmRecorder(){
        return this.mRecorder;
    }


}
