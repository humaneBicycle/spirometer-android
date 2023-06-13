package com.humanebicycle.spirometer.model;

public class Acceleration {
    public float X;
    public float Y;
    public float Z;
    public long timeStamp;


    public Acceleration(float[] event, long timeStamp) {
        X = event[0];
        Y = event[1];
        Z = event[2];
        this.timeStamp = timeStamp;
    }
}
