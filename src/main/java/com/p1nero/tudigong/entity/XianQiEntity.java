package com.p1nero.tudigong.entity;

import com.p1nero.tudigong.TDGConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class XianQiEntity extends PathfinderMob {
    private Vec3 targetPos = Vec3.ZERO;
    private LivingEntity owner;
    private BoundingBox structureBox;
    private static final EntityDataAccessor<Integer> HIGHLIGHT_TIMER = SynchedEntityData.defineId(XianQiEntity.class, EntityDataSerializers.INT);


    public XianQiEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public XianQiEntity(Level level, Vec3 targetPos, LivingEntity owner, BoundingBox boundingBox) {
        this(TDGEntities.XIAN_QI.get(), level);
        this.targetPos = targetPos;
        this.owner = owner;
        this.setPos(owner.position());
        this.structureBox = boundingBox;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HIGHLIGHT_TIMER, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        if (compoundTag.contains("structureBox")) {
            int[] coords = compoundTag.getIntArray("structureBox");
            if (coords.length == 6) {
                this.structureBox = new BoundingBox(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.structureBox != null) {
            compoundTag.putIntArray("structureBox", new int[]{this.structureBox.minX(), this.structureBox.minY(), this.structureBox.minZ(), this.structureBox.maxX(), this.structureBox.maxY(), this.structureBox.maxZ()});
        }
    }

    public void setTargetPos(Vec3 targetPos) {
        this.targetPos = targetPos;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        if(source.getEntity() instanceof Player) {
            this.discard();
        }
        return super.hurt(source, value);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 200D)
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

            if (this.entityData.get(HIGHLIGHT_TIMER) > 0 && this.structureBox != null) {
                for (int i = 0; i < 5; i++) {
                    double x = this.structureBox.minX() + level().random.nextDouble() * (this.structureBox.getXSpan());
                    double y = this.structureBox.minY() + level().random.nextDouble() * (this.structureBox.getYSpan());
                    double z = this.structureBox.minZ() + level().random.nextDouble() * (this.structureBox.getZSpan());
                    level().addParticle(ParticleTypes.GLOW_SQUID_INK, x, y, z, 0, 0, 0);
                }
            }

        } else {
            int highlightTime = this.entityData.get(HIGHLIGHT_TIMER);
            if (highlightTime > 0) {
                this.entityData.set(HIGHLIGHT_TIMER, highlightTime - 1);
            }

            if(tickCount % 40 == 0) {
                level().playSound(null, getX(), getY(), getZ(), random.nextBoolean() ? SoundEvents.AMETHYST_CLUSTER_STEP : SoundEvents.AMETHYST_BLOCK_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if(owner != null) {
                if (highlightTime == 0 && this.structureBox != null && owner.position().distanceToSqr(Vec3.atCenterOf(this.structureBox.getCenter())) < Math.pow(TDGConfig.HIGHLIGHT_DISTANCE_BLOCKS.get(), 2)) {
                    this.entityData.set(HIGHLIGHT_TIMER, TDGConfig.HIGHLIGHT_DURATION_TICKS.get());
                }

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

            if(this.distanceToSqr(targetPos) < Math.pow(TDGConfig.XIANQI_DISCARD_DISTANCE_BLOCKS.get(), 2)) {
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