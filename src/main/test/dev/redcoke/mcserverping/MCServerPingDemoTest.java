package dev.redcoke.mcserverping;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MCServerPingDemoTest {
  @Test
  void test() {
    var server = "mc.hypixel.net";
    try {
      MCServerPing.getPing(server, 25565);
    } catch (IOException ex) {
      fail(ex);
    }
  }
}
