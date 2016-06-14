package com.qukan.playsdk.sample;


import android.app.Application;
import com.qukan.playsdk.PlaySdkUtils;
import com.qukan.playsdk.QLog;

/**
 * Created by wsy on 2015/12/18.
 */
public class QkApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        PlaySdkUtils.init(this, QLog.DEBUG);
        QLog.i("play sdk version: %s",PlaySdkUtils.getVersion());
    }
}
