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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.ItemTouchHelper.Callback;

import com.xyz.screen.recorder.R;
import com.CoderlyticsCameraView.CameraView;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;

public class SwimCameraViewService extends Service implements OnClickListener {
    
    public static SwimCameraViewService context;
    private IBinder binder = new ServiceBinder();
    private CameraView cameraView;
    
    public Handler handler = new Handler();
    
    public ImageButton hideCameraBtn;
    private boolean isCameraViewHidden;
    
    public View mCurrentView;
    private LinearLayout mFloatingView;
    
    public WindowManager mWindowManager;
    private OverlayResize overlayResize = OverlayResize.MINWINDOW;
    
    public LayoutParams params;
    private SharedPreferences prefs;
    
    public ImageButton resizeOverlay;
    
    public Runnable runnable = new Runnable() {
        public void run() {
            resizeOverlay.setVisibility(View.GONE);
            hideCameraBtn.setVisibility(View.GONE);
            switchCameraBtn.setVisibility(View.GONE);
        }
    };
    
    public ImageButton switchCameraBtn;
    private Values values;

    private enum OverlayResize {
        MAXWINDOW,
        MINWINDOW
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        
        public SwimCameraViewService getService() {
            return SwimCameraViewService.this;
        }
    }

    private class Values {
        int bigCameraX;
        int bigCameraY;
        int cameraHideX = dpToPx(60);
        int cameraHideY = dpToPx(60);
        int smallCameraX;
        int smallCameraY;

        public Values() {
            buildValues();
        }

        private int dpToPx(int i) {
            return Math.round(((float) i) * (getResources().getDisplayMetrics().xdpi / 160.0f));
        }

        
        public void buildValues() {
            if (context.getResources().getConfiguration().orientation == 2) {
                smallCameraX = dpToPx(160);
                smallCameraY = dpToPx(120);
                bigCameraX = dpToPx(Callback.DEFAULT_DRAG_ANIMATION_DURATION);
                bigCameraY = dpToPx(150);
                return;
            }
            smallCameraX = dpToPx(120);
            smallCameraY = dpToPx(160);
            bigCameraX = dpToPx(150);
            bigCameraY = dpToPx(Callback.DEFAULT_DRAG_ANIMATION_DURATION);
        }
    }

    public SwimCameraViewService() {
        context = this;
    }

    public IBinder onBind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Binding successful!");
        return binder;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Unbinding and stopping service");
        stopSelf();
        return super.onUnbind(intent);
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        mFloatingView = (LinearLayout) ((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.content_cameraview_bubble, null);
        cameraView = (CameraView) mFloatingView.findViewById(R.id.cameraView);
        hideCameraBtn =  mFloatingView.findViewById(R.id.hide_camera);
        switchCameraBtn =  mFloatingView.findViewById(R.id.switch_camera);
        resizeOverlay =  mFloatingView.findViewById(R.id.overlayResize);
        values = new Values();
        hideCameraBtn.setOnClickListener(this);
        switchCameraBtn.setOnClickListener(this);
        resizeOverlay.setOnClickListener(this);
        mCurrentView = mFloatingView;
        int xPos = getXPos();
        int yPos = getYPos();
        LayoutParams layoutParams = new LayoutParams(values.smallCameraX, values.smallCameraY, VERSION.SDK_INT < 26 ? 2005 : 2038, 8, -3);
        params = layoutParams;
        LayoutParams layoutParams2 = params;
        layoutParams2.gravity = 8388659;
        layoutParams2.x = xPos;
        layoutParams2.y = yPos;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mCurrentView, params);


        cameraView.start();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resizeOverlay.setVisibility(View.GONE);
                hideCameraBtn.setVisibility(View.GONE);
                switchCameraBtn.setVisibility(View.GONE);
                if (cameraView.getFacing() == 0)
                {
                    cameraView.setFacing(1);
                    cameraView.setAutoFocus(true);
                    return;
                }
                cameraView.setFacing(0);
                cameraView.setAutoFocus(true);
            }
        },500);

        setupDragListener();
        return START_STICKY;
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        changeCameraOrientation();
    }

    private void changeCameraOrientation() {
        values.buildValues();
        int i = overlayResize == OverlayResize.MAXWINDOW ? values.bigCameraX : values.smallCameraX;
        int i2 = overlayResize == OverlayResize.MAXWINDOW ? values.bigCameraY : values.smallCameraY;
        if (!isCameraViewHidden) {
            LayoutParams layoutParams = params;
            layoutParams.height = i2;
            layoutParams.width = i;
            try {
                mWindowManager.updateViewLayout(mCurrentView, layoutParams);

            }
            catch (Exception d)
            {

            }
        }
    }

    private void setupDragListener() {
        mCurrentView.setOnTouchListener(new OnTouchListener() {
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
            boolean isMoving = false;
            private LayoutParams paramsF = params;

            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                int action = motionEvent.getAction();
                if (action != 0) {
                    if (action == 1) {
                        handler.postDelayed(runnable, 3000);
                    } else if (action == 2) {
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                        LayoutParams layoutParams = paramsF;
                        layoutParams.x = initialX + rawX;
                        layoutParams.y = initialY + rawY;
                        if (Math.abs(rawX) > 10 || Math.abs(rawY) > 10) {
                            isMoving = true;
                        }
                        mWindowManager.updateViewLayout(mCurrentView, paramsF);
                        persistCoordinates(initialX + rawX, initialY + rawY);
                        return true;
                    }
                    return false;
                }
                if (resizeOverlay.isShown())
                {
                    resizeOverlay.setVisibility(View.GONE);
                    hideCameraBtn.setVisibility(View.GONE);
                    switchCameraBtn.setVisibility(View.GONE);
                }
                else
                    {
                    resizeOverlay.setVisibility(View.VISIBLE);
                    hideCameraBtn.setVisibility(View.VISIBLE);
                    switchCameraBtn.setVisibility(View.VISIBLE);
                    handler.removeCallbacks(runnable);
                }
                isMoving = false;
                initialX = paramsF.x;
                initialY = paramsF.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                return true;
            }
        });
        resizeOverlay.setOnTouchListener(new OnTouchListener() {
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
@Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action != 0) {
                    if (action == 1) {
                        handler.postDelayed(runnable, 3000);
                    } else if (action == 2) {
                        if (resizeOverlay.isShown()) {
                            handler.removeCallbacks(runnable);
                        }
                        params.width = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        params.height = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        mWindowManager.updateViewLayout(mCurrentView, params);
                        return true;
                    }
                    return false;
                }
                initialX = params.width;
                initialY = params.height;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                return true;
            }
        });
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
        if (prefs == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return prefs;
    }
@Override
    public void onDestroy() {
        super.onDestroy();
        if(prefs !=null)
            prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_CAMERA, false).apply();
        if (mFloatingView != null) {
            handler.removeCallbacks(runnable);
            mWindowManager.removeView(mCurrentView);
            cameraView.stop();
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.hide_camera)
        {
            Log.d(CoderlyticsConstants.TAG, "hide camera");
            if (mCurrentView.equals(mFloatingView)) {
                mWindowManager.removeViewImmediate(mCurrentView);
                cameraView.stop();
                mFloatingView = null;
            }
            prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_CAMERA, false).apply();
            setupDragListener();
        } else if (id == R.id.switch_camera)
        {
            if (cameraView.getFacing() == 0)
            {
                cameraView.setFacing(1);
                cameraView.setAutoFocus(true);
                return;
            }
            cameraView.setFacing(0);
            cameraView.setAutoFocus(true);
        }
    }

    private void showCameraView() {
        mWindowManager.removeViewImmediate(mCurrentView);
        mCurrentView = mFloatingView;
        mWindowManager.addView(mCurrentView, params);
        isCameraViewHidden = false;
        setupDragListener();
    }
}
