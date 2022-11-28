package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.PrefUtils;
import com.xyz.screen.recorder.CoderlyticsActivities.TakeScreenShotActivity;

public class BubbleControlCaptureService extends Service implements OnClickListener {
    private final int TIME_DELAY = 2000;
    private IBinder binder = new ServiceBinder();
    
    public LinearLayout floatingControls;
    
    public GestureDetector gestureDetector;
    
    public Handler handler = new Handler();
    private int height;
    
    public ImageView img;
    
    public boolean isOverRemoveView;
    
    public View mRemoveView;
    
    public int[] overlayViewLocation = {0, 0};
    
    public LayoutParams params;
    
    public SharedPreferences prefs;
    public BroadcastReceiver receiverCapture = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                int i = intent.getExtras().getInt("capture");
                if (i == 0) {
                    floatingControls.setVisibility(View.INVISIBLE);
                } else if (i == 1) {
                    floatingControls.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    
    public int[] removeViewLocation = {0, 0};
    
    public Runnable runnable = new Runnable() {
        public void run() {
            setAlphaAssistiveIcon();
        }
    };
    
    public Vibrator vibrate;
    
    public int width;
    
    public WindowManager windowManager;

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }


        public BubbleControlCaptureService getService() {
            return BubbleControlCaptureService.this;
        }
    }

    
    public boolean isPointInArea(int i, int i2, int i3, int i4, int i5) {
        return i >= i3 - i5 && i <= i3 + i5 && i2 >= i4 - i5 && i2 <= i4 + i5;
    }

    
    public void setAlphaAssistiveIcon() {
        ViewGroup.LayoutParams layoutParams = this.img.getLayoutParams();
        int i = this.width;
        layoutParams.height = i / 10;
        layoutParams.width = i / 10;
        this.img.setImageResource(R.drawable.ic_camera_service);
        this.floatingControls.setAlpha(0.5f);
        this.img.setLayoutParams(layoutParams);
        if (this.params.x < this.width - this.params.x) {
            this.params.x = 0;
        } else {
            this.params.x = this.width;
        }
        try {
            this.windowManager.updateViewLayout(this.floatingControls, this.params);
        }catch (Exception e){
            Toast.makeText(this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onCreate() {
        super.onCreate();
        this.vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        this.floatingControls = (LinearLayout) ((LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.swimbutton_layout_control_capture, null);
        this.img = (ImageView) this.floatingControls.findViewById(R.id.imgIcon);
        this.mRemoveView = onGetRemoveView();
        setupRemoveView(this.mRemoveView);
        LayoutParams layoutParams = new LayoutParams(-2, -2, 2038, 8, -3);
        this.params = layoutParams;
        if (VERSION.SDK_INT < 26) {
            this.params.type = 2005;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        this.height = displayMetrics.heightPixels;
        this.width = displayMetrics.widthPixels;
        LayoutParams layoutParams2 = this.params;
        layoutParams2.gravity = 8388659;
        layoutParams2.x = this.width;
        layoutParams2.y = this.height / 2;
        this.gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }
        });
        this.floatingControls.setOnTouchListener(new OnTouchListener() {
            private boolean flag = false;
            private float initialTouchX;
            private float initialTouchY;
            private int initialX;
            private int initialY;
            private boolean oneRun = false;
            private LayoutParams paramsF = params;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                handler.removeCallbacks(runnable);
                if (gestureDetector.onTouchEvent(motionEvent)) {
                    mRemoveView.setVisibility(View.GONE);
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 2000);
                    openCapture();
                } else {
                    int action = motionEvent.getAction();
                    if (action == 0) {
                        ViewGroup.LayoutParams layoutParams = img.getLayoutParams();
                        layoutParams.height = width / 8;
                        layoutParams.width = width / 8;
                        img.setLayoutParams(layoutParams);
                        floatingControls.setAlpha(1.0f);
                        this.initialX = this.paramsF.x;
                        this.initialY = this.paramsF.y;
                        this.initialTouchX = motionEvent.getRawX();
                        this.initialTouchY = motionEvent.getRawY();
                        this.flag = true;
                    } else if (action == 1) {
                        this.flag = false;
                        if (params.x < width - params.x) {
                            params.x = 0;
                        } else {
                            params.x = width - floatingControls.getWidth();
                        }
                        if (isOverRemoveView) {
                            prefs.edit().putBoolean(CoderlyticsConstants.PREFS_TOOLS_CAPTURE, false).apply();
                            stopSelf();
                        } else {
                            windowManager.updateViewLayout(floatingControls, params);
                            handler.postDelayed(runnable, 2000);
                        }
                        mRemoveView.setVisibility(View.GONE);
                    } else if (action == 2) {
                        int rawX = (int) (motionEvent.getRawX() - this.initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - this.initialTouchY);
                        LayoutParams layoutParams2 = this.paramsF;
                        layoutParams2.x = this.initialX + rawX;
                        layoutParams2.y = this.initialY + rawY;
                        if (this.flag) {
                            mRemoveView.setVisibility(View.VISIBLE);
                        }
                        windowManager.updateViewLayout(floatingControls, this.paramsF);
                        floatingControls.getLocationOnScreen(overlayViewLocation);
                        mRemoveView.getLocationOnScreen(removeViewLocation);
                        BubbleControlCaptureService bubbleControlCaptureService = BubbleControlCaptureService.this;
                        bubbleControlCaptureService.isOverRemoveView = bubbleControlCaptureService.isPointInArea(bubbleControlCaptureService.overlayViewLocation[0], overlayViewLocation[1], removeViewLocation[0], removeViewLocation[1], mRemoveView.getWidth());
                        if (isOverRemoveView) {
                            if (this.oneRun) {
                                if (VERSION.SDK_INT < 26) {
                                    vibrate.vibrate(200);
                                } else {
                                    vibrate.vibrate(VibrationEffect.createOneShot(200, 255));
                                }
                            }
                            this.oneRun = false;
                        } else {
                            this.oneRun = true;
                        }
                    } else if (action == 3) {
                        mRemoveView.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        addBubbleView();
        this.handler.postDelayed(this.runnable, 2000);
        registerReceiver(this.receiverCapture, new IntentFilter(CoderlyticsConstants.ACTION_SCREEN_SHOT));
    }

    private void setupRemoveView(View view) {
        view.setVisibility(View.GONE);
        this.windowManager.addView(view, newWindowManagerLayoutParamsForRemoveView());
    }

    private static LayoutParams newWindowManagerLayoutParamsForRemoveView() {
        LayoutParams layoutParams = new LayoutParams(-2, -2, VERSION.SDK_INT < 26 ? 2002 : 2038, 262664, -3);
        layoutParams.gravity = 81;
        layoutParams.y = 56;
        return layoutParams;
    }

    
    @SuppressLint({"InflateParams"})
    public View onGetRemoveView() {
        return LayoutInflater.from(this).inflate(R.layout.content_removeview_overlay, null);
    }

    @SuppressLint({"WrongConstant"})
    public int onStartCommand(Intent intent, int i, int i2) {
        ViewGroup.LayoutParams layoutParams = this.img.getLayoutParams();
        int i3 = this.width;
        layoutParams.height = i3 / 8;
        layoutParams.width = i3 / 8;
        this.img.setLayoutParams(layoutParams);
        this.floatingControls.setAlpha(1.0f);
        return super.onStartCommand(intent, i, i2);
    }

    public void addBubbleView() {
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null) {
            LinearLayout linearLayout = this.floatingControls;
            if (linearLayout != null) {
                windowManager2.addView(linearLayout, this.params);
            }
        }
    }

    public void removeBubbleView() {
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null) {
            LinearLayout linearLayout = this.floatingControls;
            if (linearLayout != null) {
                windowManager2.removeView(linearLayout);
            }
        }
    }

    public void onClick(View view) {
        view.getId();
        if (PrefUtils.readBooleanValue(this, getString(R.string.preference_vibrate_key), true)) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
        }
    }

    
    public void openCapture() {
        Intent intent = new Intent(this, TakeScreenShotActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onDestroy() {
        removeBubbleView();
        unregisterReceiver(this.receiverCapture);
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null) {
            View view = this.mRemoveView;
            if (view != null) {
                windowManager2.removeView(view);
            }
        }
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Binding successful!");
        return this.binder;
    }
}
