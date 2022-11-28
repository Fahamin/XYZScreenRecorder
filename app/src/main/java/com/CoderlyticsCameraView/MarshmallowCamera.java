package com.CoderlyticsCameraView;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Size;

@TargetApi(23)
class MarshmallowCamera extends KitKatCamera {
    MarshmallowCamera(Callback callback, PreviewImpl previewImpl, Context context) {
        super(callback, previewImpl, context);
    }

    
    public void collectPictureSizes(SizeMap sizeMap, StreamConfigurationMap streamConfigurationMap) {
        Size[] highResolutionOutputSizes;
        if (streamConfigurationMap.getHighResolutionOutputSizes(256) != null) {
            for (Size size : streamConfigurationMap.getHighResolutionOutputSizes(256)) {
                sizeMap.add(new com.CoderlyticsCameraView.Size(size.getWidth(), size.getHeight()));
            }
        }
        if (sizeMap.isEmpty()) {
            super.collectPictureSizes(sizeMap, streamConfigurationMap);
        }
    }
}
