package io.github.pma10.serverprofiles;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class ServerProfilesClient implements ClientModInitializer {
    public static final String MOD_ID = "serverprofiles";

    private static final ProfileManager PROFILE_MANAGER = new ProfileManager();

    public static ProfileManager profileManager() {
        return PROFILE_MANAGER;
    }

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MOD_ID, "general")
        );

        KeyMapping openScreen = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.serverprofiles.open",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_O,
            category
        ));

        ClientLifecycleEvents.CLIENT_STARTED.register(PROFILE_MANAGER::restoreCrashBackup);
        ClientLifecycleEvents.CLIENT_STOPPING.register(PROFILE_MANAGER::onClientStopping);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> PROFILE_MANAGER.onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PROFILE_MANAGER.onDisconnect(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ServerProfilesScreen(null, PROFILE_MANAGER));
                }
            }
        });
    }
}
