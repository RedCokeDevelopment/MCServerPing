package dev.redcoke.mcserverping;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

/**
 * API for pinging and obtaining info about a Minecraft server.
 */
public final class MCServerPing {

  private MCServerPing() {
  }


  /**
   * Pings a Minecraft Server to obtain server info.
   *
   * @param address server address
   * @return MCServerPingResponse
   * @throws IOException
   */
  public static MCServerPingResponse getPing(final String address) throws IOException {
    return getPing(address, 25565);
  }

  /**
   * Pings a Minecraft Server to obtain server info.
   *
   * @param address server address
   * @param port    server port
   * @return MCServerPingResponse
   * @throws IOException
   */
  public static MCServerPingResponse getPing(final String address, final int port)
          throws IOException {

    if (address == null) {
      throw new IOException("Hostname cannot be null!");
    }

    var serverHost = address;
    var serverPort = port;

    var srvRecords = new Lookup(String.format("_minecraft._tcp.%s", address), Type.SRV).run();

    if (srvRecords != null) {
      for (var srvRecord : srvRecords) {
        var srv = (SRVRecord) srvRecord;


        serverHost = srv.getTarget().toString().replaceFirst("\\.$", "");
        serverPort = srv.getPort();

      }
    }

    String json;

    var ping = System.currentTimeMillis();

    try (var socket = new Socket()) {

      socket.connect(new InetSocketAddress(serverHost, serverPort), 5000);
      ping = System.currentTimeMillis() - ping;

      var handshakeStream = new ByteArrayOutputStream();
      var handshake = new DataOutputStream(handshakeStream);

      handshake.write(0x00); // Handshake Packet
      writeVarInt(handshake, 4); // Protocol Version
      writeVarInt(handshake, address.length());
      handshake.writeBytes(address);
      handshake.writeShort(port);
      writeVarInt(handshake, 1); // Status Handshake

      var out = new DataOutputStream(socket.getOutputStream());
      writeVarInt(out, handshakeStream.size());
      out.write(handshakeStream.toByteArray());

      // STATUS REQUEST ->
      out.writeByte(0x01); // Packet Size
      out.writeByte(0x00); // Packet Status Request

      // <- STATUS RESPONSE
      var in = new DataInputStream(socket.getInputStream());
      readVarInt(in);
      var id = readVarInt(in);

      io(id == -1, "Server ended data stream unexpectedly.");
      io(id != 0x00, "Server returned invalid packet.");

      var length = readVarInt(in);
      io(length == -1, "Server ended data stream unexpectedly.");
      io(length == 0, "Server returned unexpected value.");

      var data = new byte[length];
      in.readFully(data);
      json = new String(data, StandardCharsets.UTF_8);

      // Ping ->
      out.writeByte(0x09); // Packet Size
      out.writeByte(0x01); // Ping Packet
      out.writeLong(System.currentTimeMillis());

      // Ping <-
      readVarInt(in);
      id = readVarInt(in);
      io(id == -1, "Server ended data stream unexpectedly.");
      io(id != 0x01, "Server returned invalid packet"); // Check Ping Packet

    }

    var jsonObj = JsonParser.parseString(json).getAsJsonObject();
    var descriptionJsonElement = jsonObj.get("description");

    if (descriptionJsonElement.isJsonObject()) {
      // TextComponent MOTDs

      var descriptionJsonObject = descriptionJsonElement.getAsJsonObject();

      if (descriptionJsonObject.has("extra")) {
        descriptionJsonObject.addProperty("text",
                new TextComponent(ComponentSerializer.parse(
                        descriptionJsonObject
                                .get("extra")
                                .getAsJsonArray()
                                .toString()
                )).toLegacyText()
        );
        jsonObj.add("description", descriptionJsonObject);
      }

    } else {
      // String MOTDs

      var description = descriptionJsonElement.getAsString();
      var descriptionJsonObj = new JsonObject();
      descriptionJsonObj.addProperty("text", description);
      jsonObj.add("description", descriptionJsonObj);

    }

    jsonObj.addProperty("ping", ping);

    return MCServerPingResponse.serverPingFromJsonObj(jsonObj);
  }


  /**
   * Throws IOException when condition is false.
   *
   * @param b Condition
   * @param m Exception cause
   * @throws IOException Exception
   */
  public static void io(final boolean b, final String m) throws IOException {
    if (b) {
      throw new IOException(m);
    }
  }

  public static int readVarInt(DataInputStream in) throws IOException {
    int i = 0;
    int j = 0;
    while (true) {
      int k = in.readByte();

      i |= (k & 0x7F) << j++ * 7;

      if (j > 5) {
        throw new IOException("VarInt too big");
      }

      if ((k & 0x80) != 128) {
        break;
      }
    }

    return i;
  }

  public static void writeVarInt(DataOutputStream out, int inputParamInt) throws IOException {
    var paramInt = inputParamInt;
    while (true) {
      if ((paramInt & 0xFFFFFF80) == 0) {
        out.writeByte(paramInt);
        return;
      }

      out.writeByte(paramInt & 0x7F | 0x80);
      paramInt >>>= 7;
    }
  }


}
