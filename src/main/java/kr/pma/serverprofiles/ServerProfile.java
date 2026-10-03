package kr.pma.serverprofiles;

public final class ServerProfile {
    public boolean enabled = true;
    public SettingsSnapshot settings;

    public ServerProfile() {
    }

    public ServerProfile(SettingsSnapshot settings) {
        this.settings = settings;
    }
}
