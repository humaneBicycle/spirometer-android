package com.humanebicycle.spirometer.model;

import java.util.ArrayList;
import java.util.List;

public class SpirometerTest {
    public String name;
    public long time;
    public String audioAddress;
    public boolean isProcessed=false;

    public List<Acceleration> accelerationList;
    String gender;
    int age;
    boolean isSmoker;


    public long id;


    public SpirometerTest(long time, String audioAddress){
        this.time=time;
        this.id=time;
        this.audioAddress=audioAddress;

        accelerationList = new ArrayList<>();
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


    public List<Acceleration> getAccelerationList() {
        return accelerationList;
    }

    public void setAccelerationList(List<Acceleration> accelerationList) {
        this.accelerationList = accelerationList;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isSmoker() {
        return isSmoker;
    }

    public void setSmoker(boolean smoker) {
        isSmoker = smoker;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
