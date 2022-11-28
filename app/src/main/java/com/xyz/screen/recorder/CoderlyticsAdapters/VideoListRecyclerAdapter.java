package com.xyz.screen.recorder.CoderlyticsAdapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore.Video.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.xyz.screen.recorder.BuildConfig;
import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;
import com.xyz.screen.recorder.adsMAnager.FbNativeAd;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.Video;

import com.xyz.screen.recorder.CoderlyticsFragments.VideosShowingFragment;
import com.xyz.screen.recorder.TrimmingVideos.TrimmerActivity;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;



public class VideoListRecyclerAdapter extends Adapter<ViewHolder> {
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_SECTION = 0;

    public Activity context;
    com.facebook.ads.InterstitialAd fbinterstitialAd;

    public int count = 0;

    public boolean isMultiSelect = false;

    public ActionMode mActionMode;
    FbNativeAd fbNativeAd_obj;
    private Callback mActionModeCallback = new Callback()
    {
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            videosShowingFragment.setEnableSwipe(false);
            actionMode.getMenuInflater().inflate(R.menu.video_list_action_menu, menu);
            return true;
        }
@Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            try {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.delete)
                {
                    //Toast.makeText(context, "onActionItemClicked", Toast.LENGTH_SHORT).show();
                    ArrayList arrayList = new ArrayList();
                    Iterator it = videos.iterator();
                    while (it.hasNext()) {
                        Video video = (Video) it.next();
                        if (video.isSelected()) {
                            arrayList.add(video);
                        }
                    }
                    if (!arrayList.isEmpty())
                    {
                        confirmDelete(arrayList);
                    }
                    mActionMode.finish();
                } else if (itemId == R.id.select_all) {
                    Iterator it2 = videos.iterator();
                    while (it2.hasNext()) {
                        ((Video) it2.next()).setSelected(true);
                    }
                    ActionMode access$300 = mActionMode;
                    StringBuilder sb = new StringBuilder();
                    sb.append("");
                    sb.append(videos.size());
                    access$300.setTitle((CharSequence) sb.toString());
                    notifyDataSetChanged();
                } else if (itemId == R.id.share) {
                    ArrayList arrayList2 = new ArrayList();
                    Iterator it3 = videos.iterator();
                    while (it3.hasNext()) {
                        Video video2 = (Video) it3.next();
                        if (video2.isSelected()) {
                            arrayList2.add(Integer.valueOf(videos.indexOf(video2)));
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        shareVideos(arrayList2);
                    }
                    mActionMode.finish();
                }
            } catch (Exception unused) {
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            Iterator it = videos.iterator();
            while (it.hasNext()) {
                ((Video) it.next()).setSelected(false);
            }
            isMultiSelect = false;
            videosShowingFragment.setEnableSwipe(true);
            notifyDataSetChanged();
        }
    };

    public ArrayList<Video> videos;

    public VideosShowingFragment videosShowingFragment;

    private final class ItemViewHolder extends ViewHolder
    {

        public ImageView iv_play,ivDeleteVideo,ivEditVideo;

        public ImageView iv_thumbnail;

        public ImageView overflow,ivShareVideo;

        public FrameLayout selectableFrame;

        public TextView tv_fileName;

        public RelativeLayout videoCard;

        ItemViewHolder(View view) {
            super(view);
            tv_fileName = (TextView) view.findViewById(R.id.fileName);
            iv_thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            ivShareVideo =  view.findViewById(R.id.share_video);
            ivDeleteVideo = (ImageView) view.findViewById(R.id.delete_video);
            ivEditVideo = (ImageView) view.findViewById(R.id.rename_video);
            iv_thumbnail.setScaleType(ScaleType.CENTER_CROP);
            videoCard = (RelativeLayout) view.findViewById(R.id.videoCard);
            overflow = (ImageButton) view.findViewById(R.id.ic_overflow);
            selectableFrame = (FrameLayout) view.findViewById(R.id.selectableFrame);
            iv_play = (ImageView) view.findViewById(R.id.play_iv);
        }
    }

    private final class SectionViewHolder extends ViewHolder
    {

        public TextView section;

        SectionViewHolder(View view)
        {
            super(view);
            fbNativeAd_obj.loadNativeAd(context,view);

        }
    }

    public VideoListRecyclerAdapter(Activity context2, ArrayList<Video> arrayList, VideosShowingFragment videosShowingFragment2) {
        videos = arrayList;
        context = context2;
        videosShowingFragment = videosShowingFragment2;
        fbNativeAd_obj=new FbNativeAd();

        fbinterstitialAd = new com.facebook.ads.InterstitialAd(context2, context2.getString(R.string.fb_inters_dlt));
    }

    public ArrayList<Video> getVideos()
    {
        return videos;
    }

    @Override
    public int getItemViewType(int i)
    {

        return isSection(i) ^ true ? 1 : 0;
    }

    public boolean isSection(int i)
    {
        return ((Video) videos.get(i)).isSection();
    }
@Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
{
        if (i != 0)
        {
            Log.i("iaminvl","onCreateViewHolder if int i  ="+i);

            return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video_withcions, viewGroup, false));
        }
    Log.i("iaminvl","onCreateViewHolder int i ="+i);

    return new SectionViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video_showadtest, viewGroup, false));
    }
@SuppressLint("ResourceType")
@Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i)
{
        final Video video = (Video) videos.get(i);
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType == 0)
        {


        } else if (itemViewType == 1)
        {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            itemViewHolder.tv_fileName.setText(video.getFileName());
            if (((Video) videos.get(i)).getThumbnail() != null)
            {
                itemViewHolder.iv_thumbnail.setImageBitmap(video.getThumbnail());
            }
            else
                {
                itemViewHolder.iv_thumbnail.setImageResource(0);
                Log.d(CoderlyticsConstants.TAG, "thumbnail error");
            }
            if (isMultiSelect)
            {
                itemViewHolder.iv_play.setVisibility(View.INVISIBLE);
            }
            else {
                itemViewHolder.iv_play.setVisibility(View.VISIBLE);
            }
            if (video.isSelected())
            {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(context, R.color.multiSelectColor)));
            } else {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(context, 17170445)));
            }

            itemViewHolder.ivDeleteVideo.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    fbinterstitialAd.loadAd();

                    confirmDelete(viewHolder.getAdapterPosition());

                }
            });
            itemViewHolder.ivEditVideo.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.i("iaminf","video_path = "+video.getFile().getAbsolutePath());
                    StringBuilder sb = new StringBuilder();
                    sb.append("Uri: ");
                    sb.append(Uri.fromFile(video.getFile()));
                    Log.d(CoderlyticsConstants.TAG, sb.toString());

                    Intent intent = new Intent(context, TrimmerActivity.class);

                    intent.putExtra(CoderlyticsConstants.VIDEO_EDIT_URI_KEY, Uri.fromFile(video.getFile()).toString());

                    videosShowingFragment.startActivityForResult(intent, CoderlyticsConstants.VIDEO_EDIT_REQUEST_CODE);

                }
            });
            itemViewHolder.ivShareVideo.setOnClickListener(new OnClickListener() {

                public void onClick(View view) {

                    shareVideo(itemViewHolder.getAdapterPosition());

                }
            });


            itemViewHolder.videoCard.setOnClickListener(new OnClickListener()
            {
                public void onClick(View view) {
                    Log.d("Videos List", "onClick = ");

                    if (isMultiSelect) {
                        if (video.isSelected()) {
                            count = count - 1;
                        } else {
                            count = count + 1;
                        }
                        video.setSelected(true ^ video.isSelected());
                        notifyDataSetChanged();

                        StringBuilder sb = new StringBuilder();
                        sb.append("");
                        sb.append(count);
                        mActionMode.setTitle((CharSequence) sb.toString());
                        if (count == 0) {
                            setMultiSelect(false);
                        }
                        return;
                    }
                    try {
                        File file = video.getFile();
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("video position clicked: ");
                        sb2.append(itemViewHolder.getAdapterPosition());
                        Log.d("Videos List", sb2.toString());
                        Context access$1100 = context;
                        StringBuilder sb3 = new StringBuilder();
                        sb3.append(BuildConfig.APPLICATION_ID);
                        sb3.append(".provider");
                        Uri uriForFile = FileProvider.getUriForFile(access$1100, sb3.toString(), file);
                        Log.d(CoderlyticsConstants.TAG, uriForFile.toString());
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).setDataAndType(uriForFile, context.getContentResolver().getType(uriForFile));
                        context.startActivity(intent);
                    } catch (Exception unused)
                    {
                        Log.d("Videos List", "unused error = "+unused.toString());

                    }
                }
            });
            itemViewHolder.videoCard.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View view) {
                    if (!isMultiSelect) {
                        setMultiSelect(true);
                        video.setSelected(true);
                        count = count + 1;

                        StringBuilder sb = new StringBuilder();
                        sb.append("");
                        sb.append(count);
                        mActionMode.setTitle((CharSequence) sb.toString());
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });
        }
    }

    
    public void setMultiSelect(boolean z)
    {
        if (z) {
            isMultiSelect = true;
            count = 0;
            mActionMode = ((AppCompatActivity) videosShowingFragment.getActivity()).
                    startSupportActionMode(mActionModeCallback);
            return;
        }
        isMultiSelect = false;
        mActionMode.finish();
    }

    
    public void shareVideo(int i) {
        try {
            Context context2 = context;
            StringBuilder sb = new StringBuilder();
            sb.append(BuildConfig.APPLICATION_ID);
            sb.append(".provider");
            context.startActivity(Intent.createChooser(new Intent().setAction("android.intent.action.SEND").setType("video/*").setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .putExtra("android.intent.extra.STREAM",
                            FileProvider.getUriForFile(context2, sb.toString(), ((Video) videos.get(i)).getFile())), context.getString(R.string.share_intent_notification_title)));
        } catch (Exception unused) {
        }
    }

    
    public void shareVideos(ArrayList<Integer> arrayList) {
        try {
            ArrayList arrayList2 = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                int intValue = ((Integer) it.next()).intValue();
                Context context2 = context;
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.APPLICATION_ID);
                sb.append(".provider");
                arrayList2.add(FileProvider.getUriForFile(context2, sb.toString(), ((Video) videos.get(intValue)).getFile()));
            }
            context.startActivity(Intent.createChooser(new Intent().setAction("android.intent.action.SEND_MULTIPLE").setType("video/*").setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList2), context.getString(R.string.share_intent_notification_title)));
        } catch (Exception unused) {
        }
    }

    
    public void deleteVideo(int i) {
        String str = "Videos List";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("delete position clicked: ");
            sb.append(i);
            Log.d(str, sb.toString());
            File file = new File(((Video) videos.get(i)).getFile().getPath());
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Media.EXTERNAL_CONTENT_URI;
            StringBuilder sb2 = new StringBuilder();
            sb2.append("_id =");
            sb2.append(((Video) videos.get(i)).getIdVideo());
            contentResolver.delete(uri, sb2.toString(), null);
            file.delete();
            Toast.makeText(context, context.getString(R.string.file_delete_successfuly), Toast.LENGTH_SHORT).show();
            videos.remove(i);
            notifyItemRemoved(i);
            notifyItemRangeChanged(i, videos.size());
if (videosShowingFragment!=null)
    videosShowingFragment.refreshingList();
        } catch (Exception unused) {
        }
    }

    
    public void deleteVideos(ArrayList<Video> arrayList)
    {
        try {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Video video = (Video) it.next();
                if (!video.isSection() && video.getFile().delete())
                {
                    notifyItemRemoved(videos.indexOf(video));
                    videos.remove(video);
                }
            }
            notifyDataSetChanged();
            if (videosShowingFragment!=null)
                videosShowingFragment.refreshingList();
        } catch (Exception unused) {
        }
    }

    
    public void confirmDelete(final int f)
    {


        try {
            new Builder(context).setTitle((CharSequence) context.getResources().
                    getQuantityString(R.plurals.delete_alert_title, 1))
                    .setMessage((CharSequence)
                    context.getResources().getQuantityString(R.plurals.delete_alert_message, 1))
                    .setCancelable(false).setPositiveButton(android.R.string.yes, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    deleteVideo(f);
                    if (fbinterstitialAd.isAdLoaded())
                        fbinterstitialAd.show();
                }
            }).setNegativeButton(android.R.string.no,
                    (DialogInterface.OnClickListener) new DialogInterface.OnClickListener()
                    {
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    if (fbinterstitialAd.isAdLoaded())
                        fbinterstitialAd.show();
                }
            }).show();
        } catch (Exception unused) {
        }

    }

    
    public void confirmDelete(final ArrayList<Video> arrayList)
    {

        try {
            int size = arrayList.size();
            new Builder(context).setTitle((CharSequence) context.getResources().getQuantityString(R.plurals.delete_alert_title, size))
                    .setMessage((CharSequence) context.getResources().getQuantityString(R.plurals.delete_alert_message, size, new Object[]{Integer.valueOf(size)})).setCancelable(false)
                    .setPositiveButton(android.R.string.yes, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    deleteVideos(arrayList);
                }
            }).setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception unused) {
        }

    }

    private String generateSectionTitle(Date date) {
        Calendar calendar = Utils.toCalendar(new Date().getTime());
        Calendar calendar2 = Utils.toCalendar(date.getTime());
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

    public int getItemCount() {
        return videos.size();
    }
}
