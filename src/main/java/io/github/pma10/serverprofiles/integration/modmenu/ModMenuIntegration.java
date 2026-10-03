package io.github.pma10.serverprofiles.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.pma10.serverprofiles.client.ServerProfilesClient;
import io.github.pma10.serverprofiles.screen.ServerProfilesScreen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ServerProfilesScreen(parent, ServerProfilesClient.profileManager());
    }
}
