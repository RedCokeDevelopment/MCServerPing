package dev.redcoke.mcserverping;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class ServerPingResponse {
    private final int ping;
    private final String version;
    private final int protocol;
    @Nullable private final Integer maxPlayers;
    @Nullable private final Integer onlinePlayers;
    private final String motd;
    private final JsonArray descriptionExtras;
    private final String serverIcon;

    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static ServerPingResponse serverPingFromJsonObj(JsonObject jsonObj) {
        var serverPing = jsonObj.get("ping").getAsInt();
        String versionName;
        int serverProtocol;
        if (jsonObj.get("version").getAsJsonObject().has("name")) { // 1.19+ format
            versionName = jsonObj.get("version").getAsJsonObject().get("name").getAsString();
            serverProtocol = jsonObj.get("version").getAsJsonObject().get("protocol").getAsInt();
        } else { // legacy SLP format (pre 1.19.4)
            versionName = jsonObj.get("version").getAsString();
            serverProtocol = jsonObj.get("protocol").getAsInt();
        }
        Integer playerMax = null;
        Integer playerOnline = null;
        if (jsonObj.has("players")) { // Players object is optional somehow
            playerMax = jsonObj.get("players").getAsJsonObject().get("max").getAsInt();
            playerOnline = jsonObj.get("players").getAsJsonObject().get("online").getAsInt();
        }
        var serverMOTD = jsonObj.get("description").getAsJsonObject().get("text").getAsString();
        var serverDescriptionExtra = (jsonObj.get("description").getAsJsonObject().get("extra") == null) ? null : jsonObj.get("description").getAsJsonObject().get("extra").getAsJsonArray();
        var favIcon = jsonObj.get("favicon").getAsString();
        return new ServerPingResponse(
                serverPing, versionName, serverProtocol, playerMax, playerOnline, serverMOTD, serverDescriptionExtra, favIcon
        );
    }

    @CheckReturnValue
    @SuppressWarnings("unused")
    public String getAsJsonString() {
        return gson.toJson(this);
    }

    @CheckReturnValue
    @SuppressWarnings("unused")
    public JsonObject getAsJsonObject() {
        return JsonParser.parseString(getAsJsonString()).getAsJsonObject();
    }
}
