package io.github.pma10.serverprofiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class ProfileManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ServerProfiles");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("serverprofiles.json");

    private ProfileConfig config;
    private SettingsSnapshot sessionBackup;
    private ProfileRules sessionRules;
    private String activeProfile;

    public ProfileManager() {
        config = load();
    }

    public void restoreCrashBackup(Minecraft client) {
        if (config.recoveryBackup == null) {
            return;
        }

        try {
            ProfileRules rules = config.recoveryRules == null ? new ProfileRules() : config.recoveryRules;
            config.recoveryBackup.apply(client.options, rules);
            persistOptions(client);

            LOGGER.info("Restored client settings left behind by an interrupted Server Profiles session");

            config.recoveryBackup = null;
            config.recoveryRules = null;
            config.recoveryProfile = null;
            save();
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to restore recovery settings; the recovery snapshot was kept", exception);
        }
    }

    public void onJoin(Minecraft client) {
        String target = currentTarget(client);
        Match match = findMatch(target);

        if (match == null || !match.profile().enabled || match.profile().settings == null) {
            return;
        }

        applyMatch(client, match, true);
    }

    public void onDisconnect(Minecraft client) {
        finishSession(client);
    }

    public void onClientStopping(Minecraft client) {
        finishSession(client);
    }

    public String currentTarget(Minecraft client) {
        ServerData server = client.getCurrentServer();

        if (server != null && server.ip != null && !server.ip.isBlank()) {
            return normalizeProfileKey(server.ip);
        }

        if (client.hasSingleplayerServer()) {
            return "singleplayer";
        }

        return null;
    }

    public String normalizeProfileKey(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        if (normalized.startsWith("minecraft://")) {
            normalized = normalized.substring("minecraft://".length());
        }

        int slash = normalized.indexOf('/');
        if (slash >= 0) {
            normalized = normalized.substring(0, slash);
        }

        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (hasDefaultPort(normalized)) {
            normalized = normalized.substring(0, normalized.length() - ":25565".length());
        }

        return normalized;
    }

    public boolean isValidProfileKey(String value) {
        String normalized = normalizeProfileKey(value);

        if (normalized.isBlank() || normalized.length() > 255) {
            return false;
        }

        for (int i = 0; i < normalized.length(); i++) {
            char character = normalized.charAt(i);
            if (Character.isWhitespace(character) || Character.isISOControl(character)) {
                return false;
            }
        }

        return true;
    }

    public List<String> profileKeys() {
        List<String> keys = new ArrayList<>(config.profiles.keySet());
        keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }

    public ServerProfile getProfile(String key) {
        if (key == null) {
            return null;
        }

        return config.profiles.get(normalizeProfileKey(key));
    }

    public Match findMatch(String target) {
        if (target == null) {
            return null;
        }

        String normalizedTarget = normalizeProfileKey(target);
        String hostTarget = hostOnly(normalizedTarget);

        ServerProfile exact = config.profiles.get(normalizedTarget);
        if (exact != null) {
            return new Match(normalizedTarget, exact);
        }

        if ("singleplayer".equals(normalizedTarget)) {
            return null;
        }

        if (!hostTarget.equals(normalizedTarget)) {
            ServerProfile hostProfile = config.profiles.get(hostTarget);
            if (hostProfile != null) {
                return new Match(hostTarget, hostProfile);
            }
        }

        Match best = null;
        int bestSpecificity = -1;

        for (Map.Entry<String, ServerProfile> entry : config.profiles.entrySet()) {
            String pattern = entry.getKey();

            if (!containsWildcard(pattern)) {
                continue;
            }

            if (!globMatches(pattern, normalizedTarget) && !globMatches(pattern, hostTarget)) {
                continue;
            }

            int specificity = pattern.replace("*", "").replace("?", "").length();
            if (specificity > bestSpecificity) {
                best = new Match(pattern, entry.getValue());
                bestSpecificity = specificity;
            }
        }

        return best;
    }

    public String saveCurrentProfile(String key, Minecraft client) {
        String normalized = normalizeProfileKey(key);
        if (!isValidProfileKey(normalized)) {
            return null;
        }

        ServerProfile profile = config.profiles.computeIfAbsent(normalized, ignored -> new ServerProfile());
        profile.normalize();
        profile.settings = SettingsSnapshot.capture(client.options);
        save();
        return normalized;
    }

    public boolean deleteProfile(String key) {
        String normalized = normalizeProfileKey(key);
        if (normalized.isBlank()) {
            return false;
        }

        boolean removed = config.profiles.remove(normalized) != null;
        if (removed) {
            save();
        }
        return removed;
    }

    public Boolean toggleProfile(String key) {
        ServerProfile profile = getProfile(key);
        if (profile == null) {
            return null;
        }

        profile.enabled = !profile.enabled;
        save();
        return profile.enabled;
    }

    public Boolean toggleRule(String key, ProfileRules.Setting setting) {
        ServerProfile profile = getProfile(key);
        if (profile == null) {
            return null;
        }

        profile.normalize();
        boolean enabled = !profile.rules.get(setting);
        profile.rules.set(setting, enabled);
        save();
        return enabled;
    }

    public boolean setAllRules(String key, boolean enabled) {
        ServerProfile profile = getProfile(key);
        if (profile == null) {
            return false;
        }

        profile.normalize();
        profile.rules.setAll(enabled);
        save();
        return true;
    }

    public boolean applyProfile(String key, Minecraft client) {
        String normalized = normalizeProfileKey(key);
        ServerProfile profile = config.profiles.get(normalized);

        if (profile == null || profile.settings == null) {
            return false;
        }

        profile.normalize();
        applyMatch(client, new Match(normalized, profile), false);
        return true;
    }

    public boolean applyMatchedProfile(String target, Minecraft client) {
        Match match = findMatch(target);
        if (match == null || match.profile().settings == null) {
            return false;
        }

        match.profile().normalize();
        applyMatch(client, match, false);
        return true;
    }

    public boolean restorePrevious(Minecraft client) {
        if (sessionBackup == null) {
            return false;
        }

        try {
            ProfileRules rules = sessionRules == null ? new ProfileRules() : sessionRules;
            sessionBackup.apply(client.options, rules);
            persistOptions(client);
            clearSessionState();
            return true;
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to restore previous settings; the recovery snapshot was kept", exception);
            return false;
        }
    }

    public boolean hasSessionBackup() {
        return sessionBackup != null;
    }

    public boolean restoreOnDisconnect() {
        return config.restoreOnDisconnect;
    }

    public void toggleRestoreOnDisconnect() {
        config.restoreOnDisconnect = !config.restoreOnDisconnect;

        if (!config.restoreOnDisconnect) {
            sessionBackup = null;
            sessionRules = null;
            config.recoveryBackup = null;
            config.recoveryRules = null;
            config.recoveryProfile = null;
        }

        save();
    }

    private void applyMatch(Minecraft client, Match match, boolean automatic) {
        ProfileRules rules = match.profile().rules == null ? new ProfileRules() : match.profile().rules;

        if (rules.enabledCount() == 0) {
            LOGGER.info("Profile '{}' matched '{}' but manages no settings", match.key(), currentTarget(client));
            return;
        }

        if (config.restoreOnDisconnect) {
            if (sessionBackup == null) {
                sessionBackup = SettingsSnapshot.capture(client.options);
                sessionRules = ProfileRules.none();
            }

            if (sessionRules == null) {
                sessionRules = ProfileRules.none();
            }

            sessionRules.merge(rules);
            config.recoveryBackup = sessionBackup;
            config.recoveryRules = sessionRules.copy();
            config.recoveryProfile = match.key();
            save();
        }

        match.profile().settings.apply(client.options, rules);
        activeProfile = match.key();
        persistOptions(client);

        LOGGER.info("{} Server Profiles profile '{}' for '{}'",
            automatic ? "Applied" : "Manually applied",
            match.key(),
            currentTarget(client));
    }

    private void finishSession(Minecraft client) {
        if (sessionBackup != null && config.restoreOnDisconnect) {
            try {
                ProfileRules rules = sessionRules == null ? new ProfileRules() : sessionRules;
                sessionBackup.apply(client.options, rules);
                persistOptions(client);
            } catch (RuntimeException exception) {
                LOGGER.error("Failed to restore settings on disconnect; recovery data was kept", exception);
                return;
            }
        }

        clearSessionState();
    }

    private void clearSessionState() {
        sessionBackup = null;
        sessionRules = null;
        activeProfile = null;
        config.recoveryBackup = null;
        config.recoveryRules = null;
        config.recoveryProfile = null;
        save();
    }

    private void persistOptions(Minecraft client) {
        client.options.save();

        if (client.player != null) {
            client.options.broadcastOptions();
        }
    }

    private ProfileConfig load() {
        if (!Files.exists(configPath)) {
            return new ProfileConfig();
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            ProfileConfig loaded = GSON.fromJson(reader, ProfileConfig.class);
            if (loaded == null) {
                throw new IllegalStateException("Config file contained no JSON object");
            }

            loaded.normalize();

            Map<String, ServerProfile> normalizedProfiles = new LinkedHashMap<>();
            for (Map.Entry<String, ServerProfile> entry : loaded.profiles.entrySet()) {
                String key = normalizeProfileKey(entry.getKey());
                ServerProfile profile = entry.getValue();

                if (isValidProfileKey(key) && profile != null) {
                    profile.normalize();

                    if (normalizedProfiles.put(key, profile) != null) {
                        LOGGER.warn("Multiple profile keys normalized to '{}'; the last profile was kept", key);
                    }
                }
            }

            loaded.profiles = normalizedProfiles;
            loaded.version = Math.max(loaded.version, 3);
            return loaded;
        } catch (Exception exception) {
            LOGGER.error("Failed to load {}; starting with a clean config", configPath, exception);
            backupBrokenConfig();
            return new ProfileConfig();
        }
    }

    private void backupBrokenConfig() {
        if (!Files.exists(configPath)) {
            return;
        }

        Path backupPath = configPath.resolveSibling(
            "serverprofiles.broken-" + System.currentTimeMillis() + ".json"
        );

        try {
            Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Moved unreadable Server Profiles config to {}", backupPath);
        } catch (Exception backupException) {
            LOGGER.error("Could not back up unreadable Server Profiles config", backupException);
        }
    }

    private void save() {
        config.version = 3;

        try {
            Files.createDirectories(configPath.getParent());
            Path temporary = configPath.resolveSibling(configPath.getFileName() + ".tmp");

            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(config, writer);
            }

            try {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, configPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception exception) {
            LOGGER.error("Failed to save {}", configPath, exception);
        }
    }

    private static boolean hasDefaultPort(String value) {
        if (!value.endsWith(":25565")) {
            return false;
        }

        if (value.startsWith("[")) {
            int closingBracket = value.indexOf(']');
            return closingBracket >= 0 && closingBracket == value.length() - ":25565".length() - 1;
        }

        return value.indexOf(':') == value.lastIndexOf(':');
    }

    private static String hostOnly(String value) {
        if (value.startsWith("[")) {
            int closingBracket = value.indexOf(']');
            if (closingBracket >= 0) {
                return value.substring(0, closingBracket + 1);
            }
        }

        int firstColon = value.indexOf(':');
        if (firstColon > 0 && firstColon == value.lastIndexOf(':')) {
            return value.substring(0, firstColon);
        }

        return value;
    }

    private static boolean containsWildcard(String value) {
        return value.indexOf('*') >= 0 || value.indexOf('?') >= 0;
    }

    private static boolean globMatches(String glob, String value) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < glob.length(); i++) {
            char character = glob.charAt(i);

            if (character == '*') {
                regex.append(".*");
            } else if (character == '?') {
                regex.append('.');
            } else {
                regex.append(Pattern.quote(String.valueOf(character)));
            }
        }

        regex.append('$');
        return value.matches(regex.toString());
    }

    public record Match(String key, ServerProfile profile) {
    }
}
