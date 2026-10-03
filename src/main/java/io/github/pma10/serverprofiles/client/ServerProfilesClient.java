package io.github.pma10.serverprofiles.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.pma10.serverprofiles.ServerProfiles;
import io.github.pma10.serverprofiles.profile.ProfileManager;
import io.github.pma10.serverprofiles.screen.ServerProfilesScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
//? if >=1.21.9 {
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import org.lwjgl.glfw.GLFW;

public final class ServerProfilesClient implements ClientModInitializer {
    private static final ProfileManager PROFILE_MANAGER = new ProfileManager();

    public static ProfileManager profileManager() {
        return PROFILE_MANAGER;
    }

    @Override
    public void onInitializeClient() {
        //? if >=1.21.9 {
        KeyMapping.Category category = KeyMapping.Category.register(
            //? if >=1.21.11 {
            Identifier.fromNamespaceAndPath(ServerProfiles.MOD_ID, "general")
            //?} else
            /*ResourceLocation.fromNamespaceAndPath(ServerProfiles.MOD_ID, "general")*/
        );
        //?}

        KeyMapping openScreen = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.serverprofiles.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            //? if >=1.21.9 {
            category
            //?} else
            /*"key.category.serverprofiles.general"*/
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
