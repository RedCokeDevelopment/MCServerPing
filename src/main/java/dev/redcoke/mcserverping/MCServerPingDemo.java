package dev.redcoke.mcserverping;

import com.google.gson.Gson;

import java.io.IOException;

public class MCServerPingDemo {
    public static void main(String[] args) throws IOException {
        System.out.println(new Gson().newBuilder().setPrettyPrinting().create().toJson(MCServerPing.getPing("[ip/domain]", 25545)));
    }
}
