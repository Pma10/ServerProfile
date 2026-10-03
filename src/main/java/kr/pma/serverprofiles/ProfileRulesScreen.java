package kr.pma.serverprofiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProfileRulesScreen extends Screen {
    private final Screen parent;
    private final ProfileManager manager;
    private final String profileKey;

    public ProfileRulesScreen(Screen parent, ProfileManager manager, String profileKey) {
        super(Component.translatable("screen.serverprofiles.rules_title"));
        this.parent = parent;
        this.manager = manager;
        this.profileKey = manager.normalizeProfileKey(profileKey);
    }

    @Override
    protected void init() {
        ServerProfile profile = manager.getProfile(profileKey);
        if (profile == null) {
            onClose();
            return;
        }

        profile.normalize();

        int totalWidth = Math.min(360, width - 32);
        int gap = 4;
        int columnWidth = (totalWidth - gap) / 2;
        int left = width / 2 - totalWidth / 2;
        int startY = 52;
        int rowHeight = 22;

        ProfileRules.Setting[] settings = ProfileRules.Setting.values();

        for (int i = 0; i < settings.length; i++) {
            ProfileRules.Setting setting = settings[i];
            int column = i % 2;
            int row = i / 2;
            int x = left + column * (columnWidth + gap);
            int y = startY + row * rowHeight;

            addRenderableWidget(Button.builder(
                settingLabel(profile, setting),
                button -> {
                    manager.toggleRule(profileKey, setting);
                    rebuildWidgets();
                }
            ).bounds(x, y, columnWidth, 20).build());
        }

        int controlsY = startY + ((settings.length + 1) / 2) * rowHeight + 4;

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.enable_all"),
            button -> {
                manager.setAllRules(profileKey, true);
                rebuildWidgets();
            }
        ).bounds(left, controlsY, columnWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.disable_all"),
            button -> {
                manager.setAllRules(profileKey, false);
                rebuildWidgets();
            }
        ).bounds(left + columnWidth + gap, controlsY, columnWidth, 20).build());

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.done"),
            button -> onClose()
        ).bounds(left, controlsY + 26, totalWidth, 20).build());
    }

    private Component settingLabel(ServerProfile profile, ProfileRules.Setting setting) {
        boolean enabled = profile.rules.get(setting);
        return Component.translatable(
            "screen.serverprofiles.setting_toggle",
            Component.translatable(setting.translationKey()),
            Component.translatable(
                enabled
                    ? "screen.serverprofiles.value.on"
                    : "screen.serverprofiles.value.off"
            )
        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(graphics, mouseX, mouseY, deltaTicks);

        ServerProfile profile = manager.getProfile(profileKey);
        int enabled = profile == null ? 0 : profile.rules.enabledCount();

        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal(profileKey), width / 2, 30, 0xB8B8B8);
        graphics.drawCenteredString(
            font,
            Component.translatable(
                "screen.serverprofiles.managed_count",
                enabled,
                ProfileRules.Setting.values().length
            ),
            width / 2,
            40,
            0x909090
        );

        super.render(graphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
