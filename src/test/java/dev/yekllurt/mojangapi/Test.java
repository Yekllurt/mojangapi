package dev.yekllurt.mojangapi;

import dev.yekllurt.mojangapi.*;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Test {

    public static void main(String[] args) {

        Logger.getGlobal().info("##### API Status #####");
        MojangAPIStatus mojangAPIStatus = MojangAPI.getAPIStatus();
        Map<String, MojangAPIStatus.Status> apis = mojangAPIStatus.getAPIs();
        apis.keySet().forEach(key -> {
            Logger.getGlobal().info(key + ": " + apis.get(key).toString());
        });

        Logger.getGlobal().info("##### UUID #####");
        Logger.getGlobal().info("UUID from Yekllurt at 1559830084: " + MojangAPI.getUUIDAt("Yekllurt", 1559830084).toString());
        Logger.getGlobal().info("UUID from Yekllurt: " + MojangAPI.getUUID("Yekllurt").toString());

        Logger.getGlobal().info("##### Name History #####");
        NameHistory nameHistory = MojangAPI.getNameHistory(UUID.fromString("03929abf-feef-49ba-b313-3aac0d18bac2"));
        Logger.getGlobal().info("Initial name: " + nameHistory.getInitialName());
        nameHistory.getNameChanges().forEach(nameChange -> {
            Logger.getGlobal().info("Name change at " + nameChange.getTimestamp() + " to " + nameChange.getName());
        });
        Logger.getGlobal().info("Current name: " + nameHistory.getCurrentName());

        Logger.getGlobal().info("##### Player profile #####");
        PlayerProfile playerProfile = MojangAPI.getPlayerProfile(UUID.fromString("03929abf-feef-49ba-b313-3aac0d18bac2"));
        Logger.getGlobal().info("Name: " + playerProfile.getName());
        Logger.getGlobal().info("Base64 Signature: " + playerProfile.getBase64Signature());
        Logger.getGlobal().info("UUID: " + playerProfile.getId().toString());
        Texture texture = playerProfile.getTexture();
        Logger.getGlobal().info("Profile id: " + texture.getProfileId().toString());
        Logger.getGlobal().info("Profile name: " + texture.getProfileName());
        Logger.getGlobal().info("Timestamp: " + texture.getTimestamp());
        Logger.getGlobal().info("Skin: " + texture.getSkin());
        Logger.getGlobal().info("Has cape: " + texture.hasCape());
        Logger.getGlobal().info("Cape: " + texture.getCape());

    }

}
