package io.github.pma10.serverprofile.screen;

import io.github.pma10.serverprofile.compat.MinecraftCompat;
import io.github.pma10.serverprofile.compat.ScreenGraphics;
import io.github.pma10.serverprofile.profile.ProfileManager;
import io.github.pma10.serverprofile.profile.ProfileRules;
import io.github.pma10.serverprofile.profile.ServerProfile;
import io.github.pma10.serverprofile.profile.SettingsSnapshot;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else
/*import net.minecraft.client.gui.GuiGraphics;*/
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ServerProfileScreen extends Screen {
    private final Screen parent;
    private final ProfileManager manager;

    private String target;
    private String draftKey;
    private Component status = Component.empty();

    private EditBox profileKeyBox;
    private Button saveButton;
    private Button applyButton;
    private Button enabledButton;
    private Button rulesButton;

    public ServerProfileScreen(Screen parent, ProfileManager manager) {
        super(Component.translatable("screen.serverprofile.title"));
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    protected void init() {
        target = manager.currentTarget(Minecraft.getInstance());
        ProfileManager.Match match = manager.findMatch(target);

        if (draftKey == null) {
            draftKey = match != null ? match.key() : target != null ? target : "";
        }

        int contentWidth = Math.min(300, width - 40);
        int x = width / 2 - contentWidth / 2;
        int halfWidth = (contentWidth - 4) / 2;

        profileKeyBox = new EditBox(
            font,
            x,
            78,
            contentWidth,
            20,
            Component.translatable("screen.serverprofile.profile_key")
        );
        profileKeyBox.setMaxLength(255);
        profileKeyBox.setValue(draftKey);
        profileKeyBox.setResponder(value -> {
            draftKey = value;
            updateButtonState();
        });
        addRenderableWidget(profileKeyBox);

        saveButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofile.save_as"),
            button -> saveCurrentSettings()
        ).bounds(x, 104, halfWidth, 20).build());

        applyButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofile.apply_selected"),
            button -> {
                String key = manager.normalizeProfileKey(draftKey);
                if (manager.applyProfile(key, Minecraft.getInstance())) {
                    draftKey = key;
                    profileKeyBox.setValue(key);
                    status = Component.translatable("screen.serverprofile.status.applied", key);
                    updateButtonState();
                }
            }
        ).bounds(x + halfWidth + 4, 104, halfWidth, 20).build());

        enabledButton = addRenderableWidget(Button.builder(
            Component.empty(),
            button -> {
                Boolean enabled = manager.toggleProfile(draftKey);
                if (enabled != null) {
                    status = Component.translatable(
                        enabled
                            ? "screen.serverprofile.status.enabled"
                            : "screen.serverprofile.status.disabled",
                        manager.normalizeProfileKey(draftKey)
                    );
                }
                updateButtonState();
            }
        ).bounds(x, 128, halfWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofile.manage"),
            button -> MinecraftCompat.setScreen(
                new ProfileListScreen(this, manager, manager.normalizeProfileKey(draftKey))
            )
        ).bounds(x + halfWidth + 4, 128, halfWidth, 20).build());

        rulesButton = addRenderableWidget(Button.builder(
            Component.empty(),
            button -> MinecraftCompat.setScreen(
                new ProfileRulesScreen(this, manager, manager.normalizeProfileKey(draftKey))
            )
        ).bounds(x, 152, contentWidth, 20).build());

        Button restoreButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofile.restore_previous"),
            button -> {
                if (manager.restorePrevious(Minecraft.getInstance())) {
                    status = Component.translatable("screen.serverprofile.status.restored");
                } else {
                    status = Component.translatable("screen.serverprofile.status.no_backup");
                }
                updateButtonState();
            }
        ).bounds(x, 176, halfWidth, 20).build());
        restoreButton.active = manager.hasSessionBackup();

        addRenderableWidget(Button.builder(
            manager.restoreOnDisconnect()
                ? Component.translatable("screen.serverprofile.restore_on_disconnect_on")
                : Component.translatable("screen.serverprofile.restore_on_disconnect_off"),
            button -> {
                manager.toggleRestoreOnDisconnect();
                rebuildWidgets();
            }
        ).bounds(x + halfWidth + 4, 176, halfWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofile.done"),
            button -> onClose()
        ).bounds(x, 200, contentWidth, 20).build());

        updateButtonState();
    }

    public void selectProfile(String key) {
        draftKey = key;
        status = Component.empty();
    }

    private void saveCurrentSettings() {
        String savedKey = manager.saveCurrentProfile(draftKey, Minecraft.getInstance());
        if (savedKey == null) {
            status = Component.translatable("screen.serverprofile.status.invalid_key");
            return;
        }

        draftKey = savedKey;
        profileKeyBox.setValue(savedKey);
        status = Component.translatable("screen.serverprofile.status.saved", savedKey);
        updateButtonState();
    }

    private void updateButtonState() {
        if (saveButton == null || applyButton == null || enabledButton == null || rulesButton == null) {
            return;
        }

        boolean valid = manager.isValidProfileKey(draftKey);
        ServerProfile profile = valid ? manager.getProfile(draftKey) : null;

        saveButton.active = valid;
        applyButton.active = target != null && profile != null && profile.settings != null;
        enabledButton.active = profile != null;
        rulesButton.active = profile != null;

        if (profile == null) {
            enabledButton.setMessage(Component.translatable("screen.serverprofile.profile_missing"));
            rulesButton.setMessage(Component.translatable("screen.serverprofile.managed_settings_unavailable"));
        } else {
            profile.normalize();

            enabledButton.setMessage(Component.translatable(
                profile.enabled
                    ? "screen.serverprofile.profile_enabled"
                    : "screen.serverprofile.profile_disabled"
            ));

            rulesButton.setMessage(Component.translatable(
                "screen.serverprofile.managed_settings",
                profile.rules.enabledCount(),
                ProfileRules.Setting.values().length
            ));
        }
    }

    @Override
    //? if >=26.1 {
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
    //?} else
    /*public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {*/
        //? if <26.1 {
        //? if >=1.20.2 {
        /*renderBackground(graphics, mouseX, mouseY, deltaTicks);*/
        //?} else
        /*renderBackground(graphics);*/
        //?}

        ScreenGraphics.centered(graphics, font, title, width / 2, 18, 0xFFFFFF);

        Component targetText = target == null
            ? Component.translatable("screen.serverprofile.not_connected")
            : Component.literal(target);
        ScreenGraphics.centered(
            graphics,
            font,
            Component.translatable("screen.serverprofile.target", targetText),
            width / 2,
            38,
            0xC0C0C0
        );

        ProfileManager.Match match = manager.findMatch(target);
        Component matchText = match == null
            ? Component.translatable("screen.serverprofile.no_profile")
            : Component.translatable("screen.serverprofile.matched", match.key());
        ScreenGraphics.centered(graphics, font, matchText, width / 2, 52, 0xA0A0A0);

        ScreenGraphics.text(
            graphics,
            font,
            Component.translatable("screen.serverprofile.profile_key"),
            width / 2 - Math.min(300, width - 40) / 2,
            66,
            0xA0A0A0
        );

        ServerProfile profile = manager.getProfile(draftKey);
        if (height >= 300 && profile != null && profile.settings != null) {
            SettingsSnapshot settings = profile.settings;

            ScreenGraphics.centered(
                graphics,
                font,
                Component.translatable(
                    "screen.serverprofile.summary.primary",
                    settings.fov,
                    Math.round(settings.sensitivity * 200.0),
                    settings.renderDistance,
                    settings.simulationDistance
                ),
                width / 2,
                232,
                0xB8B8B8
            );

            Object fps = settings.framerateLimit == null
                ? "-"
                : settings.framerateLimit >= 260
                    ? Component.translatable("screen.serverprofile.value.unlimited")
                    : settings.framerateLimit;
            Object vsync = settings.vsync == null
                ? "-"
                : Component.translatable(
                    settings.vsync
                        ? "screen.serverprofile.value.on"
                        : "screen.serverprofile.value.off"
                );

            ScreenGraphics.centered(
                graphics,
                font,
                Component.translatable(
                    "screen.serverprofile.summary.performance",
                    settings.particles == null ? "-" : settings.particles.name(),
                    Math.round(settings.masterVolume * 100.0),
                    fps,
                    vsync
                ),
                width / 2,
                246,
                0x969696
            );

            Object entityDistance = settings.entityDistanceScaling == null
                ? "-"
                : Math.round(settings.entityDistanceScaling * 100.0);
            Object fovEffects = settings.fovEffectScale == null
                ? "-"
                : Math.round(settings.fovEffectScale * 100.0);
            Object brightness = settings.gamma == null
                ? "-"
                : Math.round(settings.gamma * 100.0);
            Object guiScale = settings.guiScale == 0
                ? Component.translatable("screen.serverprofile.value.auto")
                : settings.guiScale;

            ScreenGraphics.centered(
                graphics,
                font,
                Component.translatable(
                    "screen.serverprofile.summary.visual",
                    entityDistance,
                    fovEffects,
                    brightness,
                    guiScale
                ),
                width / 2,
                260,
                0x808080
            );
        }

        if (!status.getString().isEmpty()) {
            ScreenGraphics.centered(graphics, font, status, width / 2, height - 16, 0xFFFFFF);
        }

        //? if <26.1
        /*super.render(graphics, mouseX, mouseY, deltaTicks);*/
    }

    @Override
    public void onClose() {
        MinecraftCompat.setScreen(parent);
    }
}
