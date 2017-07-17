package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;
import com.onceapps.m.models.GalleryImage;
import com.onceapps.m.models.MagazineContentItem;
import com.onceapps.m.utils.Utils;

import java.util.List;

public class GalleryImagesAdapter extends RecyclerView.Adapter<GalleryImagesAdapter.ViewHolder> {

    private Context mContext;
    private List<GalleryImage> mImages;

    public GalleryImagesAdapter(Context context, List<GalleryImage> galleryImages) {
        this.mContext = context;
        this.mImages = galleryImages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        GalleryImage galleryImage = mImages.get(position);
        if (!TextUtils.isEmpty(galleryImage.getUrl())) {
            Utils.loadImageResized(holder.image, galleryImage.getUrl());
        }
        if (galleryImage.getDesc() != null) {
            holder.description.setText(Html.fromHtml(galleryImage.getDesc()));
        }
        if (!TextUtils.isEmpty(galleryImage.getSource())) {
            holder.source.setVisibility(View.VISIBLE);
            holder.source.setText(mContext.getString(R.string.source, Html.fromHtml(galleryImage.getSource())));
        } else {
            holder.source.setVisibility(View.GONE);
        }
        holder.itemView.setTag(galleryImage);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView image;
        public TextView description;
        public TextView source;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            description = (TextView) itemView.findViewById(R.id.description);
            source = (TextView) itemView.findViewById(R.id.source);
            AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView) itemView.findViewById(R.id.loading_anim)).getDrawable();
            animationDrawable.start();
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, MagazineContentItem article);

    }
}
