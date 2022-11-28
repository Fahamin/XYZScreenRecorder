package com.xyz.screen.recorder.CoderlyticsAdapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.Utils;
import com.xyz.screen.recorder.CoderlyticsMindWork.modelLisnr.Photo;
import com.xyz.screen.recorder.CoderlyticsFragments.DisplayScreenshotsFragment;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class PicScreenShortRecyclerAdapter extends Adapter<ViewHolder> {
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_SECTION = 0;
    
    public Context context;
    
    public int count = 0;
    
    public boolean isMultiSelect = false;
    
    public ActionMode mActionMode;
    private Callback mActionModeCallback = new Callback() {
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            photosListFragment.setEnableSwipe(false);
            actionMode.getMenuInflater().inflate(R.menu.video_list_action_menu, menu);
            return true;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            try {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.delete) {
                    ArrayList arrayList = new ArrayList();
                    Iterator it = photos.iterator();
                    while (it.hasNext()) {
                        Photo photo = (Photo) it.next();
                        if (photo.isSelected()) {
                            arrayList.add(photo);
                        }
                    }
                    if (!arrayList.isEmpty()) {
                        confirmDelete(arrayList);
                    }
                    mActionMode.finish();
                } else if (itemId == R.id.select_all) {
                    Iterator it2 = photos.iterator();
                    while (it2.hasNext()) {
                        ((Photo) it2.next()).setSelected(true);
                    }
                    ActionMode access$300 = mActionMode;
                    StringBuilder sb = new StringBuilder();
                    sb.append("");
                    sb.append(photos.size());
                    access$300.setTitle((CharSequence) sb.toString());
                    notifyDataSetChanged();
                } else if (itemId == R.id.share) {
                    ArrayList arrayList2 = new ArrayList();
                    Iterator it3 = photos.iterator();
                    while (it3.hasNext()) {
                        Photo photo2 = (Photo) it3.next();
                        if (photo2.isSelected()) {
                            arrayList2.add(Integer.valueOf(photos.indexOf(photo2)));
                        }
                    }
                    if (!arrayList2.isEmpty()) {
                        sharePhotos(arrayList2);
                    }
                    mActionMode.finish();
                }
            } catch (Exception unused) {
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            try {
                Iterator it = photos.iterator();
                while (it.hasNext()) {
                    ((Photo) it.next()).setSelected(false);
                }
                isMultiSelect = false;
                photosListFragment.setEnableSwipe(true);
                notifyDataSetChanged();
            } catch (Exception unused) {
            }
        }
    };
    
    public ArrayList<Photo> photos;
    
    public DisplayScreenshotsFragment photosListFragment;

    private final class ItemViewHolder extends ViewHolder {
        
        public ImageView iv_thumbnail;
        
        public RelativeLayout photoCard;
        
        public FrameLayout selectableFrame;

        ItemViewHolder(View view) {
            super(view);
            this.iv_thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            this.iv_thumbnail.setScaleType(ScaleType.CENTER_CROP);
            this.photoCard = (RelativeLayout) view.findViewById(R.id.videoCard);
            this.selectableFrame = (FrameLayout) view.findViewById(R.id.selectableFrame);
        }
    }

    private final class SectionViewHolder extends ViewHolder {
        
        public TextView section;

        SectionViewHolder(View view) {
            super(view);
            this.section = (TextView) view.findViewById(R.id.sectionID);
        }
    }

    public PicScreenShortRecyclerAdapter(Context context2, ArrayList<Photo> arrayList, DisplayScreenshotsFragment displayScreenshotsFragment) {
        this.photos = arrayList;
        this.context = context2;
        this.photosListFragment = displayScreenshotsFragment;
    }

    public ArrayList<Photo> getPhotos() {
        return this.photos;
    }

    public int getItemViewType(int i) {
        return isSection(i) ^ true ? 1 : 0;
    }

    public boolean isSection(int i) {
        return ((Photo) this.photos.get(i)).isSection();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 0) {
            return new SectionViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_video_section, viewGroup, false));
        }
        if (i != 1) {
            return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_photo, viewGroup, false));
        }
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_photo, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        final Photo photo = (Photo) this.photos.get(i);
        int itemViewType = viewHolder.getItemViewType();
        if (itemViewType == 0) {
            ((SectionViewHolder) viewHolder).section.setText(Utils.generateSectionTitle(photo.getLastModified()));
        } else if (itemViewType == 1) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
            if (((Photo) this.photos.get(i)).getThumbnail() != null) {
                itemViewHolder.iv_thumbnail.setImageBitmap(photo.getThumbnail());
            } else {
                itemViewHolder.iv_thumbnail.setImageResource(0);
            }
            if (photo.isSelected()) {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(this.context, R.color.multiSelectColor)));
            } else {
                itemViewHolder.selectableFrame.setForeground(new ColorDrawable(ContextCompat.getColor(this.context, 17170445)));
            }
            itemViewHolder.photoCard.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (isMultiSelect) {
                        if (photo.isSelected()) {
                            count = count - 1;
                        } else {
                            count = count + 1;
                        }

                        photo.setSelected(true ^ photo.isSelected());
                        notifyDataSetChanged();
                        ActionMode access$300 = mActionMode;
                        StringBuilder sb = new StringBuilder();
                        sb.append("");
                        sb.append(count);
                        access$300.setTitle((CharSequence) sb.toString());
                        if (count == 0) {
                            setMultiSelect(false);
                        }
                        return;
                    }
                    try {
                        File file = photo.getFile();
                        Context access$1000 = context;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(BuildConfig.APPLICATION_ID);
                        sb2.append(".provider");
                        Uri uriForFile = FileProvider.getUriForFile(access$1000, sb2.toString(), file);
                        Log.d(CoderlyticsConstants.TAG, uriForFile.toString());
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW").addFlags(1).setDataAndType(uriForFile, context.getContentResolver().getType(uriForFile));
                        context.startActivity(intent);
                    } catch (Exception unused) {
                    }
                }
            });
            itemViewHolder.photoCard.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View view) {
                    if (!isMultiSelect) {
                        setMultiSelect(true);
                        photo.setSelected(true);
                        count = count + 1;
                        ActionMode access$300 = mActionMode;
                        StringBuilder sb = new StringBuilder();
                        sb.append("");
                        sb.append(count);
                        access$300.setTitle((CharSequence) sb.toString());
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });
        }
    }

    
    public void setMultiSelect(boolean z) {
        if (z) {
            this.isMultiSelect = true;
            this.count = 0;
            this.mActionMode = ((AppCompatActivity) this.photosListFragment.getActivity()).startSupportActionMode(this.mActionModeCallback);
            return;
        }
        this.isMultiSelect = false;
        this.mActionMode.finish();
    }

    
    public void sharePhotos(ArrayList<Integer> arrayList) {
        try {
            ArrayList arrayList2 = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                int intValue = ((Integer) it.next()).intValue();
                Context context2 = this.context;
                StringBuilder sb = new StringBuilder();
                sb.append(BuildConfig.APPLICATION_ID);
                sb.append(".provider");
                arrayList2.add(FileProvider.getUriForFile(context2, sb.toString(), ((Photo) this.photos.get(intValue)).getFile()));
            }
            this.context.startActivity(Intent.createChooser(new Intent().setAction("android.intent.action.SEND_MULTIPLE").setType("photo/*").setFlags(1).putParcelableArrayListExtra("android.intent.extra.STREAM", arrayList2), this.context.getString(R.string.share_intent_notification_title)));
        } catch (Exception unused) {
        }
    }

    
    public void deletePhotos(ArrayList<Photo> arrayList) {
        try {
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                Photo photo = (Photo) it.next();
                if (!photo.isSection() && photo.getFile().delete()) {
                    notifyItemRemoved(this.photos.indexOf(photo));
                    this.photos.remove(photo);
                }
            }
            notifyDataSetChanged();
        } catch (Exception unused) {
        }
    }

    
    public void confirmDelete(final ArrayList<Photo> arrayList) {
        try {
            int size = arrayList.size();
            new Builder(this.context).setTitle((CharSequence) this.context.getResources().getQuantityString(R.plurals.delete_photo_alert_title, size)).setMessage((CharSequence) this.context.getResources().getQuantityString(R.plurals.delete_photo_alert_message, size, new Object[]{Integer.valueOf(size)})).setCancelable(false).setPositiveButton(17039379, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    deletePhotos(arrayList);
                }
            }).setNegativeButton(17039369, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            }).show();
        } catch (Exception unused) {
        }
    }

    public int getItemCount() {
        return this.photos.size();
    }
}
