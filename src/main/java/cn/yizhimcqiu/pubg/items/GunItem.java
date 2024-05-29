package cn.yizhimcqiu.pubg.items;

import cn.yizhimcqiu.pubg.registries.ModItems;
import cn.yizhimcqiu.pubg.util.HitUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GunItem extends Item {
    public static final SoundEvent NO_ENOUGH_BULLET = SoundEvents.BLAZE_DEATH;
    public static final SoundEvent SHOOT_BULLET = SoundEvents.BLAZE_HURT;
    public static final SoundEvent HIT_TARGET = SoundEvents.ARROW_HIT_PLAYER;
    protected GunType type;
    protected float damageBonus;
    public GunItem(Properties properties, GunType type) {
        this(properties, type, 0);
    }
    public GunItem(Properties properties, GunType type, float damageBonus) {
        super(properties);
        this.type = type;
        this.damageBonus = damageBonus;
    }
    public float getAttackDamage() {
        return this.damageBonus + this.type.baseDamage;
    }
    private void shoot(Level level, Player player, ItemStack stack) {
        HitUtil.getTargetedEntity(player, this.type.getRange()).ifPresent((entity) -> {
            if (HitUtil.hasNoBlockInTheRayWay(player, entity)) {
                entity.hurt(player.damageSources().playerAttack(player), this.getAttackDamage());
            }
        });
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        player.getCooldowns().addCooldown(this, this.type.getShotInterval());
        player.playSound(SHOOT_BULLET);
    }
    private void clientShoot(Level level, Player player) {
        HitUtil.getTargetedEntity(player, this.type.getRange()).ifPresent((entity) -> {
            player.playSound(HIT_TARGET);
        });
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.BLOCK && level.getBlockState(hitResult.getBlockPos()).is(Blocks.TARGET)) {
            player.playSound(HIT_TARGET);
        }
        player.playSound(SHOOT_BULLET);
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        boolean hasBullet = false;
        ItemStack stack = player.getItemInHand(hand);
        if (!player.getAbilities().instabuild) {
            for (ItemStack item : player.getInventory().items) {
                if (item.is(ModItems.BULLET.get())) {
                    hasBullet = true;
                    if (!level.isClientSide()) {
                        item.shrink(1);
                        shoot(level, player, stack);
                        break;
                    } else {
                        clientShoot(level, player);
                    }
                }
            }
            if (!hasBullet) {
                player.playSound(NO_ENOUGH_BULLET);
            }
        } else {
            shoot(level, player, stack);
            if (level.isClientSide()) {
                player.playSound(SoundEvents.BLAZE_HURT);
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
        list.add(this.type.getName());
        list.add(Component.translatable("gun_types.attribute.shot_interval", this.type.getShotInterval()));
        list.add(Component.translatable("gun_types.attribute.attack_damage", this.type.getBaseDamage(), this.damageBonus));
        list.add(Component.translatable("gun_types.attribute.range", this.type.getRange()));
    }

    public enum GunType {
        AUTOMATIC_RIFLES(4, 7, Component.translatable("gun_types.automatic_refles"), 64), // 自动步枪
        SHOT_GUN(10, 18, Component.translatable("gun_types.shot_gun"), 5), // 霰弹枪
        SUBMACHINE_GUN(2, 4, Component.translatable("gun_types.submachine_gun"), 20), // 冲锋枪
        SNIPER_GUN(20, 40, Component.translatable("gun_types.sniper_gun"), 1024); // 狙击枪
        private final int shotInterval;
        private final float baseDamage;
        private final Component name;
        private final int range;
        GunType(int interval, float damage, Component name, int range) {
            shotInterval = interval;
            baseDamage = damage;
            this.name = name;
            this.range = range;
        }

        public double getBaseDamage() {
            return baseDamage;
        }

        public int getShotInterval() {
            return shotInterval;
        }

        public Component getName() {
            return name;
        }

        public int getRange() {
            return range;
        }
    }
}
