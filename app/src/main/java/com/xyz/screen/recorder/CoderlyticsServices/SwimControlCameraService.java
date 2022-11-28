package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.Nullable;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;

public class SwimControlCameraService extends Service {
    private IBinder binder = new ServiceBinder();
    private ServiceConnection floatingCameraConnection = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName componentName) {
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ((SwimCameraViewService.ServiceBinder) iBinder).getService();
        }
    };
    private SharedPreferences prefs;

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        
        public SwimControlCameraService getService() {
            return SwimControlCameraService.this;
        }
    }

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    public int onStartCommand(Intent intent, int i, int i2) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent2 = new Intent(this, SwimCameraViewService.class);
        startService(intent2);
        bindService(intent2, this.floatingCameraConnection, 1);
        return super.onStartCommand(intent, i, i2);
    }

    public void onDestroy() {
        unbindService(this.floatingCameraConnection);
        this.prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_BRUSH, false).apply();
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Binding successful!");
        return this.binder;
    }
}
