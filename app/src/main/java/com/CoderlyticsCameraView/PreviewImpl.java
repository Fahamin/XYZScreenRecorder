package com.CoderlyticsCameraView;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

abstract class PreviewImpl {
    private Callback mCallback;
    private int mHeight;
    private int mWidth;

    interface Callback {
        void onSurfaceChanged();
    }

    
    public abstract Class getOutputClass();

    
    public abstract Surface getSurface();

    
    public SurfaceHolder getSurfaceHolder() {
        return null;
    }

    
    public Object getSurfaceTexture() {
        return null;
    }

    
    public abstract View getView();

    
    public abstract boolean isReady();

    
    public void setBufferSize(int i, int i2) {
    }

    
    public abstract void setDisplayOrientation(int i);

    PreviewImpl() {
    }

    
    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }


    public void dispatchSurfaceChanged() {
        this.mCallback.onSurfaceChanged();
    }

    
    public void setSize(int i, int i2) {
        this.mWidth = i;
        this.mHeight = i2;
    }

    
    public int getWidth() {
        return this.mWidth;
    }

    
    public int getHeight() {
        return this.mHeight;
    }
}
