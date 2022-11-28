package com.xyz.screen.recorder.CoderlyticsActivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.BaseActivity;

public class PermissionHintActivity extends BaseActivity {
  @Override
    public void onBackPressed()
    {

    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_dialogpermission);
        findViewById(R.id.na_guide_ok).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void openAndroidPermissionsMenu()
    {
        try {
            Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            StringBuilder sb = new StringBuilder();
            sb.append("package:");
            sb.append(BuildConfig.APPLICATION_ID);
            intent.setData(Uri.parse(sb.toString()));
            startActivity(intent);
        } catch (Exception unused) {
        }
    }
}
