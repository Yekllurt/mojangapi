package dev.yekllurt.mojangapi;

import java.util.Map;
import java.util.Objects;

public class MojangAPIStatus {

    public enum Status {
        GREEN, YELLOW, RED, UNKNOWN;
    }

    private final Map<String, Status> apis;

    public MojangAPIStatus(Map<String, Status> apis) {
        Objects.requireNonNull(apis);
        this.apis = apis;
    }

    public Status getAPIStatus(String api) {
        Objects.requireNonNull(api);
        return this.apis.get(api);
    }

    public Map<String, Status> getAPIs() {
        return this.apis;
    }

}
