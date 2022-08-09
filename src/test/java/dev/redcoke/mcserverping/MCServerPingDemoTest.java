package dev.redcoke.mcserverping;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

class MCServerPingDemoTest {
  @Test
  void test() {
    List<String> servers = List.of("hypixel.net", "2b2t.org", "play.cubecraft.net", "play.potwmc.com");
    for (var server : servers) {
      try {
        MCServerPing.getPing(server, 25565).getAsJsonString();
      } catch (IOException | TimeoutException ex) {
        Assertions.fail(ex);
      }
    }
  }
}
