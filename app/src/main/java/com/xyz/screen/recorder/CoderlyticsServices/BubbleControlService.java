package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.CountDownTimer;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.internal.view.SupportMenu;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsRecorderApplication;
import com.xyz.screen.recorder.CoderlyticsActivities.MainActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants.RecordingState;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.PrefUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverInterface;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.ErrorRecordService;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbClickBlur;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbRecordTime;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStageRecord;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStartRecord;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStopService;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.HideService;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.ShowService;
import com.xyz.screen.recorder.CoderlyticsActivities.StartRecorderActivity;
import com.xyz.screen.recorder.CoderlyticsActivities.TakeRequestRecorderActivity;
import com.xyz.screen.recorder.CoderlyticsActivities.TakeScreenShotActivity;
import com.xyz.screen.recorder.CoderlyticsActivities.SplashScreenActivity;
import java.util.ArrayList;

public class BubbleControlService extends Service implements OnClickListener, ObserverInterface {
    public static final String ACTION_NOTIFICATION_BUTTON_CLICK = "recorder acction click notification";
    public static final String EXTRA_BUTTON_CLICKED = "recorder extra click button";
    private static BubbleControlService instance = null;
    public static boolean isCountdown = false;
    public static boolean isExpand = false;
    public static boolean isPause = false;
    public static boolean isRecording = false;
    
    public static boolean isRightSide = true;
    private final int NOTIFICATION_ID = 212;
    private final int NOTIFICATION_ID_NEW = 213;
    private final int TIME_DELAY = 2000;
    private IBinder binder = new ServiceBinder();
    
    public View controlsMain;
    
    public View controlsMainLeft;
    
    public View controlsRecorder;
    
    public View controlsRecorderLeft;
    
    public LinearLayout floatingControls;
    
    public GestureDetector gestureDetector;
    
    public Handler handler = new Handler();
    
    public int height;
    
    public ImageView img;
    private ImageView imgBrush;
    private ImageView imgBrushLeft;
    public boolean isBottom = false;
    public boolean isCollapRecord = false;
    public boolean isMove = false;
    
    public boolean isOverRemoveView;
    public boolean isTop = false;
    
    public FrameLayout layoutTime;
    private LinearLayout layoutTimer;
    
    public View mRemoveView;
    
    public int[] overlayViewLocation = {0, 0};
    private ImageView panelIB;
    private ImageView panelLeftIB;
    
    public LayoutParams params;
    private LayoutParams paramsClose;
    private LayoutParams paramsTimer;

    public ImageView pauseIB;
    
    public ImageView pauseLeftIB;
    private SharedPreferences prefs;
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(BubbleControlService.EXTRA_BUTTON_CLICKED, -1)) {
                case R.id.capture :
                    Intent intent2 = new Intent(context, TakeScreenShotActivity.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utils.startActivityAllStage(context, intent2);
                    break;
                case R.id.close :
                    onDestroy();
                    stopSelf();
                    stopForeground(true);
                    break;
                case R.id.notification_layout_main_container :
                    if (Utils.isAppOnForeground(context)) {
                        if (!(CoderlyticsRecorderApplication.getInstance().getTopActivity() instanceof MainActivity)) {
                            Intent intent3 = new Intent(context, MainActivity.class);
                            intent3.addFlags(335544320);
                            Utils.startActivityAllStage(context, intent3);
                            break;
                        }
                    } else {
                        Intent intent4 = new Intent(context, MainActivity.class);
                        intent4.addFlags(335544320);
                        Utils.startActivityAllStage(context, intent4);
                        break;
                    }
                    break;
                case R.id.pause_new :
                    Intent intent5 = new Intent(context, StartRecorderActivity.class);
                    intent5.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent5.setAction(CoderlyticsConstants.SCREEN_RECORDING_PAUSE);
                    Utils.startActivityAllStage(context, intent5);
                    break;
                case R.id.record :
                    Intent intent6 = new Intent(context, StartRecorderActivity.class);
                    intent6.setAction(CoderlyticsConstants.SCREEN_RECORDING_START_FROM_NOTIFY);
                    intent6.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utils.startActivityAllStage(context, intent6);
                    break;
                case R.id.resume_new :
                    Intent intent7 = new Intent(context, StartRecorderActivity.class);
                    intent7.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent7.setAction(CoderlyticsConstants.SCREEN_RECORDING_RESUME);
                    Utils.startActivityAllStage(context, intent7);
                    break;
                case R.id.stop_new :
                    Intent intent8 = new Intent(context, StartRecorderActivity.class);
                    intent8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent8.setAction(CoderlyticsConstants.SCREEN_RECORDING_STOP);
                    Utils.startActivityAllStage(context, intent8);
                    break;
                case R.id.tools :
                    openTools();
                    break;
                case R.id.tools_new :
                    openTools();
                    break;
            }
            context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        }
    };
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
    private ImageView recorderIB;
    private ImageView recorderLeftIB;
    
    public int[] removeViewLocation = {0, 0};
    
    public ImageView resumeIB;
    
    public ImageView resumeLeftIB;
    private ImageView rewardIB;
    private Runnable runAnim = new Runnable() {
        public void run() {
            if (tvTime.getAlpha() == 0.5f) {
                tvTime.setAlpha(1.0f);
            } else {
                tvTime.setAlpha(0.5f);
            }
            handler.postDelayed(this, 800);
        }
    };
    
    public Runnable runnable = new Runnable() {
        public void run() {
            collapseFloatingControls();
            setAlphaAssistiveIcon();
        }
    };
    private Runnable runnableRecord = new Runnable() {
        public void run() {
            if (!isMove) {
                
                isCollapRecord = true;
                ViewGroup.LayoutParams layoutParams = layoutTime.getLayoutParams();
                layoutParams.height = Utils.convertDpToPixel(48.0f, BubbleControlService.this);
                layoutParams.width = Utils.convertDpToPixel(24.0f, BubbleControlService.this);
                img.setVisibility(View.GONE);
                tvTime.setVisibility(View.GONE);
                if (isRightSide) {
                    layoutTime.setBackgroundResource(R.drawable.ic_record_dot_right);
                } else {
                    layoutTime.setBackgroundResource(R.drawable.ic_record_dot);
                }
                floatingControls.setAlpha(0.5f);
                layoutTime.setLayoutParams(layoutParams);
            }
        }
    };
    private ImageView screenshotIB;
    private ImageView screenshotLeftIB;
    private ImageView stopIB;
    private ImageView stopLeftIB;
    private ImageView toolRecordLeft;
    private ImageView toolsRecord;
    
    public TextView tvTime;
    
    public TextView txtTimer;
    
    public Vibrator vibrate;
    
    public int width;
    
    public WindowManager windowManager;
    
    public int yTranstion = 0;


    static class AnonymousClass12 {
        static final  int[] $SwitchMap$com$test$screenrecord$common$Const$RecordingState = new int[RecordingState.values().length];

    
      static {
            $SwitchMap$com$test$screenrecord$common$Const$RecordingState[RecordingState.PAUSED.ordinal()] = 1;
            $SwitchMap$com$test$screenrecord$common$Const$RecordingState[RecordingState.RECORDING.ordinal()] = 2;
            try {
                $SwitchMap$com$test$screenrecord$common$Const$RecordingState[RecordingState.STOPPED.ordinal()] = 3;
            } catch (NoSuchFieldError unused) {
            }
        }
    }

    public class ServiceBinder extends Binder {
        public ServiceBinder() {
        }

        
        public BubbleControlService getService() {
            return BubbleControlService.this;
        }
    }

    
    public boolean isPointInArea(int i, int i2, int i3, int i4, int i5) {
        return i >= i3 - i5 && i <= i3 + i5 && i2 >= i4 - i5 && i2 <= i4 + i5;
    }

    public static BubbleControlService getInstance() {
        return instance;
    }

    public static void setInstance(BubbleControlService bubbleControlService) {
        instance = bubbleControlService;
    }

    
    public void setAlphaAssistiveIcon() {
        if (floatingControls == null) {
            return;
        }
        if (controlsRecorder.getVisibility() != View.VISIBLE ||
                controlsMain.getVisibility() != View.VISIBLE ||
                controlsRecorderLeft.getVisibility() != View.VISIBLE ||
                controlsMainLeft.getVisibility() != View.VISIBLE) {
            ViewGroup.LayoutParams layoutParams = layoutTime.getLayoutParams();
            int i = width;
            layoutParams.height = i / 10;
            layoutParams.width = i / 10;
            img.setImageResource(R.drawable.icon64);
            floatingControls.setAlpha(0.5f);
            layoutTime.setLayoutParams(layoutParams);
            if (params.x < width - params.x) {
                params.x = 0;
            } else {
                params.x = width;
            }
            windowManager.updateViewLayout(floatingControls, params);
            if (isRecording) {
                handler.removeCallbacks(runnableRecord);
                handler.postDelayed(runnableRecord, 2000);
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        ObserverUtils.getInstance().registerObserver((ObserverInterface<ObserverUtils>) this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(CoderlyticsConstants.PREFS_TOOLS_BRUSH, false)) {
            openBrushControlService();
        }
        if (prefs.getBoolean(CoderlyticsConstants.PREFS_TOOLS_CAPTURE, false)) {
            openCaptureControlService();
        }
        if (prefs.getBoolean(CoderlyticsConstants.PREFS_TOOLS_CAMERA, false)) {
            openCameraControlService();
        }
        windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatingControls = (LinearLayout) layoutInflater.inflate(R.layout.content_layout_swimbutton, null);
        mRemoveView = onGetRemoveView();
        setupRemoveView(mRemoveView);
        layoutTimer = (LinearLayout) layoutInflater.inflate(R.layout.content_count_timing, null);
        txtTimer = (TextView) layoutTimer.findViewById(R.id.txt_timer);
        controlsRecorder = floatingControls.findViewById(R.id.controls_recorder);
        controlsRecorderLeft = floatingControls.findViewById(R.id.controls_recorder_left);
        controlsMain = floatingControls.findViewById(R.id.controls_main);
        controlsMainLeft = floatingControls.findViewById(R.id.controls_main_left);
        layoutTime = (FrameLayout) floatingControls.findViewById(R.id.layout_time);
        imgBrush =  floatingControls.findViewById(R.id.imgTools);
        imgBrushLeft =  floatingControls.findViewById(R.id.imgTools_left);
        img =  floatingControls.findViewById(R.id.imgIcon);
        tvTime = (TextView) floatingControls.findViewById(R.id.tv_time);
        floatingControls.post(new Runnable() {
            public void run() {

                yTranstion = (floatingControls.getHeight() / 2) - (layoutTime.getHeight() / 2);
            }
        });
        controlsMain.setVisibility(View.GONE);
        controlsMainLeft.setVisibility(View.GONE);
        controlsRecorder.setVisibility(View.GONE);
        controlsRecorderLeft.setVisibility(View.GONE);
        stopIB =  controlsRecorder.findViewById(R.id.stop);
        pauseIB =  controlsRecorder.findViewById(R.id.pause);
        resumeIB =  controlsRecorder.findViewById(R.id.resume);
        toolsRecord =  controlsRecorder.findViewById(R.id.tools_record);
        stopLeftIB =  controlsRecorderLeft.findViewById(R.id.stop_left);
        pauseLeftIB =  controlsRecorderLeft.findViewById(R.id.pause_left);
        resumeLeftIB =  controlsRecorderLeft.findViewById(R.id.resume_left);
        toolRecordLeft =  controlsRecorderLeft.findViewById(R.id.tools_record_left);
        recorderIB =  controlsMain.findViewById(R.id.recorder);
        screenshotIB =  controlsMain.findViewById(R.id.screenshot);
        panelIB =  controlsMain.findViewById(R.id.panel);
        recorderLeftIB =  controlsMainLeft.findViewById(R.id.recorder_left);
        screenshotLeftIB =  controlsMainLeft.findViewById(R.id.screenshot_left);
        panelLeftIB =  controlsMainLeft.findViewById(R.id.panel_left);
        stopIB.setOnClickListener(this);
        imgBrush.setOnClickListener(this);
        imgBrushLeft.setOnClickListener(this);
        stopLeftIB.setOnClickListener(this);
        recorderIB.setOnClickListener(this);
        recorderLeftIB.setOnClickListener(this);
        screenshotIB.setOnClickListener(this);
        screenshotLeftIB.setOnClickListener(this);
        panelIB.setOnClickListener(this);
        panelLeftIB.setOnClickListener(this);
        toolsRecord.setOnClickListener(this);
        toolRecordLeft.setOnClickListener(this);
        pauseIB.setOnClickListener(this);
        pauseLeftIB.setOnClickListener(this);
        resumeIB.setOnClickListener(this);
        resumeLeftIB.setOnClickListener(this);
        LayoutParams layoutParams = new LayoutParams(-2, -2, 2038, 8, -3);
        params = layoutParams;
        if (VERSION.SDK_INT < 26) {
            params.type = 2005;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        LayoutParams layoutParams2 = params;
        layoutParams2.gravity = 8388659;
        layoutParams2.x = 0;
        layoutParams2.y = height / 4;
        LayoutParams layoutParams3 = new LayoutParams(-2, -2, 2038, 8, -3);
        paramsTimer = layoutParams3;
        if (VERSION.SDK_INT < 26) {
            paramsTimer.type = 2005;
        }
        paramsTimer.gravity = 17;
        gestureDetector = new GestureDetector(this, new SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent motionEvent) {
                return true;
            }

            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return true;
            }
        });
        floatingControls.setOnTouchListener(new OnTouchListener() {
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
                    if (controlsRecorder.getVisibility() == View.VISIBLE ||
                            controlsMain.getVisibility() == View.VISIBLE ||
                            controlsRecorderLeft.getVisibility() == View.VISIBLE ||
                            controlsMainLeft.getVisibility() == View.VISIBLE) {
                        collapseFloatingControls();
                    } else {
                        expandFloatingControls();
                    }
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 2000);
                } else if (!BubbleControlService.isExpand) {
                    int action = motionEvent.getAction();
                    if (action == 0) {
                        if (BubbleControlService.isRecording) {
                            layoutTime.setBackgroundResource(R.drawable.bg_time);
                            ViewGroup.LayoutParams layoutParams = layoutTime.getLayoutParams();
                            layoutParams.height = width / 8;
                            layoutParams.width = width / 8;
                            tvTime.setVisibility(View.VISIBLE);
                            floatingControls.setAlpha(1.0f);
                            layoutTime.setLayoutParams(layoutParams);
                        } else {
                            ViewGroup.LayoutParams layoutParams2 = layoutTime.getLayoutParams();
                            layoutParams2.height = width / 8;
                            layoutParams2.width = width / 8;
                            layoutTime.setLayoutParams(layoutParams2);
                            floatingControls.setAlpha(1.0f);
                        }
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        flag = true;
                    } else if (action == 1) {

                        isMove = false;
                        if (params.x < width - params.x) {
                            params.x = 0;
                            isRightSide = true;
                        } else {
                            params.x = width - floatingControls.getWidth();
                            isRightSide = false;
                        }

                        isTop = params.y <= yTranstion;

                        isBottom = params.y >= (height - yTranstion) - floatingControls.getHeight();
                        if (params.y <= 0) {
                            params.y = 0;
                        } else if (params.y >= height - floatingControls.getHeight()) {
                            params.y = height;
                        }
                        flag = false;
                        if (isOverRemoveView) {
                            onDestroy();
                            stopSelf();
                            stopForeground(true);
                        } else {
                            windowManager.updateViewLayout(floatingControls, params);
                            handler.removeCallbacks(runnable);
                            handler.postDelayed(runnable, 2000);
                        }
                        mRemoveView.setVisibility(View.GONE);
                    } else if (action == 2) {
                        isMove = true;
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                        LayoutParams layoutParams3 = paramsF;
                        layoutParams3.x = initialX + rawX;
                        layoutParams3.y = initialY + rawY;
                        if (flag && !BubbleControlService.isRecording) {
                            mRemoveView.setVisibility(View.VISIBLE);
                        }
                        windowManager.updateViewLayout(floatingControls, paramsF);
                        floatingControls.getLocationOnScreen(overlayViewLocation);
                        mRemoveView.getLocationOnScreen(removeViewLocation);

                        isOverRemoveView = isPointInArea(overlayViewLocation[0], overlayViewLocation[1], removeViewLocation[0], removeViewLocation[1], mRemoveView.getWidth());
                        if (isOverRemoveView) {
                            if (oneRun) {
                                floatingControls.setY(mRemoveView.getY());
                                if (VERSION.SDK_INT < 26) {
                                    vibrate.vibrate(200);
                                } else {
                                    vibrate.vibrate(VibrationEffect.createOneShot(200, 255));
                                }
                            }
                            oneRun = false;
                        } else {
                            oneRun = true;
                        }
                    } else if (action == 3) {
                        mRemoveView.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        addBubbleView();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 2000);
        registerReceiver(receiverCapture, new IntentFilter(CoderlyticsConstants.ACTION_SCREEN_SHOT));
    }

    private void openCaptureControlService() {
        startService(new Intent(this, BubbleControlCaptureService.class));
    }

    private void openCameraControlService() {
        startService(new Intent(this, SwimControlCameraService.class));
    }

    private void openBrushControlService() {
        startService(new Intent(this, SwimControlBrushService.class));
    }

    private void setupRemoveView(View view) {
        view.setVisibility(View.GONE);
        windowManager.addView(view, newWindowManagerLayoutParamsForRemoveView());
    }

    private LayoutParams newWindowManagerLayoutParamsForRemoveView() {
        LayoutParams layoutParams = new LayoutParams(-2, -2, VERSION.SDK_INT < 26 ? 2002 : 2038, 262664, -3);
        paramsClose = layoutParams;
        LayoutParams layoutParams2 = paramsClose;
        layoutParams2.gravity = 81;
        layoutParams2.y = 56;
        return layoutParams2;
    }

    
    @SuppressLint({"InflateParams"})
    public View onGetRemoveView() {
        return LayoutInflater.from(this).inflate(R.layout.content_removeview_overlay, null);
    }

    @SuppressLint({"WrongConstant"})
    public int onStartCommand(Intent intent, int i, int i2) {
        if (getInstance() == null) {
            setInstance(this);
        }

        showNotification2();
        if (!(intent == null || intent.getAction() == null))
        {
            String action = intent.getAction();
            char c = 65535;

            switch (action.hashCode())
            {
                case -1996135482:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_START_FROM_NOTIFY)) {
                        c = 0;
                        break;
                    }
                    break;
                case -1053033865:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_STOP))
                    {
                        c = 3;
                        break;
                    }
                    break;
                case -453103993:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_START))
                    {
                        c = 2;
                        break;
                    }
                    break;
                case 143300674:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_DESTROY)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                handlerTimer();
            } else if (c == 1) {
                onDestroy();
                stopSelf();
                stopForeground(true);
                stopBrush();
                stopCamera();
                stopCapture();
            } else if (c == 2 || c == 3) {
                collapseFloatingControls();
            }
        }
        if (SplashScreenActivity.isFirstOpen && !isRecording && !isPause) {
            if (isRightSide) {
                controlsMain.setVisibility(0);
            } else {
                controlsMainLeft.setVisibility(0);
            }
            expandFloatingControls();
            ViewGroup.LayoutParams layoutParams = layoutTime.getLayoutParams();
            int i3 = width;
            layoutParams.height = i3 / 8;
            layoutParams.width = i3 / 8;
            layoutTime.setLayoutParams(layoutParams);
            floatingControls.setAlpha(1.0f);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (!BubbleControlService.isRecording && !BubbleControlService.isPause) {
                        if (BubbleControlService.isRightSide) {
                            controlsMain.setVisibility(8);
                        } else {
                            controlsMainLeft.setVisibility(8);
                        }
                        collapseFloatingControls();
                        setAlphaAssistiveIcon();
                    }
                }
            }, 2000);
            SplashScreenActivity.isFirstOpen = false;
        }
        return super.onStartCommand(intent, i, i2);
    }

    private void stopCapture() {
        stopService(new Intent(this, BubbleControlCaptureService.class));
    }

    private void stopCamera() {
        stopService(new Intent(this, SwimControlCameraService.class));
    }

    private void stopBrush() {
        stopService(new Intent(this, SwimControlBrushService.class));
    }

    public void addBubbleView() {
        WindowManager windowManager2 = windowManager;
        if (windowManager2 != null) {
            LinearLayout linearLayout = floatingControls;
            if (linearLayout != null) {
                windowManager2.addView(linearLayout, params);
            }
        }
    }

    public void addTimerView() {
        WindowManager windowManager2 = windowManager;
        if (windowManager2 != null) {
            LinearLayout linearLayout = layoutTimer;
            if (linearLayout != null) {
                windowManager2.addView(linearLayout, paramsTimer);
            }
        }
    }

    public void removeTimerView() {
        WindowManager windowManager2 = windowManager;
        if (windowManager2 != null) {
            LinearLayout linearLayout = layoutTimer;
            if (linearLayout != null) {
                windowManager2.removeView(linearLayout);
            }
        }
    }

    public void removeBubbleView() {
        try {
            if (windowManager != null && floatingControls != null) {
                windowManager.removeView(floatingControls);
            }
        } catch (Exception unused) {
        }
    }

    public void handlerTimer() {
        final Animation loadAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_tv);
        collapseFloatingControls();
        int intValue=1;
       try {
           intValue = Integer.parseInt(PrefUtils.readStringValue(this, getString(R.string.timer_key), "3"));

       }
       catch (Exception s)
       {

         intValue = Integer.valueOf(PrefUtils.readStringValue(this, getString(R.string.timer_key), "3")).intValue();


       }

        if (intValue == 0)
        {
            requestRecorder();
            return;
        }
        isCountdown = true;
        addTimerView();
        TextView textView = txtTimer;
        StringBuilder sb = new StringBuilder();
        int i = intValue + 1;
        sb.append(i);
        sb.append("");
        textView.setText(sb.toString());
        CountDownTimer r2 = new CountDownTimer((long) (i * 1000), 1000) {
            public void onFinish() {
                txtTimer.setText("");
                removeTimerView();
                requestRecorder();
                BubbleControlService.isCountdown = false;
            }

            public void onTick(long j) {
                TextView access$2600 = txtTimer;
                StringBuilder sb = new StringBuilder();
                sb.append(j / 1000);
                sb.append("");
                access$2600.setText(sb.toString());
                txtTimer.startAnimation(loadAnimation);
            }
        };
        r2.start();
    }

    public void removeNotification() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(212);
    }

    public void removeNotificationNew() {
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(213);
    }

    private void showNotification2() {
        RemoteViews remoteViews;
        Builder builder = new Builder(this);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(-2);
        builder.setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon), 52, 52, false));
        builder.setOngoing(true);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.app_name));
        sb.append(" is running");
        builder.setContentTitle(sb.toString()).setTicker("Notification keeps app always run properly");
        if (isRecording) {
            remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.content_notification_new_record);
            if (isPause) {
                remoteViews.setViewVisibility(R.id.pause_new, 8);
                remoteViews.setViewVisibility(R.id.resume_new, 0);
            } else {
                remoteViews.setViewVisibility(R.id.pause_new, 0);
                remoteViews.setViewVisibility(R.id.resume_new, 8);
            }
        } else {
            remoteViews = new RemoteViews(BuildConfig.APPLICATION_ID, R.layout.content_custom_notification);
        }
        remoteViews.setOnClickPendingIntent(R.id.notification_layout_main_container, onButtonNotificationClick(R.id.notification_layout_main_container));
        remoteViews.setOnClickPendingIntent(R.id.record, onButtonNotificationClick(R.id.record));
        remoteViews.setOnClickPendingIntent(R.id.capture, onButtonNotificationClick(R.id.capture));
        remoteViews.setOnClickPendingIntent(R.id.tools, onButtonNotificationClick(R.id.tools));
        remoteViews.setOnClickPendingIntent(R.id.close, onButtonNotificationClick(R.id.close));
        remoteViews.setOnClickPendingIntent(R.id.pause_new, onButtonNotificationClick(R.id.pause_new));
        remoteViews.setOnClickPendingIntent(R.id.resume_new, onButtonNotificationClick(R.id.resume_new));
        remoteViews.setOnClickPendingIntent(R.id.stop_new, onButtonNotificationClick(R.id.stop_new));
        remoteViews.setOnClickPendingIntent(R.id.tools_new, onButtonNotificationClick(R.id.tools_new));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFICATION_BUTTON_CLICK);
        try {
            unregisterReceiver(receiver);
        } catch (Exception unused) {
        }
        registerReceiver(receiver, intentFilter);
        if (Utils.isAndroid26()) {
            createChanelIDNew();
            builder.setChannelId("my_channel_screenrecorder_new");
        }
        builder.setCustomContentView(remoteViews);
        startForeground(212, builder.build());
    }

    public PendingIntent onButtonNotificationClick(@IdRes int i) {
        Intent intent = new Intent(ACTION_NOTIFICATION_BUTTON_CLICK);
        intent.putExtra(EXTRA_BUTTON_CLICKED, i);
        return PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressLint({"WrongConstant", "RestrictedApi"})
    @TargetApi(26)
    private void createChanelIDNew() {
        NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
        String string = getString(R.string.app_name);
        String string2 = getString(R.string.app_name);
        NotificationChannel notificationChannel = new NotificationChannel("my_channel_screenrecorder_new", string, 2);
        notificationChannel.setDescription(string2);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(notificationChannel);
    }

    public void expandFloatingControls() {
        startBlur();
        isExpand = true;
        handler.removeCallbacks(runnableRecord);
        isCollapRecord = false;
        if (isRecording) {
            layoutTime.setBackgroundResource(R.drawable.bg_time);
            ViewGroup.LayoutParams layoutParams = layoutTime.getLayoutParams();
            int i = width;
            layoutParams.height = i / 8;
            layoutParams.width = i / 8;
            tvTime.setVisibility(View.VISIBLE);
            floatingControls.setAlpha(1.0f);
            layoutTime.setLayoutParams(layoutParams);
        }
        img.setImageResource(R.drawable.ic_close_brush);
        final ArrayList arrayList = new ArrayList();
        layoutTime.setVisibility(View.INVISIBLE);
        layoutTime.post(new Runnable() {
            public void run() {
                if (BubbleControlService.isRightSide) {
                    if (BubbleControlService.isRecording) {
                        controlsRecorder.setVisibility(View.INVISIBLE);
                        controlsMain.setVisibility(View.GONE);
                        arrayList.add(controlsRecorder);
                    } else {
                        controlsMain.setVisibility(View.INVISIBLE);
                        controlsRecorder.setVisibility(View.GONE);
                        arrayList.add(controlsMain);
                    }
                    if (BubbleControlService.isPause) {
                        pauseIB.setVisibility(View.GONE);
                        resumeIB.setVisibility(View.VISIBLE);
                    } else {
                        pauseIB.setVisibility(View.VISIBLE);
                        resumeIB.setVisibility(View.GONE);
                    }
                } else {
                    if (BubbleControlService.isRecording) {
                        controlsRecorderLeft.setVisibility(View.INVISIBLE);
                        controlsMainLeft.setVisibility(View.GONE);
                        arrayList.add(controlsRecorderLeft);
                    } else {
                        controlsMainLeft.setVisibility(View.INVISIBLE);
                        controlsRecorderLeft.setVisibility(View.GONE);
                        arrayList.add(controlsMainLeft);
                    }
                    if (BubbleControlService.isPause) {
                        pauseLeftIB.setVisibility(View.GONE);
                        resumeLeftIB.setVisibility(View.VISIBLE);
                    } else {
                        pauseLeftIB.setVisibility(View.VISIBLE);
                        resumeLeftIB.setVisibility(View.GONE);
                    }
                }
                floatingControls.post(new Runnable() {
                    public void run() {
                        params.y -= (floatingControls.getHeight() / 2) - (layoutTime.getHeight() / 2);
                        windowManager.removeView(floatingControls);
                        windowManager.addView(floatingControls, params);
                        if (!arrayList.isEmpty()) {
                            ((View) arrayList.get(0)).setVisibility(View.VISIBLE);
                        }
                        layoutTime.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void startBlur() {
        startService(new Intent(this, ScreenBlurService.class));
    }

    private void stopBlur() {
        stopService(new Intent(this, ScreenBlurService.class));
    }


    public void collapseFloatingControls() {
        stopBlur();
        img.setImageResource(R.drawable.icon64);
        isExpand = false;
        tvTime.setText("00:00");
        layoutTime.setVisibility(View.INVISIBLE);
        if (isTop) {
            params.y = (floatingControls.getHeight() / 2) - (layoutTime.getHeight() / 2);
            windowManager.removeView(floatingControls);
            windowManager.addView(floatingControls, params);
        } else if (isBottom) {
            params.y = height - ((floatingControls.getHeight() / 2) + (layoutTime.getHeight() / 2));
try {
    windowManager.removeView(floatingControls);

}
catch (Exception d){

}

            windowManager.addView(floatingControls, params);
        } else {
            params.y += (floatingControls.getHeight() / 2) - (layoutTime.getHeight() / 2);
try
{
    windowManager.removeView(floatingControls);

}
catch (Exception f)
{

}
            windowManager.addView(floatingControls, params);
        }
        controlsMain.setVisibility(View.GONE);
        controlsRecorder.setVisibility(View.GONE);
        controlsMainLeft.setVisibility(View.GONE);
        controlsRecorderLeft.setVisibility(View.GONE);
        layoutTime.setVisibility(View.VISIBLE);
    }

    public void onClick(View view) {
        if (!isCountdown) {
            switch (view.getId()) {
                case R.id.imgTools /*2131361972*/:
                case R.id.imgTools_left /*2131361973*/:
                case R.id.tools_record /*2131362209*/:
                case R.id.tools_record_left /*2131362210*/:
                    openTools();
                    break;
                case R.id.panel /*2131362073*/:
                case R.id.panel_left /*2131362074*/:
                    openSetting();
                    break;
                case R.id.pause /*2131362079*/:
                case R.id.pause_left :
                    pauseScreenRecording();
                    break;
                case R.id.recorder /*2131362096*/:
                case R.id.recorder_left /*2131362097*/:
                    handlerTimer();
                    break;
                case R.id.resume /*2131362102*/:
                case R.id.resume_left /*2131362103*/:
                    resumeScreenRecording();
                    break;
                case R.id.screenshot /*2131362118*/:
                case R.id.screenshot_left /*2131362119*/:
                    openPanel();
                    break;
                case R.id.stop :
                case R.id.stop_left :
                    stopScreenSharing();
                    break;
            }
            if (PrefUtils.readBooleanValue(this, getString(R.string.preference_vibrate_key), true)) {
                ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(100);
            }
        }
    }

    public void openTools() {
        collapseFloatingControls();
        startService(new Intent(this, ToolsService.class));
    }

    private void openSetting() {
        Log.i("iaminsd"," opensetting from bubble");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("action", "setting");
        Utils.startActivityAllStage(this, intent);



        collapseFloatingControls();
        Log.i("iaminsd"," collapseFloatingControls from bubble");

    }

    private void openPanel()
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.startActivityAllStage(this, intent);
        collapseFloatingControls();
    }


    public void requestRecorder() {
        tvTime.setVisibility(View.VISIBLE);
        img.setVisibility(View.GONE);
        Intent intent = new Intent(this, TakeRequestRecorderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Utils.startActivityAllStage(this, intent);
    }

    private void screenshot() {
        collapseFloatingControls();
        Intent intent = new Intent(this, TakeScreenShotActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void resumeScreenRecording() {
        handler.removeCallbacks(runAnim);
        isRecording = true;
        isPause = false;
        collapseFloatingControls();
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(CoderlyticsConstants.SCREEN_RECORDING_RESUME);
        startService(intent);
    }

    private void pauseScreenRecording()
    {
        Log.i("imainb","bubblepasuse ");
        handler.postDelayed(runAnim, 800);
        isRecording = true;
        isPause = true;
        collapseFloatingControls();
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(CoderlyticsConstants.SCREEN_RECORDING_PAUSE);
        startService(intent);
    }

    private void stopScreenSharing() {
        if (isPause&&isRecording)
        {
            resumeScreenRecording();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRec();
            }
        },1000);


    }
void stopRec()
{
    isRecording = false;
    isPause = false;
    layoutTime.setBackgroundResource(R.drawable.bg_time);
    ViewGroup.LayoutParams layoutParams = layoutTime.getLayoutParams();
    int i = width;
    layoutParams.height = i / 8;
    layoutParams.width = i / 8;
    floatingControls.setAlpha(1.0f);
    layoutTime.setLayoutParams(layoutParams);
    collapseFloatingControls();
    Intent intent = new Intent(this, RecorderService.class);
    intent.setAction(CoderlyticsConstants.SCREEN_RECORDING_STOP);
    startService(intent);
}
    public void setRecordingState(RecordingState recordingState) {
        int i = AnonymousClass12.$SwitchMap$com$test$screenrecord$common$Const$RecordingState[recordingState.ordinal()];
        if (i == 1) {
            isPause = true;
        } else if (i == 2) {
            isPause = false;
        } else if (i == 3) {
            isRecording = false;
            isPause = false;
            collapseFloatingControls();
        }
    }

    public void onDestroy() {
        setInstance(null);
        try {
            ObserverUtils.getInstance().notifyObservers(new EvbStopService());
            removeBubbleView();
            removeNotification();
            removeNotificationNew();
            ObserverUtils.getInstance().removeObserver((ObserverInterface<ObserverUtils>) this);
            handler.removeCallbacks(runAnim);
            unregisterReceiver(receiverCapture);
            unregisterReceiver(receiver);
            if (!(windowManager == null || mRemoveView == null)) {
                windowManager.removeView(mRemoveView);
            }
        } catch (Exception unused) {
        }
        super.onDestroy();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        Log.d(CoderlyticsConstants.TAG, "Binding successful!");
        return binder;
    }

    public void notifyAction(Object obj)
    {
        if (obj instanceof EvbStageRecord) {
            if (((EvbStageRecord) obj).isStart) {
                handlerTimer();
            } else
                {
                stopScreenSharing();
            }
        } else if (obj instanceof EvbRecordTime) {
            tvTime.setText(((EvbRecordTime) obj).time);
        } else if (obj instanceof EvbStopService) {
            img.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.GONE);
        }
        if (obj instanceof EvbStartRecord) {
            handler.postDelayed(runnable, 2000);
        }
        if (obj instanceof EvbClickBlur) {
            collapseFloatingControls();
        }
        if (obj instanceof ShowService) {
            floatingControls.setVisibility(View.VISIBLE);
        }
        if (obj instanceof HideService) {
            floatingControls.setVisibility(View.GONE);
        }
        if (obj instanceof ErrorRecordService) {
            stopScreenSharing();
        }
    }
}
