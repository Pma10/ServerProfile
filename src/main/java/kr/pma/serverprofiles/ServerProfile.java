package kr.pma.serverprofiles;

public final class ServerProfile {
    public boolean enabled = true;
    public SettingsSnapshot settings;
    public ProfileRules rules = new ProfileRules();

    public ServerProfile() {
    }

    public ServerProfile(SettingsSnapshot settings) {
        this.settings = settings;
    }

    public void normalize() {
        if (rules == null) {
            rules = new ProfileRules();
        }
    }
}
