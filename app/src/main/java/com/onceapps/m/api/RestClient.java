package com.onceapps.m.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.onceapps.core.util.Broadcast;
import com.onceapps.core.util.DeviceUtils;
import com.onceapps.core.util.HTTP;
import com.onceapps.core.util.HttpRequestPart;
import com.onceapps.core.util.HttpRequestPartPlain;
import com.onceapps.core.util.HttpResponse;
import com.onceapps.core.util.HttpUtils;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.OfflineException;
import com.onceapps.core.util.SerializationUtils;
import com.onceapps.m.models.Article;
import com.onceapps.m.models.ArticleList;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.BrandList;
import com.onceapps.m.models.Favorite;
import com.onceapps.m.models.FavoriteList;
import com.onceapps.m.models.FbVideoList;
import com.onceapps.m.models.FollowedItem;
import com.onceapps.m.models.FollowedList;
import com.onceapps.m.models.Gallery;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineContents;
import com.onceapps.m.models.MagazineList;
import com.onceapps.m.models.Topic;
import com.onceapps.m.models.TopicList;
import com.onceapps.m.models.User;
import com.onceapps.m.ui.PageRequest;
import com.onceapps.m.utils.Preferences;
import com.onceapps.m.utils.Utils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by balage on 18/04/16.
 * Rest client to communicate with marquard api
 */
@EBean(scope = EBean.Scope.Singleton)
public class RestClient {

    public static final String DUMMY_GRANT_TYPE = "";
    public static final String PASSWORD_GRANT_TYPE = "";
    public static final String CLIENT_ID = "";
    public static final String CLIENT_SECRET = "";

    private static final String DEV_PATH = "";
    private static final String PROD_PATH = "";

    public static final String PATH = Logger.isDevMode() ? DEV_PATH : PROD_PATH;

    private static final String OAUTH_URL = PATH + "";
    private static final String OAUTH_FB_URL = PATH + "";
    private static final String BRAND_LIST_URL = PATH + "";
    private static final String TOPIC_LIST_URL = PATH + "";
    private static final String ARTICLE_LIST_URL = PATH + "";
    private static final String FB_VIDEO_ARTICLE_LIST_URL = PATH + "";
    private static final String ARTICLE_URL = PATH + "";
    private static final String ARTICLE_HTML_URL = PATH + "";
    private static final String MAGAZINE_LIST_URL = PATH + "";
    private static final String MAGAZINE_URL = PATH + "";
    private static final String MAGAZINE_CONTENTS_URL = PATH + "";
    private static final String IMPRESS_HTML_URL = PATH + "";
    private static final String GALLERY_URL = PATH + "";
    private static final String USER_URL = PATH + "";
    private static final String LOGOUT_URL = PATH + "";
    private static final String FORGOTTEN_PASSWORD_URL = PATH + "";
    private static final String FOLLOWS_LIST_URL = PATH + "";
    private static final String FOLLOWS_URL = PATH + "";

    private static final String DEVICE_TOKEN_URL = PATH + "";

    private static final String MY_MAGAZINE_URL = PATH + ""; // TODO change, when ready

    private static final String FAVORITES_URL = PATH + "";
    private static final String DELETE_FAVORITE_URL = PATH + "";

    // extreme long cache time, because rest client always update cache if device online
    private static final long CACHE_DEFAULT_SEC = TimeUnit.DAYS.toSeconds(365);
    private static final String DATEFORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mmZ";

    @Bean
    protected Preferences mPreferences;

    @Bean
    protected DeviceUtils mDeviceUtils;

    @Bean
    protected HttpUtils mHttpUtils;

    @Bean
    protected DataCache mDataCache;

    @RootContext
    protected Context mContext;

    @AfterInject
    protected void init() {
        mHttpUtils.setUseCache(true);
        mHttpUtils.addDefaultHttpHeader(HTTP.USER_AGENT, Utils.generateUserAgentString(mContext));
    }

    @SupposeBackground
    public OauthResponse getAnonymousOauthToken(String grantType, String clientId, String clientSecret) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("grant_type", grantType);
        request.put("client_id", clientId);
        request.put("client_secret", clientSecret);
        return postForm(OAUTH_URL, OauthResponse.class, request);
    }

    ///// USER

    @SupposeBackground
    public OauthResponse login(@NonNull String email, @NonNull String password) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("grant_type", PASSWORD_GRANT_TYPE);
        request.put("client_id", CLIENT_ID);
        request.put("client_secret", CLIENT_SECRET);
        request.put("username", email);
        request.put("password", password);
        return postForm(OAUTH_URL, OauthResponse.class, request);
    }

    @SupposeBackground
    public OauthResponse loginWithFacebook(@NonNull String fbToken) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("client_id", CLIENT_ID);
        request.put("client_secret", CLIENT_SECRET);
        request.put("token", fbToken);
        return postForm(OAUTH_FB_URL, OauthResponse.class, request);
    }

    @SupposeBackground
    public OauthResponse register(@NonNull String email, @NonNull String password, @NonNull String name, @Nullable String dob, @Nullable User.Gender gender) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("client_id", CLIENT_ID);
        request.put("client_secret", CLIENT_SECRET);
        request.put("username", email);
        request.put("password", password);
        request.put("name", name);
        if (!TextUtils.isEmpty(dob)) request.put("date_of_birth", dob);
        if (gender != null) request.put("gender", Integer.toString(gender.getValue()));
        return postForm(USER_URL, OauthResponse.class, request);
    }

    @SupposeBackground
    public UserErrorResponse modifyUser(
            @Nullable String email,
            @Nullable String password,
            @Nullable String name,
            @Nullable String dob,
            @Nullable User.Gender gender,
            @Nullable Boolean push) throws Exception {
        Map<String, String> request = new HashMap<>();
        if (!TextUtils.isEmpty(email)) request.put("username", email);
        if (!TextUtils.isEmpty(password)) request.put("password", password);
        if (!TextUtils.isEmpty(name)) request.put("name", name);
        if (!TextUtils.isEmpty(dob)) request.put("date_of_birth", dob);
        if (gender != null) request.put("gender", Integer.toString(gender.getValue()));
        if (push != null) request.put("give_push", Integer.toString(push ? 1 : 0));
        UserErrorResponse response = null;
        try {
            response = putFormMultipart(USER_URL, UserErrorResponse.class, request);
        } catch (JsonMappingException e) {
            // this is normal, only returning response if there was an error
        }
        return response;
    }

    @SupposeBackground
    public User getUser() throws Exception {
        return get(USER_URL, User.class);
    }

    @SupposeBackground
    public void logout() throws Exception {
        delete(LOGOUT_URL);
    }

    @SupposeBackground
    public boolean requestNewPassword(@NonNull String email) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("username", email);
        return postForm(FORGOTTEN_PASSWORD_URL, Boolean.class, request);
    }

    @SupposeBackground
    public void sendDeviceToken(@NonNull String deviceToken) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", deviceToken);
        postForm(DEVICE_TOKEN_URL, Void.TYPE, request);
    }

    ///// BRAND

    @SupposeBackground
    public BrandList getBrands() throws Exception {
        return get(BRAND_LIST_URL, BrandList.class);
    }

    ///// TOPIC

    @SupposeBackground
    public TopicList getTopics() throws Exception {
        return get(TOPIC_LIST_URL, TopicList.class);
    }

    ///// ARTICLE

    @SupposeBackground
    public ArticleList getArticles(@Nullable Brand brand, @Nullable Topic topic, @Nullable Date fromTime) throws Exception {
        Map<String, String> args = new HashMap<>();
        if (brand != null) args.put("brand_id", brand.getId().toString());
        if (topic != null) args.put("topic_id", topic.getId().toString());
        if (fromTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_ISO8601, Locale.getDefault());
            args.put("from_time", dateFormat.format(fromTime));
        }
        return get(args.size() == 0 ? ARTICLE_LIST_URL : String.format("%s?%s", ARTICLE_LIST_URL, HttpUtils.buildFormUrlEncoded(args)), ArticleList.class);
    }

    @SupposeBackground
    public FbVideoList getFbVideos() throws Exception {
        return get(FB_VIDEO_ARTICLE_LIST_URL, FbVideoList.class);
    }

    @SupposeBackground
    public ArticleList getMyMagazineArticleList(@Nullable Date fromTime) throws Exception {
        Map<String, String> args = new HashMap<>();
        if (fromTime != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_ISO8601, Locale.getDefault());
            args.put("from_time", dateFormat.format(fromTime));
        }
        return get(args.size() == 0 ? MY_MAGAZINE_URL : String.format("%s?%s", MY_MAGAZINE_URL, HttpUtils.buildFormUrlEncoded(args)), ArticleList.class);
    }

    @SupposeBackground
    public Article getArticle(@NonNull Integer articleId) throws Exception {
        return get(String.format(ARTICLE_URL, articleId), Article.class);
    }

    @SupposeBackground
    public String getArticleHtml(@NonNull Integer articleId) throws Exception {
        return IOUtils.toString(getRaw(String.format(ARTICLE_HTML_URL, articleId), null));
    }

    ///// FAVORITES

    @SupposeBackground
    public FavoriteList getFavorites() throws Exception {
        return get(FAVORITES_URL, FavoriteList.class);
    }

    @SupposeBackground
    public Favorite addToFavorites(@NonNull Integer articleId) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("article_id", articleId.toString());
        return postForm(FAVORITES_URL, Favorite.class, request);
    }

    @SupposeBackground
    public void removeFromFavorites(@NonNull Favorite favorite) throws Exception {
        delete(String.format(DELETE_FAVORITE_URL, favorite.getId()));
    }

    ///// GALLERY

    @SupposeBackground
    public Gallery getGallery(@NonNull Long galleryId) throws Exception {
        return get(String.format(GALLERY_URL, galleryId), Gallery.class);
    }

    ///// MAGAZINE

    @SupposeBackground
    public MagazineList getMagazines(@NonNull Brand brand) throws Exception {
        return get(String.format(MAGAZINE_LIST_URL, brand.getId()), MagazineList.class);
    }

    @SupposeBackground
    public Magazine getMagazine(int magazineId) throws Exception {
        return get(String.format(MAGAZINE_URL, magazineId), Magazine.class);
    }

    @SupposeBackground
    public MagazineContents getMagazineContents(@NonNull Magazine magazine) throws Exception {
        return get(String.format(MAGAZINE_CONTENTS_URL, magazine.getId()), MagazineContents.class);
    }

    ///// IMPRESS

    @SupposeBackground
    public String getImpressHtml() throws Exception {
        return IOUtils.toString(getRaw(IMPRESS_HTML_URL, null));
    }

    ///// FOLLOWS

    @SupposeBackground
    public FollowedList getFollowedList(@Nullable FollowedItem.Type type) throws Exception {
        Map<String, String> request = new HashMap<>();
        if (type != null) request.put("type", type.name().toLowerCase());
        return get(request.size() == 0 ? FOLLOWS_LIST_URL : String.format("%s?%s", FOLLOWS_LIST_URL, HttpUtils.buildFormUrlEncoded(request)), FollowedList.class);
    }

    @SupposeBackground
    public FollowedItem follow(@NonNull FollowedItem.Type type, @NonNull Integer followedId) throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("type", type.name().toLowerCase());
        request.put("followed_id", followedId.toString());
        return postForm(FOLLOWS_LIST_URL, FollowedItem.class, request);
    }

    @SupposeBackground
    public FollowedItem followTopic(@NonNull Topic topic) throws Exception {
        return follow(FollowedItem.Type.TOPIC, topic.getId());
    }

    @SupposeBackground
    public FollowedItem followBrand(@NonNull Brand brand) throws Exception {
        return follow(FollowedItem.Type.BRAND, brand.getId());
    }

    @SupposeBackground
    public void unfollow(@NonNull FollowedItem followedItem) throws Exception {
        delete(String.format(FOLLOWS_URL, followedItem.getId()));
    }

    //
    // helper methods
    // **********************

    public void checkEnvironment() throws OfflineException, GeneralSecurityException {
        if (!mDeviceUtils.isOnline()) {
            Broadcast.postEvent(new PageRequest(PageRequest.Page.Offline));
            throw new OfflineException();
        }
    }

    private static final ThreadLocal<Boolean> mClearCacheFlag = new ThreadLocal<>();

    @SupposeBackground
    public void clearCacheForNextRequestOnThisThread() {
        mClearCacheFlag.set(true);
    }


    @SupposeBackground
    protected <T> T get(String url, Class<T> responseType) throws Exception {
        return get(url, responseType, null);
    }

    @SupposeBackground
    protected <T> T get(String url, Class<T> responseType, Map<String, String> headers) throws Exception {
        return SerializationUtils.deserializeJson(responseType, getRaw(url, headers));
    }

    @SupposeBackground
    protected InputStream getRaw(String url, Map<String, String> headers) throws Exception {
        if (!mDeviceUtils.isOnline() && mPreferences.app.useLocalCache().getOr(true)) {
            if (ObjectUtils.defaultIfNull(mClearCacheFlag.get(), false)) {
                mDataCache.clear(url, headers);
                mClearCacheFlag.set(false);
            }
            InputStream cached = mDataCache.get(url, headers);
            if (cached != null) return cached;
        }

        checkEnvironment();
        setAuthorizationHeader();
        HttpResponse response = mHttpUtils.doGetRequest(new URL(url), headers, null);
        validateResponse(response);
        Utils.isClientVersionSupported(response.getHeaderFields());
        return mDataCache.set(url, headers, response.getResponseStream(), CACHE_DEFAULT_SEC);
    }

    @SupposeBackground
    protected HttpResponse head(String url, Map<String, String> headers) throws Exception {
        checkEnvironment();
        setAuthorizationHeader();
        HttpResponse response = mHttpUtils.doHttpRequest(HttpUtils.Method.Head, new URL(url), null, headers, null);
        validateResponse(response);
        Utils.isClientVersionSupported(response.getHeaderFields());
        return response;
    }

    @SupposeBackground
    protected <T> T postJson(String url, Class<T> responseType, Serializable request) throws Exception {
        checkEnvironment();
        setAuthorizationHeader();
        HttpResponse response;
        if (request == null) {
            response = mHttpUtils.doPostRequest(new URL(url), null, null, null);
        } else {
            String json = SerializationUtils.serializeToJson(request);
            response = mHttpUtils.postJson(new URL(url), null, null, json);
        }
        validateResponse(response);
        Utils.isClientVersionSupported(response.getHeaderFields());
        if (responseType.equals(Void.TYPE)) return null;
        return SerializationUtils.deserializeJson(responseType, response.getResponseStream());
    }

    @SupposeBackground
    protected <T> T postForm(String url, Class<T> responseType, Map<String, String> request) throws Exception {
        checkEnvironment();
        setAuthorizationHeader();
        HttpResponse response;
        if (request == null) {
            response = mHttpUtils.doPostRequest(new URL(url), null, null, null);
        } else {
            response = mHttpUtils.postFormUrlEncoded(new URL(url), null, null, request);
        }
        validateResponse(response);
        Utils.isClientVersionSupported(response.getHeaderFields());
        if (responseType.equals(Void.TYPE)) return null;
        return SerializationUtils.deserializeJson(responseType, response.getResponseStream());
    }

    @SupposeBackground
    protected <T> T putFormMultipart(String url, Class<T> responseType, Map<String, String> request) throws Exception {
        checkEnvironment();
        setAuthorizationHeader();
        HttpResponse response;
        if (request == null) {
            response = mHttpUtils.doPutRequest(new URL(url), null, null);
        } else {
            List<HttpRequestPart> parts = new ArrayList<>();
            for (Map.Entry<String, String> entry : request.entrySet()) {
                parts.add(new HttpRequestPartPlain(entry.getKey(), null, entry.getValue()));
            }
            response = mHttpUtils.putMultipart(new URL(url), null, null, parts);
        }
        validateResponse(response);
        Utils.isClientVersionSupported(response.getHeaderFields());
        if (responseType.equals(Void.TYPE)) return null;
        return SerializationUtils.deserializeJson(responseType, response.getResponseStream());
    }

    @SupposeBackground
    protected void delete(String url) throws Exception {
        checkEnvironment();
        setAuthorizationHeader();
        HttpResponse response;
        response = mHttpUtils.doDeleteRequest(new URL(url), null, null);
        validateResponse(response);
        Utils.isClientVersionSupported(response.getHeaderFields());
    }

    private void validateResponse(HttpResponse response) throws Exception {
        if (response == null) throw new IllegalArgumentException("response null");

        Logger.debug(response.getResponseAsString());

        int responseCode = response.getResponseCode();
        if (responseCode < 100 || responseCode >= 600) {
            throw new IllegalArgumentException("invalid response code: " + response.getResponseCode());
        }

        responseCode /= 100;

        switch (responseCode) {
            case 1: // 1xx info
            case 2: // 2xx success
                return;
            case 3: // 3xx redirect
                throw new BadResponseException("code is not a clear success response", response.getResponseCode());
            case 4:
            case 5:
                try {
                    ErrorResponse errorResponse = SerializationUtils.deserializeJson(ErrorResponse.class, response.getResponseStream());
                    throw new BadResponseException(errorResponse);
                } catch (IOException e) {
                    Logger.debug("cant deserialize error response");
                    throw new BadResponseException("error ", response.getResponseCode());
                }
            default:
                throw new BadResponseException("invalid response code", response.getResponseCode());
        }
    }

    private void setAuthorizationHeader() {
        if (mPreferences.app.authToken().exists()) {
            mHttpUtils.addDefaultHttpHeader(HTTP.AUTHORIZATION,
                    String.format("%s %s", mPreferences.app.authTokenType().get(), mPreferences.app.authToken().get()));
        }
    }
}
