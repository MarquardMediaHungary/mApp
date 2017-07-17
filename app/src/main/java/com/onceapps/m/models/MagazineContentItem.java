package com.onceapps.m.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "title",
        "lead",
        "html_page",
        "pdf_page",
        "file_url_small",
        "topics"
})
public class MagazineContentItem implements Serializable {

    @JsonProperty("title")
    private String title;
    @JsonProperty("lead")
    private String lead;
    @JsonProperty("html_page")
    private Integer htmlPage;
    @JsonProperty("pdf_page")
    private Integer pdfPage;
    @JsonProperty("file_url_small")
    private String fileUrlSmall;
    @JsonProperty("topics")
    private List<Topic> topics = new ArrayList<Topic>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * The fileUrlSmall
     */
    @JsonProperty("file_url_small")
    public String getFileUrlSmall() {
        return fileUrlSmall;
    }

    /**
     *
     * @param fileUrlSmall
     * The file_url_small
     */
    @JsonProperty("file_url_small")
    public void setFileUrlSmall(String fileUrlSmall) {
        this.fileUrlSmall = fileUrlSmall;
    }

    public Integer getHtmlPage() {
        return htmlPage == null ? new Integer(0) : htmlPage;
    }

    public void setHtmlPage(Integer htmlPage) {
        this.htmlPage = htmlPage;
    }

    public Integer getPdfPage() {
        return pdfPage == null ? new Integer(0) : pdfPage;
    }

    public void setPdfPage(Integer pdfPage) {
        this.pdfPage = pdfPage;
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
        return "MagazineContentItem{" +
                "fileUrlSmall='" + fileUrlSmall + '\'' +
                ", pdfPage='" + pdfPage + '\'' +
                ", htmlPage='" + htmlPage + '\'' +
                ", lead='" + lead + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
