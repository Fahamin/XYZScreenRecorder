package com.CoderlyticsCameraView;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureRequest.Key;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import androidx.annotation.NonNull;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;

@TargetApi(21)
class KitKatCamera extends CameraViewImpl {
    private static final SparseIntArray INTERNAL_FACINGS = new SparseIntArray();
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final String TAG = "KitKatCamera";
    private AspectRatio mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
    private boolean mAutoFocus;
    CameraDevice mCamera;
    private CameraCharacteristics mCameraCharacteristics;
    private final StateCallback mCameraDeviceCallback = new StateCallback() {
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            
            mCamera = cameraDevice;
            mCallback.onCameraOpened();
            startCaptureSession();
        }

        public void onClosed(@NonNull CameraDevice cameraDevice) {
            mCallback.onCameraClosed();
        }

        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCamera = null;
        }

        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            StringBuilder sb = new StringBuilder();
            sb.append("onError: ");
            sb.append(cameraDevice.getId());
            sb.append(" (");
            sb.append(i);
            sb.append(")");
            Log.e(KitKatCamera.TAG, sb.toString());
            mCamera = null;
        }
    };
    private String mCameraId;
    private final CameraManager mCameraManager;
    PictureCaptureCallback mCaptureCallback = new PictureCaptureCallback() {
        public void onPrecaptureRequired() {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(1));
            setState(3);
            try {
                mCaptureSession.capture(mPreviewRequestBuilder.build(), this, null);
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, Integer.valueOf(0));
            } catch (CameraAccessException e) {
                Log.e(KitKatCamera.TAG, "Failed to run precapture sequence.", e);
            }
        }

        public void onReady() {
            captureStillPicture();
        }
    };
    CameraCaptureSession mCaptureSession;
    private int mDisplayOrientation;
    private int mFacing;
    private int mFlash;
    private ImageReader mImageReader;
    private final OnImageAvailableListener mOnImageAvailableListener = new OnImageAvailableListener() {


        public void onImageAvailable(ImageReader imageReader) {
            Image acquireNextImage = imageReader.acquireNextImage();
            Plane[] planes = acquireNextImage.getPlanes();
            if (planes.length > 0) {
                ByteBuffer buffer = planes[0].getBuffer();
                byte[] bArr = new byte[buffer.remaining()];
                buffer.get(bArr);
                mCallback.onPictureTaken(bArr);
            }
            if (acquireNextImage != null) {
                acquireNextImage.close();
                return;
            }
            return;
        }
    };
    private final SizeMap mPictureSizes = new SizeMap();
    Builder mPreviewRequestBuilder;
    private final SizeMap mPreviewSizes = new SizeMap();
    private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            String str = KitKatCamera.TAG;
            if (mCamera != null) {
                
                mCaptureSession = cameraCaptureSession;
                updateAutoFocus();
                updateFlash();
                try {
                    mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
                } catch (CameraAccessException e) {
                    Log.e(str, "Failed to start camera preview because it couldn't access camera", e);
                } catch (IllegalStateException e2) {
                    Log.e(str, "Failed to start camera preview.", e2);
                }
            }
        }

        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Log.e(KitKatCamera.TAG, "Failed to configure capture session.");
        }

        public void onClosed(@NonNull CameraCaptureSession cameraCaptureSession) {
            if (mCaptureSession != null && mCaptureSession.equals(cameraCaptureSession)) {
                mCaptureSession = null;
            }
        }
    };

    private static abstract class PictureCaptureCallback extends CaptureCallback {
        static final int STATE_CAPTURING = 5;
        static final int STATE_LOCKED = 2;
        static final int STATE_LOCKING = 1;
        static final int STATE_PRECAPTURE = 3;
        static final int STATE_PREVIEW = 0;
        static final int STATE_WAITING = 4;
        private int mState;

        public abstract void onPrecaptureRequired();

        public abstract void onReady();

        PictureCaptureCallback() {
        }

        
        public void setState(int i) {
            this.mState = i;
        }

        public void onCaptureProgressed(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull CaptureResult captureResult) {
            process(captureResult);
        }

        public void onCaptureCompleted(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull TotalCaptureResult totalCaptureResult) {
            process(totalCaptureResult);
        }

        private void process(@NonNull CaptureResult captureResult) {
            int i = this.mState;
            if (i == 1) {
                Integer num = (Integer) captureResult.get(CaptureResult.CONTROL_AF_STATE);
                if (num != null) {
                    if (num.intValue() == 4 || num.intValue() == 5) {
                        Integer num2 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                        if (num2 == null || num2.intValue() == 2) {
                            setState(5);
                            onReady();
                            return;
                        }
                        setState(2);
                        onPrecaptureRequired();
                    }
                }
            } else if (i == 3) {
                Integer num3 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num3 == null || num3.intValue() == 5 || num3.intValue() == 4 || num3.intValue() == 2) {
                    setState(4);
                }
            } else if (i == 4) {
                Integer num4 = (Integer) captureResult.get(CaptureResult.CONTROL_AE_STATE);
                if (num4 == null || num4.intValue() != 5) {
                    setState(5);
                    onReady();
                }
            }
        }
    }

    static {
        INTERNAL_FACINGS.put(0, 1);
        INTERNAL_FACINGS.put(1, 0);
    }

    KitKatCamera(Callback callback, PreviewImpl previewImpl, Context context) {
        super(callback, previewImpl);
        this.mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.mPreview.setCallback(new PreviewImpl.Callback() {
                                      @Override
                                      public void onSurfaceChanged() {

                                      }
                                  }
        );


    }

    
    public boolean start() {
        if (!chooseCameraIdByFacing()) {
            return false;
        }
        collectCameraInfo();
        prepareImageReader();
        startOpeningCamera();
        return true;
    }

    
    public void stop() {
        CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            this.mCaptureSession = null;
        }
        CameraDevice cameraDevice = this.mCamera;
        if (cameraDevice != null) {
            cameraDevice.close();
            this.mCamera = null;
        }
        ImageReader imageReader = this.mImageReader;
        if (imageReader != null) {
            imageReader.close();
            this.mImageReader = null;
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
        return this.mPreviewSizes.ratios();
    }

    
    public boolean setAspectRatio(AspectRatio aspectRatio) {
        if (aspectRatio == null || aspectRatio.equals(this.mAspectRatio) || !this.mPreviewSizes.ratios().contains(aspectRatio)) {
            return false;
        }
        this.mAspectRatio = aspectRatio;
        prepareImageReader();
        CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            this.mCaptureSession = null;
            startCaptureSession();
        }
        return true;
    }

    
    public AspectRatio getAspectRatio() {
        return this.mAspectRatio;
    }

    
    public void setAutoFocus(boolean z) {
        if (this.mAutoFocus != z) {
            this.mAutoFocus = z;
            if (this.mPreviewRequestBuilder != null) {
                updateAutoFocus();
                CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
                if (cameraCaptureSession != null) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
                    } catch (CameraAccessException unused) {
                        this.mAutoFocus = !this.mAutoFocus;
                    }
                }
            }
        }
    }

    
    public boolean getAutoFocus() {
        return this.mAutoFocus;
    }

    
    public void setFlash(int i) {
        int i2 = this.mFlash;
        if (i2 != i) {
            this.mFlash = i;
            if (this.mPreviewRequestBuilder != null) {
                updateFlash();
                CameraCaptureSession cameraCaptureSession = this.mCaptureSession;
                if (cameraCaptureSession != null) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
                    } catch (CameraAccessException unused) {
                        this.mFlash = i2;
                    }
                }
            }
        }
    }

    
    public int getFlash() {
        return this.mFlash;
    }

    
    public void takePicture() {
        if (this.mAutoFocus) {
            lockFocus();
        } else {
            captureStillPicture();
        }
    }

    
    public void setDisplayOrientation(int i) {
        this.mDisplayOrientation = i;
        this.mPreview.setDisplayOrientation(this.mDisplayOrientation);
    }

    private boolean chooseCameraIdByFacing() {
        try {
            int i = INTERNAL_FACINGS.get(this.mFacing);
            String[] cameraIdList = this.mCameraManager.getCameraIdList();
            if (cameraIdList.length != 0) {
                int length = cameraIdList.length;
                int i2 = 0;
                while (true) {
                    String str = "Unexpected state: LENS_FACING null";
                    if (i2 < length) {
                        String str2 = cameraIdList[i2];
                        CameraCharacteristics cameraCharacteristics = this.mCameraManager.getCameraCharacteristics(str2);
                        Integer num = (Integer) cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                        if (num != null) {
                            if (num.intValue() != 2) {
                                Integer num2 = (Integer) cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                                if (num2 == null) {
                                    throw new NullPointerException(str);
                                } else if (num2.intValue() == i) {
                                    this.mCameraId = str2;
                                    this.mCameraCharacteristics = cameraCharacteristics;
                                    return true;
                                }
                            }
                        }
                        i2++;
                    } else {
                        this.mCameraId = cameraIdList[0];
                        this.mCameraCharacteristics = this.mCameraManager.getCameraCharacteristics(this.mCameraId);
                        Integer num3 = (Integer) this.mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                        if (num3 != null) {
                            if (num3.intValue() != 2) {
                                Integer num4 = (Integer) this.mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                                if (num4 != null) {
                                    int size = INTERNAL_FACINGS.size();
                                    for (int i3 = 0; i3 < size; i3++) {
                                        if (INTERNAL_FACINGS.valueAt(i3) == num4.intValue()) {
                                            this.mFacing = INTERNAL_FACINGS.keyAt(i3);
                                            return true;
                                        }
                                    }
                                    this.mFacing = 0;
                                    return true;
                                }
                                throw new NullPointerException(str);
                            }
                        }
                        return false;
                    }
                }
            } else {
                throw new RuntimeException("No camera available.");
            }
        } catch (CameraAccessException e) {
            throw new RuntimeException("Failed to get a list of camera devices", e);
        }
    }

    private void collectCameraInfo() {
        Size[] outputSizes;
        StreamConfigurationMap streamConfigurationMap = (StreamConfigurationMap) this.mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (streamConfigurationMap != null) {
            this.mPreviewSizes.clear();
            for (Size size : streamConfigurationMap.getOutputSizes(this.mPreview.getOutputClass())) {
                int width = size.getWidth();
                int height = size.getHeight();
                if (width <= MAX_PREVIEW_WIDTH && height <= MAX_PREVIEW_HEIGHT) {
                    this.mPreviewSizes.add(new com.CoderlyticsCameraView.Size(width, height));
                }
            }
            this.mPictureSizes.clear();
            collectPictureSizes(this.mPictureSizes, streamConfigurationMap);
            for (AspectRatio aspectRatio : this.mPreviewSizes.ratios()) {
                if (!this.mPictureSizes.ratios().contains(aspectRatio)) {
                    this.mPreviewSizes.remove(aspectRatio);
                }
            }
            if (!this.mPreviewSizes.ratios().contains(this.mAspectRatio)) {
                this.mAspectRatio = (AspectRatio) this.mPreviewSizes.ratios().iterator().next();
                return;
            }
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Failed to get configuration map: ");
        sb.append(this.mCameraId);
        throw new IllegalStateException(sb.toString());
    }

    
    public void collectPictureSizes(SizeMap sizeMap, StreamConfigurationMap streamConfigurationMap) {
        Size[] outputSizes;
        for (Size size : streamConfigurationMap.getOutputSizes(256)) {
            this.mPictureSizes.add(new com.CoderlyticsCameraView.Size(size.getWidth(), size.getHeight()));
        }
    }

    private void prepareImageReader() {
        ImageReader imageReader = this.mImageReader;
        if (imageReader != null) {
            imageReader.close();
        }
        com.CoderlyticsCameraView.Size size = this.mPictureSizes.sizes(this.mAspectRatio).last();
        this.mImageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), 256, 2);
        this.mImageReader.setOnImageAvailableListener(this.mOnImageAvailableListener, null);
    }

    private void startOpeningCamera() {
        try {
            this.mCameraManager.openCamera(this.mCameraId, this.mCameraDeviceCallback, null);
        } catch (CameraAccessException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Failed to open camera: ");
            sb.append(this.mCameraId);
            throw new RuntimeException(sb.toString(), e);
        }
    }

    
    public void startCaptureSession() {
        if (isCameraOpened() && this.mPreview.isReady() && this.mImageReader != null) {
            com.CoderlyticsCameraView.Size chooseOptimalSize = chooseOptimalSize();
            this.mPreview.setBufferSize(chooseOptimalSize.getWidth(), chooseOptimalSize.getHeight());
            Surface surface = this.mPreview.getSurface();
            try {
                this.mPreviewRequestBuilder = this.mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                this.mPreviewRequestBuilder.addTarget(surface);
                this.mCamera.createCaptureSession(Arrays.asList(new Surface[]{surface, this.mImageReader.getSurface()}), this.mSessionCallback, null);
            } catch (CameraAccessException unused) {
                throw new RuntimeException("Failed to start camera session");
            }
        }
    }

    private com.CoderlyticsCameraView.Size chooseOptimalSize() {
        int width = this.mPreview.getWidth();
        int height = this.mPreview.getHeight();
        if (width < height) {
            int i = height;
            height = width;
            width = i;
        }
        SortedSet<com.CoderlyticsCameraView.Size> sizes = this.mPreviewSizes.sizes(this.mAspectRatio);
        for (com.CoderlyticsCameraView.Size size : sizes) {
            if (size.getWidth() >= width && size.getHeight() >= height) {
                return size;
            }
        }
        return  sizes.last();
    }

    
    public void updateAutoFocus() {
        boolean z = this.mAutoFocus;
        Integer valueOf = Integer.valueOf(0);
        if (z) {
            int[] iArr = (int[]) this.mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            if (iArr == null || iArr.length == 0 || (iArr.length == 1 && iArr[0] == 0)) {
                this.mAutoFocus = false;
                this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, valueOf);
                return;
            }
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, Integer.valueOf(4));
            return;
        }
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, valueOf);
    }

    
    public void updateFlash() {
        int i = this.mFlash;
        if (i == 0) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
        } else if (i == 1) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(3));
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
        } else if (i == 2) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
        } else if (i == 3) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
        } else if (i == 4) {
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(4));
            this.mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
        }
    }

    private void lockFocus() {
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(1));
        try {
            this.mCaptureCallback.setState(1);
            this.mCaptureSession.capture(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to lock focus.", e);
        }
    }

    
    public void captureStillPicture() {
        try {
            Builder createCaptureRequest = this.mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            createCaptureRequest.addTarget(this.mImageReader.getSurface());
            createCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, this.mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_MODE));
            int i = this.mFlash;
            int i2 = 1;
            if (i == 0) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                createCaptureRequest.set(CaptureRequest.FLASH_MODE, Integer.valueOf(0));
            } else if (i == 1) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(3));
            } else if (i == 2) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(1));
                createCaptureRequest.set(CaptureRequest.FLASH_MODE, Integer.valueOf(2));
            } else if (i == 3) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
            } else if (i == 4) {
                createCaptureRequest.set(CaptureRequest.CONTROL_AE_MODE, Integer.valueOf(2));
            }
            int intValue = ((Integer) this.mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
            Key key = CaptureRequest.JPEG_ORIENTATION;
            int i3 = this.mDisplayOrientation;
            if (this.mFacing != 1) {
                i2 = -1;
            }
            createCaptureRequest.set(key, Integer.valueOf(((intValue + (i3 * i2)) + 360) % 360));
            this.mCaptureSession.stopRepeating();
            this.mCaptureSession.capture(createCaptureRequest.build(), new CaptureCallback() {
                public void onCaptureCompleted(@NonNull CameraCaptureSession cameraCaptureSession, @NonNull CaptureRequest captureRequest, @NonNull TotalCaptureResult totalCaptureResult) {
                    unlockFocus();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot capture a still picture.", e);
        }
    }

    
    public void unlockFocus() {
        this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(2));
        try {
            this.mCaptureSession.capture(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
            updateAutoFocus();
            updateFlash();
            this.mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, Integer.valueOf(0));
            this.mCaptureSession.setRepeatingRequest(this.mPreviewRequestBuilder.build(), this.mCaptureCallback, null);
            this.mCaptureCallback.setState(0);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to restart camera preview.", e);
        }
    }
}
