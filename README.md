# ServerProfile

[![Build](https://github.com/Pma10/ServerProfile/actions/workflows/build.yml/badge.svg)](https://github.com/Pma10/ServerProfile/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20--26.3-green.svg)](#supported-versions)

ServerProfile is a client-side Fabric mod that automatically switches selected Minecraft settings for each server.

No server-side installation is required.

## Features

- Save different client settings for different multiplayer servers.
- Match exact addresses, host-only addresses, wildcard domains, or singleplayer.
- Choose exactly which settings each profile manages.
- Restore only settings changed by the active profile.
- Recover previous settings after an unexpected client shutdown.
- Browse, enable, disable, apply, overwrite, and delete profiles in-game.
- Open the configuration with **O** or through Mod Menu.
- English and Korean translations.
- One shared source tree with independently verified version builds.

## Supported versions

Each target gets its own JAR and its own GitHub Actions build.

| Minecraft | Java |
| --- | --- |
| 1.20 - 1.20.4 | 17+ |
| 1.20.5 - 1.21.11 | 21+ |
| 26.1.x, 26.2, 26.3 | 25+ |

Verified targets are 1.20 through 1.20.6, 1.21 through 1.21.11, and 26.1, 26.2, 26.3.

Fabric Loader 0.19.0+ and Fabric API are required. Mod Menu is optional.

## Quick start

1. Install Fabric Loader, Fabric API, and the ServerProfile JAR matching your Minecraft version.
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

Matching priority:

1. Exact address and port
2. Host-only profile
3. Most-specific wildcard profile

An exact disabled profile still takes priority over wildcard profiles, so it can be used as an exclusion.

## Managed settings

Each profile can independently manage FOV, mouse sensitivity, render distance, simulation distance, particles, GUI scale, view bobbing, master volume, entity distance, maximum FPS, VSync, FOV effect scale, and brightness.

Profiles are stored in `config/serverprofile.json`. Existing beta configs stored as `config/serverprofiles.json` are migrated automatically.

## Project structure

```text
ServerProfile/
├─ .github/workflows/
├─ docs/
├─ src/main/
│  ├─ java/io/github/pma10/serverprofile/
│  │  ├─ ServerProfileMod.java
│  │  ├─ client/
│  │  ├─ compat/
│  │  ├─ config/
│  │  ├─ integration/
│  │  ├─ profile/
│  │  └─ screen/
│  └─ resources/
│     ├─ assets/serverprofile/lang/
│     └─ fabric.mod.json
├─ versions/
│  ├─ 1.20/
│  ├─ ...
│  └─ 26.3/
├─ build.gradle
├─ settings.gradle
└─ stonecutter.gradle
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Multi-version build

ServerProfile uses Stonecutter. Minecraft 1.20 through 1.21.11 use the remapping Loom pipeline with Mojang mappings. Minecraft 26.1+ uses Fabric's unobfuscated Loom pipeline and Java 25.

Build one target:

```bash
gradle :26.3:build
```

Build every supported target:

```bash
gradle buildAll
```

Artifacts are written under `versions/<minecraft-version>/build/libs/`.

## Releasing

Open **Actions → Release → Run workflow** and enter a version such as `0.5.0-beta.1`.

The release workflow builds every supported target, uploads each version-specific JAR, generates `SHA256SUMS.txt`, and creates one GitHub Release.

A commit containing `[release]` uses the version from `gradle.properties`.

### Modrinth publishing

The same release workflow can publish every Minecraft-specific JAR to Modrinth automatically.

Configure these once in **GitHub → Settings → Secrets and variables → Actions**:

- Repository variable `MODRINTH_PROJECT_ID`: the Modrinth project ID or slug.
- Repository secret `MODRINTH_TOKEN`: a Modrinth personal access token with permission to create versions for the project.

If `MODRINTH_PROJECT_ID` is not configured, the Modrinth jobs are skipped and GitHub Releases continue to work normally.

For each release, ServerProfile publishes one Modrinth version per Minecraft target. For example, release `0.5.1` produces Modrinth versions such as `0.5.1+1.21.11` and `0.5.1+26.3`. This keeps each JAR associated with the exact Minecraft version it was built against.

Fabric API is declared as a required Modrinth dependency and Mod Menu as optional.

## Compatibility and recovery

The primary mod ID is `serverprofile`. It also provides the legacy `serverprofiles` ID for compatibility with the earlier beta line.

If Minecraft exits before restoration, the recovery snapshot is applied on the next launch. If the JSON config cannot be parsed, it is moved to a timestamped `serverprofile.broken-*.json` file instead of being overwritten.

## Changelog

See [CHANGELOG.md](CHANGELOG.md).

## License

MIT
