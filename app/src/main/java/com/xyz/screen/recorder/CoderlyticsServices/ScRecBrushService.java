package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.content.FileProvider;
import androidx.core.internal.view.SupportMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.xyz.screen.recorder.R;
import com.raed.drawingview.BrushView;
import com.raed.drawingview.DrawingView;
import com.raed.drawingview.brushes.BrushSettings;
import com.xyz.screen.recorder.CoderlyticsAdapters.ColorBrushAdapter;
import com.xyz.screen.recorder.CoderlyticsAdapters.ColorBrushAdapter.OnClick;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsActivities.TakeScreenShotActivity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ScRecBrushService extends Service implements OnTouchListener, OnClickListener {

    public static final String BUNDLE_TYPE = "TYPE";
    private static final int NOTIFICATION_ID = 161;
    public static final int TYPE = 1001;
    private ColorBrushAdapter colorBrushAdapter;
    
    public DrawingView drawingView;
    private ConstraintLayout mLayout;
    private NotificationManager mNotificationManager;
    private LayoutParams mParams;
    private String path = "";
    private WindowManager windowManager;

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint({"WrongConstant"})
    private void initView() {
        windowManager = (WindowManager) getApplicationContext().getSystemService("window");
        mNotificationManager = (NotificationManager) getSystemService("notification");
        mLayout = (ConstraintLayout) ((LayoutInflater) getApplicationContext().getSystemService("layout_inflater")).inflate(R.layout.layout_main_brush, null);
        drawingView = (DrawingView) mLayout.findViewById(R.id.drawview);
        RecyclerView recyclerView = (RecyclerView) mLayout.findViewById(R.id.rcv);
        final ConstraintLayout constraintLayout = (ConstraintLayout) mLayout.findViewById(R.id.container_color);
        final LinearLayout linearLayout = (LinearLayout) mLayout.findViewById(R.id.layout_brush);
        ImageView imageView =  mLayout.findViewById(R.id.imv_close);
        ImageView imageView2 =  mLayout.findViewById(R.id.imgCamera);
        ImageView imageView3 =  mLayout.findViewById(R.id.imgPaint);
        ImageView imageView4 =  mLayout.findViewById(R.id.imgEraser);
        ImageView imageView5 =  mLayout.findViewById(R.id.imgUndo);
        SeekBar seekBar = (SeekBar) mLayout.findViewById(R.id.size_seek_bar);
        ((ImageView) mLayout.findViewById(R.id.imgClose)).setOnClickListener(this);
        imageView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                constraintLayout.setVisibility(8);
            }
        });
        LayoutParams layoutParams = new LayoutParams(-1, -1, 2038, 8, -3);
        mParams = layoutParams;
        if (VERSION.SDK_INT < 26) {
            mParams.type = 2005;
        }
        final BrushView brushView = (BrushView) mLayout.findViewById(R.id.brush_view);
        brushView.setDrawingView(drawingView);
        final BrushSettings brushSettings = drawingView.getBrushSettings();
        brushSettings.setSelectedBrush(0);
        drawingView.setUndoAndRedoEnable(true);
        imageView4.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                brushSettings.setSelectedBrush(4);
            }
        });
        imageView5.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                drawingView.undo();
            }
        });
        imageView3.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                brushSettings.setSelectedBrush(0);
                constraintLayout.setVisibility(0);
            }
        });
        imageView2.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                linearLayout.setVisibility(8);
                screenShot();
            }
        });

        windowManager.addView(mLayout, mParams);
        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                brushSettings.setSelectedBrushSize(((float) i) / 100.0f);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                brushView.setVisibility(0);
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                brushView.setVisibility(8);
            }
        });
        colorBrushAdapter = new ColorBrushAdapter(this, initColors(), new OnClick() {
            public void onClickColor(int i) {
                brushSettings.setColor(i);
            }
        });
        recyclerView.setAdapter(colorBrushAdapter);
    }

    
    public void screenShot() {
        Intent intent = new Intent(this, TakeScreenShotActivity.class);
        intent.putExtra(BUNDLE_TYPE, 1001);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private ArrayList<Integer> initColors() {
        ArrayList<Integer> arrayList = new ArrayList<>();

        arrayList.add(Integer.valueOf(Color.parseColor("#ffffff")));
        arrayList.add(Integer.valueOf(Color.parseColor("#8E388E")));
        arrayList.add(Integer.valueOf(Color.parseColor("#f58996")));
        arrayList.add(Integer.valueOf(Color.parseColor("#900c3e")));
        arrayList.add(Integer.valueOf(Color.parseColor("#FF8000")));
        arrayList.add(Integer.valueOf(Color.parseColor("#F0E68C")));
        arrayList.add(Integer.valueOf(Color.parseColor("#97E075")));
        arrayList.add(Integer.valueOf(Color.parseColor("#FF83FA")));
        arrayList.add(Integer.valueOf(Color.parseColor("#039BE5")));
        arrayList.add(Integer.valueOf(Color.parseColor("#00ACC1")));
        arrayList.add(Integer.valueOf(Color.parseColor("#00897B")));
        arrayList.add(Integer.valueOf(Color.parseColor("#FDD835")));
        arrayList.add(Integer.valueOf(Color.parseColor("#FFB300")));
        arrayList.add(Integer.valueOf(Color.parseColor("#F4511E")));



        return arrayList;
    }

    private void saveResult(Bitmap bitmap) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String string = getString(R.string.savelocation_key);
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.APPDIR);
        String string2 = defaultSharedPreferences.getString(string, sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(string2);
        sb2.append("/Screenshot_");
        sb2.append(getDateTime());
        sb2.append(".png");
        String sb3 = sb2.toString();
        try {
            bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(sb3));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        showNotificationScreenshot(sb3);
        StringBuilder sb4 = new StringBuilder();
        sb4.append("file://");
        sb4.append(sb3);
        sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.parse(sb4.toString())));
        stopSelf();
    }

    private void showNotificationScreenshot(String str) {
        Utils.showDialogResult(getApplicationContext(), str);
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
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

    public String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        initView();
        return START_NOT_STICKY;
    }

    public void onClick(View view) {
        if (view.getId() == R.id.imgClose) {
            stopSelf();
        }
    }

    public void onDestroy() {
        WindowManager windowManager2 = windowManager;
        if (windowManager2 != null) {
            ConstraintLayout constraintLayout = mLayout;
            if (constraintLayout != null) {
                windowManager2.removeView(constraintLayout);
            }
        }
        super.onDestroy();
    }
}
