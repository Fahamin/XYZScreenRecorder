package com.xyz.screen.recorder.CoderlyticsActivities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.BaseActivity;
import com.xyz.screen.recorder.adsMAnager.FacebookInterAds;

public class SplashScreenActivity extends BaseActivity {
    public static boolean isFirstOpen = true;
    Handler mHandler;
    Runnable r;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView((int) R.layout.activity_splash_screen);

        isFirstOpen = true;

        new FacebookInterAds().loadFbInterSplash(this);
        this.mHandler = new Handler();
        this.r = new Runnable()
        {
            public void run() {

                checkPermission();

            }
        };
        this.mHandler.postDelayed(this.r, 5000);




    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != 12)
        {
            return;
        }
        if (isSystemAlertPermissionGranted(this))
        {
            onPermissionGranted();
            return;
        }
        Toast.makeText(this, R.string.str_permission_remind, Toast.LENGTH_LONG).show();
        finishAffinity();
    }

    @SuppressLint({"NewApi"})
    public static boolean isSystemAlertPermissionGranted(Context context) {
        if (VERSION.SDK_INT < 23) {
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    @SuppressLint({"NewApi"})
    public void checkPermission() {
        if (isSystemAlertPermissionGranted(this))
        {
            onPermissionGranted();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("package:");
        sb.append(BuildConfig.APPLICATION_ID);
        startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(sb.toString())), 12);
        startActivity(new Intent(this, PermissionHintActivity.class));
    }

    private void onPermissionGranted()
    {
        try {

                {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    intent.setFlags(335544320);
                    startActivity(intent);
                    finish();
                }

        } catch (Exception unused) {
        }
    }




    @Override
    public void onDestroy() {
        Handler handler = this.mHandler;
        if (handler != null) {
            Runnable runnable = this.r;
            if (runnable != null) {
                handler.removeCallbacks(runnable);
            }
        }
        super.onDestroy();
    }
}
