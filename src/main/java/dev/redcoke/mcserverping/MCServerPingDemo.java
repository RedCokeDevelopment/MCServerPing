package dev.redcoke.mcserverping;


import java.io.IOException;

public class MCServerPingDemo {
    public static void main(String[] args) throws IOException {
        String server = "redcoke.dev";
        System.out.println(MCServerPing.getPing(server, 25545)); // MCServerPingResponse Object
        System.out.println(MCServerPing.getPing(server, 25545).getAsJsonString()); // Formatted Json String
        System.out.println(MCServerPing.getPing(server, 25545).getAsJsonObject()); // Json Object
    }
}
