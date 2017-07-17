package com.onceapps.m.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by balage on 20/04/16.
 * Immutable status info (download, uncompress, etc.) for magazines
 */
public class MagazineStatus implements Serializable {

    public enum DownloadStatus {
        NOT_DOWNLOADED          (0),
        DOWNLOAD_IN_PROGRESS    (1),
        DOWNLOAD_FINISHED       (2),
        DOWNLOAD_CANCELLED      (3),
        DOWNLOAD_FAILED         (4);

        private int value;
        private DownloadStatus(int value){
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private final Magazine magazine;
    private final int percent;
    private final DownloadStatus downloadStatus;

    @JsonCreator
    public MagazineStatus(
            @JsonProperty("magazine")
            Magazine magazine,
            @JsonProperty("percent")
            int percent,
            @JsonProperty("downloadStatus")
            DownloadStatus downloadStatus) {
        this.magazine = magazine;
        this.percent = percent;
        this.downloadStatus = downloadStatus;
    }

    public Magazine getMagazine() {
        return magazine;
    }

    public int getPercent() {
        return percent;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MagazineStatus && ((MagazineStatus) o).magazine == magazine;
    }

    public static class Builder {
        private Magazine magazine;
        private int percent;
        private DownloadStatus downloadStatus;

        public Builder() {
        }

        public Builder(MagazineStatus status) {
            this.magazine = status.magazine;
            this.percent = status.percent;
            this.downloadStatus = status.downloadStatus;
        }

        public Builder setMagazine(Magazine magazine) {
            this.magazine = magazine;
            return this;
        }

        public Builder setPercent(int percent) {
            this.percent = percent;
            return this;
        }

        public Builder setDownloadStatus(DownloadStatus downloadStatus) {
            this.downloadStatus = downloadStatus;
            return this;
        }

        public MagazineStatus build() {
            return new MagazineStatus(magazine, percent, downloadStatus);
        }
    }

    @Override
    public String toString() {
        return "MagazineStatus{" +
                "magazine=" + magazine +
                ", percent=" + percent +
                ", downloadStatus=" + downloadStatus +
                '}';
    }
}
