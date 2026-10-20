package com.example.mediaportal.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Episode {

    private Integer number;
    private String title;
    private Integer durationSeconds;

    protected Episode() {
    }

    public Episode(Integer number, String title, Integer durationSeconds) {
        this.number = number;
        this.title = title;
        this.durationSeconds = durationSeconds;
    }

    public Integer getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }
}
