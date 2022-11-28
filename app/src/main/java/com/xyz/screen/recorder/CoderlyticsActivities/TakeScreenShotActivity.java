package com.xyz.screen.recorder.CoderlyticsActivities;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.BaseActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsServices.ScRecBrushService;
import com.xyz.screen.recorder.CoderlyticsServices.BubbleControlService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TakeScreenShotActivity extends BaseActivity {

    private static final int NOTIFICATION_ID = 161;
    
    public int IMAGES_PRODUCED = 0;
    
    public String STORE_DIRECTORY;
    private int VIRTUAL_DISPLAY_FLAGS = 9;
    private Handler handler = new Handler();
    private int mDensity;
    private Display mDisplay;
    
    public int mHeight;
    
    public ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mMediaProjectionManager;
    private NotificationManager mNotificationManager;
    private int mResultCode = 0;
    private Intent mResultData = null;
    private VirtualDisplay mVirtualDisplay;
    
    public int mWidth;
    private DisplayMetrics metrics;
    private Runnable runnable = new Runnable() {
        public void run() {
            activeScreenCapture();
        }
    };
    private int type = 0;

    private class ImageAvailableListener implements OnImageAvailableListener {
        private ImageAvailableListener() {
        }
@Override
           public void onImageAvailable(ImageReader imageReader) {
            Intent intent;
            Image acquireLatestImage;
            Bitmap createBitmap;
            FileOutputStream fileOutputStream;
            String str = "capture";
            String str2 = CoderlyticsConstants.ACTION_SCREEN_SHOT;
            OutputStream outputStream = null;
            try {
                acquireLatestImage = mImageReader.acquireLatestImage();
                if (acquireLatestImage != null) {
                    try {
                        Plane[] planes = acquireLatestImage.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        createBitmap = Bitmap.createBitmap(mWidth + ((planes[0].getRowStride() - (mWidth * pixelStride)) / pixelStride), mHeight, Config.ARGB_8888);
                        try {
                            createBitmap.copyPixelsFromBuffer(buffer);
                            if (acquireLatestImage != null) {
                                acquireLatestImage.close();
                            }
                            if (IMAGES_PRODUCED == 0) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(STORE_DIRECTORY);
                                sb.append("/Screenshot_");
                                sb.append(getDateTime());
                                sb.append(".png");
                                String sb2 = sb.toString();
                                fileOutputStream = new FileOutputStream(sb2);
                                try {
                                    Utils.CropBitmapTransparency(Bitmap.createBitmap(createBitmap, 0, 0, mWidth, mHeight)).compress(CompressFormat.JPEG, 100, fileOutputStream);
                                    acquireLatestImage.close();
                                    IMAGES_PRODUCED = IMAGES_PRODUCED + 1;
                                    stopBrushService();
                                    showNotificationScreenshot(sb2);
                                    stopScreenCapture();
                                    sendBroadcast(new Intent(str2).putExtra(str, 1));
                                    tearDownMediaProjection();
                                    StringBuilder sb3 = new StringBuilder();
                                    sb3.append("file://");
                                    sb3.append(sb2);
                                    sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse(sb3.toString())));
                                    IMAGES_PRODUCED = 0;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (imageReader != null) {
                                        try {
                                            imageReader.close();
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        } catch (Throwable r ) {
                                        }
                                        fileOutputStream.close();
                                        if (createBitmap != null) {
                                        }
                                        if (acquireLatestImage != null) {
                                        }
                                    }
                                    if (createBitmap != null) {
                                        createBitmap.recycle();
                                    }
                                    if (acquireLatestImage == null) {
                                        intent = new Intent(str2);
                                        sendBroadcast(intent.putExtra(str, 1));
                                        return;
                                    }
                                    acquireLatestImage.close();
                                    fileOutputStream.close();
                                    if (createBitmap != null) {
                                    }
                                    if (acquireLatestImage != null) {
                                    }
                                } catch (Throwable t) {
                                    fileOutputStream.close();
                                    if (createBitmap != null) {
                                    }
                                    if (acquireLatestImage != null) {
                                    }
                                }
                                try {
                                    fileOutputStream.close();
                                    if (createBitmap != null) {
                                        createBitmap.recycle();
                                    }
                                    if (acquireLatestImage != null) {
                                        acquireLatestImage.close();
                                    }
                                    outputStream = fileOutputStream;
                                } catch (Exception e3) {
                                } catch (Throwable unused) {
                                }
                            }
                            IMAGES_PRODUCED = IMAGES_PRODUCED + 1;
                        } catch (Exception e4) {

                        } catch (Throwable unused2) {
                        }
                    } catch (Exception e5) {
                        e5.printStackTrace();
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (acquireLatestImage == null) {
                            intent = new Intent(str2);
                            sendBroadcast(intent.putExtra(str, 1));
                            return;
                        }
                        acquireLatestImage.close();
                    } catch (Throwable unused22) {
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Exception e6) {
                        e6.printStackTrace();
                    }
                }
                if (acquireLatestImage != null) {
                    acquireLatestImage.close();
                }
                sendBroadcast(new Intent(str2).putExtra(str, 1));
                return;


            } catch (Exception e7) {
                e7.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e8) {
                        e8.printStackTrace();
                    }
                }
                intent = new Intent(str2);
            } catch (Throwable th) {
                sendBroadcast(new Intent(str2).putExtra(str, 1));
                throw th;
            }
        }
    }

    
    public void stopBrushService() {
        if (type == 1001) {
            stopService(new Intent(this, ScRecBrushService.class));
        }
    }

    
    @SuppressLint({"WrongConstant"})
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (BubbleControlService.isCountdown) {
            finish();
            return;
        }
        sendBroadcast(new Intent(CoderlyticsConstants.ACTION_SCREEN_SHOT).putExtra("capture", 0));
        if (VERSION.SDK_INT >= 23) {
            String str = "android.permission.WRITE_EXTERNAL_STORAGE";
            if (checkSelfPermission(str) != 0) {
                ActivityCompat.requestPermissions(this, new String[]{str}, 131);
                return;
            }
        }
        if (!(getIntent() == null || getIntent().getExtras() == null)) {
            type = ((Integer) getIntent().getExtras().get(ScRecBrushService.BUNDLE_TYPE)).intValue();
        }
        metrics = getResources().getDisplayMetrics();
        mDensity = metrics.densityDpi;
        mDisplay = getWindowManager().getDefaultDisplay();
        mNotificationManager = (NotificationManager) getSystemService("notification");
        mMediaProjectionManager = (MediaProjectionManager) getSystemService("media_projection");
        handler.postDelayed(runnable, 200);
    }

    private void setUpMediaProjection() {
        try {
            if (mMediaProjection == null) {
                mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
            }
        } catch (Exception unused) {
        }
    }

    
    public void activeScreenCapture() {
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), 100);
        }
    }

    public void setUpVirtualDisplay() {
        Point point = new Point();
        mDisplay.getSize(point);
        mWidth = point.x;
        mHeight = point.y;
        StringBuilder sb = new StringBuilder();
        sb.append("Size: ");
        sb.append(mWidth);
        sb.append(" ");
        sb.append(mHeight);
        mImageReader = ImageReader.newInstance(mWidth, mHeight, 1, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(CoderlyticsConstants.APPDIR, mWidth, mHeight, mDensity, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, null);
        IMAGES_PRODUCED = 0;
        mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
    }

    public String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
    }

    
    public void showNotificationScreenshot(String str) {
        Utils.showDialogResult(getApplicationContext(), str);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        BitmapFactory.decodeFile(str);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(FileProvider.getUriForFile(this, "com.test.screenrecord.provider", new File(str)), "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent activity = PendingIntent.getActivity(this, 0, intent, 0);
        Builder builder = new Builder(this, "");
        builder.setContentIntent(activity).setContentTitle(getString(R.string.share_intent_notification_title_photo)).setContentText(getString(R.string.share_intent_notification_content_photo)).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setAutoCancel(true);
        mNotificationManager.cancel(NOTIFICATION_ID);
        if (Utils.isAndroid26()) {
            String str2 = "my_channel_id";
            NotificationChannel notificationChannel = new NotificationChannel(str2, "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            builder.setChannelId(str2);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        try {
            finish();
        } catch (Exception unused) {
        }
    }

    @Override
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i == 100) {
            if (i2 != -1) {
                Toast.makeText(this, getString(R.string.permission_deny), Toast.LENGTH_SHORT).show();
                stopScreenCapture();
                tearDownMediaProjection();
                finish();
                return;
            }
            mResultData = intent;
            mResultCode = i2;
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    startCaptureScreen();
                }
            }, 250);
            finish();
        }
    }

    public void startCaptureScreen() {
        if (mResultCode != 0 && mResultData != null) {
            if (mMediaProjection != null) {
                tearDownMediaProjection();
            }
            setUpMediaProjection();
            if (mMediaProjection != null) {
                SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                String string = getString(R.string.savelocation_key);
                StringBuilder sb = new StringBuilder();
                sb.append(Environment.getExternalStorageDirectory());
                sb.append(File.separator);
                String str = CoderlyticsConstants.APPDIR;
                sb.append(str);
                File file = new File(defaultSharedPreferences.getString(string, sb.toString()));
                String string2 = getString(R.string.savelocation_key);
                StringBuilder sb2 = new StringBuilder();
                sb2.append(Environment.getExternalStorageDirectory());
                sb2.append(File.separator);
                sb2.append(str);
                STORE_DIRECTORY = defaultSharedPreferences.getString(string2, sb2.toString());
                if (file.exists() || file.mkdirs()) {
                    setUpVirtualDisplay();
                    return;
                }
                stopScreenCapture();
                tearDownMediaProjection();
            }
        }
    }

    
    public void stopScreenCapture() {
        VirtualDisplay virtualDisplay = mVirtualDisplay;
        if (virtualDisplay != null) {
            virtualDisplay.release();
            mVirtualDisplay = null;
            ImageReader imageReader = mImageReader;
            if (imageReader != null) {
                imageReader.setOnImageAvailableListener(null, null);
            }
        }
    }

    
    public void tearDownMediaProjection() {
        MediaProjection mediaProjection = mMediaProjection;
        if (mediaProjection != null) {
            mediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public boolean isScreenshotActived() {
        return mResultCode == -1 && mResultData != null;
    }
}
