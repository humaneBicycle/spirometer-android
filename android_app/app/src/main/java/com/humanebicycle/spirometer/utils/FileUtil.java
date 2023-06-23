package com.humanebicycle.spirometer.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class FileUtil {

    static String parentDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Spirometer"+File.separator;

    public static String getAppStorageDirectoryForAudio(){
        checkIfParentDirExist();
        String str = parentDir+"audio"+File.separator;
        File file = new File(str);
        if(!file.exists()){
            if(!file.mkdir()){
                Log.d("abh", "getAppStorageDirectoryForExports: can't create dir");
            }
        }
        return str;
    }
    public static String getAppStorageDirectoryForTests(){
        checkIfParentDirExist();
        String str = parentDir+"tests"+File.separator;
        File file = new File(str);
        if(!file.exists()){
            if(!file.mkdir()){
                Log.d("abh", "getAppStorageDirectoryForExports: can't create dir");

            }
        }
        return str;
    }
    public static String getAppStorageDirectoryForExports(){
        checkIfParentDirExist();
        String str = parentDir+"exports"+File.separator;
        File file = new File(str);
        if(!file.exists()){
            if(!file.mkdir()){
                Log.d("abh", "getAppStorageDirectoryForExports: can't create dir");
            }
        }
        return str;
    }

    public static String getAppStorageDirectoryForTemporaryAudioFile(){
        checkIfParentDirExist();
        String str = parentDir+"temp.file";
        File file = new File(str);
        if(file.exists()){
            if(!file.delete()){
                Log.e("abh", "getAppStorageDirectoryForTemporaryAudioFile: can't delete temp file." );
            }
        }
        return str;
    }
    private static void checkIfParentDirExist(){
        File parentFile = new File(parentDir);
        if(!parentFile.exists()){
            parentFile.mkdir();
        }
    }

}
