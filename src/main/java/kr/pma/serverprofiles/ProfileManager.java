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
            return normalizeTarget(server.ip);
        }

        if (client.hasSingleplayerServer()) {
            return "singleplayer";
        }

        return null;
    }

    public Match findMatch(String target) {
        if (target == null) {
            return null;
        }

        ServerProfile exact = config.profiles.get(target);
        if (exact != null) {
            return new Match(target, exact);
        }

        Match best = null;
        int bestSpecificity = -1;

        for (Map.Entry<String, ServerProfile> entry : config.profiles.entrySet()) {
            String pattern = normalizeTarget(entry.getKey());

            if (!containsWildcard(pattern) || !globMatches(pattern, target)) {
                continue;
            }

            int specificity = pattern.replace("*", "").replace("?", "").length();
            if (specificity > bestSpecificity) {
                best = new Match(entry.getKey(), entry.getValue());
                bestSpecificity = specificity;
            }
        }

        return best;
    }

    public boolean hasExactProfile(String target) {
        return target != null && config.profiles.containsKey(target);
    }

    public void saveCurrentProfile(String target, Minecraft client) {
        if (target == null) {
            return;
        }

        ServerProfile profile = config.profiles.computeIfAbsent(target, key -> new ServerProfile());
        profile.settings = SettingsSnapshot.capture(client.options);
        save();
    }

    public void deleteExactProfile(String target) {
        if (target == null) {
            return;
        }

        config.profiles.remove(target);
        save();
    }

    public boolean toggleMatchedProfile(String target) {
        Match match = findMatch(target);
        if (match == null) {
            return false;
        }

        match.profile().enabled = !match.profile().enabled;
        save();
        return match.profile().enabled;
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
            return loaded;
        } catch (Exception exception) {
            LOGGER.error("Failed to load {}", configPath, exception);
            return new ProfileConfig();
        }
    }

    private void save() {
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

    private static String normalizeTarget(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
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
