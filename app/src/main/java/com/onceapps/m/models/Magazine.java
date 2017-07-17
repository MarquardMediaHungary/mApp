package com.onceapps.m.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Generated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "id",
        "name",
        "cover_url",
        "release_date",
        "generated_at",
        "pdf_in_package",
        "pdf_password",
        "package_size",
        "package_hash",
        "package_url",
        "brand"
})
public class Magazine implements Serializable {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("cover_url")
    private String coverUrl;
    @JsonProperty("release_date")
    private Date releaseDate;
    @JsonProperty("generated_at")
    private Date generatedAt;
    @JsonProperty("pdf_in_package")
    private Boolean pdfInPackage;
    @JsonProperty("pdf_password")
    private String pdfPassword;
    @JsonProperty("package_size")
    private Long packageSize;
    @JsonProperty("package_hash")
    private String packageHash;
    @JsonProperty("package_url")
    private String packageUrl;
    @JsonProperty("brand")
    private Brand brand;
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
     * @return The coverUrl
     */
    @JsonProperty("cover_url")
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * @param coverUrl The cover_url
     */
    @JsonProperty("cover_url")
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * @return The releaseDate
     */
    @JsonProperty("release_date")
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param releaseDate The release_date
     */
    @JsonProperty("release_date")
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * @return The generatedAt
     */
    @JsonProperty("generated_at")
    public Date getGeneratedAt() {
        return generatedAt;
    }

    /**
     * @param generatedAt The generated_at
     */
    @JsonProperty("generated_at")
    public void setGeneratedAt(Date generatedAt) {
        this.generatedAt = generatedAt;
    }

    /**
     * @return The packageSize
     */
    @JsonProperty("package_size")
    public Long getPackageSize() {
        return packageSize;
    }

    /**
     * @param packageSize The package_size
     */
    @JsonProperty("package_size")
    public void setPackageSize(Long packageSize) {
        this.packageSize = packageSize;
    }

    /**
     * @return The packageHash
     */
    @JsonProperty("package_hash")
    public String getPackageHash() {
        return packageHash;
    }

    /**
     * @param packageHash The package_hash
     */
    @JsonProperty("package_hash")
    public void setPackageHash(String packageHash) {
        this.packageHash = packageHash;
    }

    /**
     * @return The packageUrl
     */
    @JsonProperty("package_url")
    public String getPackageUrl() {
        return packageUrl;
    }

    /**
     * @param packageUrl The package_url
     */
    @JsonProperty("package_url")
    public void setPackageUrl(String packageUrl) {
        this.packageUrl = packageUrl;
    }

    /**
     * @return The brand
     */
    @JsonProperty("brand")
    public Brand getBrand() {
        return brand;
    }

    /**
     * @param brand The brand
     */
    @JsonProperty("brand")
    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Boolean getPdfInPackage() {
        return pdfInPackage;
    }

    public void setPdfInPackage(Boolean pdfInPackage) {
        this.pdfInPackage = pdfInPackage;
    }

    public String getPdfPassword() {
        return pdfPassword;
    }

    public void setPdfPassword(String pdfPassword) {
        this.pdfPassword = pdfPassword;
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
        return o instanceof Magazine && ((Magazine) o).getId().equals(id);
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "id=%d name=%s", id, name);
    }
}