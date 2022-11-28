package com.xyz.screen.recorder.CoderlyticsActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.xyz.screen.recorder.BaseActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsServices.BubbleControlService;
import com.xyz.screen.recorder.CoderlyticsServices.RecorderService;

public class StartRecorderActivity extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);


        try {
            if (BubbleControlService.isCountdown) {
                finish();
                return;
            }
            String action = getIntent().getAction();
            char c = 65535;
            int hashCode = action.hashCode();
            String str = CoderlyticsConstants.SCREEN_RECORDING_PAUSE;
            String str2 = CoderlyticsConstants.SCREEN_RECORDING_RESUME;
            String str3 = CoderlyticsConstants.SCREEN_RECORDING_DESTROY;
            String str4 = CoderlyticsConstants.SCREEN_RECORDING_STOP;
            String str5 = CoderlyticsConstants.SCREEN_RECORDING_START_FROM_NOTIFY;
            switch (hashCode) {
                case -1996135482:
                    if (action.equals(str5)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1053033865:
                    if (action.equals(str4)) {
                        c = 2;
                        break;
                    }
                    break;
                case 143300674:
                    if (action.equals(str3)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1599260844:
                    if (action.equals(str2)) {
                        c = 4;
                        break;
                    }
                    break;
                case 1780700019:
                    if (action.equals(str)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c == 0)
            {
                Intent intent = new Intent(this, BubbleControlService.class);
                intent.setAction(str3);
                startService(intent);
            } else if (c == 1) {
                Intent intent2 = new Intent(this, BubbleControlService.class);
                intent2.setAction(str5);
                startService(intent2);
            } else if (c == 2) {
                Intent intent3 = new Intent(this, RecorderService.class);
                intent3.setAction(str4);
                startService(intent3);
            } else if (c == 3) {
                Intent intent4 = new Intent(this, RecorderService.class);
                intent4.setAction(str);
                startService(intent4);
            } else if (c == 4) {
                Log.e(NotificationCompat.CATEGORY_STATUS, "resume click");
                Intent intent5 = new Intent(this, RecorderService.class);
                intent5.setAction(str2);
                startService(intent5);
            }
            finish();
        } catch (Exception unused) {
        }
    }
}
