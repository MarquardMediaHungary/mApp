package com.onceapps.m.ui;

import android.os.Bundle;

import java.io.Serializable;

public class PageRequest implements Serializable {
    public enum Page {
        Back,
        TopicList,
        BrandList,
        AllArticles,
        AllIssues,
        MyIssues,
        MyMagazine,
        ShowDialog,
        HideDialog,
        None,
        StoragePermissionDenied,
        Impress,
        ClientVersionUnsupported,
        ClientVersionDeprecated,
        Settings,
        FollowedList,
        Favorites,
        FbVideoList,
        Offline
    }

    private final Page page;
    private final String id;
    private Bundle extras = new Bundle();

    public PageRequest(Page page) {
        this.page = page;
        this.id = null;
    }

    public PageRequest(Page page, String id) {
        this.page = page;
        this.id = id;
    }

    public PageRequest(Page page, Bundle extras) {
        this.extras = extras;
        this.page = page;
        id = null;
    }

    public Page getPage() {
        return page;
    }

    public String getId() {
        return id;
    }

    public Bundle getExtras() {
        return extras;
    }
}
