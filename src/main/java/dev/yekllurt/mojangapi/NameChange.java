package dev.yekllurt.mojangapi;

import java.util.Objects;

public class NameChange {

    private String name;
    private long timestamp;

    public NameChange(String name, long timestamp) {
        Objects.requireNonNull(name);
        this.name = name;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getName() {
        return this.name;
    }
}
