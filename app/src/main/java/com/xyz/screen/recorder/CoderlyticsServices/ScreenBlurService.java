package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbClickBlur;

public class ScreenBlurService extends Service implements OnTouchListener, OnClickListener {
    private ConstraintLayout mLayout;
    private LayoutParams mParams;
    private WindowManager windowManager;

    @Nullable
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    public void onClick(View view)
    {
    }
@Override
    public boolean onTouch(View view, MotionEvent motionEvent)
{
        return true;
    }

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    private void initView() {
        this.windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        this.mLayout = (ConstraintLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_main_blur, null);
        LayoutParams layoutParams = new LayoutParams(-1, -1, 2038, 8, -3);
        this.mParams = layoutParams;
        if (VERSION.SDK_INT < 26) {
            this.mParams.type = 2005;
        }
        this.windowManager.addView(this.mLayout, this.mParams);
        this.mLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ObserverUtils.getInstance().notifyObservers(new EvbClickBlur());
                ScreenBlurService.this.stopSelf();
            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        initView();
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        WindowManager windowManager2 = this.windowManager;
        if (windowManager2 != null) {
            ConstraintLayout constraintLayout = this.mLayout;
            if (constraintLayout != null) {
                windowManager2.removeView(constraintLayout);
            }
        }
        super.onDestroy();
    }
}
