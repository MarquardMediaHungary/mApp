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
        "white",
        "black",
        "croppped"
})
public class Logo implements Serializable {
    
    @JsonProperty("white")
    private String white;
    @JsonProperty("black")
    private String black;
    @JsonProperty("croppped")
    private Logo cropped;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The white
     */
    @JsonProperty("white")
    public String getWhite() {
        return white;
    }

    /**
     * @param white The white
     */
    @JsonProperty("white")
    public void setWhite(String white) {
        this.white = white;
    }

    /**
     * @return The black
     */
    @JsonProperty("black")
    public String getBlack() {
        return black;
    }

    /**
     * @param black The black
     */
    @JsonProperty("black")
    public void setBlack(String black) {
        this.black = black;
    }

    @JsonProperty("croppped")
    public Logo getCropped() {
        return cropped;
    }

    @JsonProperty("croppped")
    public void setCropped(Logo cropped) {
        this.cropped = cropped;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String white, Object value) {
        this.additionalProperties.put(white, value);
    }

}