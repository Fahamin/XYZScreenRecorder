package com.CoderlyticsCameraView;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.view.SurfaceHolder;
import androidx.collection.SparseArrayCompat;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.PrefUtils;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

class PreKitkatCamera extends CameraViewImpl {
    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();
    private static final int INVALID_CAMERA_ID = -1;
    
    public final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);
    private AspectRatio mAspectRatio;
    private boolean mAutoFocus;
    Camera mCamera;
    private int mCameraId;
    private final CameraInfo mCameraInfo = new CameraInfo();
    private Parameters mCameraParameters;
    private int mDisplayOrientation;
    private int mFacing;
    private int mFlash;
    private final SizeMap mPictureSizes = new SizeMap();
    private final SizeMap mPreviewSizes = new SizeMap();
    private boolean mShowingPreview;

    private boolean isLandscape(int i) {
        return i == 90 || i == 270;
    }

    static {
        FLASH_MODES.put(0, "off");
        FLASH_MODES.put(1, "on");
        FLASH_MODES.put(2, "torch");
        FLASH_MODES.put(3, PrefUtils.VALUE_ORIENTATION);
        FLASH_MODES.put(4, "red-eye");
    }

    PreKitkatCamera(Callback callback, PreviewImpl previewImpl) {
        super(callback, previewImpl);
        previewImpl.setCallback(new PreviewImpl.Callback() {
            public void onSurfaceChanged() {
                if (mCamera != null) {
                    setUpPreview();
                    adjustCameraParameters();
                }
            }
        });
    }

    
    public boolean start() {
        chooseCamera();
        openCamera();
        if (this.mPreview.isReady()) {
            setUpPreview();
        }
        this.mShowingPreview = true;
        this.mCamera.startPreview();
        return true;
    }

    
    public void stop() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.stopPreview();
        }
        this.mShowingPreview = false;
        releaseCamera();
    }

    
    @SuppressLint({"NewApi"})
    public void setUpPreview() {
        try {
            if (this.mPreview.getOutputClass() == SurfaceHolder.class) {
                boolean z = this.mShowingPreview && VERSION.SDK_INT < 14;
                if (z) {
                    this.mCamera.stopPreview();
                }
                this.mCamera.setPreviewDisplay(this.mPreview.getSurfaceHolder());
                if (z) {
                    this.mCamera.startPreview();
                    return;
                }
                return;
            }
            this.mCamera.setPreviewTexture((SurfaceTexture) this.mPreview.getSurfaceTexture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    
    public boolean isCameraOpened() {
        return this.mCamera != null;
    }

    
    public void setFacing(int i) {
        if (this.mFacing != i) {
            this.mFacing = i;
            if (isCameraOpened()) {
                stop();
                start();
            }
        }
    }

    
    public int getFacing() {
        return this.mFacing;
    }

    
    public Set<AspectRatio> getSupportedAspectRatios() {
        SizeMap sizeMap = this.mPreviewSizes;
        for (AspectRatio aspectRatio : sizeMap.ratios()) {
            if (this.mPictureSizes.sizes(aspectRatio) == null) {
                sizeMap.remove(aspectRatio);
            }
        }
        return sizeMap.ratios();
    }

    
    public boolean setAspectRatio(AspectRatio aspectRatio) {
        if (this.mAspectRatio == null || !isCameraOpened()) {
            this.mAspectRatio = aspectRatio;
            return true;
        } else if (this.mAspectRatio.equals(aspectRatio)) {
            return false;
        } else {
            if (this.mPreviewSizes.sizes(aspectRatio) != null) {
                this.mAspectRatio = aspectRatio;
                adjustCameraParameters();
                return true;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(aspectRatio);
            sb.append(" is not supported");
            throw new UnsupportedOperationException(sb.toString());
        }
    }

    
    public AspectRatio getAspectRatio() {
        return this.mAspectRatio;
    }

    
    public void setAutoFocus(boolean z) {
        if (this.mAutoFocus != z && setAutoFocusInternal(z)) {
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    
    public boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return this.mAutoFocus;
        }
        String focusMode = this.mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    
    public void setFlash(int i) {
        if (i != this.mFlash && setFlashInternal(i)) {
            this.mCamera.setParameters(this.mCameraParameters);
        }
    }

    
    public int getFlash() {
        return this.mFlash;
    }

    
    public void takePicture() {
        if (!isCameraOpened()) {
            throw new IllegalStateException("Camera is not ready. Call start() before takePicture().");
        } else if (getAutoFocus()) {
            this.mCamera.cancelAutoFocus();
            this.mCamera.autoFocus(new AutoFocusCallback() {
                public void onAutoFocus(boolean z, Camera camera) {
                    takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }
    }

    
    public void takePictureInternal() {
        if (!this.isPictureCaptureInProgress.getAndSet(true)) {
            this.mCamera.takePicture(null, null, null, new PictureCallback() {
                public void onPictureTaken(byte[] bArr, Camera camera) {
                    isPictureCaptureInProgress.set(false);
                    mCallback.onPictureTaken(bArr);
                    camera.cancelAutoFocus();
                    camera.startPreview();
                }
            });
        }
    }

    
    public void setDisplayOrientation(int i) {
        if (this.mDisplayOrientation != i) {
            this.mDisplayOrientation = i;
            if (isCameraOpened()) {
                this.mCameraParameters.setRotation(calcCameraRotation(i));
                this.mCamera.setParameters(this.mCameraParameters);
                boolean z = this.mShowingPreview && VERSION.SDK_INT < 14;
                if (z) {
                    this.mCamera.stopPreview();
                }
                this.mCamera.setDisplayOrientation(calcDisplayOrientation(i));
                if (z) {
                    this.mCamera.startPreview();
                }
            }
        }
    }

    private void chooseCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, this.mCameraInfo);
            if (this.mCameraInfo.facing == this.mFacing) {
                this.mCameraId = i;
                return;
            }
        }
        this.mCameraId = -1;
    }

    private void openCamera() {
        if (this.mCamera != null) {
            releaseCamera();
        }
        this.mCamera = Camera.open(this.mCameraId);
        this.mCameraParameters = this.mCamera.getParameters();
        this.mPreviewSizes.clear();
        for (Size size : this.mCameraParameters.getSupportedPreviewSizes()) {
            this.mPreviewSizes.add(new com.CoderlyticsCameraView.Size(size.width, size.height));
        }
        this.mPictureSizes.clear();
        for (Size size2 : this.mCameraParameters.getSupportedPictureSizes()) {
            this.mPictureSizes.add(new com.CoderlyticsCameraView.Size(size2.width, size2.height));
        }
        if (this.mAspectRatio == null) {
            this.mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        }
        adjustCameraParameters();
        this.mCamera.setDisplayOrientation(calcDisplayOrientation(this.mDisplayOrientation));
        this.mCallback.onCameraOpened();
    }

    private AspectRatio chooseAspectRatio() {
        AspectRatio aspectRatio1=null;
        for (AspectRatio aspectRatio : this.mPreviewSizes.ratios()) {
           aspectRatio1=aspectRatio;
            if (aspectRatio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                break;
            }
        }
        return aspectRatio1;
    }

    
    public void adjustCameraParameters() {
        SortedSet sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        if (sizes == null)
        {
            this.mAspectRatio = chooseAspectRatio();
            sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        }
        com.CoderlyticsCameraView.Size chooseOptimalSize = chooseOptimalSize(sizes);
        com.CoderlyticsCameraView.Size size = this.mPictureSizes.sizes(this.mAspectRatio).last();
        if (this.mShowingPreview) {
            this.mCamera.stopPreview();
        }
        this.mCameraParameters.setPreviewSize(chooseOptimalSize.getWidth(), chooseOptimalSize.getHeight());
        this.mCameraParameters.setPictureSize(size.getWidth(), size.getHeight());
        this.mCameraParameters.setRotation(calcCameraRotation(this.mDisplayOrientation));
        setAutoFocusInternal(this.mAutoFocus);
        setFlashInternal(this.mFlash);
        this.mCamera.setParameters(this.mCameraParameters);
        if (this.mShowingPreview) {
            this.mCamera.startPreview();
        }
    }

    private com.CoderlyticsCameraView.Size chooseOptimalSize(SortedSet<com.CoderlyticsCameraView.Size> sortedSet) {
        if (!this.mPreview.isReady()) {
            return  sortedSet.first();
        }
        int width = this.mPreview.getWidth();
        int height = this.mPreview.getHeight();
        if (isLandscape(this.mDisplayOrientation)) {
            int i = height;
            height = width;
            width = i;
        }
        com.CoderlyticsCameraView.Size size1=null;
        for ( com.CoderlyticsCameraView.Size size : sortedSet)
        {
            size1=size;
            if (width <= size.getWidth() && height <= size.getHeight()) {
                break;
            }
        }
        return size1;
    }

    private void releaseCamera() {
        Camera camera = this.mCamera;
        if (camera != null) {
            camera.release();
            this.mCamera = null;
            this.mCallback.onCameraClosed();
        }
    }

    private int calcDisplayOrientation(int i) {
        if (this.mCameraInfo.facing == 1) {
            return (360 - ((this.mCameraInfo.orientation + i) % 360)) % 360;
        }
        return ((this.mCameraInfo.orientation - i) + 360) % 360;
    }

    private int calcCameraRotation(int i) {
        if (this.mCameraInfo.facing == 1) {
            return (this.mCameraInfo.orientation + i) % 360;
        }
        return ((this.mCameraInfo.orientation + i) + (isLandscape(i) ? 180 : 0)) % 360;
    }

    private boolean setAutoFocusInternal(boolean z) {
        this.mAutoFocus = z;
        if (!isCameraOpened()) {
            return false;
        }
        List supportedFocusModes = this.mCameraParameters.getSupportedFocusModes();
        if (z) {
            String str = "continuous-picture";
            if (supportedFocusModes.contains(str)) {
                this.mCameraParameters.setFocusMode(str);
                return true;
            }
        }
        String str2 = "fixed";
        if (supportedFocusModes.contains(str2)) {
            this.mCameraParameters.setFocusMode(str2);
        } else {
            String str3 = "infinity";
            if (supportedFocusModes.contains(str3)) {
                this.mCameraParameters.setFocusMode(str3);
            } else {
                this.mCameraParameters.setFocusMode((String) supportedFocusModes.get(0));
            }
        }
        return true;
    }

    private boolean setFlashInternal(int i) {
        if (isCameraOpened()) {
            List supportedFlashModes = this.mCameraParameters.getSupportedFlashModes();
            String str = (String) FLASH_MODES.get(i);
            if (supportedFlashModes == null || !supportedFlashModes.contains(str)) {
                String str2 = (String) FLASH_MODES.get(this.mFlash);
                if (supportedFlashModes != null && supportedFlashModes.contains(str2)) {
                    return false;
                }
                this.mCameraParameters.setFlashMode("off");
                this.mFlash = 0;
                return true;
            }
            this.mCameraParameters.setFlashMode(str);
            this.mFlash = i;
            return true;
        }
        this.mFlash = i;
        return false;
    }
}
