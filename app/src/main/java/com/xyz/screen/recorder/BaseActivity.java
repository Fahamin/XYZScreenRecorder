package com.xyz.screen.recorder;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        CoderlyticsRecorderApplication.getInstance().doForCreate(this);
    }

    
    public void onDestroy() {
        super.onDestroy();
        CoderlyticsRecorderApplication.getInstance().doForFinish(this);
    }

    public final void clear() {
        super.finish();
    }
}
