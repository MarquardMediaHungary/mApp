package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.MagazineContentItem;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView_;
import com.onceapps.m.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MagazineContentsAdapter extends RecyclerView.Adapter<MagazineContentsAdapter.ViewHolder> implements View.OnClickListener {

    private final Context mContext;
    private List<MagazineContentItem> mArticles = Collections.synchronizedList(new ArrayList<MagazineContentItem>());
    private OnItemClickListener onItemClickListener;

    private Pair<Integer, Integer> mImageSize;
    private final boolean mIsTablet;
    private final Brand mBrand;

    public MagazineContentsAdapter(Context context, List<MagazineContentItem> articles, Brand brand) {
        this.mContext = context;
        this.mBrand = brand;
        if (articles != null)
            mArticles.addAll(articles);
        mIsTablet = mContext.getResources().getBoolean(R.bool.is_tablet);
    }

    public void setArticles(List<MagazineContentItem> articles) {
        mArticles.clear();
        addArticles(articles);
    }

    public void addArticles(List<MagazineContentItem> articles) {
        mArticles.addAll(articles);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content_item, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        MagazineContentItem article = mArticles.get(position);
        if(mImageSize == null) {
            holder.image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageSize = new Pair<>(holder.image.getMeasuredWidth(), holder.image.getMeasuredHeight());
                    holder.image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            Utils.loadImageResized(holder.image, article.getFileUrlSmall());
        }
        else {
            Utils.loadImageResized(holder.image, article.getFileUrlSmall(), mImageSize.first, mImageSize.second);
        }

        if (mIsTablet && (position - 1) % 5 == 0) {
            holder.image.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high));
            holder.placeHolder.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high));
        } else {
            holder.image.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_normal));
            holder.placeHolder.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_normal));
        }

        if(!TextUtils.isEmpty(article.getTitle())) holder.title.setText(article.getTitle());
        holder.brand.setText(mBrand.getName());

        holder.lead.setText(article.getLead());

        holder.topicsParent.removeAllViews();
        if (article.getTopics() != null && article.getTopics().size() > 0) {
            holder.topicsParent.setVisibility(View.VISIBLE);
            for (Topic topic : article.getTopics()) {
                TopicTextWithIndicatorView topicView = TopicTextWithIndicatorView_.build(mContext);
                topicView.setDataSource(topic);
                holder.topicsParent.addView(topicView);
            }
            holder.topicsParent.invalidate();
        } else {
            holder.topicsParent.setVisibility(View.GONE);
        }
        holder.topicsParent.getParent().requestLayout();

        holder.itemView.setTag(article);
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    @Override
    public void onClick(final View v) {
        onItemClickListener.onItemClick(v, (MagazineContentItem) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView image;
        public SimpleDraweeView placeHolder;
        public TextView brand;
        public TextView title;
        public TextView lead;
        public LinearLayout topicsParent;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            placeHolder = (SimpleDraweeView) itemView.findViewById(R.id.place_holder);
            brand = (TextView) itemView.findViewById(R.id.brand);
            title = (TextView) itemView.findViewById(R.id.title);
            lead = (TextView) itemView.findViewById(R.id.lead);
            topicsParent = (LinearLayout) itemView.findViewById(R.id.topics_parent);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, MagazineContentItem article);

    }
}
