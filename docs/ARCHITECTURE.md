# Architecture

ServerProfile keeps one source tree and isolates responsibilities by package.

## Java packages

```text
io.github.pma10.serverprofile
├─ ServerProfileMod
├─ client
│  └─ ServerProfileClient
├─ compat
│  ├─ MinecraftCompat
│  └─ ScreenGraphics
├─ config
│  └─ ProfileConfig
├─ profile
│  ├─ ProfileManager
│  ├─ ProfileRules
│  ├─ ServerProfile
│  └─ SettingsSnapshot
├─ screen
│  ├─ ServerProfileScreen
│  ├─ ProfileListScreen
│  └─ ProfileRulesScreen
└─ integration
   └─ modmenu
      └─ ModMenuIntegration
```

### Root

`ServerProfileMod` contains only application-wide constants such as the mod ID and logger.

### client

Fabric client bootstrap and event registration.

### compat

Small version-compatibility shims for Minecraft API moves such as screen access and the 26.x GUI extraction pipeline. Version checks should stay here when they can be isolated cleanly.

### config

Serialized configuration models.

### profile

Profile matching, managed-setting rules, settings capture/application, persistence orchestration, config migration, and restoration.

### screen

Minecraft GUI screens. Screens use the profile API and do not access the filesystem directly.

### integration

Optional third-party integrations, isolated from the core packages.

## Multi-version strategy

Stonecutter creates one Gradle node per Minecraft target under `versions/`. Shared Java and resources stay under `src/main`.

Minecraft 1.20 through 1.21.11 use `net.fabricmc.fabric-loom-remap` with Mojang mappings.

Minecraft 26.1 and newer are unobfuscated and use `net.fabricmc.fabric-loom` with standard Gradle dependency configurations and Java 25.

Version-specific API differences are kept behind the smallest possible Stonecutter condition instead of copying whole classes.

## CI rule

A Minecraft version is advertised as supported only when its dedicated GitHub Actions matrix build succeeds.
