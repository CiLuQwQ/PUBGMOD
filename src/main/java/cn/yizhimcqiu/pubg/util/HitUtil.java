package cn.yizhimcqiu.pubg.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class HitUtil {
    public static BlockHitResult getPlayerPOVHitResult(Level p_41436_, Player p_41437_, ClipContext.Fluid p_41438_, double reach) {
        float f = p_41437_.getXRot();
        float f1 = p_41437_.getYRot();
        Vec3 vec3 = p_41437_.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        Vec3 vec31 = vec3.add((double)f6 * reach, (double)f5 * reach, (double)f7 * reach);
        return p_41436_.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, p_41438_, p_41437_));
    }
    public static Optional<Entity> getTargetedEntity(@Nullable Entity p_113449_, int p_113450_) {
        if (p_113449_ == null) {
            return Optional.empty();
        } else {
            Vec3 vec3 = p_113449_.getEyePosition();
            Vec3 vec31 = p_113449_.getViewVector(1.0F).scale((double)p_113450_);
            Vec3 vec32 = vec3.add(vec31);
            AABB aabb = p_113449_.getBoundingBox().expandTowards(vec31).inflate(1.0D);
            int i = p_113450_ * p_113450_;
            Predicate<Entity> predicate = (p_113447_) -> {
                return !p_113447_.isSpectator() && p_113447_.isPickable();
            };
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(p_113449_, vec3, vec32, aabb, predicate, (double)i);
            if (entityhitresult == null) {
                return Optional.empty();
            } else {
                return vec3.distanceToSqr(entityhitresult.getLocation()) > (double)i ? Optional.empty() : Optional.of(entityhitresult.getEntity());
            }
        }
    }
    public static boolean hasNoBlockInTheRayWay(Player player, Entity entity) {
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(entity.getX() - player.getX(), (entity.getY()+entity.getBoundingBox().getYsize()) - player.getEyeY(), entity.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        return d1 > 1.0D - 0.025D / d0;
    }
}
