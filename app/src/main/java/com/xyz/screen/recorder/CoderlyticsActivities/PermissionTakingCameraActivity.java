package com.xyz.screen.recorder.CoderlyticsActivities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsServices.SwimControlCameraService;

public class PermissionTakingCameraActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        if (getIntent() != null) {
            String str = "boolean";
            if (getIntent().getExtras().containsKey(str)) {
                requestCameraPermission(getIntent().getExtras().getBoolean(str));
                requestSystemWindowsPermission(CoderlyticsConstants.CAMERA_SYSTEM_WINDOWS_CODE);
            }
        }
    }

    public void requestCameraPermission(boolean z) {
        String str = "android.permission.CAMERA";
        if (ContextCompat.checkSelfPermission(this, str) != 0) {
            ActivityCompat.requestPermissions(this, new String[]{str}, CoderlyticsConstants.CAMERA_REQUEST_CODE);
            return;
        }
        Intent intent = new Intent(this, SwimControlCameraService.class);
        if (!z) {
            stopService(intent);
        } else {
            startService(intent);
        }
        finish();
    }

    @TargetApi(23)
    public void requestSystemWindowsPermission(int i) {
        if (!Settings.canDrawOverlays(this)) {
            StringBuilder sb = new StringBuilder();
            sb.append("package:");
            sb.append(BuildConfig.APPLICATION_ID);
            startActivityForResult(new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse(sb.toString())), i);
        }
    }
    @Override
    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 1116) {
            return;
        }
        if (iArr.length <= 0 || iArr[0] == 0) {
            startService(new Intent(this, SwimControlCameraService.class));
            finish();
            return;
        }
        finish();
    }
}
