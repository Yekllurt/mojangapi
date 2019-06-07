package dev.yekllurt.mojangapi;

import java.util.Map;
import java.util.Objects;

public class MojangAPIStatus {

    private final Map<Service, Status> servicesStatus;

    public MojangAPIStatus(Map<Service, Status> servicesStatus) {
        Objects.requireNonNull(servicesStatus);
        this.servicesStatus = servicesStatus;
    }

    public Status getServiceStatus(Service service) {
        Objects.requireNonNull(service);
        return this.servicesStatus.get(service);
    }

    public Map<Service, Status> getServicesStatus() {
        return servicesStatus;
    }
}
