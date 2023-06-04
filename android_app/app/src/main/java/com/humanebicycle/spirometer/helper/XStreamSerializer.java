package com.humanebicycle.spirometer.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.humanebicycle.spirometer.Spirometer;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.ArrayList;
import java.util.List;


public class XStreamSerializer {
    static XStream xstream;
    static XStreamSerializer instance;


    public static XStreamSerializer getInstance() {
        if (instance == null) {
            instance = new XStreamSerializer();
        }
        return instance;
    }


    private XStreamSerializer(){
        prepareXStream();
    }

    private static void prepareXStream(){
        xstream = new XStream(new DomDriver());

        xstream.alias("spirometer_test", SpirometerTest.class);
    }

    private static boolean addTestToStorage(SharedPreferences preferences,SpirometerTest test){
        List<SpirometerTest> listOfTest;
        String beforeAddXml = preferences.getString(Spirometer.SPIROMETER_TEST_LIST,"");
        if(beforeAddXml.equals("")){
            listOfTest = new ArrayList<>();
        }else{
            listOfTest = (ArrayList)xstream.fromXML(beforeAddXml);
        }
        listOfTest.add(test);

        String afterAddXml = xstream.toXML(listOfTest);

        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(Spirometer.SPIROMETER_TEST_LIST,afterAddXml);
        editor.apply();
        return true;
    }


}
