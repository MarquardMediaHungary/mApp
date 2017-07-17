package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.api.MagazineHandler_;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MagazineListViewAdapter extends RecyclerView.Adapter<MagazineListViewAdapter.ViewHolder> implements View.OnClickListener {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMMM", new Locale("hu","HU"));

    protected List<MagazineStatus> mMagazines = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private OnDownloadButtonClickListener onDownloadButtonClickListener;
    private Context mContext;
    private MagazineHandler mMagazineHandler;

    private Pair<Integer, Integer> mImageSize;

    public MagazineListViewAdapter(Context context, List<Magazine> magazines) {
        this.mContext = context;
        this.mMagazineHandler = MagazineHandler_.getInstance_(mContext);
        for(Magazine magazine : magazines) {
            MagazineStatus status = mMagazineHandler.getStatus(magazine);
            if(status == null) {
                status = new MagazineStatus.Builder()
                    .setMagazine(magazine)
                    .setPercent(0)
                    .setDownloadStatus(MagazineStatus.DownloadStatus.NOT_DOWNLOADED)
                    .build();
            }

            mMagazines.add(status);
        }
    }

    public boolean updateMagazinStatus(MagazineStatus magazineStatus) {
        int positionToUpdate = -1;
        for (int i = 0; i < mMagazines.size(); i++) {
            if(mMagazines.get(i).getMagazine().getId().equals(magazineStatus.getMagazine().getId())) {
                positionToUpdate = i;
            }
        }
        if(positionToUpdate >= 0) {
            mMagazines.set(positionToUpdate, magazineStatus);
            notifyItemChanged(positionToUpdate, magazineStatus);
            return true;
        }
        return false;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnDownloadButtonClickListener(OnDownloadButtonClickListener onDownloadButtonClickListener) {
        this.onDownloadButtonClickListener = onDownloadButtonClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_magazine, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Magazine magazine = mMagazines.get(position).getMagazine();
        final MagazineStatus magazineStatus = mMagazines.get(position);
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
        updateDownloadStatusUI(holder, magazineStatus);
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDownloadButtonClickListener.onDownloadButtonClick(v, magazine);
            }
        });
        holder.itemView.setTag(magazine);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if(payloads != null && payloads.size() > 0 && payloads.get(0) instanceof MagazineStatus) {
            MagazineStatus magazineStatus = ((MagazineStatus) payloads.get(0));
            Logger.debug("onBindViewHolder payload status: " + magazineStatus);
            updateDownloadStatusUI(holder, magazineStatus);
        }
        else {
            onBindViewHolder(holder, position);
        }
    }

    private void updateDownloadStatusUI(ViewHolder holder, MagazineStatus magazineStatus) {
        MagazineStatus.DownloadStatus downloadStatus = magazineStatus.getDownloadStatus();
        switch (downloadStatus) {
            case DOWNLOAD_FINISHED:
                holder.download.setText(R.string.open_button);
                break;
            case NOT_DOWNLOADED:
            case DOWNLOAD_FAILED:
            case DOWNLOAD_CANCELLED:
                holder.download.setText(R.string.download_button_text);
                break;
            case DOWNLOAD_IN_PROGRESS:
                holder.download.setText(R.string.in_progress);
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mMagazines.size();
    }

    @Override
    public void onClick(final View v) {
        if(onItemClickListener != null) onItemClickListener.onItemClick(v, (Magazine) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView cover;
        public TextView date;
        public Button download;

        public ViewHolder(View itemView) {
            super(itemView);
            cover = (SimpleDraweeView) itemView.findViewById(R.id.cover);
            date = (TextView) itemView.findViewById(R.id.date);
            download = (Button) itemView.findViewById(R.id.download_button);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Magazine magazine);

    }

    public interface OnDownloadButtonClickListener {

        void onDownloadButtonClick(View view, Magazine magazine);

    }
}
