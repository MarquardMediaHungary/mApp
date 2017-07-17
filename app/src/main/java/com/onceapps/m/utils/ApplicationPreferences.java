package com.onceapps.m.utils;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by balage on 18/04/16.
 * User preferences
 */
@SharedPref(value = SharedPref.Scope.UNIQUE)
@Deprecated
/**
 * @deprecated use the thread safe
 */
public interface ApplicationPreferences {

    String authToken();

    String authTokenType();

    String magazineStorageState();

    @DefaultBoolean(false)
    boolean useInternalStorage();

    @DefaultBoolean(true)
    boolean useLocalCache();

    boolean firstRun();

    @DefaultBoolean(false)
    boolean deprecatedMessageShown();

    String user();

    String followedBrands();

    String followedTopics();

    @DefaultInt(0)
    int appVersionPrevStart();
}
