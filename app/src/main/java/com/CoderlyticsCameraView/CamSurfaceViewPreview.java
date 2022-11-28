package com.CoderlyticsCameraView;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.ViewCompat;

import com.xyz.screen.recorder.R;

class CamSurfaceViewPreview extends PreviewImpl {
    final SurfaceView mSurfaceView;

    
    public void setDisplayOrientation(int i) {
    }

    CamSurfaceViewPreview(Context context, ViewGroup viewGroup) {
        this.mSurfaceView = (SurfaceView) View.inflate(context, R.layout.surface_view, viewGroup).findViewById(R.id.surface_view);
        SurfaceHolder holder = this.mSurfaceView.getHolder();
        holder.setType(3);
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
            }

            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                setSize(i2, i3);
                if (!ViewCompat.isInLayout(mSurfaceView)) {
                    dispatchSurfaceChanged();
                }
            }

            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                setSize(0, 0);
            }
        });
    }

    
    public Surface getSurface() {
        return getSurfaceHolder().getSurface();
    }

    
    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceView.getHolder();
    }

    
    public View getView() {
        return this.mSurfaceView;
    }

    
    public Class getOutputClass() {
        return SurfaceHolder.class;
    }

    
    public boolean isReady() {
        return (getWidth() == 0 || getHeight() == 0) ? false : true;
    }
}
