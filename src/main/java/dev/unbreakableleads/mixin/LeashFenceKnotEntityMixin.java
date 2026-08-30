package dev.unbreakableleads.mixin;

import dev.unbreakableleads.LeashProtection;
import dev.unbreakableleads.UnbreakableLeads;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stops a knot interaction from transferring or removing existing leads when intentional detach is disabled. */
@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotEntityMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void unbreakableLeads$preventKnotDetach(
        final Player player,
        final InteractionHand hand,
        final Vec3 location,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (UnbreakableLeads.config().intentionalDetachmentAllowed()) {
            return;
        }
        Entity knot = (Entity)(Object)this;
        boolean playerIsAddingLeads = !Leashable.leashableLeashedTo(player).isEmpty();
        if (!playerIsAddingLeads && LeashProtection.hasProtectedConnections(knot)) {
            cir.setReturnValue(InteractionResult.SUCCESS.withoutItem());
        }
    }
}
