package com.xyz.screen.recorder.CoderlyticsServices;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjection.Callback;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.app.NotificationCompat.Builder;
import androidx.core.internal.view.SupportMenu;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.xyz.screen.recorder.CoderlyticsActivities.MainActivity;
import com.xyz.screen.recorder.CoderlyticsMindWork.ShakeEventManager;
import com.xyz.screen.recorder.CoderlyticsMindWork.ShakeEventManager.ShakeListener;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants.RecordingState;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.PrefUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.ObserverUtils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbRecordTime;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStageRecord;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStartRecord;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.EvbStopService;
import com.xyz.screen.recorder.CoderlyticsServices.SwimCameraViewService.ServiceBinder;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.TrimmingVideos.Toolbox;
import com.xyz.screen.recorder.TrimmingVideos.TrimmerActivity;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class RecorderService extends Service implements ShakeListener {
    private static int BITRATE;
    private static int DENSITY_DPI;
    private static int FPS;
    private static int HEIGHT;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    public static String SAVEPATH;
    private static int WIDTH;

    public static ArrayList<String> arrPart;
    private static String audioRecSource;
    public static boolean isRecording;

    public static int part = 0;
    private Intent data;
    private long elapsedTime = 0;
    private ServiceConnection floatingCameraConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            ((ServiceBinder) iBinder).getService();

        }

        public void onServiceDisconnected(ComponentName componentName) {
            bubbleControlService = null;
        }
    };

    public BubbleControlService bubbleControlService;

    public boolean isBound = false;
    private boolean isShakeGestureActive;

    public boolean isStart = false;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            Toast.makeText(RecorderService.this, R.string.screen_recording_stopped_toast, Toast.LENGTH_SHORT).show();
            showShareNotification();
            Utils.showDialogResult(getApplicationContext(), RecorderService.SAVEPATH);
        }
    };
    private MediaProjection mMediaProjection;
    private MediaProjectionCallback mMediaProjectionCallback;
    private MediaRecorder mMediaRecorder;
    private NotificationManager mNotificationManager;
    private ShakeEventManager mShakeDetector;
    private VirtualDisplay mVirtualDisplay;
    private SharedPreferences prefs;
    private int result;
    private int screenOrientation;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bubbleControlService = ((BubbleControlService.ServiceBinder) iBinder).getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            bubbleControlService = null;
            isBound = false;
        }
    };
    private boolean showCameraOverlay;
    private long startTime;

    public int time = 0;
    private WindowManager window;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionCallback extends Callback {
        private MediaProjectionCallback() {
        }

        @Override
        public void onStop() {
            Log.v(CoderlyticsConstants.TAG, "Recording Stopped");
            stopScreenSharing();
        }
    }

    public class MyTask extends AsyncTask<Void, Void, Void> {
        ArrayList<String> sourceFiles;
        String targetFile;

        public MyTask(ArrayList<String> arrayList, String str) {
            sourceFiles = arrayList;
            targetFile = str;
        }


        @Override
        public Void doInBackground(Void... voidArr) {
            String str = CoderlyticsConstants.TAG;
            try {
                Log.e(str, "start mergeVideos");
                StringBuilder sb = new StringBuilder();
                sb.append("target: ");
                sb.append(targetFile);
                Log.e(str, sb.toString());
                ArrayList<Movie> arrayList = new ArrayList<>();
                Iterator it = sourceFiles.iterator();
                while (it.hasNext()) {
                    String str2 = (String) it.next();
                    arrayList.add(MovieCreator.build(str2));
                    Log.e(str, str2);
                }
                LinkedList linkedList = new LinkedList();
                LinkedList linkedList2 = new LinkedList();
                for (Movie tracks : arrayList) {
                    for (Track track : tracks.getTracks()) {
                        if (track.getHandler().equals("vide")) {
                            linkedList.add(track);
                        }
                        if (track.getHandler().equals("soun")) {
                            linkedList2.add(track);
                        }
                    }
                }
                Movie movie = new Movie();
                if (!linkedList.isEmpty()) {
                    movie.addTrack(new AppendTrack((Track[]) linkedList.toArray(new Track[linkedList.size()])));
                }
                if (VERSION.SDK_INT < 24 && !linkedList2.isEmpty()) {
                    movie.addTrack(new AppendTrack((Track[]) linkedList2.toArray(new Track[linkedList2.size()])));
                }
                Container build = new DefaultMp4Builder().build(movie);
                FileChannel channel = new RandomAccessFile(String.format(targetFile, new Object[0]), "rw").getChannel();
                build.writeContainer(channel);
                channel.close();
                indexFile();
                Iterator it2 = RecorderService.arrPart.iterator();
                while (it2.hasNext()) {
                    File file = new File((String) it2.next());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                RecorderService.part = 0;
                RecorderService.arrPart.clear();
                Log.e(str, "finish mergeVideos");
            } catch (Exception e) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Error merging media files. exception: ");
                sb2.append(e.getMessage());
                Log.e(str, sb2.toString());
            }
            return null;
        }


        @Override
        public void onPreExecute() {
            super.onPreExecute();
            Log.e(CoderlyticsConstants.TAG, "onPreExcute");
        }


        @Override
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            Log.e(CoderlyticsConstants.TAG, "onPostExcute");
            stopForeground(true);
        }
    }

    private class TimeCount extends AsyncTask<Void, Integer, Void> {
        private TimeCount() {
        }


        @Override
        public Void doInBackground(Void... voidArr) {
            ObserverUtils.getInstance().notifyObservers(new EvbStartRecord());
            while (isStart) {
                if (RecorderService.isRecording) {
                    time = time + 1;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(new Integer[]{Integer.valueOf(time)});
            }
            return null;
        }


        @Override
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            ObserverUtils instance = ObserverUtils.getInstance();
            StringBuilder sb = new StringBuilder();
            sb.append(numArr[0]);
            sb.append("");
            instance.notifyObservers(new EvbRecordTime(Toolbox.converTime(sb.toString())));
        }

        @Override
        public void onPostExecute(Void voidR) {
            super.onPostExecute(voidR);
            ObserverUtils.getInstance().notifyObservers(new EvbStageRecord(false));
            time = 0;
        }
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    static {
        ORIENTATIONS.append(0, 0);
        ORIENTATIONS.append(1, 90);
        ORIENTATIONS.append(2, 180);
        ORIENTATIONS.append(3, 270);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        char c;
        if (VERSION.SDK_INT >= 26) {
           createNotificationChannel();
        }
        Intent intent2 = new Intent(this, BubbleControlService.class);
        if (intent != null) {
            PendingIntent pendingIntent1 = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification1 = new NotificationCompat.Builder(this, "notification_id")
                    .setContentTitle("yNote studios")
                    .setContentText("Filming...")
                    .setContentIntent(pendingIntent1).build();

            intent2.setAction(intent.getAction());
            startService(intent2);
            startForeground(110, notification1);
            bindService(intent2, serviceConnection, BIND_AUTO_CREATE);

            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String action = intent.getAction();
            int hashCode = action.hashCode();
            String str = CoderlyticsConstants.SCREEN_RECORDING_DESTORY_SHAKE_GESTURE;
            Log.i("iaminfg", "action.hashCode()  =  " + action.hashCode());

            switch (hashCode) {
                case -1053033865:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_STOP)) {
                        c = 3;
                        break;
                    }
                case -592011553:
                    if (action.equals(str)) {
                        c = 4;
                        break;
                    }
                case -453103993:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_START)) {
                        c = 0;
                        break;
                    }
                case 1599260844:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_RESUME)) {
                        c = 2;
                        break;
                    }
                case 1780700019:
                    if (action.equals(CoderlyticsConstants.SCREEN_RECORDING_PAUSE)) {
                        c = 1;
                        break;
                    }
                default:
                    c = 65535;
                    break;
            }
            if (c != 0) {
                if (c == 1) {
                    pauseScreenRecording();
                } else if (c == 2) {
                    resumeScreenRecording();
                } else if (c != 3) {
                    if (c == 4) {
                        mShakeDetector.stop();
                        stopSelf();
                    }
                } else if (isRecording) {
                    stopRecording();
                }
                Log.i("iamngs", "isRecording=  " + isRecording + " c== " + c);
            } else if (!isRecording
            ) {
                screenOrientation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
                data = (Intent) intent.getParcelableExtra(CoderlyticsConstants.RECORDER_INTENT_DATA);
                result = intent.getIntExtra(CoderlyticsConstants.RECORDER_INTENT_RESULT, -1);
                getValues();
                if (prefs.getBoolean(getString(R.string.preference_enable_target_app_key), false)) {
                    startAppBeforeRecording(prefs.getString(getString(R.string.preference_app_chooser_key), "none"));
                }
                if (isShakeGestureActive) {
                    mShakeDetector = new ShakeEventManager(this);
                    mShakeDetector.init(this);
                    Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                    Intent intent3 = new Intent(this, RecorderService.class);
                    intent3.setAction(str);
                    startNotificationForeGround(new Builder(this, CoderlyticsConstants.RECORDING_NOTIFICATION_CHANNEL_ID).setContentTitle("Waiting for device shake").setContentText("Shake your device to start recording or press this notification to cancel").setOngoing(true).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setContentIntent(PendingIntent.getService(this, 0, intent3, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)).build(), CoderlyticsConstants.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
                    Toast.makeText(this, R.string.screenrecording_waiting_for_gesture_toast, Toast.LENGTH_LONG).show();
                } else {

                    if (VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        startForeground(i,
                                createRecordingNotification().build(),
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                    }
                    // createNotificationChannel();
                    startRecording();

                }
            } else {
                Toast.makeText(this, R.string.screenrecording_already_active_toast, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onStartCommand(intent, i, i2);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopRecording() {
        ObserverUtils.getInstance().notifyObservers(new EvbStopService());
        boolean z = isBound;
        if (z) {
            if (z) {
                bubbleControlService.setRecordingState(RecordingState.STOPPED);
            }
            unbindService(serviceConnection);
            if (showCameraOverlay) {
                unbindService(floatingCameraConnection);
            }
            Log.d(CoderlyticsConstants.TAG, "Unbinding connection service");
        }
        stopScreenSharing();
        isRecording = false;
        isStart = false;
    }

    private void startAppBeforeRecording(String str) {
        if (!str.equals("none")) {
            startActivity(getPackageManager().getLaunchIntentForPackage(str));
        }
    }

    @TargetApi(24)
    private void pauseScreenRecording() {
        if (isRecording) {
            if (VERSION.SDK_INT < 24) {
                destroyMediaProjection();
            } else {
                mMediaRecorder.pause();
                isRecording = false;
            }
            elapsedTime += System.currentTimeMillis() - startTime;
            new Intent(this, RecorderService.class).setAction(CoderlyticsConstants.SCREEN_RECORDING_RESUME);
            updateNotification(createRecordingNotification().setUsesChronometer(false).build(), CoderlyticsConstants.SCREEN_RECORDER_NOTIFICATION_ID);
            Toast.makeText(this, R.string.screen_recording_paused_toast, Toast.LENGTH_SHORT).show();
            if (isBound) {
                bubbleControlService.setRecordingState(RecordingState.PAUSED);
            }
        }
    }

    @TargetApi(24)
    private void resumeScreenRecording() {
        if (!isRecording) {
            if (VERSION.SDK_INT < 24) {
                startRecording();
            } else {
                mMediaRecorder.resume();
            }
            isRecording = true;
            startTime = System.currentTimeMillis();
            new Intent(this, RecorderService.class).setAction(CoderlyticsConstants.SCREEN_RECORDING_PAUSE);
            Toast.makeText(this, R.string.screen_recording_resumed_toast, Toast.LENGTH_SHORT).show();
            if (isBound) {
                bubbleControlService.setRecordingState(RecordingState.RECORDING);
            }
        }
    }

    public DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    MediaProjectionManager mProjectionManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        //  startMyOwnForeground();
        mMediaRecorder = new MediaRecorder();

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        mDisplayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        initRecorder();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecording() {
        BubbleControlService.isRecording = true;
        mMediaRecorder = new MediaRecorder();

        initRecorder();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if (VERSION.SDK_INT >= 29)
        mMediaProjectionCallback = new MediaProjectionCallback();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(110, createNotificationChannel(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                }

                createNotificationChannel();
                mMediaProjection = ((MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE)).getMediaProjection(result, data);
                mMediaProjection.registerCallback(mMediaProjectionCallback, null);
                mVirtualDisplay = createVirtualDisplay();
                MediaProjectionManager projectionManager = (MediaProjectionManager)
                        getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            }
        }, 1000);
        try {
            mMediaRecorder.start();
            if (showCameraOverlay) {
                if (!isMyServiceRunning(SwimCameraViewService.class, this)) {
                    Intent intent = new Intent(this, SwimCameraViewService.class);
                    startService(intent);
                    bindService(intent, floatingCameraConnection, BIND_AUTO_CREATE);
                }
            }
            if (isBound) {
                bubbleControlService.setRecordingState(RecordingState.RECORDING);
            }
            isRecording = true;
            if (part == 0) {
                Toast.makeText(this, R.string.screen_recording_started_toast, Toast.LENGTH_SHORT).show();
                isStart = true;
                new TimeCount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        } catch (Exception unused) {
            Log.e(CoderlyticsConstants.TAG, "Mediarecorder reached Illegal state exception. Did you start the recording twice?");
            Toast.makeText(this, R.string.recording_failed_toast, Toast.LENGTH_SHORT).show();
            isRecording = false;
            // mMediaProjection.stop();
            stopSelf();
        }
        if (VERSION.SDK_INT >= 24) {
            startTime = System.currentTimeMillis();
            Intent intent2 = new Intent(this, RecorderService.class);
            intent2.setAction(CoderlyticsConstants.SCREEN_RECORDING_PAUSE);
            new Action(17301539, getString(R.string.screen_recording_notification_action_pause), PendingIntent.getService(this, 0, intent2, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
            startNotificationForeGround(createRecordingNotification().build(), CoderlyticsConstants.SCREEN_RECORDER_NOTIFICATION_ID);
            return;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private VirtualDisplay createVirtualDisplay() {
        int screenDensity = mDisplayMetrics.densityDpi;
        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;

        return mMediaProjection.createVirtualDisplay(this.getClass().getSimpleName(),
                width, height, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null /*Callbacks*/, null /*Handler*/);
    }

    public String getCurSysDate() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
    }

    private void initRecorder() {

        final String directory = Environment.getExternalStorageDirectory() + File.separator + "Recordings";
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show();
            return;
        }
        final File folder = new File(directory);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        String filePath;
        if (success) {
            String videoName = ("capture_" + getCurSysDate() + ".mp4");
            filePath = directory + File.separator + videoName;
        } else {
            Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show();
            return;
        }

        int width = mDisplayMetrics.widthPixels;
        int height = mDisplayMetrics.heightPixels;

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(width, height);
        mMediaRecorder.setOutputFile(filePath);
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public static boolean isMyServiceRunning(Class<?> cls, Context context) {
        String str;
        Iterator it = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE).iterator();
        do {
            str = "isMyServiceRunning?";
            if (it.hasNext()) {
            } else {
                Log.i(str, "false");
                return false;
            }
        } while (!cls.getName().equals(((ActivityManager.RunningServiceInfo) it.next()).service.getClassName()));
        Log.i(str, "true");
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private int getBestVideoEncoder() {
        if (getMediaCodecFor("video/hevc")) {
            if (VERSION.SDK_INT >= 24) {
                return 5;
            }
        } else if (getMediaCodecFor("video/avc")) {
            return 2;
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean getMediaCodecFor(String str) {
        String findEncoderForFormat = new MediaCodecList(0).findEncoderForFormat(MediaFormat.createVideoFormat(str, WIDTH, HEIGHT));
        if (findEncoderForFormat == null) {
            Log.d("Null Encoder: ", str);
            return false;
        }
        Log.d("Encoder", findEncoderForFormat);
        return !findEncoderForFormat.startsWith("OMX.google");
    }

    private long getFreeSpaceInBytes(String str) {
        long availableBytes = new StatFs(str).getAvailableBytes();
        StringBuilder sb = new StringBuilder();
        sb.append("Free space in GB: ");
        sb.append(availableBytes / 1000000000);
        Log.d(CoderlyticsConstants.TAG, sb.toString());
        return availableBytes;
    }

    @SuppressLint("RestrictedApi")
    @TargetApi(26)
    private void createNotificationChannels() {
        ArrayList arrayList = new ArrayList();
        NotificationChannel notificationChannel = new NotificationChannel(CoderlyticsConstants.RECORDING_NOTIFICATION_CHANNEL_ID, CoderlyticsConstants.RECORDING_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(SupportMenu.CATEGORY_MASK);
        notificationChannel.setShowBadge(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(1);
        arrayList.add(notificationChannel);
        NotificationChannel notificationChannel2 = new NotificationChannel(CoderlyticsConstants.SHARE_NOTIFICATION_CHANNEL_ID, CoderlyticsConstants.SHARE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel2.enableLights(true);
        notificationChannel2.setLightColor(SupportMenu.CATEGORY_MASK);
        notificationChannel2.setShowBadge(true);
        notificationChannel2.enableVibration(true);
        notificationChannel2.setLockscreenVisibility(1);
        arrayList.add(notificationChannel2);
        getManager().createNotificationChannels(arrayList);
    }

    private Builder createRecordingNotification() {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        new Intent(this, RecorderService.class).setAction(CoderlyticsConstants.SCREEN_RECORDING_STOP);
        return new Builder(this, CoderlyticsConstants.RECORDING_NOTIFICATION_CHANNEL_ID).setContentTitle(getResources().getString(R.string.screen_recording_notification_title)).setTicker(getResources().getString(R.string.screen_recording_notification_title)).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setUsesChronometer(true).setOngoing(true).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)).setPriority(3);
    }


    public void showShareNotification() {
        Bitmap decodeResource = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        new Intent(this, TrimmerActivity.class).putExtra(CoderlyticsConstants.VIDEO_EDIT_URI_KEY, SAVEPATH);
        updateNotification(new Builder(this, CoderlyticsConstants.SHARE_NOTIFICATION_CHANNEL_ID).setContentTitle(getString(R.string.share_intent_notification_title)).setContentText(getString(R.string.share_intent_notification_content)).setSmallIcon(R.drawable.ic_notification).setLargeIcon(Bitmap.createScaledBitmap(decodeResource, 128, 128, false)).setAutoCancel(true).setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).setAction(CoderlyticsConstants.SCREEN_RECORDER_VIDEOS_LIST_FRAGMENT_INTENT), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)).build(),
                CoderlyticsConstants.SCREEN_RECORDER_SHARE_NOTIFICATION_ID);
    }

    private void startNotificationForeGround(Notification notification, int i) {
        startForeground(i, notification);
    }

    private void updateNotification(Notification notification, int i) {
        getManager().notify(i, notification);
    }

    private NotificationManager getManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    public void onDestroy() {
        Log.d(CoderlyticsConstants.TAG, "Recorder service destroyed");
        isStart = false;
        super.onDestroy();
    }

    public void getValues() {
        setWidthHeight(getResolution());
        ArrayList<String> arrayList = arrPart;
        if (arrayList == null) {
            arrPart = new ArrayList<>();
        } else {
            arrayList.clear();
        }
        FPS = Integer.parseInt(prefs.getString(getString(R.string.fps_key), PrefUtils.VALUE_FRAMES));
        BITRATE = Integer.parseInt(prefs.getString(getString(R.string.bitrate_key), PrefUtils.VALUE_BITRATE));
        audioRecSource = prefs.getString(getString(R.string.audiorec_key), PrefUtils.VALUE_AUDIO);
        SharedPreferences sharedPreferences = prefs;
        String string = getString(R.string.savelocation_key);
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory());
        sb.append(File.separator);
        sb.append(CoderlyticsConstants.APPDIR);
        String string2 = sharedPreferences.getString(string, sb.toString());
        File file = new File(string2);
        if (Environment.getExternalStorageState().equals("mounted") && !file.isDirectory()) {
            file.mkdirs();
        }
        showCameraOverlay = prefs.getBoolean(getString(R.string.preference_camera_overlay_key), false);
        String fileSaveName = getFileSaveName();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(string2);
        sb2.append(File.separator);
        sb2.append(fileSaveName);
        sb2.append(".mp4");
        SAVEPATH = sb2.toString();
        isShakeGestureActive = prefs.getBoolean(getString(R.string.preference_shake_gesture_key), false);
    }

    private void setWidthHeight(String str) {
        char c;
        String[] split = str.split("x");
        SharedPreferences sharedPreferences = prefs;
        String string = getString(R.string.orientation_key);
        String str2 = PrefUtils.VALUE_ORIENTATION;
        String string2 = sharedPreferences.getString(string, str2);
        int hashCode = string2.hashCode();
        if (hashCode != 3005871) {
            if (hashCode != 729267099) {
                if (hashCode == 1430647483 && string2.equals("landscape")) {
                    c = 2;
                    if (c != 0) {
                        int i = screenOrientation;
                        if (i == 0 || i == 2) {
                            WIDTH = Integer.parseInt(split[0]);
                            HEIGHT = Integer.parseInt(split[1]);
                        } else {
                            HEIGHT = Integer.parseInt(split[0]);
                            WIDTH = Integer.parseInt(split[1]);
                        }
                    } else if (c == 1) {
                        WIDTH = Integer.parseInt(split[0]);
                        HEIGHT = Integer.parseInt(split[1]);
                    } else if (c == 2) {
                        HEIGHT = Integer.parseInt(split[0]);
                        WIDTH = Integer.parseInt(split[1]);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("Width: landscape");
                    sb.append(WIDTH);
                    sb.append(",Height:landscape");
                    sb.append(HEIGHT);
                    Log.d(CoderlyticsConstants.TAG, sb.toString());
                }
            } else if (string2.equals("portrait")) {
                WIDTH = Integer.parseInt(split[0]);
                HEIGHT = Integer.parseInt(split[1]);
                c = 1;
                if (c != 0) {

                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append(" Width portrait : ");
                sb2.append(WIDTH);
                sb2.append(",Height portrait :");
                sb2.append(HEIGHT);
                Log.d(CoderlyticsConstants.TAG, sb2.toString());
            }
        } else if (string2.equals(str2)) {
            c = 0;
            if (c != 0) {
            }
            WIDTH = Integer.parseInt(split[0]);
            HEIGHT = Integer.parseInt(split[1]);
            StringBuilder sb22 = new StringBuilder();
            sb22.append("Width: AUto");
            sb22.append(WIDTH);
            sb22.append(",Height:Auto");
            sb22.append(HEIGHT);
            Log.d(CoderlyticsConstants.TAG, sb22.toString());
        }
        c = 65535;
        if (c != 0) {
        }
        StringBuilder sb222 = new StringBuilder();
        sb222.append("Width: ");
        sb222.append(WIDTH);
        sb222.append(",Height:");
        sb222.append(HEIGHT);
        Log.d(CoderlyticsConstants.TAG, sb222.toString());
    }

    private String getResolution() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        window = (WindowManager) getSystemService(WINDOW_SERVICE);
        window.getDefaultDisplay().getRealMetrics(displayMetrics);
        DENSITY_DPI = displayMetrics.densityDpi;
        int parseInt = Integer.parseInt(prefs.getString(getString(R.string.res_key), Integer.toString(displayMetrics.widthPixels)));
        float aspectRatio = getAspectRatio(displayMetrics);
        int calculateClosestHeight = calculateClosestHeight(parseInt, aspectRatio);
        StringBuilder sb = new StringBuilder();
        sb.append(parseInt);
        sb.append("x");
        sb.append(calculateClosestHeight);
        String sb2 = sb.toString();
        StringBuilder sb3 = new StringBuilder();
        sb3.append("resolution service: [Width: ");
        sb3.append(parseInt);
        sb3.append(", Height: ");
        sb3.append(((float) parseInt) * aspectRatio);
        sb3.append(", aspect ratio: ");
        sb3.append(aspectRatio);
        sb3.append("]");
        Log.d(CoderlyticsConstants.TAG, sb3.toString());
        return sb2;
    }

    private int calculateClosestHeight(int i, float f) {
        int i2 = (int) (((float) i) * f);
        StringBuilder sb = new StringBuilder();
        sb.append("Calculated width=");
        sb.append(i2);
        String sb2 = sb.toString();
        String str = CoderlyticsConstants.TAG;
        Log.d(str, sb2);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("Aspect ratio: ");
        sb3.append(f);
        Log.d(str, sb3.toString());
        int i3 = i2 / 16;
        if (i3 == 0) {
            return i2;
        }
        StringBuilder sb4 = new StringBuilder();
        sb4.append(i2);
        sb4.append(" not divisible by 16");
        Log.d(str, sb4.toString());
        int i4 = i3 * 16;
        StringBuilder sb5 = new StringBuilder();
        sb5.append("Maximum possible height is ");
        sb5.append(i4);
        Log.d(str, sb5.toString());
        return i4;
    }

    private float getAspectRatio(DisplayMetrics displayMetrics) {
        float f = (float) displayMetrics.widthPixels;
        float f2 = (float) displayMetrics.heightPixels;
        return f > f2 ? f / f2 : f2 / f;
    }

    private String getFileSaveName() {
        String string = prefs.getString(getString(R.string.filename_key), PrefUtils.VALUE_NAME_FORMAT);
        String string2 = prefs.getString(getString(R.string.fileprefix_key), PrefUtils.VALUE_NAME_PREFIX);
        Date time2 = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(string);
        StringBuilder sb = new StringBuilder();
        sb.append(string2);
        sb.append("_");
        sb.append(simpleDateFormat.format(time2));
        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void destroyMediaProjection() {
        mMediaRecorder.stop();
        if (VERSION.SDK_INT >= 24) {
            indexFile();
        } else {
            part++;
        }
        mMediaRecorder.reset();
        mVirtualDisplay.release();
        mMediaRecorder.release();
        MediaProjection mediaProjection = mMediaProjection;
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mMediaProjectionCallback);
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        isRecording = false;
    }


    public void indexFile() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(SAVEPATH);
        MediaScannerConnection.scanFile(this, (String[]) arrayList.toArray(new String[arrayList.size()]), null, new OnScanCompletedListener() {
            public void onScanCompleted(String str, Uri uri) {
                mHandler.obtainMessage().sendToTarget();
                stopSelf();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void stopScreenSharing() {
        VirtualDisplay virtualDisplay = mVirtualDisplay;
        String str = CoderlyticsConstants.TAG;
        if (virtualDisplay == null) {
            Log.d(str, "Virtual display is null. Screen sharing already stopped");
            return;
        }
        if (VERSION.SDK_INT >= 24) {
            if (isRecording)
                destroyMediaProjection();
        } else {
            if (isRecording) {
                destroyMediaProjection();
            }
            mergeMediaFiles(arrPart, SAVEPATH);
        }
        Iterator it = arrPart.iterator();
        while (it.hasNext()) {
            Log.e(str, (String) it.next());
        }
    }

    public void mergeMediaFiles(ArrayList<String> arrayList, String str) {
        new MyTask(arrayList, str).execute(new Void[0]);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onShake() {
        if (!isRecording) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            getManager().cancel(CoderlyticsConstants.SCREEN_RECORDER_WAITING_FOR_SHAKE_NOTIFICATION_ID);
            if (VERSION.SDK_INT < 26) {
                vibrator.vibrate(500);
            } else {
                VibrationEffect.createOneShot(500, 255);
            }
            startRecording();
            return;
        }
        Intent intent = new Intent(this, RecorderService.class);
        intent.setAction(CoderlyticsConstants.SCREEN_RECORDING_STOP);
        startService(intent);
        mShakeDetector.stop();
    }

    private Notification createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class); //点击后跳转的界面，可以设置跳转数据

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)) // 设置PendingIntent
                //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
                .setContentText("is running......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }

        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);
        return notification;
    }

}
