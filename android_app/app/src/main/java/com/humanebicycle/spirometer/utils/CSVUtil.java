package com.humanebicycle.spirometer.utils;

import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.Acceleration;
import com.humanebicycle.spirometer.model.SpirometerTest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

public class CSVUtil {
    public static final String DEFAULT_SEPARATOR = ",";

    public static boolean exportTestAsCSV(String path, SpirometerTest test) throws IOException {
        List<Acceleration> accelerationList = test.getAccelerationList();
        StringBuilder sb = new StringBuilder();

        String header = "timestamp"+DEFAULT_SEPARATOR+"X"+DEFAULT_SEPARATOR+"Y"+DEFAULT_SEPARATOR+"Z"+"\n";

        sb.append(header);

        for (int i =0;i<accelerationList.size();i++){
            Acceleration a = accelerationList.get(i);
            String line = getTimefromMillis(a.timeStamp) + DEFAULT_SEPARATOR + a.X+DEFAULT_SEPARATOR+a.Y+DEFAULT_SEPARATOR+a.Z+"\n";
            sb.append(line);
        }


        File file = new File(path);
        if(file.exists()){
            throw new IOException("Already Exist.");
        }

        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(sb.toString());
        fileWriter.close();

        return true;
    }

    public static boolean exportAllTestsAsCSV(String path)throws IOException{
        List<SpirometerTest> tests = XStreamSerializer.getInstance().getPreviousTests();
        StringBuilder sb = new StringBuilder();

        String header = "id"+DEFAULT_SEPARATOR+"time"+DEFAULT_SEPARATOR+"name"+"\n";
        sb.append(header);

        for(int i =0;i<tests.size();i++){
            SpirometerTest test = tests.get(i);
            String line = test.getId() + DEFAULT_SEPARATOR +
                    getTimefromMillis(test.getTime())+DEFAULT_SEPARATOR +
                    test.getName()+"\n";
            sb.append(line);

        }

        FileWriter fileWriter = new FileWriter(path);
        fileWriter.write(sb.toString());
        fileWriter.close();
        return true;
    }

    public static String getTimefromMillis(long time){
        String str = DateFormat.getDateTimeInstance().format(time);
        StringBuilder sb = new StringBuilder();
        for(int i =0;i<str.length();i++){
            if(!(str.charAt(i)==',')){
                sb.append(str.charAt(i));
            }
        }
        return sb.toString();
    }
}
