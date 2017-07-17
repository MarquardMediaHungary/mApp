package com.onceapps.m.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onceapps.core.util.Broadcast;
import com.onceapps.m.R;
import com.onceapps.m.ui.PageRequest;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.SupposeUiThread;
import org.androidannotations.annotations.ViewById;

/**
 * BrandTopicListBottomTabBar
 * Created by szipe on 19/04/16.
 */
@EViewGroup(R.layout.inc_brand_topic_list_bottom_tabbar)
public class BrandTopicListBottomTabBar extends LinearLayout {

    @ViewById(R.id.topics)
    protected TextView mTopics;

    @ViewById(R.id.brands)
    protected TextView mBrands;

    public BrandTopicListBottomTabBar(Context context) {
        super(context);
    }

    public BrandTopicListBottomTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BrandTopicListBottomTabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SupposeUiThread
    public void selectTopics() {
        mBrands.setTextColor(getResources().getColor(R.color.textColorSecondary));
        mTopics.setTextColor(getResources().getColor(R.color.textColorPrimary));
    }

    @SupposeUiThread
    public void selectBrands() {
        mBrands.setTextColor(getResources().getColor(R.color.textColorPrimary));
        mTopics.setTextColor(getResources().getColor(R.color.textColorSecondary));
    }

    @Click(R.id.brands)
    protected void onBrandsClicked() {
        Broadcast.postEvent(new PageRequest(PageRequest.Page.BrandList));
    }

    @Click(R.id.topics)
    protected void onTopicsClicked() {
        Broadcast.postEvent(new PageRequest(PageRequest.Page.TopicList));
    }
}
