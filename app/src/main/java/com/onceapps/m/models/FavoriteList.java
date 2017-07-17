package com.onceapps.m.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * response type for favorite list request
 */
public class FavoriteList extends ArrayList<Favorite> {

    public boolean isFavorite(@NonNull Article article) {
        boolean isFavorite = false;
        for (Favorite item : this) {
            if (item.getArticle() != null &&
                    item.getArticle().getId() != null &&
                    article.getId() != null &&
                    item.getArticle().getId().equals(article.getId())) {
                isFavorite = true;
            }
        }
        return isFavorite;
    }

    public Favorite getFavoriteByArticleId(Article article) {
        Favorite favorite = null;
        for (Favorite item : this) {
            if (item.getArticle() != null &&
                    item.getArticle().getId() != null &&
                    article.getId() != null &&
                    item.getArticle().getId().equals(article.getId())) {
                favorite = item;
            }
        }
        return favorite;
    }
}
