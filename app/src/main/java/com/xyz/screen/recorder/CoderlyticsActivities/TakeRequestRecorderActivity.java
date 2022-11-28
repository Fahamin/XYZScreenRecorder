package com.xyz.screen.recorder.CoderlyticsActivities;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.BaseActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStopService;
import com.xyz.screen.recorder.CoderlyticsServices.BubbleControlService;
import com.xyz.screen.recorder.CoderlyticsServices.RecorderService;

public class TakeRequestRecorderActivity extends BaseActivity {
    private MediaProjectionManager mProjectionManager;

    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        this.mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(this.mProjectionManager.createScreenCaptureIntent(), CoderlyticsConstants.SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == 0 && i == 1113) {
            ObserverUtils.getInstance().notifyObservers(new EvbStopService());
            Toast.makeText(this, getString(R.string.screen_recording_permission_denied), 0).show();
            finish();
            return;
        }
        BubbleControlService.isRecording = true;
        Intent intent2 = new Intent(this, RecorderService.class);
        intent2.setAction(CoderlyticsConstants.SCREEN_RECORDING_START);
        intent2.putExtra(CoderlyticsConstants.RECORDER_INTENT_DATA, intent);
        intent2.putExtra(CoderlyticsConstants.RECORDER_INTENT_RESULT, i2);
        startService(intent2);
        finish();
    }
}
