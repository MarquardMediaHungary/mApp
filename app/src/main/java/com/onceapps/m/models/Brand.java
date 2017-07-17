package com.onceapps.m.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "id",
        "name",
        "print",
        "online",
        "image",
        "logo",
        "topics"
})
public class Brand implements Serializable {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("print")
    private Boolean print;
    @JsonProperty("online")
    private Boolean online;
    @JsonProperty("image")
    private String image;
    @JsonProperty("logo")
    private Logo logo;
    @JsonProperty("topics")
    private TopicList topics;
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
     * @return The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The print
     */
    @JsonProperty("print")
    public Boolean getPrint() {
        return print;
    }

    /**
     * @param print The print
     */
    @JsonProperty("print")
    public void setPrint(Boolean print) {
        this.print = print;
    }

    /**
     * @return The online
     */
    @JsonProperty("online")
    public Boolean getOnline() {
        return online;
    }

    /**
     * @param online The online
     */
    @JsonProperty("online")
    public void setOnline(Boolean online) {
        this.online = online;
    }

    /**
     * @return The image
     */
    @JsonProperty("image")
    public String getImage() {
        return image;
    }

    /**
     * @param image The image
     */
    @JsonProperty("image")
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * @return The logo
     */
    @JsonProperty("logo")
    public Logo getLogo() {
        return logo;
    }

    /**
     * @param logo The logo
     */
    @JsonProperty("logo")
    public void setLogo(Logo logo) {
        this.logo = logo;
    }

    /**
     * @return The topic list
     */
    @JsonProperty("topics")
    public TopicList getTopics() {
        return topics;
    }

    /**
     * @param logo The topics
     */
    @JsonProperty("topics")
    public void setTopics(TopicList topics) {
        this.topics = topics;
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
        return o instanceof Brand && ((Brand) o).getId().equals(getId());
    }
}
