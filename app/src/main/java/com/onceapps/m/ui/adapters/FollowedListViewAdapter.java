package com.onceapps.m.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.onceapps.m.R;
import com.onceapps.m.models.FollowedItem;
import com.onceapps.m.models.FollowedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FollowedListViewAdapter extends RecyclerView.Adapter<FollowedListViewAdapter.ViewHolder> implements View.OnClickListener {

    private final List<FollowedItem> mFollowedItems = Collections.synchronizedList(new ArrayList<FollowedItem>());
    private OnItemClickListener onItemClickListener;

    public FollowedListViewAdapter(FollowedList items) {

        mFollowedItems.clear();
        if (items != null) mFollowedItems.addAll(items);
        setHasStableIds(true);
    }

    public void setFollowedList(FollowedList items) {
        mFollowedItems.clear();
        addItems(items);
    }

    public void addItems(FollowedList items) {
        mFollowedItems.addAll(items);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_followed_item, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FollowedItem followedItem = mFollowedItems.get(position);
        holder.unfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FollowedListViewAdapter.this.onClick(v);
            }
        });
        holder.name.setText(followedItem.getName());
        holder.unfollowButton.setTag(followedItem);
        holder.itemView.setTag(followedItem);
    }

    @Override
    public int getItemCount() {
        return mFollowedItems.size();
    }

    @Override
    public void onClick(final View v) {
        onItemClickListener.onItemClick(v, (FollowedItem) v.getTag());
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageButton unfollowButton;
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            unfollowButton = (ImageButton) itemView.findViewById(R.id.unfollow_button);
            name = (TextView) itemView.findViewById(R.id.name);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, FollowedItem followedItem);

    }
}
