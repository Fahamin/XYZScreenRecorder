package com.CoderlyticsCameraView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.ViewGroup;

import com.xyz.screen.recorder.R;

@TargetApi(14)
class CamTextureViewPreview extends PreviewImpl {
    private int mDisplayOrientation;
    private final TextureView mTextureView;

    CamTextureViewPreview(Context context, ViewGroup viewGroup)
    {
        this.mTextureView = (TextureView) View.inflate(context, R.layout.texture_view, viewGroup).findViewById(R.id.texture_view);
        this.mTextureView.setSurfaceTextureListener(new SurfaceTextureListener()
        {
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
                setSize(i, i2);
                configureTransform();
                dispatchSurfaceChanged();
            }

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
                setSize(i, i2);
                configureTransform();
                dispatchSurfaceChanged();
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                setSize(0, 0);
                return true;
            }
        });
    }

    
    @TargetApi(15)
    public void setBufferSize(int i, int i2) {
        this.mTextureView.getSurfaceTexture().setDefaultBufferSize(i, i2);
    }

    
    public Surface getSurface() {
        return new Surface(this.mTextureView.getSurfaceTexture());
    }

    
    public SurfaceTexture getSurfaceTexture() {
        return this.mTextureView.getSurfaceTexture();
    }

    
    public View getView() {
        return this.mTextureView;
    }

    
    public Class getOutputClass() {
        return SurfaceTexture.class;
    }

    
    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        configureTransform();
    }

    
    public boolean isReady() {
        return this.mTextureView.getSurfaceTexture() != null;
    }

    
    public void configureTransform() {
        Matrix matrix = new Matrix();
        int i = this.mDisplayOrientation;
        if (i % 180 == 90) {
            float width = (float) getWidth();
            float height = (float) getHeight();
            matrix.setPolyToPoly(new float[]{0.0f, 0.0f, width, 0.0f, 0.0f, height, width, height}, 0, this.mDisplayOrientation == 90 ? new float[]{0.0f, height, 0.0f, 0.0f, width, height, width, 0.0f} : new float[]{width, 0.0f, width, height, 0.0f, 0.0f, 0.0f, height}, 0, 4);
        } else if (i == 180) {
            matrix.postRotate(180.0f, (float) (getWidth() / 2), (float) (getHeight() / 2));
        }
        this.mTextureView.setTransform(matrix);
    }
}
