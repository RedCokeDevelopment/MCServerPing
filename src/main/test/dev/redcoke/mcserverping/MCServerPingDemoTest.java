package dev.redcoke.mcserverping;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MCServerPingDemoTest {
    @Test
    public void test() throws IOException {
        String server = "mc.hypixel.net";
        MCServerPingResponse mcServerPingResponse = MCServerPing.getPing(server, 25545);
        assertNotNull(mcServerPingResponse.getAsJsonString());

    }
}