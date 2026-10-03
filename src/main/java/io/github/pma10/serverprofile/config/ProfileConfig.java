package io.github.pma10.serverprofile.config;

import io.github.pma10.serverprofile.profile.ProfileRules;
import io.github.pma10.serverprofile.profile.ServerProfile;
import io.github.pma10.serverprofile.profile.SettingsSnapshot;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProfileConfig {
    public int version = 3;
    public boolean restoreOnDisconnect = true;
    public Map<String, ServerProfile> profiles = new LinkedHashMap<>();
    public SettingsSnapshot recoveryBackup;
    public ProfileRules recoveryRules;
    public String recoveryProfile;

    public void normalize() {
        if (profiles == null) {
            profiles = new LinkedHashMap<>();
        }

        if (recoveryBackup != null && recoveryRules == null) {
            recoveryRules = new ProfileRules();
        }
    }
}
