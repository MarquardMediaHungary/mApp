package com.onceapps.m.ui.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onceapps.m.R;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.ui.AllBrandsMagazineListItemViewHolder;
import com.onceapps.m.ui.activites.MagazinePreviewActivity_;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AllBrandsBrandListViewAdapter extends RecyclerView.Adapter<AllBrandsBrandListViewAdapter.ViewHolder> implements AllBrandsMagazineListAnimatedViewAdapter.OnItemClickListener {

    private Context mContext;
    private final Map<Brand, List<Magazine>> mMagazineMap = Collections.synchronizedMap(new LinkedHashMap<Brand, List<Magazine>>());

    public AllBrandsBrandListViewAdapter(Context context) {
        this.mContext = context;
    }

    public void setContent(Map<Brand, List<Magazine>> magazines) {
        mMagazineMap.clear();
        mMagazineMap.putAll(magazines);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_brands_brand, parent, false);
        return new ViewHolder(v);
    }

    public boolean hasContent() {
        return mMagazineMap.size() > 0;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final List<Magazine> magazineList = (new ArrayList<>(mMagazineMap.values())).get(position);
        final Brand brand = (Brand) mMagazineMap.keySet().toArray()[position];

        initRecyclerView(holder.magazinesRecycler);
        setRecyclerAdapter(holder.magazinesRecycler, magazineList);
        holder.itemView.setTag(brand);
        holder.brandName.setText(brand.getName());
    }

    @Override
    public int getItemCount() {
        return mMagazineMap.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView brandName;
        public RecyclerView magazinesRecycler;

        public ViewHolder(View itemView) {
            super(itemView);
            brandName = (TextView) itemView.findViewById(R.id.brand_name);
            magazinesRecycler = (RecyclerView) itemView.findViewById(R.id.magazines_recycler);
        }
    }

    private void initRecyclerView(RecyclerView recyclerView) {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                for (int i = 0; i < recyclerView.getChildCount(); ++i) {
                    RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                    if (viewHolder instanceof AllBrandsMagazineListItemViewHolder) {
                        AllBrandsMagazineListItemViewHolder cellViewHolder = ((AllBrandsMagazineListItemViewHolder) viewHolder);
                        cellViewHolder.newPosition(i);
                    }
                }
            }
        });
    }

    private void setRecyclerAdapter(RecyclerView recyclerView, List<Magazine> magazineList) {
        AllBrandsMagazineListAnimatedViewAdapter adapter = new AllBrandsMagazineListAnimatedViewAdapter(magazineList);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, Magazine magazine) {

        MagazinePreviewActivity_.intent(mContext).magazine(magazine).start();
    }
}
