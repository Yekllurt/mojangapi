package dev.yekllurt.mojangapi;

import java.util.Objects;
import java.util.UUID;

public class PlayerProfile {

    private UUID id;
    private String name;
    private Texture texture;
    private String base64Signature;

    public PlayerProfile(UUID id, String name, Texture texture, String base64Signature) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(texture);
        Objects.requireNonNull(base64Signature);
        this.id = id;
        this.name = name;
        this.texture = texture;
        this.base64Signature = base64Signature;
    }

    public String getName() {
        return this.name;
    }

    public String getBase64Signature() {
        return this.base64Signature;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public UUID getId() {
        return this.id;
    }
}
