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
  private final JsonArray sample;

  /**
   *  @param ping ping
   * @param name Server version name
   * @param protocol Server protocol version
   * @param playerMax Max Player
   * @param playerOnline Player Online count
   * @param motd server MOTD
   * @param descriptionExtras description extra
   * @param serverIcon Server Icon in Base64 encoding
   * @param sample
   */
  public MCServerPingResponse(int ping, String name, int protocol, int playerMax, int playerOnline, String motd, JsonArray descriptionExtras, String serverIcon, JsonArray sample) {
    this.ping = ping;
    this.version = name;
    this.protocol = protocol;
    this.maxPlayers = playerMax;
    this.onlinePlayers = playerOnline;
    this.motd = motd;
    this.descriptionExtras = descriptionExtras;
    this.serverIcon = serverIcon;
    this.sample = sample;
  }

  public static MCServerPingResponse serverPingFromJsonObj(JsonObject jsonObj) {
    return new MCServerPingResponse(
            jsonObj.get("ping").getAsInt(),
            jsonObj.get("version").getAsJsonObject().get("name").getAsString(),
            jsonObj.get("version").getAsJsonObject().get("protocol").getAsInt(),
            jsonObj.get("players").getAsJsonObject().get("max").getAsInt(),
            jsonObj.get("players").getAsJsonObject().get("online").getAsInt(),
            jsonObj.get("description").getAsJsonObject().get("text").getAsString(),
            (jsonObj.get("description").getAsJsonObject().get("extra") == null) ? null : jsonObj.get("description").getAsJsonObject().get("extra").getAsJsonArray(),
            (jsonObj.get("favicon") == null ? null : jsonObj.get("favicon").getAsString()),
            jsonObj.get("players").getAsJsonObject().get("sample") == null ? null : jsonObj.get("players").getAsJsonObject().get("sample").getAsJsonArray());
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

  public int getPlayerMax() {
    return maxPlayers;
  }

  public int getPlayerOnline() {
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

  public JsonArray getSample() {
    return sample;
  }

  public JsonObject getAsJsonObject() {
    return JsonParser.parseString(getAsJsonString()).getAsJsonObject();
  }
}
