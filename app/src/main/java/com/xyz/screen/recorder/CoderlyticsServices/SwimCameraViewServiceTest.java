package com.xyz.screen.recorder.CoderlyticsServices;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;

public class SwimCameraViewServiceTest extends Service implements OnClickListener {
    private static SwimCameraViewServiceTest context;
    private IBinder binder = new ServiceBinder();
    ImageView btnOptions;
    private Handler handler = new Handler();
    private boolean isCameraViewHidden;
    
    public View mCurrentView;
    private ConstraintLayout mFloatingView;
    
    public WindowManager mWindowManager;
    
    public LayoutParams params;
    private SharedPreferences prefs;
    ConstraintLayout viewOptions;

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        
        public SwimCameraViewServiceTest getService() {
            return SwimCameraViewServiceTest.this;
        }
    }

    public SwimCameraViewServiceTest() {
        context = this;
    }

    public IBinder onBind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Binding successful!");
        return this.binder;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Unbinding and stopping service");
        stopSelf();
        return super.onUnbind(intent);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            this.mFloatingView = (ConstraintLayout) ((LayoutInflater) getSystemService("layout_inflater")).inflate(R.layout.content_layout_swimbutton, null);
            this.btnOptions = (ImageView) this.mFloatingView.findViewById(R.id.imgIcon);
            this.btnOptions.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (viewOptions.isShown()) {
                        viewOptions.setVisibility(View.INVISIBLE);
                    } else {
                        viewOptions.setVisibility(View.VISIBLE);
                    }
                }
            });
            this.mCurrentView = this.mFloatingView;
            int xPos = getXPos();
            int yPos = getYPos();
            LayoutParams layoutParams = new LayoutParams(-2, -2, VERSION.SDK_INT < 26 ? 2005 : 2038, 8, -3);
            this.params = layoutParams;
            this.params.gravity = 8388659;
            this.params.x = xPos;
            this.params.y = yPos;
            this.mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            this.mWindowManager.addView(this.mCurrentView, this.params);
            setupDragListener();
        } catch (Exception e) {
            e.getMessage();
        }
        return START_STICKY;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    private void setupDragListener() {
        try {
            this.mCurrentView.setOnTouchListener(new OnTouchListener() {
                private float initialTouchX;
                private float initialTouchY;
                private int initialX;
                private int initialY;
                boolean isMoving = false;
                private LayoutParams paramsF = params;

                public boolean onTouch(View view, MotionEvent motionEvent) {
                    try {
                        int action = motionEvent.getAction();
                        if (action != 0) {
                            if (action != 1) {
                                if (action == 2) {
                                    int rawX = (int) (motionEvent.getRawX() - this.initialTouchX);
                                    int rawY = (int) (motionEvent.getRawY() - this.initialTouchY);
                                    this.paramsF.x = this.initialX + rawX;
                                    this.paramsF.y = this.initialY + rawY;
                                    if (Math.abs(rawX) > 10 || Math.abs(rawY) > 10) {
                                        this.isMoving = true;
                                    }
                                    mWindowManager.updateViewLayout(mCurrentView, this.paramsF);
                                    persistCoordinates(this.initialX + rawX, this.initialY + rawY);
                                    return true;
                                }
                            }
                            return false;
                        }
                        this.isMoving = false;
                        this.initialX = this.paramsF.x;
                        this.initialY = this.paramsF.y;
                        this.initialTouchX = motionEvent.getRawX();
                        this.initialTouchY = motionEvent.getRawY();
                        return true;
                    } catch (Exception unused) {
                    }
                return false;
                }
            });
        } catch (Exception unused) {
        }
    }

    private int getXPos() {
        return Integer.parseInt(getDefaultPrefs().getString(CoderlyticsConstants.PREFS_CAMERA_OVERLAY_POS, "0X100").split("X")[0]);
    }

    private int getYPos() {
        return Integer.parseInt(getDefaultPrefs().getString(CoderlyticsConstants.PREFS_CAMERA_OVERLAY_POS, "0X100").split("X")[1]);
    }

    
    public void persistCoordinates(int i, int i2) {
        Editor edit = getDefaultPrefs().edit();
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(i));
        sb.append("X");
        sb.append(String.valueOf(i2));
        edit.putString(CoderlyticsConstants.PREFS_CAMERA_OVERLAY_POS, sb.toString()).apply();
    }

    private SharedPreferences getDefaultPrefs() {
        if (this.prefs == null) {
            this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return this.prefs;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View view) {
        try {
            view.getId();
        } catch (Exception unused) {
        }
    }
}
