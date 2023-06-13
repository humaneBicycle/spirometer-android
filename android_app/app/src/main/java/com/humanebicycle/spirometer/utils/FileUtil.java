package com.humanebicycle.spirometer.utils;

import android.os.Environment;

import java.io.File;

public class FileUtil {



    public static String getAppStorageDirectoryForAudio(){
        String str = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Spirometer"+File.separator+"audio"+File.separator;
        File file = new File(str);
        if(!file.exists()){
            file.mkdir();
        }
        return str;
    }
    public static String getAppStorageDirectoryForTests(){
        String str = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Spirometer"+File.separator+"tests"+File.separator;
        File file = new File(str);
        if(!file.exists()){
            file.mkdir();
        }
        return str;
    }
    public static String getAppStorageDirectoryForExports(){
        String str = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Spirometer"+File.separator+"exports"+File.separator;
        File file = new File(str);
        if(!file.exists()){
            file.mkdir();
        }
        return str;
    }
}
