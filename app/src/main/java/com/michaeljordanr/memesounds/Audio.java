package com.michaeljordanr.memesounds;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Audio implements Serializable {
    @Expose
    private int id;

    @Expose
    @SerializedName("audio_name")
    private String audioName;

    @Expose
    @SerializedName("audio_description")
    private String audioDescription;

    @Expose
    @SerializedName("audio_thumb")
    private String audioThumb;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAudioName() {
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public String getAudioDescription() {
        return audioDescription;
    }

    public void setAudioDescription(String audioDescription) {
        this.audioDescription = audioDescription;
    }

    public String getAudioThumb() {
        return audioThumb;
    }

    public void setAudioThumb(String audioThumb) {
        this.audioThumb = audioThumb;
    }
}
