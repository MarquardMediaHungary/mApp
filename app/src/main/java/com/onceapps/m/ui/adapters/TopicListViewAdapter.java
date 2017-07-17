package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.onceapps.m.R;
import com.onceapps.m.models.Topic;

import java.util.List;

public class TopicListViewAdapter extends RecyclerView.Adapter<TopicListViewAdapter.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private List<Topic> mTopics;
    private OnItemClickListener onItemClickListener;

    public TopicListViewAdapter(Context context, List<Topic> topics) {
        this.mContext = context;
        this.mTopics = topics;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Topic topic = mTopics.get(position);
        Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_topic_plus);
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable.mutate(), Color.parseColor(topic.getColor()));
        holder.image.setImageDrawable(drawable);
        holder.name.setText(topic.getName());
        holder.itemView.setTag(topic);
    }

    @Override
    public int getItemCount() {
        return mTopics.size();
    }

    @Override
    public void onClick(final View v) {
        onItemClickListener.onItemClick(v, (Topic) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }

    @Override
    public long getItemId(int position) {
        return mTopics.get(position).getId();
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Topic topic);

    }
}
