package com.onceapps.m.models;

import java.util.ArrayList;

/**
 * MagazineContents
 * Created by szipe on 29/04/16.
 */
public class MagazineContents extends ArrayList<MagazineContentItem> {

    public MagazineContentItem getContentsItemByHTMLPageNumber(int page) {
        if(size() > 0) {
            for(MagazineContentItem item : this) {
                if(item.getHtmlPage().equals(page)) {
                    return item;
                }
            }
            return null;
        }
        else {
            return null;
        }
    }
}
