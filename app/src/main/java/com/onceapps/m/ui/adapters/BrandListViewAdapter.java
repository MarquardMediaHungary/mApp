package com.onceapps.m.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.utils.Utils;

import java.util.List;

public class BrandListViewAdapter extends RecyclerView.Adapter<BrandListViewAdapter.ViewHolder> implements View.OnClickListener {

    private List<Brand> mBrands;
    private OnItemClickListener onItemClickListener;

    private Pair<Integer, Integer> mImageSize;

    public BrandListViewAdapter(List<Brand> brands) {
        this.mBrands = brands;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Brand brand = mBrands.get(position);
        if (mImageSize == null) {
            holder.image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageSize = new Pair<>(holder.image.getMeasuredWidth(), holder.image.getMeasuredHeight());
                    holder.image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            Utils.loadImageResized(holder.image, brand.getLogo().getWhite());
            Utils.loadImageResized(holder.background, brand.getImage());
        } else {
            Utils.loadImageResized(holder.background, brand.getImage(), mImageSize.first, mImageSize.second);
            Utils.loadImageResized(holder.image, brand.getLogo().getWhite(), mImageSize.first, mImageSize.second);
        }
        holder.itemView.setTag(brand);
    }

    @Override
    public int getItemCount() {
        return mBrands.size();
    }

    @Override
    public void onClick(final View v) {
        onItemClickListener.onItemClick(v, (Brand) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public final SimpleDraweeView image;
        public final SimpleDraweeView background;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            background = (SimpleDraweeView) itemView.findViewById(R.id.background);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Brand brand);

    }
}
