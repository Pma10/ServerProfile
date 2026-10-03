# ServerProfile

[![Build](https://github.com/Pma10/ServerProfile/actions/workflows/build.yml/badge.svg)](https://github.com/Pma10/ServerProfile/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20--26.3-green.svg)](#supported-versions)

<!-- modrinth:start -->

ServerProfile is a lightweight client-side Fabric mod that automatically switches selected Minecraft settings for each server.

Set up a profile once, and ServerProfile applies it whenever you join a matching server. When you leave, it can restore your previous settings automatically.

No server-side installation is required.

## Features

- Automatic per-server settings profiles
- Exact server, host, wildcard, and singleplayer matching
- Choose exactly which settings each profile is allowed to change
- Restore only the settings managed by the active profile
- Crash recovery if Minecraft closes before settings are restored
- In-game profile browser and editor
- Native **ServerProfile** button in Minecraft's **Options** screen
- **Ctrl + O** shortcut
- Optional Mod Menu integration
- English and Korean translations

## Supported versions

Each Minecraft target is built and verified independently.

| Minecraft | Java |
| --- | --- |
| 1.20 - 1.20.4 | 17+ |
| 1.20.5 - 1.21.11 | 21+ |
| 26.1.x, 26.2, 26.3 | 25+ |

Supported build targets:

`1.20`, `1.20.1`, `1.20.2`, `1.20.3`, `1.20.4`, `1.20.5`, `1.20.6`,  
`1.21`, `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`, `1.21.5`, `1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`,  
`26.1`, `26.2`, `26.3`.

### Requirements

- Fabric Loader 0.19.0+
- Fabric API
- Mod Menu is optional

## Quick start

1. Install Fabric Loader, Fabric API, and the ServerProfile JAR for your Minecraft version.
2. Join a server.
3. Change Minecraft's settings to the values you want for that server.
4. Open **Options → ServerProfile** or press **Ctrl + O**.
5. Enter or confirm the profile pattern.
6. Click **Save current**.
7. Open **Managed settings** if the profile should control only selected options.

The next time you join a matching server, ServerProfile applies the profile automatically.

## Profile matching

Profiles can target one server or a group of servers.

| Pattern | Matches |
| --- | --- |
| `play.example.com` | That host |
| `play.example.com:25566` | That exact custom-port endpoint |
| `*.example.com` | Matching subdomains |
| `singleplayer` | Integrated singleplayer worlds |
| `*` | Every multiplayer server |

The default Minecraft port `:25565` is normalized away.

Matching priority:

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
- Particles
- GUI scale
- View bobbing
- Master volume
- Entity distance
- Maximum FPS
- VSync
- FOV effect scale
- Brightness

You can disable any item you do not want ServerProfile to touch.

For example, a PvP profile can manage only **FOV, sensitivity, particles, and render distance** while leaving your volume and brightness unchanged.

## Safe restoration

With **Restore on exit** enabled, ServerProfile captures your current settings before applying a profile.

When you disconnect, only settings that were actually managed during that session are restored. Unmanaged settings are left untouched.

If Minecraft closes unexpectedly, the recovery snapshot is retained and restored on the next launch.

If the configuration file becomes unreadable, ServerProfile backs it up instead of silently overwriting it.

## Configuration

Profiles are stored in:

```text
config/serverprofile.json
```

Older beta configs stored as `config/serverprofiles.json` are migrated automatically.

The primary mod ID is `serverprofile`. The legacy `serverprofiles` ID is also provided for compatibility with the earlier beta line.

## Links

- Source code: https://github.com/Pma10/ServerProfile
- Issues: https://github.com/Pma10/ServerProfile/issues

<!-- modrinth:end -->

---

## Development

ServerProfile uses a shared source tree with Stonecutter to build every supported Minecraft target independently.

### Project structure

```text
ServerProfile/
├─ .github/
│  └─ workflows/
│     ├─ build.yml
│     ├─ release.yml
│     └─ modrinth-description.yml
├─ docs/
│  └─ ARCHITECTURE.md
├─ src/main/
│  ├─ java/io/github/pma10/serverprofile/
│  │  ├─ ServerProfileMod.java
│  │  ├─ client/
│  │  ├─ compat/
│  │  ├─ config/
│  │  ├─ integration/
│  │  ├─ mixin/
│  │  ├─ profile/
│  │  └─ screen/
│  └─ resources/
│     ├─ assets/serverprofile/lang/
│     ├─ fabric.mod.json
│     └─ serverprofile.client.mixins.json
├─ versions/
│  ├─ 1.20/
│  ├─ ...
│  └─ 26.3/
├─ build.gradle
├─ settings.gradle
└─ stonecutter.gradle
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for package responsibilities and the multi-version strategy.

### Building

Minecraft 1.20 through 1.21.11 use the remapping Loom pipeline with Mojang mappings. Minecraft 26.1+ uses Fabric's unobfuscated Loom pipeline.

Build one target:

```bash
gradle :26.3:build
```

Build every target:

```bash
gradle buildAll
```

Artifacts are written to:

```text
versions/<minecraft-version>/build/libs/
```

### Releasing

Open **Actions → Release → Run workflow** and enter the release version.

The workflow:

1. Builds every supported Minecraft target.
2. Uploads every version-specific JAR.
3. Generates `SHA256SUMS.txt`.
4. Creates the GitHub Release.
5. Publishes each target to Modrinth when Modrinth credentials are configured.

A commit containing `[release]` uses the version in `gradle.properties`.

### Modrinth automation

Configure these in **Settings → Secrets and variables → Actions**:

- Variable `MODRINTH_PROJECT_ID`
- Secret `MODRINTH_TOKEN`

The token needs permission to create project versions and update the project description.

Every README change on `main` automatically syncs the content between the `modrinth:start` and `modrinth:end` markers to the Modrinth project description.

If the Modrinth project variable is not configured, the sync and publishing jobs are skipped without affecting normal GitHub builds.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## License

MIT
