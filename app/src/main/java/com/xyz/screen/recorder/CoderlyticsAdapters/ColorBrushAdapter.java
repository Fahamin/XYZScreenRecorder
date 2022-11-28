package com.xyz.screen.recorder.CoderlyticsAdapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.xyz.screen.recorder.R;

import java.util.ArrayList;

public class ColorBrushAdapter extends Adapter<ColorBrushAdapter.ViewHolder> {
    
    public ArrayList<Integer> colors;
    private Context context;
    
    public int itemCheck = 0;
    
    public OnClick onClick;

    public interface OnClick {
        void onClickColor(int i);
    }

    public class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        
        public ImageView imv_check;
        
        public ImageView imv_color;

        ViewHolder(View view) {
            super(view);
            this.imv_color = (ImageView) view.findViewById(R.id.imv_color);
            this.imv_check = (ImageView) view.findViewById(R.id.imv_check);
            this.itemView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    itemCheck = ((Integer) colors.get(ViewHolder.this.getAdapterPosition())).intValue();
                    onClick.onClickColor(((Integer) colors.get(ViewHolder.this.getAdapterPosition())).intValue());
                    notifyDataSetChanged();
                }
            });
        }
    }

    public ColorBrushAdapter(Context context2, ArrayList<Integer> arrayList, OnClick onClick2) {
        this.colors = arrayList;
        this.context = context2;
        this.onClick = onClick2;
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(this.context).inflate(R.layout.content_item_color, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Drawable background = viewHolder.imv_color.getBackground();
        if (background instanceof ShapeDrawable) {
            ((ShapeDrawable) background).getPaint().setColor(((Integer) this.colors.get(i)).intValue());
        } else if (background instanceof GradientDrawable) {
            ((GradientDrawable) background).setColor(((Integer) this.colors.get(i)).intValue());
        } else if (background instanceof ColorDrawable) {
            ((ColorDrawable) background).setColor(((Integer) this.colors.get(i)).intValue());
        }
        if (this.itemCheck == ((Integer) this.colors.get(i)).intValue()) {
            viewHolder.imv_check.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imv_check.setVisibility(View.GONE);
        }
    }

    public int getItemCount() {
        return this.colors.size();
    }
}
