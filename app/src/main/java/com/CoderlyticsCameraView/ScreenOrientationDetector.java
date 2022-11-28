package com.CoderlyticsCameraView;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.OrientationEventListener;

abstract class ScreenOrientationDetector {
    static final SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();
    Display mDisplay;
    private int mLastKnownDisplayOrientation = 0;
    private final OrientationEventListener mOrientationEventListener;

    public abstract void onDisplayOrientationChanged(int i);

    static {
        DISPLAY_ORIENTATIONS.put(0, 0);
        DISPLAY_ORIENTATIONS.put(1, 90);
        DISPLAY_ORIENTATIONS.put(2, 180);
        DISPLAY_ORIENTATIONS.put(3, Constants.LANDSCAPE_270);
    }

    public ScreenOrientationDetector(Context context) {
        this.mOrientationEventListener = new OrientationEventListener(context) {
            private int mLastKnownRotation = -1;

            public void onOrientationChanged(int i) {
                if (i != -1 && mDisplay != null) {
                    int rotation = mDisplay.getRotation();
                    if (this.mLastKnownRotation != rotation) {
                        this.mLastKnownRotation = rotation;
                        dispatchOnDisplayOrientationChanged(ScreenOrientationDetector.DISPLAY_ORIENTATIONS.get(rotation));
                    }
                }
            }
        };
    }

    public void enable(Display display) {
        this.mDisplay = display;
        this.mOrientationEventListener.enable();
        dispatchOnDisplayOrientationChanged(DISPLAY_ORIENTATIONS.get(display.getRotation()));
    }

    public void disable() {
        this.mOrientationEventListener.disable();
        this.mDisplay = null;
    }

    public int getLastKnownDisplayOrientation() {
        return this.mLastKnownDisplayOrientation;
    }


    public void dispatchOnDisplayOrientationChanged(int i) {
        this.mLastKnownDisplayOrientation = i;
        onDisplayOrientationChanged(i);
    }
}
