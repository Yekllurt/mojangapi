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

    private static final long CACHE_DURATION_MOJANG_SERVICE_STATUS = 1 * 60 * 1000;
    private static final long CACHE_DURATION_NAME_CONVERSION = 5 * 60 * 1000;
    private static final long CACHE_DURATION_NAME_HISTORY = 5 * 60 * 1000;
    private static final long CACHE_DURATION_PLAYER_PROFILE = 5 * 60 * 1000;

    private static final String API_STATUS_URL = "https://status.mojang.com/check";
    private static final String UUID_AT_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
    private static final String NAME_HISTORY_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final String PLAYERPROFILE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final JsonParser JSON_PARSER = new JsonParser();

    private static MojangServiceStatus CACHE_MOJANG_SERVICE_STATUS = null;
    private static Long CACHE_MOJANG_SERVICE_STATUS_TIMEDATE = 0L;
    private static final Map<String, UUID> CACHE_NAME_UUID_CONVERSION = new HashMap<>();
    private static final Map<String, Long> CACHE_NAME_UUID_CONVERSION_FETCH_TIMEDATE = new HashMap<>();
    private static final Map<UUID, NameHistory> CACHE_NAME_HISTORY = new HashMap<>();
    private static final Map<UUID, Long> CACHE_NAME_HISTORY_FETCH_TIMEDATE = new HashMap<>();
    private static final Map<UUID, PlayerProfile> CACHE_PLAYER_PROFILE = new HashMap<>();
    private static final Map<UUID, Long> CACHE_PLAYER_PROFILE_FETCH_TIMEDATE = new HashMap<>();

    /**
     * Clear the cached data
     */
    public static final void clearCache() {
        CACHE_MOJANG_SERVICE_STATUS = null;
        CACHE_MOJANG_SERVICE_STATUS_TIMEDATE = null;
        CACHE_NAME_UUID_CONVERSION.clear();
        CACHE_NAME_UUID_CONVERSION_FETCH_TIMEDATE.clear();
        CACHE_NAME_HISTORY.clear();
        CACHE_NAME_HISTORY_FETCH_TIMEDATE.clear();
        CACHE_PLAYER_PROFILE.clear();
        CACHE_PLAYER_PROFILE_FETCH_TIMEDATE.clear();
    }

    /**
     * Fetch the status of a specific Mojang service.
     * Possible values are green (no issues), yellow (some issues), red (service unavailable).
     * This method is asynchronous.
     *
     * @param service  The requested service status {@link Service}
     * @param callback The method that is called as soon as the task is completed.
     * @return {@link Status}
     */
    public static final void getServiceStatus(Service service, Consumer<Status> callback) {
        Objects.requireNonNull(service);
        Objects.requireNonNull(callback);
        getServicesStatus((mojangServiceStatus) -> callback.accept(mojangServiceStatus.getServiceStatus(service)));
    }

    /**
     * Fetch the status of a specific Mojang service.
     * Possible values are green (no issues), yellow (some issues), red (service unavailable).
     *
     * @param service The requested service status {@link Service}
     * @return {@link Status}
     */
    public static final Status getServiceStatus(Service service) {
        Objects.requireNonNull(service);
        return getServicesStatus().getServiceStatus(service);
    }

    /**
     * Fetch the status of various Mojang services.
     * Possible values are green (no issues), yellow (some issues), red (service unavailable).
     * This method is asynchronous.
     *
     * @param callback The method that is called as soon as the task is completed.
     * @return {@link MojangServiceStatus}
     */
    public static final void getServicesStatus(Consumer<MojangServiceStatus> callback) {
        EXECUTOR_SERVICE.execute(() -> callback.accept(getServicesStatus()));
    }

    /**
     * Fetch the status of various Mojang services.
     * Possible values are green (no issues), yellow (some issues), red (service unavailable).
     *
     * @return {@link MojangServiceStatus}
     */
    public static final MojangServiceStatus getServicesStatus() {
        if (isCacheDataValid(CACHE_MOJANG_SERVICE_STATUS_TIMEDATE, CACHE_DURATION_MOJANG_SERVICE_STATUS)) {
            return CACHE_MOJANG_SERVICE_STATUS;
        }
        try {
            URL url = new URL(API_STATUS_URL);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);

            if (httpsURLConnection.getResponseCode() != 200) return null;

            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonArray() == false) return null;
            Map<Service, Status> servicesStatus = new HashMap<>();
            JsonArray jsonArray = result.getAsJsonArray();
            jsonArray.forEach(jsonElement -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                jsonObject.keySet().forEach(key -> {
                    servicesStatus.put(Service.getByService(key), Status.getByMojangStatus(jsonObject.get(key).getAsString()));
                });
            });
            CACHE_MOJANG_SERVICE_STATUS = new MojangServiceStatus(servicesStatus);
            CACHE_MOJANG_SERVICE_STATUS_TIMEDATE = System.currentTimeMillis();
            return CACHE_MOJANG_SERVICE_STATUS;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the UUID associated with a name at a given time.
     * This method is asynchronous.
     *
     * @param name      The player's name {@link String}
     * @param timestamp The give time {@link Long}
     * @param callback  The method that is called as soon as the task is completed.
     */
    public static final void getUUIDAt(String name, long timestamp, Consumer<UUID> callback) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getUUIDAt(name, timestamp)));
    }

    /**
     * Returns the UUID associated with a name at a given time.
     *
     * @param name      The player's name {@link String}
     * @param timestamp The given time {@link Long}
     * @return
     */
    public static final UUID getUUIDAt(String name, long timestamp) {
        Objects.requireNonNull(name);
        if (isCacheDataValid(CACHE_NAME_UUID_CONVERSION_FETCH_TIMEDATE.getOrDefault(name, 0L), CACHE_DURATION_NAME_CONVERSION)) {
            return CACHE_NAME_UUID_CONVERSION.get(name);
        }
        try {
            URL url = new URL(String.format(UUID_AT_URL, name, timestamp));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);

            if (httpsURLConnection.getResponseCode() != 200) return null;

            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonObject() == false) return null;
            JsonObject jsonObject = result.getAsJsonObject();
            CACHE_NAME_UUID_CONVERSION.put(name, UUID.fromString(toValidUUIDFormat(jsonObject.get("id").getAsString())));
            CACHE_NAME_UUID_CONVERSION_FETCH_TIMEDATE.put(name, System.currentTimeMillis());
            return CACHE_NAME_UUID_CONVERSION.get(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the current UUID associated with a name.
     * This method is asynchronous.
     *
     * @param name     The player's name {@link String}
     * @param callback The method that is called as soon as the task is completed.
     */
    public static final void getUUID(String name, Consumer<UUID> callback) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getUUID(name)));
    }

    /**
     * Returns the current UUID associated with a name.
     *
     * @param name The player's name {@link String}
     * @return
     */
    public static final UUID getUUID(String name) {
        Objects.requireNonNull(name);
        return getUUIDAt(name, System.currentTimeMillis());
    }

    /**
     * Returns all the usernames this user has used in the past and the one they are using currently.
     * This method is asynchronous.
     *
     * @param uuid     The player's UUID {@link UUID}
     * @param callback The method that is called as soon as the task is completed.
     */
    public static final void getNameHistory(UUID uuid, Consumer<NameHistory> callback) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getNameHistory(uuid)));
    }

    /**
     * Returns all the usernames this user has used in the past and the one they are using currently.
     *
     * @param uuid The player's UUID {@link UUID}
     * @return
     */
    public static final NameHistory getNameHistory(UUID uuid) {
        Objects.requireNonNull(uuid);
        if (isCacheDataValid(CACHE_NAME_HISTORY_FETCH_TIMEDATE.getOrDefault(uuid, 0L), CACHE_DURATION_NAME_HISTORY)) {
            return CACHE_NAME_HISTORY.get(uuid);
        }
        try {
            URL url = new URL(String.format(NAME_HISTORY_URL, toMojangAPIFormat(uuid)));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);

            if (httpsURLConnection.getResponseCode() != 200) return null;

            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonArray() == false) return null;
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
            CACHE_NAME_HISTORY.put(uuid, new NameHistory(initialName, nameChanges));
            CACHE_NAME_HISTORY_FETCH_TIMEDATE.put(uuid, System.currentTimeMillis());
            return CACHE_NAME_HISTORY.get(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the player's username plus any additional information about them (e.g. skins).
     * This method is asynchronous.
     *
     * @param uuid     The player's UUID {@link UUID}
     * @param callback The method that is called as soon as the task is completed.
     */
    public static final void getPlayerProfile(UUID uuid, Consumer<PlayerProfile> callback) {
        Objects.requireNonNull(uuid);
        Objects.requireNonNull(callback);
        EXECUTOR_SERVICE.execute(() -> callback.accept(getPlayerProfile(uuid)));
    }

    /**
     * Returns the player's username plus any additional information about them (e.g. skins).
     *
     * @param uuid The player's UUID {@link UUID}
     * @return
     */
    public static final PlayerProfile getPlayerProfile(UUID uuid) {
        Objects.requireNonNull(uuid);
        if (isCacheDataValid(CACHE_PLAYER_PROFILE_FETCH_TIMEDATE.getOrDefault(uuid, 0L), CACHE_DURATION_PLAYER_PROFILE)) {
            return CACHE_PLAYER_PROFILE.get(uuid);
        }
        try {
            URL url = new URL(String.format(PLAYERPROFILE_URL, uuid.toString().replaceAll("-", "")));
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setConnectTimeout(5000);

            if (httpsURLConnection.getResponseCode() != 200) return null;

            JsonElement result = JSON_PARSER.parse(new InputStreamReader(httpsURLConnection.getInputStream()));
            if (result.isJsonObject() == false) return null;
            JsonObject raw = result.getAsJsonObject();
            UUID id = UUID.fromString(toValidUUIDFormat(raw.get("id").getAsString()));
            String name = raw.get("name").getAsString();
            Iterator<JsonElement> properties = raw.get("properties").getAsJsonArray().iterator();
            while (properties.hasNext()) {
                JsonObject jsonObject = properties.next().getAsJsonObject();
                if (jsonObject.get("name") != null && jsonObject.get("name").getAsString().equals("textures")) {
                    String signature = jsonObject.get("signature").getAsString();
                    CACHE_PLAYER_PROFILE.put(uuid, new PlayerProfile(id, name, toTexture(jsonObject.get("value").getAsString()), signature));
                    CACHE_PLAYER_PROFILE_FETCH_TIMEDATE.put(uuid, System.currentTimeMillis());
                    return CACHE_PLAYER_PROFILE.get(uuid);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String toMojangAPIFormat(UUID uuid) {
        Objects.requireNonNull(uuid);
        return uuid.toString().replaceAll("-", "");
    }

    private static final String toValidUUIDFormat(String incompleteUUID) {
        Objects.requireNonNull(incompleteUUID);
        return new StringBuffer(incompleteUUID)
                .insert(8, "-")
                .insert(13, "-")
                .insert(18, "-")
                .insert(23, "-").toString();
    }

    private static final Texture toTexture(String base64) {
        Objects.requireNonNull(base64);
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

    private static final boolean isCacheDataValid(long cacheInsertTimeDate, long cacheDuration) {
        return System.currentTimeMillis() - cacheInsertTimeDate <= cacheDuration;
    }

}
