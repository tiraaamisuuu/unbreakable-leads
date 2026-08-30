package dev.unbreakableleads.mixin;

import dev.unbreakableleads.LeashProtection;
import dev.unbreakableleads.UnbreakableLeads;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the intentional-detachment option at the two vanilla player/dispenser detachment entry points. */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void unbreakableLeads$preventDirectDetach(
        final Player player,
        final InteractionHand hand,
        final Vec3 location,
        final CallbackInfoReturnable<InteractionResult> cir
    ) {
        Entity entity = (Entity)(Object)this;
        if (!UnbreakableLeads.config().intentionalDetachmentAllowed()
            && entity instanceof Leashable leashable
            && LeashProtection.isProtected(leashable)
            && leashable.getLeashHolder() == player) {
            cir.setReturnValue(InteractionResult.SUCCESS.withoutItem());
        }
    }

    @Inject(method = "shearOffAllLeashConnections", at = @At("HEAD"), cancellable = true)
    private void unbreakableLeads$preventShearing(
        final Player player,
        final CallbackInfoReturnable<Boolean> cir
    ) {
        if (UnbreakableLeads.config().intentionalDetachmentAllowed()) {
            return;
        }
        Entity entity = (Entity)(Object)this;
        boolean protectedSelf = entity instanceof Leashable leashable && LeashProtection.isProtected(leashable);
        if (protectedSelf || LeashProtection.hasProtectedConnections(entity)) {
            cir.setReturnValue(false);
        }
    }
}
