package com.onceapps.m.models;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.onceapps.core.util.Logger;
import com.onceapps.core.util.SerializationUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "fb_id",
        "username",
        "name",
        "date_of_birth",
        "gender",
        "give_push"
})
public class User implements Serializable {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", new Locale("hu", "HU"));

    public enum Gender {
        UNKNOWN(0),
        FEMALE(1),
        MALE(2);

        private final int value;

        Gender(int value) {
            this.value = value;
        }

        public static Gender fromValue(int value) {

            for (Gender gender : values()) {
                if (gender.value == value) {
                    return gender;
                }
            }

            return getDefault();
        }

        public static Gender getDefault() {
            return FEMALE;
        }

        public int getValue() {
            return value;
        }
    }

    @JsonProperty("id")
    private Long id;
    @JsonProperty("fb_id")
    private String fbId;
    @JsonProperty("username")
    private String username;
    @JsonProperty("name")
    private String name;
    @JsonProperty("date_of_birth")
    private String dateOfBirth;
    @JsonProperty("gender")
    private Gender gender;
    @JsonProperty("give_push")
    private Boolean give_push;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

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
     * @return The fbId
     */
    @JsonProperty("fb_id")
    public String getFbId() {
        return fbId;
    }

    /**
     * @param fbId The fb_id
     */
    @JsonProperty("fb_id")
    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    /**
     * @return The username
     */
    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username
     */
    @JsonProperty("username")
    public void setUsername(String username) {
        this.username = username;
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
     * @return The dateOfBirth
     */
    @JsonProperty("date_of_birth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth The date_of_birth
     */
    @JsonProperty("date_of_birth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return The gender
     */
    @JsonProperty("gender")
    public Gender getGender() {
        return gender;
    }

    /**
     * @param gender The gender
     */
    @JsonProperty("gender")
    public void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * @return The give_push
     */
    @JsonProperty("give_push")
    public Boolean getGivePush() {
        return give_push;
    }

    /**
     * @param give_push The give_push
     */
    @JsonProperty("give_push")
    public void setGivePush(Boolean give_push) {
        this.give_push = give_push;
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
            Logger.error(e, "Error serializing user");
        }
        return "";
    }

    public static User fromJsonString(String jsonString) {
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                return SerializationUtils.deserializeJson(User.class, jsonString);
            } catch (IOException e) {
                Logger.error(e, "Error deserializing user");
            }
        }

        return null;
    }
}
