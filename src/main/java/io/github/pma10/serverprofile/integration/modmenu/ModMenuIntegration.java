package io.github.pma10.serverprofile.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.pma10.serverprofile.client.ServerProfileClient;
import io.github.pma10.serverprofile.screen.ServerProfileScreen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ServerProfileScreen(parent, ServerProfileClient.profileManager());
    }
}
