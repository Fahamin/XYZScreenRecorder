package com.xyz.screen.recorder.CoderlyticsActivities;

import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import androidx.core.content.FileProvider;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.BaseActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;

import java.io.File;

public class RecCompletedDialogActivity extends BaseActivity implements OnClickListener {
    private String FILEPATH;
    private Uri fileUri;
    private ImageView imgThumbnail;
    private boolean isVideo = true;
    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.btn_cancel :
                    finish();
                    break;
                case R.id.delete :
                    try {
                        new File(this.FILEPATH).delete();
                        closeNotify();
                        break;
                    } catch (Exception unused) {
                        break;
                    }
                case R.id.edit :
                    closeNotify();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;
                case R.id.play :
                case R.id.thumbnail :
                    closeNotify();
                    Intent intent2 = new Intent();
                    intent2.setAction("android.intent.action.VIEW").addFlags(268435457).setDataAndType(this.fileUri, getContentResolver().getType(this.fileUri));
                    startActivity(intent2);
                    break;
                case R.id.share :
                    Intent type = new Intent().setAction("android.intent.action.SEND").putExtra("android.intent.extra.STREAM", this.fileUri).setType(this.isVideo ? "video/mp4" : "image/*");
                    type.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(type);
                    break;
            }
            finish();
        } catch (Exception unused2) {
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.activity_completed_dialog_recording);
        intData();
        intView();
        intEvent();

    }

    public void intView() {
        Bitmap bitmap;
        try {
            if (this.FILEPATH.endsWith(".mp4")) {
                this.isVideo = true;
                sendBroadcast(new Intent(CoderlyticsConstants.UPDATE_UI));
            } else {
                sendBroadcast(new Intent(CoderlyticsConstants.UPDATE_UI_IMAGE));
                this.isVideo = false;
                findViewById(R.id.play).setVisibility(View.GONE);
                findViewById(R.id.edit).setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.thumbnail)).setScaleType(ScaleType.CENTER_CROP);

            }
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.APPLICATION_ID);
            sb.append(".provider");
            this.fileUri = FileProvider.getUriForFile(this, sb.toString(), new File(this.FILEPATH));
            if (this.isVideo) {
                bitmap = Utils.getBitmapVideo(this, new File(this.FILEPATH));
            } else {
                bitmap = BitmapFactory.decodeFile(this.FILEPATH);
            }
            this.imgThumbnail = (ImageView) findViewById(R.id.thumbnail);
            this.imgThumbnail.setImageBitmap(bitmap);
        } catch (Exception unused) {
        }
    }

    public void intData() {
        this.FILEPATH = getIntent().getStringExtra("path");
    }

    public void intEvent() {
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.edit).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.thumbnail).setOnClickListener(this);
    }

    private void closeNotify() {
        try {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(CoderlyticsConstants.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
        } catch (Exception unused) {
        }
    }
}
