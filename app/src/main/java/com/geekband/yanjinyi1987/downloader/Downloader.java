package com.geekband.yanjinyi1987.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lexkde on 16-8-31.
 */
public class Downloader {
    private String downloadPath;
    private String downloadCachePath;
    private boolean globalFailed = false;
    Context mContext;

    public Downloader(Context context) {
        mContext = context;

    }

    private boolean buildDownloadPath() {
        String externalSDRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        downloadCachePath = Environment.getDownloadCacheDirectory().getAbsolutePath();
        downloadPath = externalSDRootPath+"/"+R.string.app_name+"Download";
        File AppDownloadDir = new File (downloadPath);
        if(!AppDownloadDir.exists()) {
            if(!AppDownloadDir.mkdir()) {
                Log.i("Downloader","create download directory failed!");
                globalFailed = true;
                Toast.makeText(mContext,"Create download directory failed!",Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private String getFileName(String url_str) {
        String[] tmp = url_str.split("/");
        return tmp[tmp.length-1];
    }

    public boolean httpDownloader(String url_str) throws  Exception{
        if(!buildDownloadPath()) {
            return false;
        }
        URL url = new URL(url_str);
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setReadTimeout(8*1000);

        int fileLength = httpURLConnection.getContentLength();
        String downloadFilename = getFileName(url_str);
        String downloadFilepath = downloadPath+"/"+downloadFilename;
        File downloadFile = new File(downloadFilepath);
        //File downloadCacheFile = new File(downloadCachePath+"/"+downloadFilename);
        if(!downloadFile.delete()) {
            Log.i("Downloader","delete existed file failed!");
            globalFailed = true;
            Toast.makeText(mContext,"Delete existed file failed!",Toast.LENGTH_LONG).show();
            return false;
        }

        RandomAccessFile accessFile = new RandomAccessFile(downloadFile,"rwd");
        accessFile.setLength(fileLength);
        accessFile.seek(0);
        httpURLConnection.setRequestProperty("Range","bytes="+0+"-"+--fileLength);
        InputStream inputStream = httpURLConnection.getInputStream();

        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = inputStream.read(buffer))!=-1) {
            accessFile.write(buffer);
        }
        inputStream.close();
        accessFile.close();
        return true;
    }

}
