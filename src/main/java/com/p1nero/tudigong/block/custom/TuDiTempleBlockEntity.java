package com.p1nero.tudigong.block.custom;

import com.p1nero.tudigong.TDGConfig;
import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.block.TDGBlockEntities;
import com.p1nero.tudigong.entity.TDGEntities;
import com.p1nero.tudigong.entity.TudiGongEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TuDiTempleBlockEntity extends BlockEntity {
    private int shiftCount;
    private int resetTimer;
    private int cooldown;
    private boolean release = true;
    private final int maxResetTimer = 60;
    private final int maxCooldown = 40;
    private TudiGongEntity tudiGongEntity;
    public TuDiTempleBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(TDGBlockEntities.TUDI_TEMPLE_ENTITY.get(), blockPos, blockState);
    }

    public void reset() {
        shiftCount = 0;
        resetTimer = 0;
        cooldown = maxCooldown;
        release = true;
        tudiGongEntity = null;
    }

    public static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState state, T t) {
        if(t instanceof TuDiTempleBlockEntity tuDiTempleBlockEntity) {
            tuDiTempleBlockEntity.tick(pLevel, pPos, state);
        }
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState state) {
        Vec3 center = pPos.getCenter();
        pLevel.getEntitiesOfClass(Player.class, (new AABB(center, center)).inflate(3)).forEach(player -> {
            if(player instanceof ServerPlayer serverPlayer) {
                if(TDGConfig.GUIDE_MODE.get()) {
                    serverPlayer.displayClientMessage(Component.translatable("block.tudigong.tudi_temple.tip"), true);
                }
                if(tudiGongEntity == null && cooldown <= 0) {
                    if(serverPlayer.isShiftKeyDown()) {
                        if(release) {
                            shiftCount++;
                            resetTimer = maxResetTimer;
                            release = false;
                        }
                    } else {
                        release = true;
                        if(shiftCount == 3) {
                            tudiGongEntity = TDGEntities.TU_DI_GONG.get().spawn(serverPlayer.serverLevel(), pPos.above(1), MobSpawnType.MOB_SUMMONED);
                            if(tudiGongEntity != null) {
                                tudiGongEntity.setHomePos(pPos);
                                tudiGongEntity.setYRot(this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite().toYRot());
                                serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(tudiGongEntity.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.answer1")), false);
                                TuDiGongMod.finishAdvancement(TuDiGongMod.MOD_ID + ":sincerity", serverPlayer);
                                TDGConfig.GUIDE_MODE.set(false);
                            }
                            shiftCount = 0;
                        }
                    }
                }
            }
        });
        if(!pLevel.isClientSide) {
            if(resetTimer > 0) {
                resetTimer--;
                if(resetTimer == 0) {
                    shiftCount = 0;
                }
            }
            if(cooldown > 0) {
                cooldown--;
            }
            if(tudiGongEntity != null && tudiGongEntity.isDeadOrDying()) {
                tudiGongEntity = null;
            }
        }

    }
}
