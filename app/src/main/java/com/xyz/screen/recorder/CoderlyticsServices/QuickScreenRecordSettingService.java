package com.xyz.screen.recorder.CoderlyticsServices;

import android.content.Intent;
import android.service.quicksettings.TileService;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsActivities.TakeRequestRecorderActivity;

@RequiresApi(api = 24)
public class QuickScreenRecordSettingService extends TileService {
    private static QuickScreenRecordSettingService instance;

    public static QuickScreenRecordSettingService getInstance() {
        return instance;
    }

    private static void setInstance(QuickScreenRecordSettingService quickScreenRecordSettingService) {
        instance = quickScreenRecordSettingService;
    }

    public void onCreate() {
        super.onCreate();
        if (getInstance() == null) {
            setInstance(this);
        }
        Log.e("YYY", "onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.e("YYY", "onDestroy");
    }

    public void onClick() {
        super.onClick();
        Intent intent = new Intent(this, TakeRequestRecorderActivity.class);
        intent.setFlags(268435456);
        Utils.startActivityAllStage(this, intent);
    }

    public void onStartListening() {
        super.onStartListening();
    }
}
