package io.github.pma10.serverprofile.mixin.client;

import io.github.pma10.serverprofile.client.ServerProfileClient;
import io.github.pma10.serverprofile.compat.MinecraftCompat;
import io.github.pma10.serverprofile.screen.ServerProfileScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21 {
import net.minecraft.client.gui.screens.options.OptionsScreen;
//?} else
/*import net.minecraft.client.gui.screens.OptionsScreen;*/
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin extends Screen {
    protected OptionsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void serverprofile$addButton(CallbackInfo ci) {
        addRenderableWidget(
            Button.builder(
                Component.translatable("screen.serverprofile.open"),
                button -> MinecraftCompat.setScreen(
                    new ServerProfileScreen(this, ServerProfileClient.profileManager())
                )
            ).bounds(width - 106, 6, 100, 20).build()
        );
    }
}
