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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import dev.redcoke.mcserverping.utils.TextComponentFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

/**
 * API for pinging and obtaining info about a Minecraft server.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCServerPing {

  /**
   * Pings a Minecraft server to obtain info with the custom timeout parameter.
   *
   * @param address The address to ping
   * @param timeout The timeout for the socket
   *
   * @return The ping response.
   *
   * @throws IOException For any i/o exception during the data exchange.
   * @throws TimeoutException If the socket connection times out.
   */
  @Contract("null, _ -> fail")
  public static @NotNull ServerPingResponse getPingWithTimeout(final String address, final int timeout) throws IOException, TimeoutException {
    return getPingWithTimeout(address, 25565, timeout);
  }

  /**
   * Pings a Minecraft Server to obtain server info.
   *
   * @param address server address
   *
   * @return MCServerPingResponse
   *
   * @throws IOException failed to resolve hostname
   */
  @Contract("null -> fail")
  @SuppressWarnings("unused")
  public static @NotNull ServerPingResponse getPing(final String address) throws IOException, TimeoutException {
    return getPingWithTimeout(address, 5000);
  }

  /**
   * Pings a Minecraft Server to obtain server info.
   *
   * @param address server address
   * @param port    server port
   *
   * @return MCServerPingResponse server info
   *
   * @throws IOException failed to resolve hostname
   * @throws TimeoutException when the server does not respond within 3 seconds
   *
   * @see ServerPingResponse
   */
  @Contract("null, _ -> fail")
  public static @NotNull ServerPingResponse getPing(final String address, final int port) throws IOException, TimeoutException {
    return getPingWithTimeout(address, port, 5000);
  }

  /**
   * Pings a Minecraft Server to obtain server info with a custom timeout parameter.
   *
   * @param address server address
   * @param port    server port
   * @param timeout The timeout for the socket
   *
   * @return MCServerPingResponse server info
   *
   * @throws IOException failed to resolve hostname
   * @throws TimeoutException when the server does not respond within 3 seconds
   *
   * @see ServerPingResponse
   */
  public static ServerPingResponse getPingWithTimeout(final String address, final int port, final int timeout)
          throws IOException, TimeoutException {

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

      socket.connect(new InetSocketAddress(serverHost, serverPort), timeout);
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

//      // Ping ->
//      out.writeByte(0x09); // Packet Size
//      out.writeByte(0x01); // Ping Packet
//      out.writeLong(System.currentTimeMillis());
//
//      // Ping <-
//      readVarInt(in);
//      id = readVarInt(in);
//      io(id == -1, "Server ended data stream unexpectedly.");
//      io(id != 0x01, "Server returned invalid packet"); // Check Ping Packet

    }

    var jsonObj = JsonParser.parseString(json).getAsJsonObject();
    var descriptionJsonElement = jsonObj.get("description");

    if (descriptionJsonElement.isJsonObject()) {
      // TextComponent MOTDs

      var descriptionJsonObject = descriptionJsonElement.getAsJsonObject();

      if (descriptionJsonObject.has("extra")) {
        descriptionJsonObject.add("raw", descriptionJsonObject.get("extra").getAsJsonArray().getAsJsonArray());
        descriptionJsonObject.addProperty("text", TextComponentFormatter.toLegacyText(descriptionJsonObject.get("extra").getAsJsonArray()));
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

    return ServerPingResponse.serverPingFromJsonObj(jsonObj);
  }


  /**
   * Throws IOException when condition is false.
   *
   * @param b Condition
   * @param m Exception cause
   *
   * @throws IOException Exception
   */
  public static void io(final boolean b, final String m) throws IOException {
    if (b) {
      throw new IOException(m);
    }
  }

  /**
   * Reads a VarInt from a DataInputStream.
   *
   * @param in DataInputStream
   *
   * @return int
   *
   * @throws IOException Failed to read VarInt or invalid VarInt
   * @throws TimeoutException Timed out
   */
  public static int readVarInt(DataInputStream in) throws IOException, TimeoutException {
    int i = 0;
    int j = 0;

    while (true) {
      AtomicInteger k = new AtomicInteger();

      var executor = Executors.newSingleThreadExecutor();
      var future = executor.submit(() -> {
        try {
          k.set(in.readByte());
        } catch (IOException e) {
          k.set(Integer.MAX_VALUE);
        }
      });

      try {
        future.get(3, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        future.cancel(true);
        throw e;
      } catch (ExecutionException | InterruptedException e) {
        throw new IOException(e);
      } finally {
        executor.shutdownNow();
      }

      if (k.get() == Integer.MAX_VALUE) throw new IOException();

      i |= (k.get() & 0x7F) << j++ * 7;

      if (j > 5) {
        throw new IOException("VarInt too big");
      }

      if ((k.get() & 0x80) != 128) {
        executor.close();
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
