package dev.unbreakableleads.mixin;

import dev.unbreakableleads.LeashProtection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Prevents the dedicated elytra-firework path from dropping every player-held lead. */
@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {
    @Redirect(
        method = "use",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;dropAllLeashConnections(Lnet/minecraft/world/entity/player/Player;)Z")
    )
    private boolean unbreakableLeads$keepConnectionsDuringBoost(final Entity player, final net.minecraft.world.entity.player.Player source) {
        return !LeashProtection.hasProtectedConnections(player) && player.dropAllLeashConnections(source);
    }
}
