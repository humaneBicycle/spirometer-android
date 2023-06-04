package com.humanebicycle.spirometer.model;

public class SpirometerTest {
    String name;
    long time;
    String audioAddress;
    boolean isProcessed=false;

    public SpirometerTest(String name, long time, String audioAddress){
        this.name=name;
        this.audioAddress=audioAddress;
        this.time=time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getAudioAddress() {
        return audioAddress;
    }

    public void setAudioAddress(String audioAddress) {
        this.audioAddress = audioAddress;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public void setProcessed(boolean processed) {
        isProcessed = processed;
    }

}
