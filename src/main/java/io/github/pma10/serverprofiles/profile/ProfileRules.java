package io.github.pma10.serverprofiles.profile;

public final class ProfileRules {
    public boolean fov = true;
    public boolean sensitivity = true;
    public boolean renderDistance = true;
    public boolean simulationDistance = true;
    public boolean particles = true;
    public boolean guiScale = true;
    public boolean viewBobbing = true;
    public boolean masterVolume = true;
    public boolean entityDistance = true;
    public boolean framerateLimit = true;
    public boolean vsync = true;
    public boolean fovEffects = true;
    public boolean brightness = true;

    public static ProfileRules none() {
        ProfileRules rules = new ProfileRules();
        rules.setAll(false);
        return rules;
    }

    public ProfileRules copy() {
        ProfileRules copy = none();
        for (Setting setting : Setting.values()) {
            copy.set(setting, get(setting));
        }
        return copy;
    }

    public void merge(ProfileRules other) {
        if (other == null) {
            return;
        }

        for (Setting setting : Setting.values()) {
            if (other.get(setting)) {
                set(setting, true);
            }
        }
    }

    public void setAll(boolean enabled) {
        for (Setting setting : Setting.values()) {
            set(setting, enabled);
        }
    }

    public int enabledCount() {
        int count = 0;
        for (Setting setting : Setting.values()) {
            if (get(setting)) {
                count++;
            }
        }
        return count;
    }

    public boolean get(Setting setting) {
        return switch (setting) {
            case FOV -> fov;
            case SENSITIVITY -> sensitivity;
            case RENDER_DISTANCE -> renderDistance;
            case SIMULATION_DISTANCE -> simulationDistance;
            case PARTICLES -> particles;
            case GUI_SCALE -> guiScale;
            case VIEW_BOBBING -> viewBobbing;
            case MASTER_VOLUME -> masterVolume;
            case ENTITY_DISTANCE -> entityDistance;
            case FRAMERATE_LIMIT -> framerateLimit;
            case VSYNC -> vsync;
            case FOV_EFFECTS -> fovEffects;
            case BRIGHTNESS -> brightness;
        };
    }

    public void set(Setting setting, boolean enabled) {
        switch (setting) {
            case FOV -> fov = enabled;
            case SENSITIVITY -> sensitivity = enabled;
            case RENDER_DISTANCE -> renderDistance = enabled;
            case SIMULATION_DISTANCE -> simulationDistance = enabled;
            case PARTICLES -> particles = enabled;
            case GUI_SCALE -> guiScale = enabled;
            case VIEW_BOBBING -> viewBobbing = enabled;
            case MASTER_VOLUME -> masterVolume = enabled;
            case ENTITY_DISTANCE -> entityDistance = enabled;
            case FRAMERATE_LIMIT -> framerateLimit = enabled;
            case VSYNC -> vsync = enabled;
            case FOV_EFFECTS -> fovEffects = enabled;
            case BRIGHTNESS -> brightness = enabled;
        }
    }

    public enum Setting {
        FOV("screen.serverprofiles.setting.fov"),
        SENSITIVITY("screen.serverprofiles.setting.sensitivity"),
        RENDER_DISTANCE("screen.serverprofiles.setting.render_distance"),
        SIMULATION_DISTANCE("screen.serverprofiles.setting.simulation_distance"),
        PARTICLES("screen.serverprofiles.setting.particles"),
        GUI_SCALE("screen.serverprofiles.setting.gui_scale"),
        VIEW_BOBBING("screen.serverprofiles.setting.view_bobbing"),
        MASTER_VOLUME("screen.serverprofiles.setting.master_volume"),
        ENTITY_DISTANCE("screen.serverprofiles.setting.entity_distance"),
        FRAMERATE_LIMIT("screen.serverprofiles.setting.framerate_limit"),
        VSYNC("screen.serverprofiles.setting.vsync"),
        FOV_EFFECTS("screen.serverprofiles.setting.fov_effects"),
        BRIGHTNESS("screen.serverprofiles.setting.brightness");

        private final String translationKey;

        Setting(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }
}
