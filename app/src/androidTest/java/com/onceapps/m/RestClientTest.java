package com.onceapps.m;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import com.onceapps.m.api.OauthResponse;
import com.onceapps.m.api.RestClient;
import com.onceapps.m.api.RestClient_;
import com.onceapps.m.models.Article;
import com.onceapps.m.models.ArticleList;
import com.onceapps.m.models.Brand;
import com.onceapps.m.models.BrandList;
import com.onceapps.m.models.Gallery;
import com.onceapps.m.models.GalleryImage;
import com.onceapps.m.models.Magazine;
import com.onceapps.m.models.MagazineContentItem;
import com.onceapps.m.models.MagazineContents;
import com.onceapps.m.models.MagazineList;
import com.onceapps.m.models.Topic;
import com.onceapps.m.models.TopicList;
import com.onceapps.m.utils.Preferences;
import com.onceapps.m.utils.Preferences_;

import org.junit.Assert;

import java.util.concurrent.TimeUnit;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class RestClientTest extends ApplicationTestCase<Application> {
    public RestClientTest() {
        super(Application.class);
    }

    private RestClient mRestClient;
    private Preferences mPreferences;

    private static final String TEST_GRANT_TYPE = "client_credentials";
    private static final String TEST_CLIENT_ID = "secret";
    private static final String TEST_CLIENT_SECRET = "secret";

    private static final Long TEST_GALLERY_ID = 4530L;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRestClient = RestClient_.getInstance_(getContext());
        mPreferences = Preferences_.getInstance_(getContext());

        checkLoggedIn(mPreferences, mRestClient);
    }

    public static void checkLoggedIn(Preferences preferences, RestClient restClient) throws Exception {
        if (!preferences.app.authToken().exists()) {
            OauthResponse response = restClient.getAnonymousOauthToken(TEST_GRANT_TYPE, TEST_CLIENT_ID, TEST_CLIENT_SECRET);
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getTokenType(), "Bearer");
            Assert.assertTrue(response.getExpiresIn() >= TimeUnit.DAYS.toSeconds(365));

            preferences.app.edit()
                    .authToken().put(response.getAccessToken())
                    .authTokenType().put(response.getTokenType())
                    .apply();
        }
    }

    @LargeTest
    public void testBrandList() throws Exception {
        BrandList response = mRestClient.getBrands();
        Assert.assertNotNull(response);
        Assert.assertTrue(response.size() > 0);
        for (Brand brand : response) {
            Assert.assertNotNull(brand);
            Assert.assertNotNull(brand.getId());
            Assert.assertNotNull(brand.getName());
            Assert.assertNotNull(brand.getPrint());
            Assert.assertNotNull(brand.getOnline());
            Assert.assertNotNull(brand.getImage());
            Assert.assertNotNull(brand.getLogo());
            Assert.assertNotNull(brand.getTopics());
            Assert.assertNotNull(brand.getTopics());
            Assert.assertTrue(brand.getTopics().size() > 0);
        }
    }

    @LargeTest
    public void testArticleListWithoutFilter() throws Exception {
        ArticleList response = mRestClient.getArticles(null, null, null);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.size() > 0);
        for (Article article : response) {
            Assert.assertNotNull(article);
            Assert.assertNotNull(article.getId());
            Assert.assertNotNull(article.getTitle());
            Assert.assertNotNull(article.getLead());
            Assert.assertNotNull(article.getDate());
            Assert.assertNotNull(article.getIsPrint());
            Assert.assertNotNull(article.getUpdatedAt());
            Assert.assertNotNull(article.getFileUrlSmall());
        }
    }

    @LargeTest
    public void testArticleListWithBrandFilter() throws Exception {
        BrandList brands = mRestClient.getBrands();
        for (Brand brand : brands) {
            ArticleList response = mRestClient.getArticles(brand, null, null);
            Assert.assertNotNull(response);
            Assert.assertTrue(response.size() > 0);
            for (Article article : response) {
                Assert.assertNotNull(article);
                Assert.assertTrue(article.getBrands().contains(brand));
            }
        }
    }

    @LargeTest
    public void testGetArticleDetails() throws Exception {
        ArticleList response = mRestClient.getArticles(null, null, null);
        Assert.assertTrue(response.size() > 0);
        for (Article base : response) {

            Article article = mRestClient.getArticle(base.getId());
            Assert.assertNotNull(article);
            Assert.assertNotNull(article.getId());
            Assert.assertNotNull(article.getTitle());
            Assert.assertNotNull(article.getLead());
            Assert.assertNotNull(article.getDate());
            Assert.assertNotNull(article.getIsPrint());
        }
    }

    @LargeTest
    public void testGetArticleHtml() throws Exception {
        ArticleList response = mRestClient.getArticles(null, null, null);
        Assert.assertTrue(response.size() > 0);
        for (Article base : response) {

            String html = mRestClient.getArticleHtml(base.getId());
            Assert.assertNotNull(html);
            Assert.assertTrue(html.length() > 10);
        }
    }

    @LargeTest
    public void testGetGallery() throws Exception {
        Gallery gallery = mRestClient.getGallery(TEST_GALLERY_ID);
        Assert.assertTrue(gallery != null);
        Assert.assertTrue(gallery.getId() != null);
        Assert.assertTrue(gallery.getTitle() != null);
        Assert.assertTrue(gallery.getImages() != null);
        for (GalleryImage image : gallery.getImages()) {
            Assert.assertTrue(image.getUrl() != null);
        }
    }

    @LargeTest
    public void testGetMagazines() throws Exception {
        BrandList response = mRestClient.getBrands();
        Assert.assertNotNull(response);
        if (response.size() > 0) {
            for (Brand brand : response) {
                MagazineList magazineList = mRestClient.getMagazines(brand);
                Assert.assertNotNull(magazineList);
            }
        }
    }

    @LargeTest
    public void testGetTopics() throws Exception {
        TopicList response = mRestClient.getTopics();
        Assert.assertNotNull(response);
        if (response.size() > 0) {
            for (Topic item : response) {
                Assert.assertNotNull(item.getId());
                Assert.assertNotNull(item.getName());
                Assert.assertNotNull(item.getImage());
                Assert.assertNotNull(item.getColor());
            }
        }
    }

    @LargeTest
    public void testMagazineContents() throws Exception {
        BrandList brands = mRestClient.getBrands();
        Assert.assertNotNull(brands);
        if(brands.size() > 0) {
            for(Brand brand : brands) {
                MagazineList magazineList = mRestClient.getMagazines(brand);
                Assert.assertNotNull(magazineList);
                if (magazineList.size() > 0) {
                    for (Magazine magazine : magazineList) {
                        MagazineContents magazineContents = mRestClient.getMagazineContents(magazine);
                        Assert.assertNotNull(magazineContents);
                        if(magazineContents.size() > 0)
                        for(MagazineContentItem contentItem : magazineContents) {
                            Assert.assertNotNull(contentItem.getTitle());
                            Assert.assertNotNull(contentItem.getLead());
                            Assert.assertNotNull(contentItem.getHtmlPage());
                            Assert.assertNotNull(contentItem.getPdfPage());
                            Assert.assertNotNull(contentItem.getFileUrlSmall());
                        }
                    }
                }
            }
        }
    }
}