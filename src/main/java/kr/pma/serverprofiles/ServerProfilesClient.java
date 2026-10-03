package kr.pma.serverprofiles;

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

    private final ProfileManager profileManager = new ProfileManager();

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

        ClientLifecycleEvents.CLIENT_STARTED.register(profileManager::restoreCrashBackup);
        ClientLifecycleEvents.CLIENT_STOPPING.register(profileManager::onClientStopping);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> profileManager.onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> profileManager.onDisconnect(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ServerProfilesScreen(null, profileManager));
                }
            }
        });
    }
}
