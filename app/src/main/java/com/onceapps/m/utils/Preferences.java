package com.onceapps.m.utils;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by balage on 21/04/16.
 * to store a single instance of preferences class
 */
@EBean(scope = EBean.Scope.Singleton)
public class Preferences {

    @Pref
    public volatile ApplicationPreferences_ app;
}
