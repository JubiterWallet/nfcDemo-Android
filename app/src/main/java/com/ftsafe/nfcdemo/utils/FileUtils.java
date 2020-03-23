package com.ftsafe.nfcdemo.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ftsafe.nfcdemo.MainActivity.LOG_RECV;
import static com.ftsafe.nfcdemo.MainActivity.LOG_SEND;

/**
 * @Date 2018/4/19  16:06
 * @Author ZJF
 * @Version 1.0
 */
public class FileUtils {

    public static void init() {
        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdir();
        }
        Log.d("ZJF",dir.getAbsolutePath());
    }

    private static File getDir() {
        String path = Environment.getExternalStorageDirectory().getPath();
        return new File(path + File.separator + "nfcTest");
    }

    public static List<String> getApdu() {
        return getApduStrList();
    }

    private static List<String> getApduStrList() {
        List<String> result = new ArrayList<>();
        File dir = getDir();
        File file = new File(dir, "apdu.txt");
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String str = null;
                while ((str = br.readLine()) != null) {
                    result.add(str);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static File makeLogFile() {
        File fileDir = getDir();
        File logFile = new File(fileDir.getAbsolutePath() + File.separator + getTime(false) + ".txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logFile;
    }

    public static void saveLog(final int type, final File logFile, final String msg) {
        Log.d("TEST","saveLog");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream fos = new FileOutputStream(logFile, true);
                    String title = "Send:";
                    if (type == LOG_RECV) {
                        title = "Recv:";
                    }
                    title = title + getTime(true);
                    fos.write(title.getBytes());
                    fos.write("\r\n".getBytes());
                    fos.write(msg.getBytes());
                    fos.write("\r\n".getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private static String getTime(boolean isAll) {
        SimpleDateFormat sdf;
        if (isAll) {
            sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        } else {
            sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        }
        return sdf.format(new Date());
    }
}
