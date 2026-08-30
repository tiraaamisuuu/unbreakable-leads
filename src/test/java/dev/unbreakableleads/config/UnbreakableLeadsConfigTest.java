package dev.unbreakableleads.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class UnbreakableLeadsConfigTest {
    @Test
    void defaultsAreSafeAndProtectionIsEnabled() {
        UnbreakableLeadsConfig config = UnbreakableLeadsConfig.parse(new Properties(), ignored -> { });

        assertTrue(config.unbreakablePlayerHeld());
        assertTrue(config.unbreakableFenceAttachments());
        assertTrue(config.teleportExtremeSeparation());
        assertTrue(config.intentionalDetachmentAllowed());
        assertTrue(config.suppressBreakSoundAndItemDrop());
        assertEquals(24.0, config.catchUpDistance());
    }

    @Test
    void validValuesAreApplied() {
        Properties properties = new Properties();
        properties.setProperty("unbreakable_player_held", "false");
        properties.setProperty("unbreakable_fence_attachments", "FALSE");
        properties.setProperty("teleport_extreme_separation", "false");
        properties.setProperty("catch_up_distance", "128.5");
        properties.setProperty("intentional_detachment_allowed", "false");
        properties.setProperty("suppress_break_sound_and_item_drop", "false");

        UnbreakableLeadsConfig config = UnbreakableLeadsConfig.parse(properties, ignored -> { });

        assertFalse(config.unbreakablePlayerHeld());
        assertFalse(config.unbreakableFenceAttachments());
        assertFalse(config.teleportExtremeSeparation());
        assertFalse(config.intentionalDetachmentAllowed());
        assertFalse(config.suppressBreakSoundAndItemDrop());
        assertEquals(128.5, config.catchUpDistance());
    }

    @Test
    void malformedValuesFallBackIndividually() {
        Properties properties = new Properties();
        properties.setProperty("unbreakable_player_held", "sometimes");
        properties.setProperty("catch_up_distance", "NaN");
        List<String> warnings = new ArrayList<>();

        UnbreakableLeadsConfig config = UnbreakableLeadsConfig.parse(properties, warnings::add);

        assertTrue(config.unbreakablePlayerHeld());
        assertEquals(24.0, config.catchUpDistance());
        assertEquals(2, warnings.size());
    }

    @Test
    void outOfRangeDistanceFallsBack() {
        Properties properties = new Properties();
        properties.setProperty("catch_up_distance", "9999");
        List<String> warnings = new ArrayList<>();

        UnbreakableLeadsConfig config = UnbreakableLeadsConfig.parse(properties, warnings::add);

        assertEquals(24.0, config.catchUpDistance());
        assertEquals(1, warnings.size());
    }
}
