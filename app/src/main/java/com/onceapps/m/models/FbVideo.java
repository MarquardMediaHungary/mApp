package com.onceapps.m.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "video_id",
        "video_url",
        "video_deeplink",
        "title",
        "lead",
        "date",
        "image_url",
        "topics",
        "brands"
})
public class FbVideo implements Serializable, Comparable {

    @JsonProperty("video_id")
    private Long videoId;
    @JsonProperty("video_url")
    private String videoUrl;
    @JsonProperty("video_deeplink")
    private String videoDeeplink;
    @JsonProperty("title")
    private String title;
    @JsonProperty("lead")
    private String lead;
    @JsonProperty("date")
    private Date date;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("topics")
    private List<Topic> topics = new ArrayList<Topic>();
    @JsonProperty("brands")
    private List<Brand> brands = new ArrayList<Brand>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The videoId
     */
    @JsonProperty("video_id")
    public Long getVideoId() {
        return videoId;
    }

    /**
     *
     * @param videoId
     * The video_id
     */
    @JsonProperty("video_id")
    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    /**
     *
     * @return
     * The videoUrl
     */
    @JsonProperty("video_url")
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     *
     * @param videoUrl
     * The video_url
     */
    @JsonProperty("video_url")
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @JsonProperty("video_deeplink")
    public String getVideoDeeplink() {
        return videoDeeplink;
    }

    @JsonProperty("video_deeplink")
    public void setVideoDeeplink(String videoDeeplink) {
        this.videoDeeplink = videoDeeplink;
    }

    /**
     *
     * @return
     * The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The lead
     */
    @JsonProperty("lead")
    public String getLead() {
        return lead;
    }

    /**
     *
     * @param lead
     * The lead
     */
    @JsonProperty("lead")
    public void setLead(String lead) {
        this.lead = lead;
    }

    /**
     *
     * @return
     * The date
     */
    @JsonProperty("date")
    public Date getDate() {
        return date;
    }

    /**
     *
     * @param date
     * The date
     */
    @JsonProperty("date")
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     *
     * @return
     * The imageUrl
     */
    @JsonProperty("image_url")
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     *
     * @param imageUrl
     * The image_url
     */
    @JsonProperty("image_url")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     *
     * @return
     * The topics
     */
    @JsonProperty("topics")
    public List<Topic> getTopics() {
        return topics;
    }

    /**
     *
     * @param topics
     * The topics
     */
    @JsonProperty("topics")
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    /**
     *
     * @return
     * The brands
     */
    @JsonProperty("brands")
    public List<Brand> getBrands() {
        return brands;
    }

    /**
     *
     * @param brands
     * The brands
     */
    @JsonProperty("brands")
    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FbVideo && ((FbVideo) o).getVideoId().equals(getVideoId());
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof FbVideo) {
            FbVideo otherArticle = (FbVideo) another;
            return -1 * this.getDate().compareTo(otherArticle.getDate()); //-1 so it will give descending order
        }
        return 0;
    }
}