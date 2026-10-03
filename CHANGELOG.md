# Changelog

All notable changes to Server Profiles are documented here.

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
