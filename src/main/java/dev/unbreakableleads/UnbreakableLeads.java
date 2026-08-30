package dev.unbreakableleads;

import dev.unbreakableleads.config.UnbreakableLeadsConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnbreakableLeads implements ModInitializer {
    public static final String MOD_ID = "unbreakable_leads";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile UnbreakableLeadsConfig config = UnbreakableLeadsConfig.defaults();

    @Override
    public void onInitialize() {
        config = UnbreakableLeadsConfig.load(
            FabricLoader.getInstance().getConfigDir().resolve("unbreakable-leads.properties"),
            LOGGER
        );
        LOGGER.info("Unbreakable Leads {} initialized (player-held: {}, fence: {}, catch-up: {})",
            FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata().getVersion(),
            config.unbreakablePlayerHeld(),
            config.unbreakableFenceAttachments(),
            config.teleportExtremeSeparation());
    }

    public static UnbreakableLeadsConfig config() {
        return config;
    }

    private UnbreakableLeads() {
    }
}
