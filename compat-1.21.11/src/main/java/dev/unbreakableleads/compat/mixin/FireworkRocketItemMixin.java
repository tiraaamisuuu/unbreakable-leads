package dev.unbreakableleads.compat.mixin;

import dev.unbreakableleads.compat.LeashProtectionCompat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {
    @Redirect(
        method = "use",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;detachAllHeldLeashes(Lnet/minecraft/entity/player/PlayerEntity;)Z")
    )
    private boolean unbreakableLeads$keepDuringElytra(final PlayerEntity player, final PlayerEntity source) {
        return !LeashProtectionCompat.hasProtectedConnections(player) && player.detachAllHeldLeashes(source);
    }
}
