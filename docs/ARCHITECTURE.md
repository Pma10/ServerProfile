# Architecture

Server Profiles keeps the codebase small, but separates responsibilities so new features do not turn the root package into a collection of unrelated classes.

## Java packages

```text
io.github.pma10.serverprofiles
├─ ServerProfiles
├─ client
│  └─ ServerProfilesClient
├─ config
│  └─ ProfileConfig
├─ profile
│  ├─ ProfileManager
│  ├─ ProfileRules
│  ├─ ServerProfile
│  └─ SettingsSnapshot
├─ screen
│  ├─ ServerProfilesScreen
│  ├─ ProfilesListScreen
│  └─ ProfileRulesScreen
└─ integration
   └─ modmenu
      └─ ModMenuIntegration
```

### Root

`ServerProfiles` owns application-wide constants such as the mod ID and logger. No feature implementation should be added directly to the root package.

### client

Fabric client bootstrap and event registration only. It wires Minecraft/Fabric lifecycle events to the profile subsystem and opens the UI.

### config

Serialized configuration models. Persistence remains accessed through the profile manager so screens never read or write JSON directly.

### profile

The domain layer: profile matching, managed-setting rules, settings capture/application, session restoration, and config persistence orchestration.

### screen

Minecraft GUI screens. Screens call the profile API and contain no direct file-system logic.

### integration

Optional third-party integrations. Each integration gets its own subpackage so optional dependencies never leak into the core packages.

## Multi-version layout

Stonecutter creates one Gradle node per Minecraft target under `versions/`.

Each node contains only version-specific build properties:

```text
versions/<minecraft-version>/gradle.properties
```

The Java and resource sources remain shared in `src/main`.

When a Minecraft API differs between versions, the smallest possible section is guarded with a Stonecutter directive instead of copying an entire class. This keeps behavior identical across versions and makes API boundaries visible during review.

## CI rule

A version is considered supported only when its dedicated GitHub Actions matrix job builds successfully. Adding a version to documentation without adding it to the matrix and `versions/` is not considered support.
