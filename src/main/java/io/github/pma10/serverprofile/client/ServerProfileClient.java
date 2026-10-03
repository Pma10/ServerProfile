package io.github.pma10.serverprofile.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.pma10.serverprofile.ServerProfileMod;
import io.github.pma10.serverprofile.compat.MinecraftCompat;
import io.github.pma10.serverprofile.profile.ProfileManager;
import io.github.pma10.serverprofile.screen.ServerProfileScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?} else
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;*/
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
//? if <1.21.9
/*import net.minecraft.client.gui.screens.Screen;*/
//? if >=1.21.9 {
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else
/*import net.minecraft.resources.ResourceLocation;*/
//?}
//? if <26.1
/*import org.lwjgl.glfw.GLFW;*/

public final class ServerProfileClient implements ClientModInitializer {
    private static final ProfileManager PROFILE_MANAGER = new ProfileManager();

    public static ProfileManager profileManager() {
        return PROFILE_MANAGER;
    }

    @Override
    public void onInitializeClient() {
        //? if >=1.21.9 {
        KeyMapping.Category category = KeyMapping.Category.register(
            //? if >=1.21.11 {
            Identifier.fromNamespaceAndPath(ServerProfileMod.MOD_ID, "general")
            //?} else
            /*ResourceLocation.fromNamespaceAndPath(ServerProfileMod.MOD_ID, "general")*/
        );
        //?}

        //? if >=26.3 {
        KeyMapping openScreen = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.serverprofile.open",
            InputConstants.KEY_O,
            category
        ));
        //?} else if >=26.1 {
        /*KeyMapping openScreen = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.serverprofile.open",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_O,
            category
        ));*/
        //?} else if >=1.21.9 {
        /*KeyMapping openScreen = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.serverprofile.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            category
        ));*/
        //?} else {
        /*KeyMapping openScreen = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.serverprofile.open",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.category.serverprofile.general"
        ));*/
        //?}

        ClientLifecycleEvents.CLIENT_STARTED.register(PROFILE_MANAGER::restoreCrashBackup);
        ClientLifecycleEvents.CLIENT_STOPPING.register(PROFILE_MANAGER::onClientStopping);

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> PROFILE_MANAGER.onJoin(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PROFILE_MANAGER.onDisconnect(client));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openScreen.consumeClick()) {
                //? if >=1.21.9 {
                boolean controlDown = client.hasControlDown();
                //?} else
                /*boolean controlDown = Screen.hasControlDown();*/

                if (controlDown && MinecraftCompat.currentScreen(client) == null) {
                    MinecraftCompat.setScreen(new ServerProfileScreen(null, PROFILE_MANAGER));
                }
            }
        });
    }
}
