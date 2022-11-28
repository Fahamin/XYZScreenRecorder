package com.xyz.screen.recorder.CoderlyticsFragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Video.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsActivities.MainActivity;
import com.xyz.screen.recorder.CoderlyticsAdapters.VideoListRecyclerAdapter;

import com.xyz.screen.recorder.CoderlyticsAdapters.SpacesItemDecoration;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.HandleCache;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.PermissionResultListener;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.Video;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class VideosShowingFragment extends BaseFragment implements PermissionResultListener, OnRefreshListener {
    public VideoListRecyclerAdapter mAdapter;
    public TextView message;
    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView videoRV;
    public ArrayList<Video> videosList = new ArrayList<>();
    String[] PERMISSIONS = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
    private View btnFloatButton;
    private ImageView imRecord;
    private View loRecord;
    private TextView tvTimeRecord;
    private SharedPreferences prefs;
    BroadcastReceiver mReceiverUpdate = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (CoderlyticsConstants.UPDATE_UI.equals(intent.getAction())) {
                onRefresh();
            }
        }
    };

    public static VideosShowingFragment newInstance() {
        return new VideosShowingFragment();
    }

    public static boolean isVideoFile(String str) {
        String guessContentTypeFromName = URLConnection.guessContentTypeFromName(str);
        return guessContentTypeFromName != null && guessContentTypeFromName.startsWith("video");
    }

    public static boolean hasPermissions(Context context, String... strArr) {
        if (!(context == null || strArr == null)) {
            for (String checkSelfPermission : strArr) {
                if (ActivityCompat.checkSelfPermission(context, checkSelfPermission) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressLint("ResourceType")
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.fragment_videos, viewGroup, false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoderlyticsConstants.UPDATE_UI);
        getActivity().registerReceiver(mReceiverUpdate, intentFilter);

        message = (TextView) inflate.findViewById(R.id.message_tv);
        videoRV = (RecyclerView) inflate.findViewById(R.id.videos_rv);
        videoRV.setNestedScrollingEnabled(false);

        btnFloatButton = inflate.findViewById(R.id.btn_floatbutton);
        loRecord = inflate.findViewById(R.id.lo_record);
        tvTimeRecord = (TextView) inflate.findViewById(R.id.tv_time_record);
        imRecord = (ImageView) inflate.findViewById(R.id.im_record);

        videoRV.addItemDecoration(new SpacesItemDecoration(Utils.convertDpToPixel(10.0f, getActivity())));
        swipeRefreshLayout = (SwipeRefreshLayout) inflate.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, 17170453, 17170457, 17170451);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        return inflate;
    }
    @RequiresApi(api = 23)
    public void onResume() {

        super.onResume();
    }

    public ArrayList<Video> getVideosList() {
        return videosList;
    }

    public void onStart() {
        super.onStart();
    }

    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void onVisibleFragment() {
        super.onVisibleFragment();
        setRecyclerView(HandleCache.getInstance().getArrVideos());
        if (getActivity() != null) {
            videosList.clear();
            checkPermission();
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void onDetach() {
        super.onDetach();
        if (mAdapter != null) {
            HandleCache.getInstance().setArrVideos(mAdapter.getVideos());
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiverUpdate);
        super.onDestroy();
    }

    public void onStop() {
        super.onStop();
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        MenuItem add = menu.add("Refresh");
        add.setIcon(R.drawable.ic_refresh_white_24dp);
        add.setShowAsActionFlags(2);
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (swipeRefreshLayout.isRefreshing()) {
                    return false;
                }
                videosList.clear();
                checkPermission();
                Log.d(CoderlyticsConstants.TAG, "Refreshing");
                return false;
            }
        });
    }

    public boolean refreshingList() {

        if (swipeRefreshLayout.isRefreshing()) {
            return false;
        }
        videosList.clear();
        checkPermission();
        Log.d(CoderlyticsConstants.TAG, "Refreshing");
        return false;
    }

    public void checkPermission() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {

                if (videosList.isEmpty()) {
                    SharedPreferences sharedPreferences = prefs;
                    String string = getString(R.string.savelocation_key);
                    StringBuilder sb = new StringBuilder();
                    sb.append(Environment.getExternalStorageDirectory());
                    sb.append(File.separator);
                    sb.append(CoderlyticsConstants.APPDIR);
                    File file = new File(sharedPreferences.getString(string, sb.toString()));
                    if (!file.exists()) {
                        Utils.createDir();
                        Log.d(CoderlyticsConstants.TAG, "Directory missing! Creating dir");
                    }
                    ArrayList arrayList = new ArrayList();
                    if (file.isDirectory() && file.exists()) {
                        arrayList.addAll(Arrays.asList(getVideos(file.listFiles())));
                    }
                    new GetVideosAsync().execute(new File[][]{(File[]) arrayList.toArray(new File[arrayList.size()])});
                }
            } else if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setPermissionResultListener(this);
                ((MainActivity) getActivity()).requestPermissionStorage();
                boolean z = getActivity() instanceof MainActivity;
            }
        } catch (Exception unused) {
        }
    }

    private File[] getVideos(File[] fileArr) {
        try {
            ArrayList arrayList = new ArrayList();
            for (File file : fileArr) {
                if (!file.isDirectory() && isVideoFile(file.getPath())) {
                    arrayList.add(file);
                }
            }
            return (File[]) arrayList.toArray(new File[arrayList.size()]);
        } catch (Exception unused) {
            return null;
        }
    }

    public void setRecyclerView(ArrayList<Video> arrayList) {
        try {
            if (!arrayList.isEmpty() && message.getVisibility() != View.GONE) {
                message.setVisibility(View.GONE);
            }
            videoRV.setHasFixedSize(true);
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);

            videoRV.setLayoutManager(gridLayoutManager);

            mAdapter = new VideoListRecyclerAdapter(getActivity(), arrayList, this);
            videoRV.setAdapter(mAdapter);
            gridLayoutManager.setSpanSizeLookup(new SpanSizeLookup() {
                public int getSpanSize(int i) {
                    try {
                        if (mAdapter.isSection(i)) {
                            return gridLayoutManager.getSpanCount();
                        }

                        return 1;
                    } catch (Exception unused) {
                        return 1;
                    }
                }
            });
        } catch (Exception unused) {
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (mAdapter != null) {
            HandleCache.getInstance().setArrVideos(mAdapter.getVideos());
        }
    }

    public void onPermissionResult(int i, String[] strArr, int[] iArr) {
        if (i == 1110) {
            int length = iArr.length;
            String str = CoderlyticsConstants.TAG;
            if (length <= 0 || iArr[0] != 0) {
                Log.d(str, "Storage permission denied.");
                videoRV.setVisibility(View.GONE);
                message.setText(R.string.video_list_permission_denied_message);
                return;
            }
            Log.d(str, "Storage permission granted.");
            checkPermission();
        }
    }

    public void removeVideosList() {
        videosList.clear();
        Log.d(CoderlyticsConstants.TAG, "Reached video fragment");
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        removeVideosList();
        checkPermission();
    }

    public void onRefresh() {
        videosList.clear();
        checkPermission();
    }

    public void setEnableSwipe(boolean z) {
        swipeRefreshLayout.setEnabled(z);
    }

    class GetVideosAsync extends AsyncTask<File[], Integer, ArrayList<Video>> {
        File[] files;
        ContentResolver resolver;

        GetVideosAsync() {
            resolver = getActivity().getApplicationContext().getContentResolver();
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }


        @Override
        public ArrayList<Video> doInBackground(File[]... fileArr) {

            files = fileArr[0];
            Log.i("iaminf", "filedata fileArr[0] = " + fileArr[0]);

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                long j = getidVideo(file);


                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                Log.i("iaminf", " filedata = " + file.getName());

                if (!file.isDirectory() && VideosShowingFragment.isVideoFile(file.getPath()) && !file.getName().endsWith("_.mp4")) {

                    Video video = new Video(j, file.getName(), file, thumbnail, new Date(file.lastModified()));
                    videosList.add(video);
                    Log.i("iaminf", " filedata = !file.isDirectory()" + file.getName());

                }
            }
            return videosList;
        }

        @Override
        public void onPostExecute(ArrayList<Video> arrayList) {
            try {
                Log.i("iaminf", "filedata onPostExecute[0] = " + arrayList);

                if (arrayList.isEmpty()) {
                    videoRV.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                } else {
                    Collections.sort(arrayList, Collections.reverseOrder());
                    setRecyclerView(addSections(arrayList));
                    videoRV.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);
                }
                swipeRefreshLayout.setRefreshing(false);
            } catch (Exception unused) {
            }
        }


        @Override
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            StringBuilder sb = new StringBuilder();
            sb.append("Progress is :");
            sb.append(numArr[0]);
            Log.d(CoderlyticsConstants.TAG, sb.toString());
        }

        private ArrayList<Video> addSections(ArrayList<Video> arrayList) {
            String str = CoderlyticsConstants.TAG;
            try {
                ArrayList<Video> arrayList2 = new ArrayList<>();
                Date date = new Date();
                StringBuilder sb = new StringBuilder();
                sb.append("Original Length: ");
                sb.append(arrayList.size());
                Log.d(str, sb.toString());
                for (int i = 0; i < arrayList.size(); i++) {
                    Video video = (Video) arrayList.get(i);

                    int s = i + 1;
                    Log.i("iaminvl", " s= " + s);
                    if (i != 0 && i % 4 == 0) {

                        arrayList2.add(new Video(true, video.getLastModified()));

                    }
                    arrayList2.add(video);

                }
                StringBuilder sb2 = new StringBuilder();
                sb2.append("Length with sections: ");
                sb2.append(arrayList2.size());
                Log.d(str, sb2.toString());
                return arrayList2;
            } catch (Exception unused) {
                return new ArrayList<>();
            }
        }

        private boolean addNewSection(Date date, Date date2) {
            Calendar calendar = toCalendar(date.getTime());
            int abs = (int) Math.abs((toCalendar(date2.getTime()).getTimeInMillis() - calendar.getTimeInMillis()) / 86400000);
            StringBuilder sb = new StringBuilder();
            sb.append("Date diff is: ");
            sb.append(abs);
            Log.d(CoderlyticsConstants.TAG, sb.toString());
            return abs > 0;
        }

        private Calendar toCalendar(long j) {
            Calendar instance = Calendar.getInstance();
            instance.setTimeInMillis(j);
            instance.set(Calendar.HOUR_OF_DAY, 0);
            instance.set(Calendar.MINUTE, 0);
            instance.set(Calendar.SECOND, 0);
            instance.set(Calendar.MILLISECOND, 0);
            return instance;
        }

        private long getidVideo(File file) {
            String str = "_id";
            String[] strArr = {str, "bucket_id", "bucket_display_name", "_data"};
            Cursor query = resolver.query(Media.getContentUri("external"), strArr, "_data=? ", new String[]{file.getPath()}, null);
            if (query == null || !query.moveToNext()) {
                return -1;
            }
            int i = query.getInt(query.getColumnIndexOrThrow(str));
            query.close();
            return (long) i;
        }
    }
}
