package kr.pma.serverprofiles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ServerProfilesScreen extends Screen {
    private final Screen parent;
    private final ProfileManager manager;

    private String target;
    private Component status = Component.empty();

    public ServerProfilesScreen(Screen parent, ProfileManager manager) {
        super(Component.translatable("screen.serverprofiles.title"));
        this.parent = parent;
        this.manager = manager;
    }

    @Override
    protected void init() {
        target = manager.currentTarget(Minecraft.getInstance());
        ProfileManager.Match match = manager.findMatch(target);

        int buttonWidth = 200;
        int x = width / 2 - buttonWidth / 2;
        int y = 82;

        Button saveButton = Button.builder(
            Component.translatable("screen.serverprofiles.save"),
            button -> {
                manager.saveCurrentProfile(target, Minecraft.getInstance());
                status = Component.translatable("screen.serverprofiles.status.saved", target);
                rebuildWidgets();
            }
        ).bounds(x, y, buttonWidth, 20).build();
        saveButton.active = target != null;
        addRenderableWidget(saveButton);

        y += 24;

        Button applyButton = Button.builder(
            Component.translatable("screen.serverprofiles.apply"),
            button -> {
                ProfileManager.Match current = manager.findMatch(target);
                if (current != null && manager.applyMatchedProfile(target, Minecraft.getInstance())) {
                    status = Component.translatable("screen.serverprofiles.status.applied", current.key());
                    rebuildWidgets();
                }
            }
        ).bounds(x, y, buttonWidth, 20).build();
        applyButton.active = match != null && match.profile().settings != null;
        addRenderableWidget(applyButton);

        y += 24;

        Component enabledLabel = match == null || match.profile().enabled
            ? Component.translatable("screen.serverprofiles.profile_enabled")
            : Component.translatable("screen.serverprofiles.profile_disabled");

        Button enabledButton = Button.builder(
            enabledLabel,
            button -> {
                manager.toggleMatchedProfile(target);
                rebuildWidgets();
            }
        ).bounds(x, y, buttonWidth, 20).build();
        enabledButton.active = match != null;
        addRenderableWidget(enabledButton);

        y += 24;

        Button deleteButton = Button.builder(
            Component.translatable("screen.serverprofiles.delete"),
            button -> {
                manager.deleteExactProfile(target);
                status = Component.translatable("screen.serverprofiles.status.deleted", target);
                rebuildWidgets();
            }
        ).bounds(x, y, buttonWidth, 20).build();
        deleteButton.active = manager.hasExactProfile(target);
        addRenderableWidget(deleteButton);

        y += 30;

        Button restoreButton = Button.builder(
            Component.translatable("screen.serverprofiles.restore_previous"),
            button -> {
                if (manager.restorePrevious(Minecraft.getInstance())) {
                    status = Component.translatable("screen.serverprofiles.status.restored");
                } else {
                    status = Component.translatable("screen.serverprofiles.status.no_backup");
                }
                rebuildWidgets();
            }
        ).bounds(x, y, buttonWidth, 20).build();
        restoreButton.active = manager.hasSessionBackup();
        addRenderableWidget(restoreButton);

        y += 24;

        Component restoreLabel = manager.restoreOnDisconnect()
            ? Component.translatable("screen.serverprofiles.restore_on_disconnect_on")
            : Component.translatable("screen.serverprofiles.restore_on_disconnect_off");

        addRenderableWidget(Button.builder(
            restoreLabel,
            button -> {
                manager.toggleRestoreOnDisconnect();
                rebuildWidgets();
            }
        ).bounds(x, y, buttonWidth, 20).build());

        y += 30;

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.done"),
            button -> onClose()
        ).bounds(x, y, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(graphics, mouseX, mouseY, deltaTicks);

        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        Component targetText = target == null
            ? Component.translatable("screen.serverprofiles.not_connected")
            : Component.literal(target);
        graphics.drawCenteredString(
            font,
            Component.translatable("screen.serverprofiles.target", targetText),
            width / 2,
            44,
            0xC0C0C0
        );

        ProfileManager.Match match = manager.findMatch(target);
        Component profileText = match == null
            ? Component.translatable("screen.serverprofiles.no_profile")
            : Component.translatable("screen.serverprofiles.matched", match.key());
        graphics.drawCenteredString(font, profileText, width / 2, 58, 0xA0A0A0);

        if (!status.getString().isEmpty()) {
            graphics.drawCenteredString(font, status, width / 2, height - 32, 0xFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
