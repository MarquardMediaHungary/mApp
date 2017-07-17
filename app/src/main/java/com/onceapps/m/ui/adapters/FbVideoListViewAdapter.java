package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;
import com.onceapps.m.models.FbVideo;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView_;
import com.onceapps.m.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FbVideoListViewAdapter extends RecyclerView.Adapter<FbVideoListViewAdapter.ViewHolder> implements View.OnClickListener {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMM dd.", new Locale("hu", "HU"));

    private final Context mContext;
    private final List<FbVideo> mFbVideos = Collections.synchronizedList(new ArrayList<FbVideo>());
    private OnItemClickListener onItemClickListener;
    private final boolean mIsTablet;

    private Pair<Integer, Integer> mImageSize;

    public FbVideoListViewAdapter(Context context, List<FbVideo> fbVideos) {
        this.mContext = context;
        mIsTablet = mContext.getResources().getBoolean(R.bool.is_tablet);
        mFbVideos.clear();
        if (fbVideos != null)
            mFbVideos.addAll(fbVideos);
        Collections.sort(mFbVideos);
        setHasStableIds(true);
    }

    public void setFbVideos(List<FbVideo> fbVideos) {
        mFbVideos.clear();
        addArticles(fbVideos);
        Collections.sort(mFbVideos);
    }

    public void addArticles(List<FbVideo> fbVideos) {
        mFbVideos.addAll(fbVideos);
        Collections.sort(mFbVideos);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fb_video, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FbVideo fbVideo = mFbVideos.get(position);

        if (mImageSize == null) {
            holder.image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageSize = new Pair<>(holder.image.getMeasuredWidth(), holder.image.getMeasuredHeight());
                    holder.image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            Utils.loadImageResized(holder.image, fbVideo.getImageUrl());
        } else {
            Utils.loadImageResized(holder.image, fbVideo.getImageUrl(), mImageSize.first, mImageSize.second);
        }

        if (mIsTablet && (position - 1) % 5 == 0) {
            holder.image.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high));
            holder.placeHolder.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high));
        } else {
            holder.image.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_normal));
            holder.placeHolder.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_normal));
        }

        if (fbVideo.getBrands() != null && fbVideo.getBrands().size() > 0) {
            holder.brand.setText(fbVideo.getBrands().get(0).getName());
        }
        holder.title.setText(fbVideo.getTitle());
        holder.date.setText(mDateFormat.format(fbVideo.getDate()));
        holder.lead.setText(fbVideo.getLead());

        holder.topicsParent.removeAllViews();
        if (fbVideo.getTopics() != null && fbVideo.getTopics().size() > 0) {
            holder.topicsParent.setVisibility(View.VISIBLE);
            for (Topic topic : fbVideo.getTopics()) {
                TopicTextWithIndicatorView topicView = TopicTextWithIndicatorView_.build(mContext);
                topicView.setDataSource(topic);
                holder.topicsParent.addView(topicView);
            }
            holder.topicsParent.invalidate();
        } else {
            holder.topicsParent.setVisibility(View.GONE);
        }
        holder.topicsParent.getParent().requestLayout();
        holder.itemView.setTag(fbVideo);
    }

    @Override
    public int getItemCount() {
        return mFbVideos.size();
    }

    @Override
    public void onClick(final View v) {
        onItemClickListener.onItemClick(v, (FbVideo) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView image;
        public SimpleDraweeView placeHolder;
        public TextView brand;
        public TextView title;
        public TextView date;
        public TextView lead;
        public LinearLayout topicsParent;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            placeHolder = (SimpleDraweeView) itemView.findViewById(R.id.place_holder);
            brand = (TextView) itemView.findViewById(R.id.brand);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            lead = (TextView) itemView.findViewById(R.id.lead);
            topicsParent = (LinearLayout) itemView.findViewById(R.id.topics_parent);
        }
    }

    @Override
    public long getItemId(int position) {
        return mFbVideos.get(position).getVideoId();
    }

    public interface OnItemClickListener {

        void onItemClick(View view, FbVideo fbVideo);

    }
}
