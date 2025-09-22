package com.p1nero.tudigong.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class XianQiEntity extends PathfinderMob {
    private Vec3 targetPos = Vec3.ZERO;
    private LivingEntity owner;
    private int discardDis = 20;
    public XianQiEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public XianQiEntity(Level level, Vec3 targetPos, LivingEntity owner) {
        this(TDGEntities.XIAN_QI.get(), level);
        this.targetPos = targetPos;
        this.owner = owner;
        this.setPos(owner.position());
    }

    public void setDiscardDis(int discardDis) {
        this.discardDis = discardDis;
    }

    public void setTargetPos(Vec3 targetPos) {
        this.targetPos = targetPos;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        this.discard();
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    @Override
    public void tick() {
        super.tick();
        if(level().isClientSide) {
            Vec3 position = this.position();
            level().addParticle(ParticleTypes.CLOUD, position.x, position.y + 0.5, position.z, 0, 0, 0);
            if(tickCount % 20 == 0) {
                level().addParticle(ParticleTypes.END_ROD, position.x, position.y + 0.5, position.z, 0, 0, 0);
            }
        } else {
            if(tickCount % 40 == 0) {
                level().playSound(null, getX(), getY(), getZ(), random.nextBoolean() ? SoundEvents.AMETHYST_CLUSTER_STEP : SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if(owner != null) {
                Vec3 totalDir = targetPos.subtract(owner.getEyePosition()).normalize();
                Vec3 goalPos = owner.getEyePosition().add(totalDir.scale(10));
                Vec3 dir = goalPos.subtract(this.position()).normalize();
                double dis1 = this.distanceTo(owner);
                if(dis1 < 3) {
                    this.setDeltaMovement(dir.scale(0.25));
                } else if(dis1 < 5) {
                    this.setDeltaMovement(dir.scale(0.1));
                } else if(this.distanceToSqr(targetPos) > owner.distanceToSqr(targetPos)) {
                    this.setDeltaMovement(dir.scale(0.5));
                }
            } else {
                discard();
                return;
            }

            if(this.distanceTo(owner) > 30) {
                this.setPos(owner.getEyePosition());
            }

            if(this.distanceToSqr(targetPos) < discardDis * discardDis) {
                Vec3 center = this.position();
                level().getEntitiesOfClass(Player.class, (new AABB(center, center)).inflate(10)).forEach(player -> {
                    player.displayClientMessage(Component.translatable("entity.tudigong.xian_qi.tip"), false);
                });
                this.discard();
            }
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

}
