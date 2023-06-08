package com.humanebicycle.spirometer.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.humanebicycle.spirometer.Constants;
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

    public boolean addTestToStorage(SharedPreferences preferences,SpirometerTest test){
        List<SpirometerTest> listOfTest;
        String beforeAddXml = preferences.getString(Constants.SPIROMETER_TEST_LIST,"");
        if(beforeAddXml.equals("")){
            listOfTest = new ArrayList<>();
        }else{
            listOfTest = (ArrayList)xstream.fromXML(beforeAddXml);
        }
        listOfTest.add(test);

        String afterAddXml = xstream.toXML(listOfTest);

        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(Constants.SPIROMETER_TEST_LIST,afterAddXml);
        editor.apply();
        return true;
    }

    public List<SpirometerTest> getPreviousTests(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String listXml = sp.getString(Constants.SPIROMETER_TEST_LIST,"");
        if(listXml.equals(""))return new ArrayList<>();
        return (List<SpirometerTest>) xstream.fromXML(listXml);
    }

    public String serialize(Object object){
        return xstream.toXML(object);
    }

    public Object deSerialize(String str){
        return xstream.fromXML(str);
    }

}
