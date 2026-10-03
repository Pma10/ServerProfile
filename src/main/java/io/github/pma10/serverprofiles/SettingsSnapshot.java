package io.github.pma10.serverprofiles;

import net.minecraft.client.Options;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundSource;

public final class SettingsSnapshot {
    public int fov;
    public double sensitivity;
    public int renderDistance;
    public int simulationDistance;
    public ParticleStatus particles;
    public int guiScale;
    public boolean viewBobbing;
    public double masterVolume;

    // Added in config version 2. Wrappers stay nullable so old profiles
    // do not overwrite settings that did not exist when they were saved.
    public Double entityDistanceScaling;
    public Integer framerateLimit;
    public Boolean vsync;
    public Double fovEffectScale;
    public Double gamma;

    public static SettingsSnapshot capture(Options options) {
        SettingsSnapshot snapshot = new SettingsSnapshot();
        snapshot.fov = options.fov().get();
        snapshot.sensitivity = options.sensitivity().get();
        snapshot.renderDistance = options.renderDistance().get();
        snapshot.simulationDistance = options.simulationDistance().get();
        snapshot.particles = options.particles().get();
        snapshot.guiScale = options.guiScale().get();
        snapshot.viewBobbing = options.bobView().get();
        snapshot.masterVolume = options.getSoundSourceOptionInstance(SoundSource.MASTER).get();
        snapshot.entityDistanceScaling = options.entityDistanceScaling().get();
        snapshot.framerateLimit = options.framerateLimit().get();
        snapshot.vsync = options.enableVsync().get();
        snapshot.fovEffectScale = options.fovEffectScale().get();
        snapshot.gamma = options.gamma().get();
        return snapshot;
    }

    public void apply(Options options) {
        apply(options, new ProfileRules());
    }

    public void apply(Options options, ProfileRules rules) {
        ProfileRules effectiveRules = rules == null ? new ProfileRules() : rules;

        if (effectiveRules.fov) {
            options.fov().set(fov);
        }
        if (effectiveRules.sensitivity) {
            options.sensitivity().set(sensitivity);
        }
        if (effectiveRules.renderDistance) {
            options.renderDistance().set(renderDistance);
        }
        if (effectiveRules.simulationDistance) {
            options.simulationDistance().set(simulationDistance);
        }
        if (effectiveRules.particles && particles != null) {
            options.particles().set(particles);
        }
        if (effectiveRules.guiScale) {
            options.guiScale().set(guiScale);
        }
        if (effectiveRules.viewBobbing) {
            options.bobView().set(viewBobbing);
        }
        if (effectiveRules.masterVolume) {
            options.getSoundSourceOptionInstance(SoundSource.MASTER).set(masterVolume);
        }
        if (effectiveRules.entityDistance && entityDistanceScaling != null) {
            options.entityDistanceScaling().set(entityDistanceScaling);
        }
        if (effectiveRules.framerateLimit && framerateLimit != null) {
            options.framerateLimit().set(framerateLimit);
        }
        if (effectiveRules.vsync && vsync != null) {
            options.enableVsync().set(vsync);
        }
        if (effectiveRules.fovEffects && fovEffectScale != null) {
            options.fovEffectScale().set(fovEffectScale);
        }
        if (effectiveRules.brightness && gamma != null) {
            options.gamma().set(gamma);
        }
    }
}
