# MCServerPing

## About

MCServerPing is an API for you to obtain basic information for a Minecraft server.

## Supported Inputs

- IP
- A record
- SRV record

## Example

```java
import dev.redcoke.mcserverping.MCServerPing;

public class MCServerPingDemo() {
  public static void main(String[] args) {
    var hypixelPingResponse = MCServerPing.getPing("mc.hypixel.net", 25565);
    System.out.println(hypixelPingResponse.getAsJsonString());
  }
}
```
