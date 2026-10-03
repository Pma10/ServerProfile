package kr.pma.serverprofiles;

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
        return snapshot;
    }

    public void apply(Options options) {
        options.fov().set(fov);
        options.sensitivity().set(sensitivity);
        options.renderDistance().set(renderDistance);
        options.simulationDistance().set(simulationDistance);

        if (particles != null) {
            options.particles().set(particles);
        }

        options.guiScale().set(guiScale);
        options.bobView().set(viewBobbing);
        options.getSoundSourceOptionInstance(SoundSource.MASTER).set(masterVolume);
    }
}
