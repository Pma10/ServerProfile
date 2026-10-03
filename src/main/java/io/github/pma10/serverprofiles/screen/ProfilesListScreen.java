package io.github.pma10.serverprofiles.screen;

import io.github.pma10.serverprofiles.profile.ProfileManager;
import io.github.pma10.serverprofiles.profile.ProfileRules;
import io.github.pma10.serverprofiles.profile.ServerProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ProfilesListScreen extends Screen {
    private final ServerProfilesScreen parent;
    private final ProfileManager manager;

    private String selectedKey;
    private String confirmDeleteKey;
    private int page;

    public ProfilesListScreen(ServerProfilesScreen parent, ProfileManager manager, String selectedKey) {
        super(Component.translatable("screen.serverprofiles.manage_title"));
        this.parent = parent;
        this.manager = manager;
        this.selectedKey = selectedKey;
    }

    @Override
    protected void init() {
        List<String> keys = manager.profileKeys();
        int pageSize = height >= 300 ? 5 : 3;
        int pageCount = Math.max(1, (keys.size() + pageSize - 1) / pageSize);

        page = Math.max(0, Math.min(page, pageCount - 1));

        if (selectedKey != null && manager.getProfile(selectedKey) != null) {
            int selectedIndex = keys.indexOf(selectedKey);
            if (selectedIndex >= 0) {
                page = selectedIndex / pageSize;
            }
        } else {
            int firstOnPage = page * pageSize;
            selectedKey = firstOnPage < keys.size() ? keys.get(firstOnPage) : null;
        }

        int contentWidth = Math.min(320, width - 40);
        int x = width / 2 - contentWidth / 2;
        int y = 52;

        int start = page * pageSize;
        int end = Math.min(keys.size(), start + pageSize);

        for (int i = start; i < end; i++) {
            String key = keys.get(i);
            ServerProfile profile = manager.getProfile(key);
            boolean selected = key.equals(selectedKey);

            Component label = Component.literal(
                (selected ? "> " : "") +
                    (profile != null && profile.enabled ? "[ON] " : "[OFF] ") +
                    key
            );

            addRenderableWidget(Button.builder(
                label,
                button -> {
                    selectedKey = key;
                    confirmDeleteKey = null;
                    rebuildWidgets();
                }
            ).bounds(x, y, contentWidth, 20).build());

            y += 22;
        }

        int halfWidth = (contentWidth - 4) / 2;

        Button previousButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.previous"),
            button -> {
                page = Math.max(0, page - 1);
                selectedKey = null;
                confirmDeleteKey = null;
                rebuildWidgets();
            }
        ).bounds(x, y + 2, halfWidth, 20).build());
        previousButton.active = page > 0;

        Button nextButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.next"),
            button -> {
                page = Math.min(pageCount - 1, page + 1);
                selectedKey = null;
                confirmDeleteKey = null;
                rebuildWidgets();
            }
        ).bounds(x + halfWidth + 4, y + 2, halfWidth, 20).build());
        nextButton.active = page + 1 < pageCount;

        y += 26;

        ServerProfile selectedProfile = selectedKey == null ? null : manager.getProfile(selectedKey);
        if (selectedProfile != null) {
            selectedProfile.normalize();
        }

        Button toggleButton = addRenderableWidget(Button.builder(
            selectedProfile != null && selectedProfile.enabled
                ? Component.translatable("screen.serverprofiles.disable")
                : Component.translatable("screen.serverprofiles.enable"),
            button -> {
                manager.toggleProfile(selectedKey);
                confirmDeleteKey = null;
                rebuildWidgets();
            }
        ).bounds(x, y, halfWidth, 20).build());
        toggleButton.active = selectedProfile != null;

        Button applyButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.apply_selected"),
            button -> {
                manager.applyProfile(selectedKey, Minecraft.getInstance());
                confirmDeleteKey = null;
                rebuildWidgets();
            }
        ).bounds(x + halfWidth + 4, y, halfWidth, 20).build());
        applyButton.active =
            manager.currentTarget(Minecraft.getInstance()) != null &&
                selectedProfile != null &&
                selectedProfile.settings != null;

        y += 24;

        Button editButton = addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.use_in_editor"),
            button -> {
                if (selectedKey != null) {
                    parent.selectProfile(selectedKey);
                    onClose();
                }
            }
        ).bounds(x, y, halfWidth, 20).build());
        editButton.active = selectedProfile != null;

        Button rulesButton = addRenderableWidget(Button.builder(
            selectedProfile == null
                ? Component.translatable("screen.serverprofiles.managed_settings_short")
                : Component.translatable(
                    "screen.serverprofiles.managed_settings_short_count",
                    selectedProfile.rules.enabledCount(),
                    ProfileRules.Setting.values().length
                ),
            button -> Minecraft.getInstance().setScreen(
                new ProfileRulesScreen(this, manager, selectedKey)
            )
        ).bounds(x + halfWidth + 4, y, halfWidth, 20).build());
        rulesButton.active = selectedProfile != null;

        y += 24;

        boolean confirming = selectedKey != null && selectedKey.equals(confirmDeleteKey);
        Button deleteButton = addRenderableWidget(Button.builder(
            confirming
                ? Component.translatable("screen.serverprofiles.confirm_delete")
                : Component.translatable("screen.serverprofiles.delete"),
            button -> {
                if (selectedKey == null) {
                    return;
                }

                if (!selectedKey.equals(confirmDeleteKey)) {
                    confirmDeleteKey = selectedKey;
                    rebuildWidgets();
                    return;
                }

                manager.deleteProfile(selectedKey);
                selectedKey = null;
                confirmDeleteKey = null;
                rebuildWidgets();
            }
        ).bounds(x, y, halfWidth, 20).build());
        deleteButton.active = selectedProfile != null;

        addRenderableWidget(Button.builder(
            Component.translatable("screen.serverprofiles.done"),
            button -> onClose()
        ).bounds(x + halfWidth + 4, y, halfWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float deltaTicks) {
        //? if >=1.20.2 {
        renderBackground(graphics, mouseX, mouseY, deltaTicks);
        //?} else
        /*renderBackground(graphics);*/

        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        graphics.drawCenteredString(
            font,
            Component.translatable("screen.serverprofiles.profile_count", manager.profileKeys().size()),
            width / 2,
            34,
            0xA0A0A0
        );

        super.render(graphics, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
