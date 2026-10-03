package kr.pma.serverprofiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
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
    private String activeProfile;

    public ProfileManager() {
        config = load();
    }

    public void restoreCrashBackup(Minecraft client) {
        if (config.recoveryBackup == null) {
            return;
        }

        try {
            config.recoveryBackup.apply(client.options);
            persistOptions(client);
            LOGGER.info("Restored client settings left behind by an interrupted Server Profiles session");
            config.recoveryBackup = null;
            config.recoveryProfile = null;
            save();
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to restore recovery settings", exception);
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
            if (Character.isWhitespace(normalized.charAt(i))) {
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

    public boolean applyProfile(String key, Minecraft client) {
        String normalized = normalizeProfileKey(key);
        ServerProfile profile = config.profiles.get(normalized);

        if (profile == null || profile.settings == null) {
            return false;
        }

        applyMatch(client, new Match(normalized, profile), false);
        return true;
    }

    public boolean applyMatchedProfile(String target, Minecraft client) {
        Match match = findMatch(target);
        if (match == null || match.profile().settings == null) {
            return false;
        }

        applyMatch(client, match, false);
        return true;
    }

    public boolean restorePrevious(Minecraft client) {
        if (sessionBackup == null) {
            return false;
        }

        SettingsSnapshot backup = sessionBackup;
        clearSessionState();

        backup.apply(client.options);
        persistOptions(client);
        return true;
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
            config.recoveryBackup = null;
            config.recoveryProfile = null;
        } else if (sessionBackup != null) {
            config.recoveryBackup = sessionBackup;
            config.recoveryProfile = activeProfile;
        }

        save();
    }

    private void applyMatch(Minecraft client, Match match, boolean automatic) {
        if (config.restoreOnDisconnect && sessionBackup == null) {
            sessionBackup = SettingsSnapshot.capture(client.options);
            config.recoveryBackup = sessionBackup;
            config.recoveryProfile = match.key();
            save();
        }

        match.profile().settings.apply(client.options);
        activeProfile = match.key();
        persistOptions(client);

        LOGGER.info("{} Server Profiles profile '{}' for '{}'",
            automatic ? "Applied" : "Manually applied",
            match.key(),
            currentTarget(client));
    }

    private void finishSession(Minecraft client) {
        if (sessionBackup != null && config.restoreOnDisconnect) {
            SettingsSnapshot backup = sessionBackup;
            clearSessionState();
            backup.apply(client.options);
            persistOptions(client);
            return;
        }

        clearSessionState();
    }

    private void clearSessionState() {
        sessionBackup = null;
        activeProfile = null;
        config.recoveryBackup = null;
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

        try (Reader reader = Files.newBufferedReader(configPath)) {
            ProfileConfig loaded = GSON.fromJson(reader, ProfileConfig.class);
            if (loaded == null) {
                loaded = new ProfileConfig();
            }

            loaded.normalize();

            Map<String, ServerProfile> normalizedProfiles = new LinkedHashMap<>();
            for (Map.Entry<String, ServerProfile> entry : loaded.profiles.entrySet()) {
                String key = normalizeProfileKey(entry.getKey());
                if (isValidProfileKey(key) && entry.getValue() != null) {
                    normalizedProfiles.put(key, entry.getValue());
                }
            }

            loaded.profiles = normalizedProfiles;
            loaded.version = Math.max(loaded.version, 2);
            return loaded;
        } catch (Exception exception) {
            LOGGER.error("Failed to load {}", configPath, exception);
            return new ProfileConfig();
        }
    }

    private void save() {
        config.version = 2;

        try {
            Files.createDirectories(configPath.getParent());
            Path temporary = configPath.resolveSibling(configPath.getFileName() + ".tmp");

            try (Writer writer = Files.newBufferedWriter(temporary)) {
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
