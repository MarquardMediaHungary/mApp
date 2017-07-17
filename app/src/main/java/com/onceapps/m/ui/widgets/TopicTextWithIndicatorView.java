package com.onceapps.m.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onceapps.m.R;
import com.onceapps.m.models.Topic;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * TopicTextWithIndicatorView
 * Created by szipe on 20/04/16.
 */
@EViewGroup(R.layout.view_topic_text_with_indicator)
public class TopicTextWithIndicatorView extends LinearLayout {

    @ViewById(R.id.topic_text)
    protected TextView text;

    public TopicTextWithIndicatorView(Context context) {
        super(context);
    }

    public TopicTextWithIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopicTextWithIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDataSource(Topic topic) {
        text.setText(topic.getName());
        Drawable[] drawables = text.getCompoundDrawablesRelative();
        if (drawables.length > 0 && drawables[0] != null && drawables[0] instanceof GradientDrawable) {
            GradientDrawable indicator = (GradientDrawable) drawables[0].mutate();
            indicator.setColor(Color.parseColor(topic.getColor()));
            text.setCompoundDrawablesRelativeWithIntrinsicBounds(indicator, null, null, null);
        }
    }

    public void setDataSource(Topic topic, int textColor) {
        text.setTextColor(textColor);
        setDataSource(topic);
    }
}
