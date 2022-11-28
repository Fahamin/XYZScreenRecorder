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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsActivities.MainActivity;
import com.xyz.screen.recorder.CoderlyticsAdapters.PicScreenShortRecyclerAdapter;
import com.xyz.screen.recorder.CoderlyticsAdapters.SpacesItemDecoration;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.HandleCache;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.lisInterface.PermissionResultListener;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.Photo;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

public class DisplayScreenshotsFragment extends BaseFragment implements PermissionResultListener, OnRefreshListener {
    
    public PicScreenShortRecyclerAdapter mAdapter;
    BroadcastReceiver mReceiverUpdate = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (CoderlyticsConstants.UPDATE_UI_IMAGE.equals(intent.getAction())) {
                onRefresh();
            }
        }
    };
    
    public TextView message;
    
    public RecyclerView photoRV;
    
    public ArrayList<Photo> photosList = new ArrayList<>();
    private SharedPreferences prefs;
    
    public SwipeRefreshLayout swipeRefreshLayout;

    class GetPhotosAsync extends AsyncTask<File[], Integer, ArrayList<Photo>> {
        File[] files;
        ContentResolver resolver;

        GetPhotosAsync() {
            this.resolver = getActivity().getApplicationContext().getContentResolver();
        }

        
        public void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        
        public void onPostExecute(ArrayList<Photo> arrayList) {
            if (arrayList.isEmpty()) {
                photoRV.setVisibility(View.GONE);
                message.setVisibility(View.VISIBLE);
            } else {
                Collections.sort(arrayList, Collections.reverseOrder());
                setRecyclerView(addSections(arrayList));
                photoRV.setVisibility(View.VISIBLE);
                message.setVisibility(View.GONE);
            }
            swipeRefreshLayout.setRefreshing(false);
        }

        private ArrayList<Photo> addSections(ArrayList<Photo> arrayList) {
            ArrayList<Photo> arrayList2 = new ArrayList<>();
            Date date = new Date();
            for (int i = 0; i < arrayList.size(); i++) {
                Photo photo = (Photo) arrayList.get(i);
                if (i == 0) {
                    arrayList2.add(new Photo(true, photo.getLastModified()));
                    arrayList2.add(photo);
                    date = photo.getLastModified();
                } else {
                    if (addNewSection(date, photo.getLastModified())) {
                        arrayList2.add(new Photo(true, photo.getLastModified()));
                        date = photo.getLastModified();
                    }
                    arrayList2.add(photo);
                }
            }
            return arrayList2;
        }

        private boolean addNewSection(Date date, Date date2) {
            return ((int) Math.abs((Utils.toCalendar(date2.getTime()).getTimeInMillis() - Utils.toCalendar(date.getTime()).getTimeInMillis()) / 86400000)) > 0;
        }

        
        public void onProgressUpdate(Integer... numArr) {
            super.onProgressUpdate(numArr);
            StringBuilder sb = new StringBuilder();
            sb.append("Progress is :");
            sb.append(numArr[0]);
            Log.d(CoderlyticsConstants.TAG, sb.toString());
        }

        
        public ArrayList<Photo> doInBackground(File[]... fileArr) {
            this.files = fileArr[0];
            int i = 0;
            while (true) {
                File[] fileArr2 = this.files;
                if (i >= fileArr2.length) {
                    return photosList;
                }
                File file = fileArr2[i];
                if (!file.isDirectory() && DisplayScreenshotsFragment.isPhotoFile(file.getPath())) {
                    photosList.add(new Photo(file.getName(), file, getBitmap(file), new Date(file.lastModified())));
                    publishProgress(new Integer[]{Integer.valueOf(i)});
                }
                i++;
            }
        }

        
        public Bitmap getBitmap(File file) {
            String str = "_id";
            String[] strArr = {str, "bucket_id", "bucket_display_name", "_data"};
            Cursor query = this.resolver.query(Media.getContentUri("external"), strArr, "_data=? ", new String[]{file.getPath()}, null);
            if (query == null || !query.moveToNext()) {
                return null;
            }
            Bitmap thumbnail = Thumbnails.getThumbnail(this.resolver, (long) query.getInt(query.getColumnIndexOrThrow(str)), 1, null);
            query.close();
            return thumbnail;
        }
    }

    private void initEvents() {
    }

    public static DisplayScreenshotsFragment newInstance() {
        return new DisplayScreenshotsFragment();
    }

    
    public static boolean isPhotoFile(String str) {
        String guessContentTypeFromName = URLConnection.guessContentTypeFromName(str);
        return guessContentTypeFromName != null && guessContentTypeFromName.startsWith("image");
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        this.mRootView = layoutInflater.inflate(R.layout.content_images_fragment, viewGroup, false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CoderlyticsConstants.UPDATE_UI_IMAGE);
        getActivity().registerReceiver(this.mReceiverUpdate, intentFilter);
        initViews();
        initEvents();
        return this.mRootView;
    }

    public void setEnableSwipe(boolean z) {
        this.swipeRefreshLayout.setEnabled(z);
    }

    public void onVisibleFragment() {
        super.onVisibleFragment();
        setRecyclerView(HandleCache.getInstance().getArrPhotos());
        if (getActivity() != null) {
            this.photosList.clear();
            checkPermission();
        }
    }

    @SuppressLint("ResourceType")
    private void initViews() {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.message = (TextView) this.mRootView.findViewById(R.id.message_tv);
        this.photoRV = (RecyclerView) this.mRootView.findViewById(R.id.videos_rv);
        this.photoRV.addItemDecoration(new SpacesItemDecoration(Utils.convertDpToPixel(10.0f, getActivity())));
        this.swipeRefreshLayout = (SwipeRefreshLayout) this.mRootView.findViewById(R.id.swipeRefresh);
        this.swipeRefreshLayout.setOnRefreshListener(this);
        this.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, 17170453, 17170457, 17170451);
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
                photosList.clear();
                checkPermission();
                return false;
            }
        });
    }

    
    public void checkPermission()
    {
        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setPermissionResultListener(this);

//                ((MainActivity) getActivity()).requestPermissionStorage();

            }
        } else if (this.photosList.isEmpty()) {
            SharedPreferences sharedPreferences = this.prefs;
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
                arrayList.addAll(Arrays.asList(getPhotos(file.listFiles())));
            }
            new GetPhotosAsync().execute(new File[][]{(File[]) arrayList.toArray(new File[arrayList.size()])});
        }
    }

    private File[] getPhotos(File[] fileArr) {
        ArrayList arrayList = new ArrayList();
        for (File file : fileArr) {
            if (!file.isDirectory() && isPhotoFile(file.getPath())) {
                arrayList.add(file);
            }
        }
        return (File[]) arrayList.toArray(new File[arrayList.size()]);
    }

    
    public void setRecyclerView(ArrayList<Photo> arrayList) {
        if (!arrayList.isEmpty() && this.message.getVisibility() != View.GONE) {
            this.message.setVisibility(View.GONE);
        }
        this.photoRV.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        this.photoRV.setLayoutManager(gridLayoutManager);
        this.mAdapter = new PicScreenShortRecyclerAdapter(getActivity(), arrayList, this);
        this.photoRV.setAdapter(this.mAdapter);
        gridLayoutManager.setSpanSizeLookup(new SpanSizeLookup() {
            public int getSpanSize(int i) {
                if (mAdapter.isSection(i)) {
                    return gridLayoutManager.getSpanCount();
                }
                return 1;
            }
        });
    }

    public void onDetach() {
        super.onDetach();
        if (this.mAdapter != null) {
            HandleCache.getInstance().setArrPhotos(this.mAdapter.getPhotos());
        }
    }

    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        if (this.mAdapter != null) {
            HandleCache.getInstance().setArrPhotos(this.mAdapter.getPhotos());
        }
    }

    public void onPermissionResult(int i, String[] strArr, int[] iArr) {
        if (i == 1110) {
            int length = iArr.length;
            String str = CoderlyticsConstants.TAG;
            if (length <= 0 || iArr[0] != 0) {
                Log.d(str, "Storage permission denied.");
                this.photoRV.setVisibility(View.GONE);
                this.message.setText(R.string.video_list_permission_denied_message);
                return;
            }
            Log.d(str, "Storage permission granted.");
            checkPermission();
        }
    }

    public void removePhotosList() {
        this.photosList.clear();
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        removePhotosList();
        checkPermission();
    }

    public void onRefresh() {
        this.photosList.clear();
        checkPermission();
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mReceiverUpdate);
        super.onDestroy();
    }
}
