package com.onceapps.m.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.onceapps.m.R;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.ui.AllBrandsMagazineListItemViewHolder;
import com.onceapps.m.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AllBrandsMagazineListAnimatedViewAdapter extends RecyclerView.Adapter<AllBrandsMagazineListItemViewHolder> implements View.OnClickListener {

    public static final int PLACEHOLDER_START = 3000;
    public static final int PLACEHOLDER_END = 3001;
    public static final int PLACEHOLDER_START_SIZE = 1;
    public static final int PLACEHOLDER_END_SIZE = 1;
    public static final int CELL = 3002;
    
    protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMMM", new Locale("hu","HU"));

    protected MagazineHandler mMagazineHandler;
    protected final List<MagazineStatus> mMagazines = Collections.synchronizedList(new ArrayList<MagazineStatus>());
    protected OnItemClickListener onItemClickListener;

    private static Pair<Integer, Integer> mImageSize;

    public AllBrandsMagazineListAnimatedViewAdapter() {}

    public AllBrandsMagazineListAnimatedViewAdapter(List<Magazine> magazines) {
        for(Magazine magazine : magazines) {
            MagazineStatus status = new MagazineStatus.Builder()
                    .setMagazine(magazine)
                    .setPercent(0)
                    .setDownloadStatus(MagazineStatus.DownloadStatus.NOT_DOWNLOADED)
                    .build();
            mMagazines.add(status);
        }
    }

    public boolean updateMagazineStatus(MagazineStatus magazineStatus) {
        int positionToUpdate = -1;
        for (int i = 0; i < mMagazines.size(); i++) {
            if(mMagazines.get(i).getMagazine().getId().equals(magazineStatus.getMagazine().getId())) {
                positionToUpdate = i;
            }
        }
        if(positionToUpdate >= 0) {
            mMagazines.set(positionToUpdate, magazineStatus);
            notifyItemChanged(positionToUpdate + PLACEHOLDER_START_SIZE, magazineStatus);
            return true;
        }
        return false;
    }

    public void removeMagazineStatus(MagazineStatus magazineStatus) {
        int positionToUpdate = -1;
        for (int i = 0; i < mMagazines.size(); i++) {
            if(mMagazines.get(i).getMagazine().getId().equals(magazineStatus.getMagazine().getId())) {
                positionToUpdate = i;
            }
        }
        if(positionToUpdate >= 0) {
            mMagazines.remove(positionToUpdate);
            notifyItemRemoved(positionToUpdate + PLACEHOLDER_START_SIZE);
        }
    }

    public void addMagazineStatus(MagazineStatus magazineStatus) {
        if(!updateMagazineStatus(magazineStatus)) {
            int pos = mMagazines.size() > 0 ? mMagazines.size() - 1 : 0;
            mMagazines.add(pos, magazineStatus);
            notifyItemInserted(pos + PLACEHOLDER_START_SIZE);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return PLACEHOLDER_START;
        else if (position == getItemCount() - 1)
            return PLACEHOLDER_END;
        else
            return CELL;
    }
    
    @Override
    public AllBrandsMagazineListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        final View view;
        switch (viewType) {
            case PLACEHOLDER_START:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_brands_magazine_placeholder, parent, false);
                return new PlaceHolderViewHolder(view, true, (int) parent.getResources().getDimension(R.dimen.default_margin));
            case PLACEHOLDER_END:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_brands_magazine_placeholder, parent, false);
                return new PlaceHolderViewHolder(view, true, (int) parent.getResources().getDimension(R.dimen.default_margin));
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_brands_magazine, parent, false);
                view.setOnClickListener(this);
        }

        return new AllBrandsMagazineListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AllBrandsMagazineListItemViewHolder holder, int position) {
        if (!(holder instanceof PlaceHolderViewHolder)) {
            final MagazineStatus magazineStatus = mMagazines.get(position - PLACEHOLDER_START_SIZE);
            final Magazine magazine = magazineStatus.getMagazine();
            if(mImageSize == null) {
                holder.cover.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mImageSize = new Pair<>(holder.cover.getMeasuredWidth(), holder.cover.getMeasuredHeight());
                        holder.cover.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
                Utils.loadImageResized(holder.cover, magazine.getCoverUrl());
            }
            else {
                Utils.loadImageResized(holder.cover, magazine.getCoverUrl(), mImageSize.first, mImageSize.second);
            }
            holder.date.setText(mDateFormat.format(magazine.getReleaseDate()));
            holder.itemView.setTag(magazine);

            if(!holder.initialScalingDone.getAndSet(true)) {
                if (position == 1)
                    holder.setEnlarged(false);
                else
                    holder.setEnlarged(true);
                holder.newPosition(position - PLACEHOLDER_START_SIZE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMagazines.size() + PLACEHOLDER_START_SIZE + PLACEHOLDER_END_SIZE;
    }

    public int getMagazinesItemCount() {
        return mMagazines.size();
    }

    @Override
    public void onClick(final View v) {
        if(onItemClickListener != null) onItemClickListener.onItemClick(v, (Magazine) v.getTag());
    }

    protected static class PlaceHolderViewHolder extends AllBrandsMagazineListItemViewHolder {

        public PlaceHolderViewHolder(View itemView, boolean horizontal, int dimen) {
            super(itemView);

            if(horizontal) {
                if (dimen != -1) {
                    itemView.getLayoutParams().width = dimen;
                    itemView.requestLayout();
                }
            }
            else {
                if (dimen != -1) {
                    itemView.getLayoutParams().height = dimen;
                    itemView.requestLayout();
                }
            }
        }

        @Override
        public void enlarge(boolean withAnimation) { }

        @Override
        public void reduce(boolean withAnimation) { }

        @Override
        public void newPosition(int position) { }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Magazine magazine);

    }
}
