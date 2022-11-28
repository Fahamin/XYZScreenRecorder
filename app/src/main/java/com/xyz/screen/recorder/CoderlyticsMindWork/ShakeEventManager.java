package com.xyz.screen.recorder.CoderlyticsMindWork;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;

public class ShakeEventManager implements SensorEventListener {
    private static final float ALPHA = 0.8f;
    private static final int MOV_COUNTS = 5;
    private static final int MOV_THRESHOLD = 4;
    private static final int SHAKE_WINDOW_TIME_INTERVAL = 1000;
    private int counter;
    private long firstMovTime;
    private float[] gravity = new float[3];
    private long lastMoveTime = 0;
    private ShakeListener listener;
    private Sensor s;
    private SensorManager sManager;

    public interface ShakeListener {
        void onShake();
    }

    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public ShakeEventManager(ShakeListener shakeListener) {
        this.listener = shakeListener;
    }

    public void init(Context context) {
        this.sManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.s = this.sManager.getDefaultSensor(1);
        register();
    }

    public void register() {
        this.sManager.registerListener(this, this.s, 3);
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        String str = CoderlyticsConstants.TAG;
        String str2 = "]";
        String str3 = "SwA";
        try {
            float calcMaxAcceleration = calcMaxAcceleration(sensorEvent);
            StringBuilder sb = new StringBuilder();
            sb.append("Max Acc [");
            sb.append(calcMaxAcceleration);
            sb.append(str2);
            Log.d(str3, sb.toString());
            if (calcMaxAcceleration < 4.0f) {
                return;
            }
            if (this.counter == 0) {
                this.counter++;
                this.firstMovTime = System.currentTimeMillis();
                Log.d(str3, "First mov..");
            } else if (System.currentTimeMillis() - this.firstMovTime < 1000) {
                this.counter++;
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Mov counter [");
                sb2.append(this.counter);
                sb2.append(str2);
                Log.d(str, sb2.toString());
                if (this.counter == 5 && System.currentTimeMillis() - this.lastMoveTime > 5000 && this.listener != null) {
                    resetAllData();
                    StringBuilder sb3 = new StringBuilder();
                    sb3.append("Shaked. count: ");
                    sb3.append(this.counter);
                    Log.d(str, sb3.toString());
                    this.listener.onShake();
                    this.lastMoveTime = System.currentTimeMillis();
                }
            } else {
                resetAllData();
            }
        } catch (Exception unused) {
        }
    }

    public void stop() {
        this.sManager.unregisterListener(this);
    }

    private float calcMaxAcceleration(SensorEvent sensorEvent) {
        this.gravity[0] = calcGravityForce(sensorEvent.values[0], 0);
        this.gravity[1] = calcGravityForce(sensorEvent.values[1], 1);
        this.gravity[2] = calcGravityForce(sensorEvent.values[2], 2);
        float f = sensorEvent.values[0] - this.gravity[0];
        float f2 = sensorEvent.values[1] - this.gravity[1];
        return Math.max(Math.max(f, f2), sensorEvent.values[2] - this.gravity[2]);
    }

    private float calcGravityForce(float f, int i) {
        return (this.gravity[i] * ALPHA) + (f * 0.19999999f);
    }

    private void resetAllData() {
        Log.d("SwA", "Reset all data");
        this.counter = 0;
        this.firstMovTime = System.currentTimeMillis();
    }
}
