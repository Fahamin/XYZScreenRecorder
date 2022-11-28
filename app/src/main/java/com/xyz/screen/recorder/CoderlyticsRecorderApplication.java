package com.xyz.screen.recorder;

import android.app.Application;

import com.admobOpenAd.AppOpenManager;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.List;

public class CoderlyticsRecorderApplication extends Application {
    private static List<BaseActivity> activityList;
    private static CoderlyticsRecorderApplication instance;
    private static AppOpenManager appOpenManager;


    public static CoderlyticsRecorderApplication getInstance() {
        return instance;
    }

    private static synchronized void setInstance(CoderlyticsRecorderApplication unitechRecorderApplication) {
        synchronized (CoderlyticsRecorderApplication.class) {
            instance = unitechRecorderApplication;
        }
    }

    public void onCreate() {
        super.onCreate();
        if (instance == null) {
            setInstance(this);
        }
        activityList = new ArrayList();
        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });
        appOpenManager = new AppOpenManager(this);

    }

    public void doForCreate(BaseActivity baseActivity) {
        activityList.add(baseActivity);
    }

    public void doForFinish(BaseActivity baseActivity) {
        activityList.remove(baseActivity);
    }

    public BaseActivity getTopActivity() {
        if (activityList.isEmpty()) {
            return null;
        }
        List<BaseActivity> list = activityList;
        return (BaseActivity) list.get(list.size() - 1);
    }

    public void clearAllActivity() {
        for (BaseActivity clear : activityList) {
            clear.clear();
        }
        activityList.clear();
    }
}
