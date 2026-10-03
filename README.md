# Server Profiles

A client-side Fabric mod that automatically applies Minecraft settings per server.

## Features

- Save the current client settings as a profile for the server you are connected to.
- Automatically apply matching profiles on join.
- Restore your previous settings when you disconnect.
- Recover the previous settings after an unexpected client shutdown.
- Exact server matches and wildcard patterns such as `*.example.com`.
- In-game configuration screen opened with the **O** key by default.
- No server-side installation required.

### Managed settings

- FOV
- Mouse sensitivity
- Render distance
- Simulation distance
- Particles
- GUI scale
- View bobbing
- Master volume

Profiles are stored in `config/serverprofiles.json`.

## Requirements

### Running the mod

- Minecraft 1.21.11
- Fabric Loader 0.19.5+
- Fabric API
- Java 21+

### Building from source

- JDK 25+
- Gradle 9.7.1+

The project still compiles the mod for Java 21; JDK 25 is only required by the current Loom build tooling.

```bash
gradle build
```

The built JAR is written to `build/libs`.

## License

MIT
