package com.xyz.screen.recorder.CoderlyticsMindWork.Utilts;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.MediaStore.Video.Media;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.Log;

import androidx.annotation.NonNull;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.CoderlyticsActivities.RecCompletedDialogActivity;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {
    public static final String ANDROID_CLIENT_TYPE = "android";

    public static void setEnableTouch(Context context, int i) {
    }

    public static boolean isAndroid26() {
        return VERSION.SDK_INT >= 26;
    }

    public static int convertDpToPixel(float f, Context context) {
        return (int) (f * (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f));
    }

    public static int convertPixelsToDp(float f, Context context) {
        return (int) (f / (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f));
    }

    public static void openURL(Activity activity, String str) {
        try {
            activity.getApplicationContext().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)).setFlags(268435456));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showDialogResult(Context context, String str) {
        Intent intent = new Intent(context, RecCompletedDialogActivity.class);
        intent.putExtra("path", str);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        if (isAppOnForeground(context)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }
        intent.setFlags(268468224);
        try {
            PendingIntent.getActivity(context, (int) (Math.random() * 9999.0d), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT).send();
        } catch (CanceledException e) {
            e.printStackTrace();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static Calendar toCalendar(long j) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance;
    }

    public static int getScreenWidth(@NonNull Context context) {
        Point point = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
        return point.x;
    }

    public static int getScreenHeight(@NonNull Context context) {
        Point point = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
        return point.y;
    }

    public static boolean isInLandscapeMode(@NonNull Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public static void createDir() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.APPDIR);
        File file = new File(sb.toString());
        if (Environment.getExternalStorageState().equals("mounted") && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static void createDirEdited() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.APPDIR);
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.FOLDER_EDITED);
        File file = new File(sb.toString());
        if (Environment.getExternalStorageState().equals("mounted") && !file.isDirectory()) {
            file.mkdirs();
        }
    }

    public static boolean isServiceRunning(Class<?> cls, Context context) {
        for (RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
            if (cls.getName().equals(runningServiceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String generateSectionTitle(Date date) {
        Calendar calendar = toCalendar(new Date().getTime());
        Calendar calendar2 = toCalendar(date.getTime());
        int abs = (int) Math.abs((calendar2.getTimeInMillis() - calendar.getTimeInMillis()) / 86400000);
        if (calendar.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR) != 0) {
            return new SimpleDateFormat("EEEE, dd MMM YYYY", Locale.getDefault()).format(date);
        } else if (abs == 0) {
            return "Today";
        } else {
            if (abs == 1) {
                return "Yesterday";
            }
            return new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(date);
        }
    }

    public static String getValue(String[] strArr, String[] strArr2, String str)
    {
        for (int i = 0; i < strArr2.length; i++) {
            Log.i("iaminu","strArr2[i] : "+strArr2[i]+"   str = "+str);
            if (strArr2[i].equalsIgnoreCase(str)) {
                return strArr[i];
            }
        }
        return strArr[0];
    }

    public static int getPosition(String[] strArr, String str) {
        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].equalsIgnoreCase(str)) {
                return i;
            }
        }
        return 0;
    }

    public static boolean isAndroid23() {
        return VERSION.SDK_INT >= 23;
    }

    public static Bitmap getBitmapVideo(Context context, File file) {
        ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
        String str = "_id";
        String[] strArr = {str, "bucket_id", "bucket_display_name", "_data"};
        ContentResolver contentResolver2 = contentResolver;
        Cursor query = contentResolver2.query(Media.getContentUri("external"), strArr, "_data=? ", new String[]{file.getPath()}, null);
        if (query == null || !query.moveToNext()) {
            return null;
        }
        Bitmap thumbnail = Thumbnails.getThumbnail(contentResolver, (long) query.getInt(query.getColumnIndexOrThrow(str)), 1, null);
        query.close();
        return thumbnail;
    }

    public static int getHeightStatusBar(Context context) {
        int identifier = context.getResources().getIdentifier("status_bar_height", "dimen", ANDROID_CLIENT_TYPE);
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    public static int getHeightNavigationBar(Context context) {
        int identifier = context.getResources().getIdentifier("navigation_bar_height", "dimen", ANDROID_CLIENT_TYPE);
        if (identifier > 0) {
            return context.getResources().getDimensionPixelSize(identifier);
        }
        return 0;
    }

    public static File getCacheFile(Context context) {
        File cacheDir = context.getCacheDir();
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        sb.append(".JPEG");
        File file = new File(cacheDir, sb.toString());
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    public static Bitmap CropBitmapTransparency(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int i = -1;
        int height = bitmap.getHeight();
        int i2 = -1;
        int i3 = width;
        int i4 = 0;
        while (i4 < bitmap.getHeight()) {
            int i5 = i2;
            int i6 = i;
            int i7 = i3;
            for (int i8 = 0; i8 < bitmap.getWidth(); i8++) {
                if (((bitmap.getPixel(i8, i4) >> 24) & 255) > 0) {
                    if (i8 < i7) {
                        i7 = i8;
                    }
                    if (i8 > i6) {
                        i6 = i8;
                    }
                    if (i4 < height) {
                        height = i4;
                    }
                    if (i4 > i5) {
                        i5 = i4;
                    }
                }
            }
            i4++;
            i3 = i7;
            i = i6;
            i2 = i5;
        }
        if (i < i3 || i2 < height) {
            return null;
        }
        return Bitmap.createBitmap(bitmap, i3, height, (i - i3) + 1, (i2 - height) + 1);
    }

    public static String getAppUrl(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://play.google.com/store/apps/details?id=");
        sb.append(BuildConfig.APPLICATION_ID);
        return sb.toString();
    }

    public static boolean isAppOnForeground(Context context) {
        List<RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }
        String packageName = BuildConfig.APPLICATION_ID;
        for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
            if (runningAppProcessInfo.importance == 100 && runningAppProcessInfo.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static void startActivityAllStage(Context context, Intent intent) {
        if (context instanceof Activity) {
            context.startActivity(intent);
            return;
        }
        try {
            PendingIntent.getActivity(context, (int) (Math.random() * 9999.0d), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT).send();
        } catch (CanceledException e) {
            e.printStackTrace();
            context.startActivity(intent);
        }
    }
}
