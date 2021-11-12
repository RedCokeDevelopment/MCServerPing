package dev.redcoke.mcserverping;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class MCServerPingDemoTest {
  @Test
  void test() {
    var server = "localhost";
    try {
      MCServerPing.getPing(server, 25565);
    } catch (IOException | TimeoutException ex) {
      fail(ex);
    }
  }
}
