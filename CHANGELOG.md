# Changelog

All notable changes to ServerProfile are documented here.

## 0.5.0-beta.2 - 2026-10-04

### Fixed

- Separated the Fabric Loader build dependency from the runtime minimum requirement.
- Lowered the runtime Fabric Loader minimum from 0.19.5 to 0.19.0, matching Fabric's 26.x metadata guidance and allowing Loader 0.19.3 installations to launch ServerProfile.

## 0.5.0-beta.1 - 2026-10-03

### Added

- Minecraft 26.1.x, 26.2, and 26.3 build targets.
- Unobfuscated Fabric Loom pipeline for Minecraft 26.1+.
- Java 25 target configuration for the 26.x line.
- Automatic migration from the legacy `serverprofiles.json` config.

### Changed

- Renamed the project and user-facing mod from Server Profiles to ServerProfile.
- Changed the primary mod ID from `serverprofiles` to `serverprofile`, while providing the legacy ID.
- Changed the Java package root to `io.github.pma10.serverprofile`.
- Changed resource namespace and translation keys to `serverprofile`.
- Changed release artifact names to `server-profile-<version>+<minecraft>.jar`.
- Expanded CI and release matrices from 19 to 22 build targets.


## 0.4.0-beta.1 - 2026-10-03

### Added

- Stonecutter-based multi-version build architecture.
- Dedicated builds for 19 Minecraft targets from 1.20 through 1.21.11.
- Version-specific Fabric API, Mod Menu, and Java toolchain pins.
- GitHub Actions matrix builds for every supported Minecraft version.
- Multi-JAR GitHub Releases with one artifact per Minecraft version.
- Architecture documentation.

### Changed

- Reorganized Java sources into client, config, profile, screen, and integration packages.
- Kept only shared application constants in the root Java package.
- Release artifacts now include the Minecraft version in the JAR version.
- Build verification now means every advertised Minecraft version compiles independently.


## 0.3.0-beta.2 - 2026-10-03

- Changed the Java package root from `kr.pma` to `io.github.pma10`.
- Changed the Gradle Maven group to `io.github.pma10`.

## 0.3.0-beta.1 - 2026-10-03

Public beta preparation.

### Added

- Per-profile managed-setting selection for all 13 supported Minecraft options.
- Safe restoration that only restores settings a profile actually changed.
- Mod Menu configuration-screen integration when Mod Menu is installed.
- Corrupt-config backup before falling back to a clean configuration.
- Tag-driven GitHub Release workflow with SHA-256 checksums.
- Release JAR artifacts from CI.

### Changed

- Configuration schema upgraded to version 3 while keeping v0.1 and v0.2 profiles compatible.
- Singleplayer now only matches the explicit `singleplayer` profile and is no longer captured by the global `*` multiplayer wildcard.
- Config files are explicitly read and written as UTF-8.
- Profile manager layout is more compact on smaller GUI scales.

## 0.2.0 - 2026-10-03

- Added the in-game saved-profile manager.
- Added wildcard profile creation from the GUI.
- Added server-address normalization and default-port handling.
- Added profile enable/disable controls and two-step deletion.
- Added entity distance, FPS limit, VSync, FOV effects, and brightness to snapshots.
- Added English and Korean UI text.
- Added CI JAR artifacts.

## 0.1.0 - 2026-10-03

- Initial Fabric client implementation.
- Added automatic per-server profile application.
- Added disconnect restoration and crash recovery.
- Added exact and wildcard server matching.
