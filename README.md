# Server Profiles

Server Profiles is a client-side Fabric mod that automatically switches selected Minecraft settings for each server.

No server-side installation is required.

> Current status: public beta for Minecraft 1.21.11.

## Quick start

1. Install Fabric Loader, Fabric API, and Server Profiles.
2. Join a server.
3. Adjust Minecraft settings the way you want for that server.
4. Press **O** to open Server Profiles.
5. Press **Save current**.
6. Optionally open **Managed settings** and disable any settings that should not be controlled by that profile.

The next time you join, the matching profile is applied automatically.

If Mod Menu is installed, Server Profiles can also be opened from its config button.

## Profile patterns

Profiles can target one server or a group of servers.

- `play.example.com` — one server
- `play.example.com:25566` — one custom-port endpoint
- `*.example.com` — every subdomain, including custom ports
- `singleplayer` — integrated singleplayer worlds
- `*` — every multiplayer server

The default Minecraft port `:25565` is normalized away, so `play.example.com` and `play.example.com:25565` share the same profile.

Matching priority is:

1. Exact address and port
2. Host-only profile
3. Most-specific wildcard profile

An exact disabled profile takes precedence over wildcard profiles, so it can be used as an exclusion.

## Managed settings

Each profile can independently enable or disable management of:

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

A profile may manage only the settings you want. For example, a PvP profile can change sensitivity, FOV, particles, and render distance without touching volume or brightness.

## Safe restoration

With **Restore on exit** enabled, Server Profiles captures the current settings before it applies a profile.

When you disconnect, only the settings that were actually managed during that session are restored. Unmanaged settings are left alone.

If Minecraft exits unexpectedly before restoration can happen, the recovery snapshot is retained in `config/serverprofiles.json` and applied on the next launch.

If the config file cannot be parsed, Server Profiles moves it to a timestamped `serverprofiles.broken-*.json` file instead of silently overwriting it.

## Profile manager

The in-game profile manager supports:

- Creating and overwriting profiles from the current settings
- Editing wildcard patterns directly
- Browsing all saved profiles
- Enabling and disabling profiles
- Choosing exactly which settings each profile manages
- Manually applying a saved profile
- Selecting an existing profile for editing
- Two-step deletion confirmation
- English and Korean UI

Profiles are stored in:

```text
config/serverprofiles.json
```

## Installation

### Required

- Minecraft 1.21.11
- Fabric Loader 0.19.5+
- Fabric API
- Java 21+

### Optional

- Mod Menu 17.x — adds a config button for Server Profiles

## Building from source

The current Loom build tooling requires JDK 25, while the produced mod targets Java 21.

Requirements:

- JDK 25+
- Gradle 9.7.1+

```bash
gradle clean build
```

The distributable JAR is written to `build/libs`.

## Releasing

Releases are built entirely by GitHub Actions.

Open **Actions → Release → Run workflow**, enter a version such as `0.3.0-beta.1`, and run it. The workflow:

1. Validates the version.
2. Builds the mod with that version injected into `fabric.mod.json`.
3. Creates the remapped distributable JAR.
4. Generates `SHA256SUMS.txt`.
5. Uploads the build as an Actions artifact.
6. Creates the matching `v<version>` Git tag and GitHub Release.
7. Marks versions containing a prerelease suffix such as `-beta.1` as prereleases.

Maintainers can also release the version currently stored in `gradle.properties` by pushing a commit whose message contains `[release]`.

## Compatibility

Profiles from v0.1 and v0.2 remain readable. Newly introduced settings are not applied from older profile snapshots until that profile is saved again.

Server Profiles currently manages vanilla Minecraft options only. Settings owned by Sodium, Iris, or other third-party mods are not yet included.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## License

MIT
