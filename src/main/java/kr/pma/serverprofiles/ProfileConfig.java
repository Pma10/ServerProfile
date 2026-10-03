package kr.pma.serverprofiles;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProfileConfig {
    public int version = 2;
    public boolean restoreOnDisconnect = true;
    public Map<String, ServerProfile> profiles = new LinkedHashMap<>();
    public SettingsSnapshot recoveryBackup;
    public String recoveryProfile;

    public void normalize() {
        if (profiles == null) {
            profiles = new LinkedHashMap<>();
        }
    }
}
