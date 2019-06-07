package dev.yekllurt.mojangapi;

public enum Status {

    GREEN("green"),
    YELLOW("yellow"),
    RED("red"),
    UNKNOWN("unknown");

    private String status;

    Status(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public static Status getByMojangStatus(String status) {
        for (Status s : values()) {
            if (s.getStatus().equals(status)) {
                return s;
            }
        }
        return Status.UNKNOWN;
    }

}
