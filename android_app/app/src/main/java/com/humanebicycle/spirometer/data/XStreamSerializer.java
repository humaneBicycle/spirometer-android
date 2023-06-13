package com.humanebicycle.spirometer.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.utils.FileUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
        String testPath = FileUtil.getAppStorageDirectoryForTests()+test.getId()+".txt";
        try {
            FileWriter fileWriter = new FileWriter(testPath);
            fileWriter.write(XStreamSerializer.getInstance().serialize(test));
            fileWriter.close();

            return true;
        } catch (IOException e) {
            Log.d("abh", "addTestToStorage: Can't add test to storage. Can't write to file");
            throw new RuntimeException(e);
        }
    }


    public List<SpirometerTest> getPreviousTests() throws IOException {
        String parentDirPath = FileUtil.getAppStorageDirectoryForTests();
        File parentDir = new File(parentDirPath);
        List<SpirometerTest> tests = new ArrayList<>();

        for(int i =0;i<parentDir.listFiles().length;i++ ){
            String testPath = parentDir.listFiles()[i].getAbsolutePath();
            Scanner scanner;
            StringBuilder line= new StringBuilder();
            try {
                scanner = new Scanner(Paths.get(testPath));
                line = new StringBuilder(scanner.nextLine());
                while (scanner.hasNext()) {
                    line.append(scanner.nextLine());
                }
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if((line.length() > 0) && !line.toString().equals("")){
                Log.d("abh", "deserialised test: ");
                SpirometerTest spirometerTest = (SpirometerTest) XStreamSerializer.getInstance().deSerialize(line.toString());
                tests.add(spirometerTest);
            }
        }
        return tests;

    }

    public String serialize(Object object){
        return xstream.toXML(object);
    }

    public Object deSerialize(String str){
        return xstream.fromXML(str);
    }
}
