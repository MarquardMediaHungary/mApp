package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onceapps.core.util.Logger;
import com.onceapps.m.R;
import com.onceapps.m.api.MagazineHandler;
import com.onceapps.m.models.MagazineStatus;
import com.onceapps.m.ui.AllBrandsMagazineListItemViewHolder;
import com.onceapps.m.ui.widgets.LoadingLayout;
import com.onceapps.m.utils.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MyIssuesAnimatedViewAdapter extends AllBrandsMagazineListAnimatedViewAdapter {

    private Context mContext;

    public MyIssuesAnimatedViewAdapter(MagazineHandler magazineHandler, Context context) {
        mMagazines.addAll(magazineHandler.getMagazineRegistry().values());
        Collections.reverse(mMagazines);
        mMagazineHandler = magazineHandler;
        mContext = context;
    }

    public void setMagazineHandler(MagazineHandler magazineHandler) {
        mMagazines.clear();
        mMagazines.addAll(magazineHandler.getMagazineRegistry().values());
        Collections.reverse(mMagazines);
        mMagazineHandler = magazineHandler;
        notifyDataSetChanged();
    }

    private Pair<Integer, Integer> mImageSize;

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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_issues, parent, false);
                view.setOnClickListener(this);
        }

        return new MyIssuesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AllBrandsMagazineListItemViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof MyIssuesViewHolder) {
            final MagazineStatus magazineStatus = mMagazines.get(position - PLACEHOLDER_START_SIZE);
            Logger.debug("onBindViewHolder status: " + magazineStatus);
            ((MyIssuesViewHolder) holder).cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMagazineHandler != null) {
                        mMagazineHandler.cancel(magazineStatus.getMagazine());
                        mMagazines.remove(magazineStatus);
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                }
            });
            updateDownloadStatusUI((MyIssuesViewHolder) holder, magazineStatus);
        }
    }

    @Override
    public void onBindViewHolder(AllBrandsMagazineListItemViewHolder holder, int position, List<Object> payloads) {
        if (holder instanceof MyIssuesViewHolder) {
            if (payloads != null && payloads.size() > 0 && payloads.get(0) instanceof MagazineStatus) {
                MagazineStatus magazineStatus = ((MagazineStatus) payloads.get(0));
                Logger.debug("onBindViewHolder payload status: " + magazineStatus);
                updateDownloadStatusUI((MyIssuesViewHolder) holder, magazineStatus);
            } else {
                onBindViewHolder(holder, position);
            }
        }
    }

    private void updateDownloadStatusUI(MyIssuesViewHolder holder, MagazineStatus magazineStatus) {
        MagazineStatus.DownloadStatus downloadStatus = magazineStatus.getDownloadStatus();
        switch (downloadStatus) {
            case DOWNLOAD_FAILED:
                holder.loadingLayout.setVisibility(View.INVISIBLE);
                holder.percent.setText(mContext.getString(R.string.download_failed));
                holder.downloadOverlay.setVisibility(View.VISIBLE);

                break;
            case DOWNLOAD_CANCELLED:
                holder.downloadOverlay.setVisibility(View.GONE);
                break;
            case DOWNLOAD_FINISHED:
                holder.downloadOverlay.setVisibility(View.GONE);
                break;
            case NOT_DOWNLOADED:
                holder.downloadOverlay.setVisibility(View.GONE);
                break;
            case DOWNLOAD_IN_PROGRESS:
                if (holder.downloadOverlay.getVisibility() != View.VISIBLE) {
                    holder.downloadOverlay.setVisibility(View.VISIBLE);
                }
                if (magazineStatus.getPercent() != 99) {
                    holder.percent.setText(String.format(Locale.getDefault(), "%d %%", magazineStatus.getPercent()));
                } else {
                    holder.percent.setText(R.string.unpacking);
                }
                break;
        }

    }

    public class MyIssuesViewHolder extends AllBrandsMagazineListItemViewHolder {

        public RelativeLayout downloadOverlay;
        public ImageButton cancelButton;
        public LoadingLayout loadingLayout;
        public TextView percent;

        public MyIssuesViewHolder(View itemView) {
            super(itemView);
            scaleReduced = Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high);

            downloadOverlay = (RelativeLayout) itemView.findViewById(R.id.download_overlay);
            cancelButton = (ImageButton) itemView.findViewById(R.id.cancel);
            loadingLayout = (LoadingLayout) itemView.findViewById(R.id.progress);
            percent = (TextView) itemView.findViewById(R.id.percent);
        }
    }

}
