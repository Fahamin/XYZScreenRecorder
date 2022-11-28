package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import androidx.annotation.Nullable;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsActivities.PermissionTakingCameraActivity;

public class ToolsService extends Service implements OnTouchListener, OnClickListener {
    private static final int NOTIFICATION_ID = 161;
    private RelativeLayout mLayout;
    private NotificationManager mNotificationManager;
    private LayoutParams mParams;
    
    public SharedPreferences prefs;
    private WindowManager windowManager;

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    private void initView() {
        this.windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        this.mLayout = (RelativeLayout) ((LayoutInflater) getApplicationContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.contenct_dialog_tools, null);

        ImageView imageView = (ImageView) this.mLayout.findViewById(R.id.imv_close);
        Switch switchR = (Switch) this.mLayout.findViewById(R.id.sw_capture);
        Switch switchR2 = (Switch) this.mLayout.findViewById(R.id.sw_camera);
        Switch switchR3 = (Switch) this.mLayout.findViewById(R.id.sw_brush);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                stopSelf();
            }
        });
        switchR.setChecked(this.prefs.getBoolean(CoderlyticsConstants.PREFS_TOOLS_CAPTURE, false));
        switchR2.setChecked(this.prefs.getBoolean(CoderlyticsConstants.PREFS_TOOLS_CAMERA, false));
        switchR3.setChecked(this.prefs.getBoolean(CoderlyticsConstants.PREFS_TOOLS_BRUSH, false));
        switchR.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_CAPTURE, z).apply();
                Intent intent = new Intent(ToolsService.this, BubbleControlCaptureService.class);
                if (!z) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });
        switchR2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_CAMERA, z).apply();
                Intent intent = new Intent(ToolsService.this, PermissionTakingCameraActivity.class);
                intent.addFlags(268435456);
                intent.putExtra("boolean", z);
                startActivity(intent);
                stopSelf();
            }
        });
        switchR3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_BRUSH, z).apply();
                Intent intent = new Intent(ToolsService.this, SwimControlBrushService.class);
                if (!z) {
                    stopService(intent);
                } else {
                    startService(intent);
                }
            }
        });
        LayoutParams layoutParams = new LayoutParams(-1, -1, 2038, 8, -3);
        this.mParams = layoutParams;
        if (VERSION.SDK_INT < 26) {
            this.mParams.type = 2005;
        }
        this.windowManager.addView(this.mLayout, this.mParams);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        initView();
        return START_NOT_STICKY;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.imv_close) {
            stopSelf();
        }
    }

    public void onDestroy() {
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null) {

            if (mLayout != null) {
                windowManager2.removeView(mLayout);
            }
        }
        super.onDestroy();
    }
}
