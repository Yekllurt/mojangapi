package dev.yekllurt.mojangapi;

import java.util.Objects;

public enum Service {

    MINECRAT_NET("minecraft.net"),
    SESSION_MINECRAFT_NET("session.minecraft.net"),
    ACCOUNT_MOJANG_COM("account.mojang.com"),
    AUTHENTIFICATION_MOJANG_COM("auth.mojang.com"),
    SKINS_MINECRAFT_NET("skins.minecraft.net"),
    AUTHENTIFICATION_SERVER_MOJANG_COM("authserver.mojang.com"),
    SESSIONSERVER_MOJANG_COM("sessionserver.mojang.com"),
    API_MOJANG_COM("api.mojang.com"),
    TEXTURES_MINECRAFT_NET("textures.minecraft.net"),
    MOJANG_COM("mojang.com"),
    UNKNOWN("unknown");

    private String service;

    Service(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public static Service getByService(String serivce) {
        Objects.requireNonNull(serivce);
        for (Service s : values()) {
            if (s.getService().equals(serivce)) {
                return s;
            }
        }
        return Service.UNKNOWN;
    }

}
