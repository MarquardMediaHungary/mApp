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
        "id",
        "title",
        "lead",
        "date",
        "is_print",
        "topics",
        "brands",
        "issue",
        "updated_at",
        "file_url_small"
})
public class Article implements Serializable, Comparable {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("title")
    private String title;
    @JsonProperty("lead")
    private String lead;
    @JsonProperty("date")
    private Date date;
    @JsonProperty("is_print")
    private Boolean isPrint;
    @JsonProperty("topics")
    private List<Topic> topics = new ArrayList<Topic>();
    @JsonProperty("brands")
    private List<Brand> brands = new ArrayList<Brand>();
    @JsonProperty("issue")
    private Object issue;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("file_url_small")
    private String fileUrlSmall;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The lead
     */
    @JsonProperty("lead")
    public String getLead() {
        return lead;
    }

    /**
     * @param lead The lead
     */
    @JsonProperty("lead")
    public void setLead(String lead) {
        this.lead = lead;
    }

    /**
     * @return The date
     */
    @JsonProperty("date")
    public Date getDate() {
        return date;
    }

    /**
     * @param date The date
     */
    @JsonProperty("date")
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return The isPrint
     */
    @JsonProperty("is_print")
    public Boolean getIsPrint() {
        return isPrint;
    }

    /**
     * @param isPrint The is_print
     */
    @JsonProperty("is_print")
    public void setIsPrint(Boolean isPrint) {
        this.isPrint = isPrint;
    }

    /**
     * @return The topics
     */
    @JsonProperty("topics")
    public List<Topic> getTopics() {
        return topics;
    }

    /**
     * @param topics The topics
     */
    @JsonProperty("topics")
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    /**
     * @return The brands
     */
    @JsonProperty("brands")
    public List<Brand> getBrands() {
        return brands;
    }

    /**
     * @param brands The brands
     */
    @JsonProperty("brands")
    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    /**
     * @return The issue
     */
    @JsonProperty("issue")
    public Object getIssue() {
        return issue;
    }

    /**
     * @param issue The issue
     */
    @JsonProperty("issue")
    public void setIssue(Object issue) {
        this.issue = issue;
    }

    /**
     * @return The updatedAt
     */
    @JsonProperty("updated_at")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt The updated_at
     */
    @JsonProperty("updated_at")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return The fileUrlSmall
     */
    @JsonProperty("file_url_small")
    public String getFileUrlSmall() {
        return fileUrlSmall;
    }

    /**
     * @param fileUrlSmall The file_url_small
     */
    @JsonProperty("file_url_small")
    public void setFileUrlSmall(String fileUrlSmall) {
        this.fileUrlSmall = fileUrlSmall;
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
        return o instanceof Article && ((Article) o).getId().equals(getId());
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof Article) {
            Article otherArticle = (Article) another;
            return -1 * this.getDate().compareTo(otherArticle.getDate()); //-1 so it will give descending order
        }
        return 0;
    }
}