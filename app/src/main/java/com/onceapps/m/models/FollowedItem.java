package com.onceapps.m.models;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "id",
        "type",
        "followed_id"
})
public class FollowedItem implements Serializable {

    public enum Type {
        BRAND,
        TOPIC;

        @JsonCreator
        public static Type fromString(String key) {
            for (Type type : Type.values()) {
                if (type.name().equalsIgnoreCase(key)) {
                    return type;
                }
            }
            return null;
        }
    }

    @JsonProperty("id")
    private Long id;
    @JsonProperty("type")
    private Type type;
    @JsonProperty("followed_id")
    private Integer followedId;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    private String name = "";

    /**
     * @return The id
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return The type
     */
    @JsonProperty("type")
    public Type getType() {
        return type;
    }

    /**
     * @param type The type
     */
    @JsonProperty("type")
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return The followedId
     */
    @JsonProperty("followed_id")
    public Integer getFollowedId() {
        return followedId;
    }

    /**
     * @param followedId The followed_id
     */
    @JsonProperty("followed_id")
    public void setFollowedId(Integer followedId) {
        this.followedId = followedId;
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
    public String toString() {
        try {
            return SerializationUtils.serializeToJson(this);
        } catch (Exception e) {
            Logger.error(e, "Error serializing followedItem");
            throw new IllegalStateException("un-serializable object", e);
        }
    }

    public static FollowedItem fromJsonString(String jsonString) {
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return SerializationUtils.deserializeJson(FollowedItem.class, jsonString);
            } catch (IOException e) {
                Logger.error(e, "Error deserializing followedItem");
            }
        }

        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

