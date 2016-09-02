package com.geekband.yanjinyi1987.downloader;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Created by lexkde on 16-8-31.
 */
public class Downloader {
    private String downloadPath;
    private String downloadCachePath;
    private boolean globalFailed = false;
    Context mContext;
    private boolean forcedBreak=false;

    public Downloader(Context context) {
        mContext = context;

    }

    private boolean buildDownloadPath() {
        String externalSDRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        downloadCachePath = Environment.getDownloadCacheDirectory().getAbsolutePath();
        downloadPath = externalSDRootPath+"/"+mContext.getResources().getString(R.string.app_name)+"/"+"Download";
        Log.i("Downloader",downloadPath);
        File AppDownloadDir = new File (downloadPath);
        if(!AppDownloadDir.exists()) {
            if(!AppDownloadDir.mkdirs()) {
                Log.i("Downloader","create download directory failed!");
                globalFailed = true;
                //Toast.makeText(mContext,"Create download directory failed!",Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    private String getFileName(String url_str) {
        String[] tmp = url_str.split("/");
        return tmp[tmp.length-1];
    }

    public boolean httpDownloader(String url_str,MainActivity.MyDownloadTask myDownloadTask) throws  Exception{
        if(!buildDownloadPath()) {
            return false;
        }
        URL url = new URL(url_str);
        //cookie
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

        HttpCookie cookie = new HttpCookie("lang", "en");
        cookie.setDomain(url.getHost());
        cookie.setPath("/");
        cookie.setVersion(0);
        Log.i("Network downloader",url.getHost());
        cookieManager.getCookieStore().add(new URI(url.getHost()), cookie);


        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setReadTimeout(8*1000);

        int fileLength = httpURLConnection.getContentLength();
        fileLength++;
        httpURLConnection.disconnect();
        String downloadFilename = getFileName(url_str);
        String downloadFilepath = downloadPath+"/"+downloadFilename;
        File downloadFile = new File(downloadFilepath);
        //File downloadCacheFile = new File(downloadCachePath+"/"+downloadFilename);
        if(downloadFile.exists()) {
            if(!downloadFile.delete()) {
                Log.i("Downloader", "delete existed file failed!");
                globalFailed = true;
                //Toast.makeText(mContext,"Delete existed file failed!",Toast.LENGTH_LONG).show();
                return false;
            }
        }
        Log.i("Downloader", "1");
        RandomAccessFile accessFile = new RandomAccessFile(downloadFile,"rwd");
        if(fileLength<0) {
            return false;
        }
        accessFile.setLength(fileLength);
        accessFile.seek(0);
        //second communication
        HttpURLConnection httpURLConnection2 = (HttpURLConnection)url.openConnection();
        httpURLConnection2.setRequestMethod("GET");
        httpURLConnection2.setReadTimeout(8*1000);
        httpURLConnection2.setRequestProperty("Range","bytes="+0+"-"+--fileLength);
        InputStream inputStream = httpURLConnection2.getInputStream();

        byte[] buffer = new byte[1024];
        int len = 0;
        int process = 0;
        Log.i("Downloader", "2");
        Log.i("Filelength",String.valueOf(fileLength));
        while((len = inputStream.read(buffer))!=-1) {
            //Log.i("Info",String.valueOf(len));
            accessFile.write(buffer,0,len); // this will get correct results
            process+=len;
            myDownloadTask.setProgress((int)((float)process/fileLength*100.0));
            if(forcedBreak==true) {
                break;
            }
        }
        Log.i("Total Length",String.valueOf(process));
        inputStream.close();
        accessFile.close();
        //httpURLConnection.disconnect();
        httpURLConnection2.disconnect();
        return true;
    }

    public void setBreak() {
        forcedBreak =true;
    }
}
