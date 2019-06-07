package dev.yekllurt.mojangapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MojangAPI {

    private static final String API_STATUS_URL = "https://status.mojang.com/check";
    private static final String UUID_AT_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
    private static final String NAME_HISTORY_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final String PLAYERPROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final JsonParser JSON_PARSER = new JsonParser();

    public static void getServiceStatus(Service service, Consumer<Status> callback) {
        getServicesStatus((mojangAPIStatus) -> callback.accept(mojangAPIStatus.getServiceStatus(service)));
    }

    public static Status getServiceStatus(Service service) {
        return getServicesStatus().getServiceStatus(service);
    }

    public static void getServicesStatus(Consumer<MojangAPIStatus> callback) {
        EXECUTOR_SERVICE.execute(() -> callback.accept(getServicesStatus()));
    }

    public static MojangAPIStatus getServicesStatus() {
        try {
            URL url = new URL(API_STATUS_URL);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);
            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonArray() == false) {
                return null;
            }
            Map<Service, Status> servicesStatus = new HashMap<>();
            JsonArray jsonArray = result.getAsJsonArray();
            jsonArray.forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject.keySet().forEach(key -> {
                    servicesStatus.put(Service.getByService(key), Status.getByMojangStatus(jsonObject.get(key).getAsString()));
                });
            });
            return new MojangAPIStatus(servicesStatus);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getUUIDAt(String name, long timestamp, Consumer<UUID> callback) {
        EXECUTOR_SERVICE.execute(() -> callback.accept(getUUIDAt(name, timestamp)));
    }

    public static UUID getUUIDAt(String name, long timestamp) {
        try {
            URL url = new URL(String.format(UUID_AT_URL, name, timestamp));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);
            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonObject() == false) {
                return null;
            }
            JsonObject jsonObject = result.getAsJsonObject();
            return UUID.fromString(toValidUUIDFormat(jsonObject.get("id").getAsString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getUUID(String name, Consumer<UUID> callback) {
        EXECUTOR_SERVICE.execute(() -> callback.accept(getUUID(name)));
    }

    public static UUID getUUID(String name) {
        return getUUIDAt(name, System.currentTimeMillis());
    }

    public static void getNameHistory(UUID uuid, Consumer<NameHistory> callback) {
        EXECUTOR_SERVICE.execute(() -> callback.accept(getNameHistory(uuid)));
    }

    public static NameHistory getNameHistory(UUID uuid) {
        try {
            URL url = new URL(String.format(NAME_HISTORY_URL, toMojangAPIFormat(uuid)));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);
            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonArray() == false) {
                return null;
            }
            String initialName = null;
            List<NameChange> nameChanges = new ArrayList<>();
            Iterator<JsonElement> iterator = result.getAsJsonArray().iterator();
            while (iterator.hasNext()) {
                JsonObject jsonObject = iterator.next().getAsJsonObject();
                if (jsonObject.get("changedToAt") == null) {
                    initialName = jsonObject.get("name").getAsString();
                } else {
                    nameChanges.add(new NameChange(jsonObject.get("name").getAsString(), jsonObject.get("changedToAt").getAsLong()));
                }
            }
            return new NameHistory(initialName, nameChanges);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void getPlayerProfile(UUID uuid, Consumer<PlayerProfile> callback) {
        EXECUTOR_SERVICE.execute(() -> callback.accept(getPlayerProfile(uuid)));
    }

    public static PlayerProfile getPlayerProfile(UUID uuid) {
        try {
            URL url = new URL(String.format(PLAYERPROFILE_URL, uuid.toString().replaceAll("-", "")));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);

            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonObject() == false) {
                return null;
            }
            JsonObject raw = result.getAsJsonObject();
            UUID id = UUID.fromString(toValidUUIDFormat(raw.get("id").getAsString()));
            String name = raw.get("name").getAsString();
            Iterator<JsonElement> properties = raw.get("properties").getAsJsonArray().iterator();
            while (properties.hasNext()) {
                JsonObject jsonObject = properties.next().getAsJsonObject();
                if (jsonObject.get("name") != null && jsonObject.get("name").getAsString().equals("textures")) {
                    String signature = jsonObject.get("signature").getAsString();
                    return new PlayerProfile(id, name, toTexture(jsonObject.get("value").getAsString()), signature);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String toMojangAPIFormat(UUID uuid) {
        return uuid.toString().replaceAll("-", "");
    }

    private static String toValidUUIDFormat(String incompleteUUID) {
        return new StringBuffer(incompleteUUID)
                .insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-").toString();
    }

    private static Texture toTexture(String base64) {
        JsonElement jsonElement = JSON_PARSER.parse(new String(DatatypeConverter.parseBase64Binary(base64)));
        if (jsonElement.isJsonObject() == false) {
            return null;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        long timestamp = jsonObject.get("timestamp").getAsLong();
        UUID profileId = UUID.fromString(toValidUUIDFormat(jsonObject.get("profileId").getAsString()));
        String profileName = jsonObject.get("profileName").getAsString();
        JsonObject textures = jsonObject.get("textures").getAsJsonObject();
        String skin = textures.get("SKIN").getAsJsonObject().get("url").getAsString();
        if (textures.get("CAPE") != null) {
            String cape = textures.get("CAPE").getAsJsonObject().get("url").getAsString();
            return new Texture(timestamp, profileId, profileName, skin, cape);
        } else {
            return new Texture(timestamp, profileId, profileName, skin);
        }
    }
}
