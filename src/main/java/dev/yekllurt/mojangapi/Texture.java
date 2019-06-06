package dev.yekllurt.mojangapi;

import java.util.UUID;

public class Texture {

    private long timestamp;
    private UUID profileId;
    private String profileName;

    private String skin;
    private boolean hasCape;
    private String cape;

    public Texture(long timestamp, UUID profileId, String profileName, String skin) {
        this.timestamp = timestamp;
        this.profileId = profileId;
        this.profileName = profileName;
        this.skin = skin;
        this.hasCape = false;
        this.cape = null;
    }

    public Texture(long timestamp, UUID profileId, String profileName, String skin, String cape) {
        this.timestamp = timestamp;
        this.profileId = profileId;
        this.profileName = profileName;
        this.skin = skin;
        this.hasCape = true;
        this.cape = cape;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public UUID getProfileId() {
        return this.profileId;
    }

    public String getProfileName() {
        return this.profileName;
    }

    public String getSkin() {
        return this.skin;
    }

    public boolean hasCape() {
        return this.hasCape;
    }

    public String getCape() {
        return this.cape;
    }

}
