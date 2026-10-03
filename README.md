# Server Profiles

[![Build](https://github.com/Pma10/ServerProfiles/actions/workflows/build.yml/badge.svg)](https://github.com/Pma10/ServerProfiles/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20--1.21.11-green.svg)](#supported-versions)

Server Profiles is a client-side Fabric mod that automatically switches selected Minecraft settings for each server.

No server-side installation is required.

## Features

- Save different client settings for different multiplayer servers.
- Match exact addresses, host-only addresses, wildcard domains, or singleplayer.
- Choose exactly which settings each profile is allowed to manage.
- Restore only the settings that were changed by the active profile.
- Recover the previous settings after an unexpected client shutdown.
- Browse, enable, disable, apply, overwrite, and delete profiles in-game.
- Open the configuration with **O** or through Mod Menu.
- English and Korean translations.
- One source tree built and verified against 19 Minecraft targets.

## Supported versions

Every supported Minecraft version gets its own JAR. GitHub Actions compiles every target independently instead of declaring an unverified wide compatibility range.

| Minecraft | Java |
| --- | --- |
| 1.20, 1.20.1, 1.20.2, 1.20.3, 1.20.4 | 17+ |
| 1.20.5, 1.20.6 | 21+ |
| 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8, 1.21.9, 1.21.10, 1.21.11 | 21+ |

Fabric API is required. Mod Menu is optional.

## Quick start

1. Install Fabric Loader, Fabric API, and the Server Profiles JAR matching your Minecraft version.
2. Join a server.
3. Adjust Minecraft settings the way you want for that server.
4. Press **O**.
5. Press **Save current**.
6. Open **Managed settings** if the profile should control only a subset of settings.

The matching profile is applied automatically on later joins.

## Profile patterns

- `play.example.com` — exact server
- `play.example.com:25566` — exact custom-port endpoint
- `*.example.com` — every matching subdomain
- `singleplayer` — integrated singleplayer worlds
- `*` — every multiplayer server

The default port `:25565` is normalized away.

Matching priority is:

1. Exact address and port
2. Host-only profile
3. Most-specific wildcard profile

An exact disabled profile still takes priority over wildcard profiles, so it can be used as an exclusion.

## Managed settings

Each profile can independently manage:

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

Profiles are stored in `config/serverprofiles.json`.

## Project structure

```text
ServerProfiles/
├─ .github/
│  └─ workflows/
│     ├─ build.yml
│     └─ release.yml
├─ docs/
│  └─ ARCHITECTURE.md
├─ src/main/
│  ├─ java/io/github/pma10/serverprofiles/
│  │  ├─ ServerProfiles.java
│  │  ├─ client/
│  │  ├─ config/
│  │  ├─ integration/
│  │  ├─ profile/
│  │  └─ screen/
│  └─ resources/
│     ├─ assets/serverprofiles/lang/
│     └─ fabric.mod.json
├─ versions/
│  ├─ 1.20/
│  ├─ ...
│  └─ 1.21.11/
├─ build.gradle
├─ settings.gradle
└─ stonecutter.gradle
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the package responsibilities and multi-version strategy.

## Building

Server Profiles uses [Stonecutter](https://stonecutter.kikugie.dev/) to keep one source tree while compiling version-specific JARs.

Build one version:

```bash
gradle :1.21.11:build
```

Build every supported version:

```bash
gradle buildAll
```

Version-specific JARs are written under:

```text
versions/<minecraft-version>/build/libs/
```

## Releasing

GitHub Actions handles releases.

Open **Actions → Release → Run workflow**, enter a version such as `0.4.0-beta.1`, and run it.

The workflow builds all 19 targets, uploads each version-specific JAR, generates `SHA256SUMS.txt`, creates the matching Git tag, and creates one GitHub Release containing every supported Minecraft build.

A commit whose message contains `[release]` releases the version currently stored in `gradle.properties`.

## Compatibility and recovery

Profiles created by previous Server Profiles versions remain readable.

With **Restore on exit** enabled, the mod stores a temporary recovery snapshot before changing client options. Only managed settings are restored. If Minecraft closes unexpectedly, that recovery snapshot is applied on the next launch.

If the JSON config cannot be parsed, it is moved to a timestamped `serverprofiles.broken-*.json` file rather than being silently overwritten.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## License

MIT
