# mojangapi
A simple Mojang API Wrapper to access the Mojang API and view Mojang services status.

## Usage
### Mojang Service Status
Get the status of one specific service
```
Logger.getGlobal().info("API service: " + MojangAPI.getServiceStatus(Service.API_MOJANG_COM).toString());
```

Get all services with their status
```
MojangServiceStatus mojangServiceStatus = MojangAPI.getServicesStatus();
Map<Service, Status> servicesStatus = mojangServiceStatus.getServicesStatus();
servicesStatus.keySet().forEach(key -> {
    Logger.getGlobal().info(key.toString() + ": " + servicesStatus.get(key).toString());
});
```
### Name to UUID
Get the UUID from a player
```
Logger.getGlobal().info("UUID from Yekllurt: " + MojangAPI.getUUID("Yekllurt").toString());
```

Get the UUID from a player at a certain time
```
Logger.getGlobal().info("UUID from Yekllurt at 1559830084: " + MojangAPI.getUUIDAt("Yekllurt", 1559830084).toString());
```

### Name History
Get the name changing of a player
```
NameHistory nameHistory = MojangAPI.getNameHistory(UUID.fromString("03929abf-feef-49ba-b313-3aac0d18bac2"));
Logger.getGlobal().info("Initial name: " + nameHistory.getInitialName());
nameHistory.getNameChanges().forEach(nameChange -> {
    Logger.getGlobal().info("Name change at " + nameChange.getTimestamp() + " to " + nameChange.getName());
});
Logger.getGlobal().info("Current name: " + nameHistory.getCurrentName());
```

### Player Profile
Get the player profile of a player
```
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
```

## Built With
* [GSON](https://github.com/google/gson) - JSON Object handling

## License 
This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/Yekllurt/mojangapi/blob/master/LICENSE) file for details
