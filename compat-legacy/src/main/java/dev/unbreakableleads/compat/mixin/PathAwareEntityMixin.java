package dev.unbreakableleads.compat.mixin;

import dev.unbreakableleads.compat.LeashProtectionCompat;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Legacy 1.16–1.20 leash simulation keeps snap logic in PathAwareEntity.updateLeash. */
@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin {
    @Inject(method = "updateLeash", at = @At("HEAD"), cancellable = true)
    private void unbreakableLeads$keepLongLeash(final CallbackInfo ci) {
        if (LeashProtectionCompat.protects((PathAwareEntity) (Object) this)) {
            ci.cancel();
        }
    }
}
