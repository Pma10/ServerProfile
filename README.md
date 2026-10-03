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

- Minecraft 1.21.11
- Fabric Loader 0.19.5+
- Fabric API
- Java 21

## Build

Use Gradle 9.7.1 or newer:

```bash
gradle build
```

The built JAR is written to `build/libs`.

## License

MIT
