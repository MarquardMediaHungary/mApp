package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.onceapps.m.R;
import com.onceapps.m.models.Article;
import com.onceapps.m.models.FavoriteList;
import com.onceapps.m.models.Topic;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView;
import com.onceapps.m.ui.widgets.TopicTextWithIndicatorView_;
import com.onceapps.m.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ArticleListViewAdapter extends RecyclerView.Adapter<ArticleListViewAdapter.ViewHolder> implements View.OnClickListener {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy. MMM dd.", new Locale("hu", "HU"));

    private final Context mContext;
    private final List<Article> mArticles = Collections.synchronizedList(new ArrayList<Article>());
    private FavoriteList mFavorites = new FavoriteList();
    private OnItemClickListener onItemClickListener;

    private Pair<Integer, Integer> mImageSize;
    private final boolean mIsTablet;

    public ArticleListViewAdapter(Context context, List<Article> articles) {
        this.mContext = context;
        mIsTablet = mContext.getResources().getBoolean(R.bool.is_tablet);
        mArticles.clear();
        if (articles != null)
            mArticles.addAll(articles);
        setHasStableIds(true);
    }

    public void setArticles(List<Article> articles) {
        mArticles.clear();
        addArticles(articles);
    }

    public void setFavorites(@NonNull FavoriteList favorites) {
        this.mFavorites = favorites;
        notifyDataSetChanged();
    }

    public Article getLastItem() {
        return mArticles.get(mArticles.size() - 1);
    }

    public void addArticles(List<Article> articles) {
        mArticles.addAll(articles);
        Collections.sort(mArticles);
        notifyDataSetChanged();
    }

    public void removeArticle(Article article) {

        int positionToUpdate = -1;
        for (int i = 0; i < mArticles.size(); i++) {
            if (mArticles.get(i).getId().equals(article.getId())) {
                positionToUpdate = i;
            }
        }
        if (positionToUpdate >= 0) {
            mArticles.remove(positionToUpdate);
            notifyItemRemoved(positionToUpdate);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Article article = mArticles.get(position);

        if (mImageSize == null) {
            holder.image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageSize = new Pair<>(holder.image.getMeasuredWidth(), holder.image.getMeasuredHeight());
                    holder.image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
            Utils.loadImageResized(holder.image, article.getFileUrlSmall());
        } else {
            Utils.loadImageResized(holder.image, article.getFileUrlSmall(), mImageSize.first, mImageSize.second);
        }

        if (mIsTablet && (position - 1) % 5 == 0) {
            holder.image.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high));
            holder.placeHolder.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_high));
        } else {
            holder.image.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_normal));
            holder.placeHolder.setAspectRatio(Utils.getFloatValue(mContext, R.dimen.article_image_aspect_ratio_normal));
        }

        if (article.getBrands() != null && article.getBrands().size() > 0) {
            holder.brand.setText(article.getBrands().get(0).getName());
        }
        holder.title.setText(article.getTitle());
        holder.date.setText(mDateFormat.format(article.getDate()));
        holder.lead.setText(article.getLead());

        holder.favorite.setActivated(mFavorites.isFavorite(article));
        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArticleListViewAdapter.this.onClick(v);
            }
        });

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
        holder.favorite.setTag(article);
    }

    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    @Override
    public void onClick(final View v) {
        onItemClickListener.onItemClick(v, (Article) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public SimpleDraweeView image;
        public SimpleDraweeView placeHolder;
        public TextView brand;
        public TextView title;
        public TextView date;
        public TextView lead;
        public ImageButton favorite;
        public LinearLayout topicsParent;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (SimpleDraweeView) itemView.findViewById(R.id.image);
            placeHolder = (SimpleDraweeView) itemView.findViewById(R.id.place_holder);
            brand = (TextView) itemView.findViewById(R.id.brand);
            title = (TextView) itemView.findViewById(R.id.title);
            date = (TextView) itemView.findViewById(R.id.date);
            lead = (TextView) itemView.findViewById(R.id.lead);
            favorite = (ImageButton) itemView.findViewById(R.id.favorite);
            topicsParent = (LinearLayout) itemView.findViewById(R.id.topics_parent);
        }
    }

    @Override
    public long getItemId(int position) {
        return mArticles.get(position).getId();
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Article article);

    }
}
