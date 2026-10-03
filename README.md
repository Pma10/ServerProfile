# Server Profiles

A client-side Fabric mod that automatically switches Minecraft settings for each server.

## What it does

Press **O** in-game to open Server Profiles. The current server address is filled in automatically.

1. Adjust Minecraft settings the way you want for that server.
2. Open Server Profiles and press **Save current**.
3. The next time you join, the profile is applied automatically.
4. When you leave, your previous settings can be restored automatically.

No server-side installation is required.

## Profile patterns

Profiles can target an exact server or a group of servers.

- `play.example.com` — one server
- `play.example.com:25566` — one custom-port endpoint
- `*.example.com` — every subdomain, including custom ports
- `singleplayer` — integrated singleplayer worlds
- `*` — every multiplayer server

The default Minecraft port `:25565` is normalized away, so `play.example.com` and `play.example.com:25565` share the same profile.

An exact disabled profile takes precedence over a wildcard profile, which can be used as an exclusion.

## Managed settings

Server Profiles currently stores:

- FOV
- Mouse sensitivity
- Render distance
- Simulation distance
- Particle amount
- GUI scale
- View bobbing
- Master volume
- Entity distance
- Maximum FPS
- VSync
- FOV effect scale
- Brightness

Profiles created with v0.1 remain compatible. Settings introduced in v0.2 are left untouched until that profile is saved again.

## Profile manager

The in-game profile manager supports:

- Creating and overwriting profiles from the current settings
- Wildcard patterns directly from the GUI
- Browsing all saved profiles
- Enabling and disabling individual profiles
- Applying a saved profile manually
- Selecting an existing profile for editing
- Two-step deletion confirmation
- English and Korean UI

Profiles are stored in `config/serverprofiles.json`.

## Recovery

With **Restore on exit** enabled, Server Profiles keeps a temporary recovery snapshot before applying a profile. It restores the previous settings when you disconnect.

If Minecraft closes unexpectedly before the restore can happen, the recovery snapshot is applied on the next launch.

## Requirements

### Running the mod

- Minecraft 1.21.11
- Fabric Loader 0.19.5+
- Fabric API
- Java 21+

### Building from source

- JDK 25+
- Gradle 9.7.1+

The mod itself is compiled for Java 21. JDK 25 is only required by the current Loom build tooling.

```bash
gradle build
```

The built JAR is written to `build/libs`. GitHub Actions also uploads the remapped JAR as a workflow artifact.

## License

MIT
