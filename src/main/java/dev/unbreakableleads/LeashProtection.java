package dev.unbreakableleads;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class LeashProtection {
    private static final List<Vec3> SAFE_OFFSETS = List.of(
        new Vec3(0.0, 0.0, 0.0),
        new Vec3(2.0, 0.0, 0.0), new Vec3(-2.0, 0.0, 0.0),
        new Vec3(0.0, 0.0, 2.0), new Vec3(0.0, 0.0, -2.0),
        new Vec3(2.0, 0.0, 2.0), new Vec3(-2.0, 0.0, 2.0),
        new Vec3(2.0, 0.0, -2.0), new Vec3(-2.0, 0.0, -2.0),
        new Vec3(0.0, 1.0, 0.0), new Vec3(0.0, -1.0, 0.0)
    );

    public static boolean isProtected(final Leashable leashable) {
        Leashable.LeashData data = leashable.getLeashData();
        return data != null && isProtectedHolder(data.leashHolder);
    }

    public static boolean isProtectedHolder(final Entity holder) {
        if (holder instanceof LeashFenceKnotEntity) {
            return UnbreakableLeads.config().unbreakableFenceAttachments();
        }
        return holder instanceof net.minecraft.world.entity.player.Player
            && UnbreakableLeads.config().unbreakablePlayerHeld();
    }

    public static boolean isProtectedDelayed(final Leashable.LeashData data) {
        Either<UUID, BlockPos> delayed = data.delayedLeashInfo;
        if (delayed == null) {
            return false;
        }
        return delayed.right().isPresent()
            ? UnbreakableLeads.config().unbreakableFenceAttachments()
            : UnbreakableLeads.config().unbreakablePlayerHeld();
    }

    public static void beforeLeashTick(final ServerLevel level, final Entity entity) {
        if (!(entity instanceof Leashable leashable)) {
            return;
        }
        Leashable.LeashData data = leashable.getLeashData();
        if (data == null || data.leashHolder == null || data.leashHolder.canInteractWithLevel()) {
            return;
        }

        Entity oldHolder = data.leashHolder;
        Entity replacement = level.getEntity(oldHolder.getUUID());
        if (replacement == null && oldHolder instanceof LeashFenceKnotEntity knot && level.hasChunkAt(knot.getPos())) {
            replacement = LeashFenceKnotEntity.getKnot(level, knot.getPos()).orElse(null);
        }
        if (replacement != null && replacement != oldHolder && replacement.canInteractWithLevel()) {
            leashable.setLeashedTo(replacement, true);
            UnbreakableLeads.LOGGER.debug("Reattached {} to reloaded holder {}", entity.getUUID(), replacement.getUUID());
        }
    }

    public static boolean restoreDelayed(final ServerLevel level, final Entity entity, final Leashable.LeashData data) {
        if (!isProtectedDelayed(data) || data.delayedLeashInfo == null || !(entity instanceof Leashable leashable)) {
            return false;
        }

        Optional<UUID> holderUuid = data.delayedLeashInfo.left();
        if (holderUuid.isPresent()) {
            Entity holder = level.getEntity(holderUuid.get());
            if (holder != null && holder.canInteractWithLevel()) {
                leashable.setLeashedTo(holder, true);
                UnbreakableLeads.LOGGER.debug("Restored saved player-held lead for {}", entity.getUUID());
            }
            return true;
        }

        BlockPos knotPos = data.delayedLeashInfo.right().orElseThrow();
        if (level.hasChunkAt(knotPos)) {
            LeashFenceKnotEntity knot = LeashFenceKnotEntity.getOrCreateKnot(level, knotPos);
            leashable.setLeashedTo(knot, true);
            UnbreakableLeads.LOGGER.debug("Restored saved fence lead for {} at {}", entity.getUUID(), knotPos);
        }
        return true;
    }

    public static boolean shouldPreventUnavailableBreak(final Leashable leashable) {
        if (!isProtected(leashable) || !((Entity)leashable).canInteractWithLevel()) {
            return false;
        }
        Entity holder = leashable.getLeashData().leashHolder;
        Entity.RemovalReason reason = holder == null ? null : holder.getRemovalReason();
        return reason == Entity.RemovalReason.UNLOADED_TO_CHUNK
            || reason == Entity.RemovalReason.UNLOADED_WITH_PLAYER;
    }

    public static void onLeashedTick(final Leashable leashable, final Entity holder) {
        leashable.whenLeashedTo(holder);
        if (!isProtectedHolder(holder) || !UnbreakableLeads.config().teleportExtremeSeparation()) {
            return;
        }
        Entity entity = (Entity)leashable;
        double limit = UnbreakableLeads.config().catchUpDistance();
        if (entity.level() == holder.level() && entity.distanceToSqr(holder) > limit * limit) {
            teleportNearHolder(entity, holder);
        }
    }

    public static void teleportNearHolder(final Entity leashedEntity, final Entity holder) {
        if (!(holder.level() instanceof ServerLevel level) || leashedEntity.level() != level || !leashedEntity.isAlive()) {
            return;
        }
        Entity moving = leashedEntity.isPassenger() ? leashedEntity.getRootVehicle() : leashedEntity;
        if (moving == holder || moving.level() != level) {
            return;
        }

        Vec3 destination = findSafeDestination(level, moving, holder);
        moving.teleportTo(destination.x(), destination.y(), destination.z());
        moving.setDeltaMovement(holder.getKnownMovement());
        if (!UnbreakableLeads.config().suppressBreakSoundAndItemDrop()) {
            level.playSound(null, holder.getX(), holder.getY(), holder.getZ(), SoundEvents.LEAD_BREAK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        UnbreakableLeads.LOGGER.debug("Caught up leashed entity {} to holder {}", leashedEntity.getUUID(), holder.getUUID());
    }

    private static Vec3 findSafeDestination(final ServerLevel level, final Entity moving, final Entity holder) {
        Vec3 origin = holder.position();
        for (Vec3 offset : SAFE_OFFSETS) {
            Vec3 candidate = origin.add(offset);
            AABB candidateBox = moving.getBoundingBox().move(candidate.subtract(moving.position()));
            if (level.getWorldBorder().isWithinBounds(candidateBox) && level.noCollision(moving, candidateBox)) {
                return candidate;
            }
        }
        return origin;
    }

    public static boolean hasProtectedConnections(final Entity holder) {
        return Leashable.leashableLeashedTo(holder).stream().anyMatch(LeashProtection::isProtected);
    }

    public static List<Entity> protectedConnections(final Entity holder) {
        return Leashable.leashableLeashedTo(holder).stream()
            .filter(LeashProtection::isProtected)
            .map(leashable -> (Entity)leashable)
            .toList();
    }

    public static void resync(final ServerLevel level, final Entity entity, final Entity holder) {
        level.getChunkSource().sendToTrackingPlayers(entity, new ClientboundSetEntityLinkPacket(entity, holder));
    }

    private LeashProtection() {
    }
}
