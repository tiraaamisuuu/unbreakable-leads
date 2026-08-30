package dev.unbreakableleads.mixin;

import dev.unbreakableleads.LeashProtection;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Captures nearby attached entities around a same-dimension player teleport and moves them after success. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Unique
    private List<Entity> unbreakableLeads$teleportingConnections = List.of();

    @Inject(method = "teleport", at = @At("HEAD"))
    private void unbreakableLeads$beforeTeleport(
        final TeleportTransition transition,
        final CallbackInfoReturnable<ServerPlayer> cir
    ) {
        ServerPlayer player = (ServerPlayer)(Object)this;
        if (transition.newLevel() == player.level()) {
            this.unbreakableLeads$teleportingConnections = LeashProtection.protectedConnections(player);
        }
    }

    @Inject(method = "teleport", at = @At("RETURN"))
    private void unbreakableLeads$afterTeleport(
        final TeleportTransition transition,
        final CallbackInfoReturnable<ServerPlayer> cir
    ) {
        ServerPlayer player = cir.getReturnValue();
        if (player != null && transition.newLevel() == player.level()) {
            for (Entity entity : this.unbreakableLeads$teleportingConnections) {
                LeashProtection.teleportNearHolder(entity, player);
            }
        }
        this.unbreakableLeads$teleportingConnections = List.of();
    }
}
