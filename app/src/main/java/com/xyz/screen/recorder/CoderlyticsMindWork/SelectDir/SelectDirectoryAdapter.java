package com.xyz.screen.recorder.CoderlyticsMindWork.SelectDir;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.xyz.screen.recorder.R;
import com.xyz.screen.recorder.CoderlyticsMindWork.Utilts.CoderlyticsConstants;

import java.io.File;
import java.util.ArrayList;

public class SelectDirectoryAdapter extends Adapter<SelectDirectoryAdapter.ItemViewHolder> {
    
    public static OnDirectoryClickedListerner onDirectoryClickedListerner;
    private Context context;
    
    public ArrayList<File> directories;

    static class ItemViewHolder extends ViewHolder {
        TextView dir;
        LinearLayout dir_view;

        public ItemViewHolder(View view) {
            super(view);
            this.dir = (TextView) view.findViewById(R.id.directory);
            this.dir_view = (LinearLayout) view.findViewById(R.id.directory_view);
        }
    }

    public interface OnDirectoryClickedListerner {
        void OnDirectoryClicked(File file);
    }

    SelectDirectoryAdapter(Context context2, OnDirectoryClickedListerner onDirectoryClickedListerner2, ArrayList<File> arrayList) {
        this.context = context2;
        onDirectoryClickedListerner = onDirectoryClickedListerner2;
        this.directories = arrayList;
    }
@Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.content_select_directory, viewGroup, false));
    }
@Override
    public void onBindViewHolder(final ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.dir.setText(((File) this.directories.get(i)).getName());
        itemViewHolder.dir.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                StringBuilder sb = new StringBuilder();
                sb.append("Item clicked: ");
                sb.append(SelectDirectoryAdapter.this.directories.get(itemViewHolder.getAdapterPosition()));
                Log.d(CoderlyticsConstants.TAG, sb.toString());
                SelectDirectoryAdapter.onDirectoryClickedListerner.OnDirectoryClicked((File) SelectDirectoryAdapter.this.directories.get(itemViewHolder.getAdapterPosition()));
            }
        });
    }
@Override
    public int getItemCount() {
        return this.directories.size();
    }
}
