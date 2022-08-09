package dev.redcoke.mcserverping;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class MCServerPingResponse {
  private final int ping;
  private final String version;
  private final int protocol;
  private final int maxPlayers;
  private final int onlinePlayers;
  private final String motd;
  private final JsonArray descriptionExtras;
  private final String serverIcon;

  public MCServerPingResponse(int ping, String name, int protocol, Integer playerMax, Integer playerOnline, String motd, JsonArray descriptionExtras, String serverIcon) {
    this.ping = ping;
    this.version = name;
    this.protocol = protocol;
    this.maxPlayers = playerMax;
    this.onlinePlayers = playerOnline;
    this.motd = motd;
    this.descriptionExtras = descriptionExtras;
    this.serverIcon = serverIcon;
  }

  public static MCServerPingResponse serverPingFromJsonObj(JsonObject jsonObj) {
    int serverPing = jsonObj.get("ping").getAsInt();
    String versionName = jsonObj.get("version").getAsString();
    int serverProtocol = jsonObj.get("protocol").getAsInt();
    Integer playerMax = null;
    Integer playerOnline = null;
    if (jsonObj.has("players")) { // Players object is optional somehow
      playerMax = jsonObj.get("players").getAsJsonObject().get("max").getAsInt();
      playerOnline = jsonObj.get("players").getAsJsonObject().get("online").getAsInt();
    }
    String serverMOTD = jsonObj.get("description").getAsJsonObject().get("text").getAsString();
    JsonArray serverDescriptionExtra = (jsonObj.get("description").getAsJsonObject().get("extra") == null) ? null : jsonObj.get("description").getAsJsonObject().get("extra").getAsJsonArray();
    String favIcon = jsonObj.get("favicon").getAsString();
    return new MCServerPingResponse(
            serverPing, versionName, serverProtocol, playerMax, playerOnline, serverMOTD, serverDescriptionExtra, favIcon
    );
  }

  public int getPing() {
    return ping;
  }

  public String getName() {
    return version;
  }

  public int getProtocol() {
    return protocol;
  }

  public Integer getPlayerMax() {
    return maxPlayers;
  }

  public Integer getPlayerOnline() {
    return onlinePlayers;
  }

  public String getMotd() {
    return motd;
  }

  public JsonArray getDescriptionExtras() {
    return descriptionExtras;
  }

  public String getServerIcon() {
    return serverIcon;
  }

  public String getAsJsonString() {
    return new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(this);
  }

  public JsonObject getAsJsonObject() {
    return JsonParser.parseString(getAsJsonString()).getAsJsonObject();
  }
}
