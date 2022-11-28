package com.CoderlyticsCameraView;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ParcelableCompat;
import androidx.core.os.ParcelableCompatCreatorCallbacks;
import androidx.core.view.ViewCompat;

import com.xyz.screen.recorder.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class CameraView extends FrameLayout {

    public static final int FACING_BACK = 0;
    public static final int FACING_FRONT = 1;
    public static final int FLASH_AUTO = 3;
    public static final int FLASH_OFF = 0;
    public static final int FLASH_ON = 1;
    public static final int FLASH_RED_EYE = 4;
    public static final int FLASH_TORCH = 2;
    private boolean mAdjustViewBounds;
    private final CallbackBridge mCallbacks;
    private final ScreenOrientationDetector mScreenOrientationDetector;
    CameraViewImpl mImpl;

    public static abstract class Callback {
        public void onCameraClosed(CameraView cameraView) {
        }

        public void onCameraOpened(CameraView cameraView) {
        }

        public void onPictureTaken(CameraView cameraView, byte[] bArr) {
        }
    }

    private class CallbackBridge extends Callback {
        private final ArrayList<Callback> mCallbacks = new ArrayList<>();
        private boolean mRequestLayoutOnOpen;

        CallbackBridge() {
        }

        public void add(Callback callback) {
            this.mCallbacks.add(callback);
        }

        public void remove(Callback callback) {
            this.mCallbacks.remove(callback);
        }

        public void onCameraOpened()
        {
            if (this.mRequestLayoutOnOpen) {
                this.mRequestLayoutOnOpen = false;
                CameraView.this.requestLayout();
            }
            Iterator it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                ((Callback) it.next()).onCameraOpened(CameraView.this);
            }
        }

        public void onCameraClosed() {
            Iterator it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                ((Callback) it.next()).onCameraClosed(CameraView.this);
            }
        }

        public void onPictureTaken(byte[] bArr) {
            Iterator it = this.mCallbacks.iterator();
            while (it.hasNext()) {
                ((Callback) it.next()).onPictureTaken(CameraView.this, bArr);
            }
        }

        public void reserveRequestLayoutOnOpen() {
            this.mRequestLayoutOnOpen = true;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    public @interface Flash {
    }

    protected static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        });
        boolean autoFocus;
        int facing;
        @Flash
        int flash;
        AspectRatio ratio;

        public SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel);
            this.facing = parcel.readInt();
            this.ratio = (AspectRatio) parcel.readParcelable(classLoader);
            this.autoFocus = parcel.readByte() != 0;
            this.flash = parcel.readInt();
        }

        public SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.facing);
            parcel.writeParcelable(this.ratio, 0);
            parcel.writeByte(this.autoFocus ? (byte) 1 : 0);
            parcel.writeInt(this.flash);
        }
    }

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CameraView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        if (isInEditMode()) {
            this.mCallbacks = null;
            this.mScreenOrientationDetector = null;
            return;
        }
        PreviewImpl createPreviewImpl = createPreviewImpl(context);
        this.mCallbacks = new CallbackBridge();
        if (VERSION.SDK_INT < 21)
        {
            this.mImpl = new PreKitkatCamera(new CameraViewImpl.Callback() {
                @Override
                public void onCameraClosed() {

                }

                @Override
                public void onCameraOpened() {

                }

                @Override
                public void onPictureTaken(byte[] bArr) {

                }
            }, createPreviewImpl);
        } else if (VERSION.SDK_INT < 23) {
            this.mImpl = new KitKatCamera(new CameraViewImpl.Callback() {
                @Override
                public void onCameraClosed() {

                }

                @Override
                public void onCameraOpened() {

                }

                @Override
                public void onPictureTaken(byte[] bArr) {

                }
            }, createPreviewImpl, context);
        } else {
            this.mImpl = new MarshmallowCamera(new CameraViewImpl.Callback() {
                @Override
                public void onCameraClosed() {

                }

                @Override
                public void onCameraOpened() {

                }

                @Override
                public void onPictureTaken(byte[] bArr) {

                }
            }, createPreviewImpl, context);
        }
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.CameraView, i, R.style.Widget_CameraView);
        this.mAdjustViewBounds = obtainStyledAttributes.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false);
        setFacing(obtainStyledAttributes.getInt(R.styleable.CameraView_facing, 0));
        String string = obtainStyledAttributes.getString(R.styleable.CameraView_aspectRatio);
        if (string != null) {
            setAspectRatio(AspectRatio.parse(string));
        } else {
            setAspectRatio(Constants.DEFAULT_ASPECT_RATIO);
        }
        setAutoFocus(obtainStyledAttributes.getBoolean(R.styleable.CameraView_autoFocus, true));
        setFlash(obtainStyledAttributes.getInt(R.styleable.CameraView_flash, 3));
        obtainStyledAttributes.recycle();
        this.mScreenOrientationDetector = new ScreenOrientationDetector(context) {
            public void onDisplayOrientationChanged(int i) {
                CameraView.this.mImpl.setDisplayOrientation(i);
            }
        };
    }

    @NonNull
    private PreviewImpl createPreviewImpl(Context context) {
        if (VERSION.SDK_INT < 14) {
            return new CamSurfaceViewPreview(context, this);
        }
        return new CamTextureViewPreview(context, this);
    }

    
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            this.mScreenOrientationDetector.enable(ViewCompat.getDisplay(this));
        }
    }

    
    public void onDetachedFromWindow() {
        if (!isInEditMode()) {
            this.mScreenOrientationDetector.disable();
        }
        super.onDetachedFromWindow();
    }

    
    public void onMeasure(int i, int i2) {
        if (isInEditMode()) {
            super.onMeasure(i, i2);
            return;
        }
        if (!this.mAdjustViewBounds) {
            super.onMeasure(i, i2);
        } else if (!isCameraOpened()) {
            this.mCallbacks.reserveRequestLayoutOnOpen();
            super.onMeasure(i, i2);
            return;
        } else {
            int mode = MeasureSpec.getMode(i);
            int mode2 = MeasureSpec.getMode(i2);
            if (mode == 1073741824 && mode2 != 1073741824) {
                int size = (int) (((float) MeasureSpec.getSize(i)) * getAspectRatio().toFloat());
                if (mode2 == Integer.MIN_VALUE) {
                    size = Math.min(size, MeasureSpec.getSize(i2));
                }
                super.onMeasure(i, MeasureSpec.makeMeasureSpec(size, 1073741824));
            } else if (mode == 1073741824 || mode2 != 1073741824) {
                super.onMeasure(i, i2);
            } else {
                int size2 = (int) (((float) MeasureSpec.getSize(i2)) * getAspectRatio().toFloat());
                if (mode == Integer.MIN_VALUE) {
                    size2 = Math.min(size2, MeasureSpec.getSize(i));
                }
                super.onMeasure(MeasureSpec.makeMeasureSpec(size2, 1073741824), i2);
            }
        }
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        AspectRatio aspectRatio = getAspectRatio();
        if (this.mScreenOrientationDetector.getLastKnownDisplayOrientation() % 180 == 0) {
            aspectRatio = aspectRatio.inverse();
        }
        if (measuredHeight < (aspectRatio.getY() * measuredWidth) / aspectRatio.getX()) {
            this.mImpl.getView().measure(MeasureSpec.makeMeasureSpec(measuredWidth, 1073741824), MeasureSpec.makeMeasureSpec((measuredWidth * aspectRatio.getY()) / aspectRatio.getX(), 1073741824));
        } else {
            this.mImpl.getView().measure(MeasureSpec.makeMeasureSpec((aspectRatio.getX() * measuredHeight) / aspectRatio.getY(), 1073741824), MeasureSpec.makeMeasureSpec(measuredHeight, 1073741824));
        }
    }

    
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.facing = getFacing();
        savedState.ratio = getAspectRatio();
        savedState.autoFocus = getAutoFocus();
        savedState.flash = getFlash();
        return savedState;
    }

    
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setFacing(savedState.facing);
        setAspectRatio(savedState.ratio);
        setAutoFocus(savedState.autoFocus);
        setFlash(savedState.flash);
    }

    public void start() {
        if (!this.mImpl.start()) {
            Parcelable onSaveInstanceState = onSaveInstanceState();
            this.mImpl = new PreKitkatCamera(new CameraViewImpl.Callback() {
                @Override
                public void onCameraClosed() {

                }

                @Override
                public void onCameraOpened() {

                }

                @Override
                public void onPictureTaken(byte[] bArr) {

                }
            }, createPreviewImpl(getContext()));
            onRestoreInstanceState(onSaveInstanceState);
            this.mImpl.start();
        }
    }

    public void stop() {
        this.mImpl.stop();
    }

    public boolean isCameraOpened() {
        return this.mImpl.isCameraOpened();
    }

    public void addCallback(@NonNull Callback callback) {
        this.mCallbacks.add(callback);
    }

    public void removeCallback(@NonNull Callback callback) {
        this.mCallbacks.remove(callback);
    }

    public void setAdjustViewBounds(boolean z) {
        if (this.mAdjustViewBounds != z) {
            this.mAdjustViewBounds = z;
            requestLayout();
        }
    }

    public boolean getAdjustViewBounds() {
        return this.mAdjustViewBounds;
    }

    public void setFacing(int i) {
        this.mImpl.setFacing(i);
    }

    public int getFacing() {
        return this.mImpl.getFacing();
    }

    public Set<AspectRatio> getSupportedAspectRatios() {
        return this.mImpl.getSupportedAspectRatios();
    }

    public void setAspectRatio(@NonNull AspectRatio aspectRatio) {
        if (this.mImpl.setAspectRatio(aspectRatio)) {
            requestLayout();
        }
    }

    @Nullable
    public AspectRatio getAspectRatio() {
        return this.mImpl.getAspectRatio();
    }

    public void setAutoFocus(boolean z) {
        this.mImpl.setAutoFocus(z);
    }

    public boolean getAutoFocus() {
        return this.mImpl.getAutoFocus();
    }

    public void setFlash(@Flash int i) {
        this.mImpl.setFlash(i);
    }

    @Flash
    public int getFlash() {
        return this.mImpl.getFlash();
    }

    public void takePicture() {
        this.mImpl.takePicture();
    }
}
