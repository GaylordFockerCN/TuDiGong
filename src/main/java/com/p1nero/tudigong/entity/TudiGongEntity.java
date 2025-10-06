package com.p1nero.tudigong.entity;

import com.p1nero.dialog_lib.api.IEntityNpc;
import com.p1nero.dialog_lib.api.goal.LookAtConservingPlayerGoal;
import com.p1nero.dialog_lib.client.screen.DialogueScreenBuilder;
import com.p1nero.dialog_lib.network.DialoguePacketRelay;
import com.p1nero.tudigong.TDGConfig;
import com.p1nero.tudigong.block.custom.TuDiTempleBlockEntity;
import com.p1nero.tudigong.client.screen.BiomeSearchScreen;
import com.p1nero.tudigong.client.screen.StructureSearchScreen;
import com.p1nero.tudigong.compat.JourneyMapCompat;
import com.p1nero.tudigong.compat.XaeroMapCompat;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.client.SyncHistoryEntryPacket;
import com.p1nero.tudigong.util.BiomeUtil;
import com.p1nero.tudigong.util.StructureUtils;
import com.p1nero.tudigong.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TudiGongEntity extends PathfinderMob implements IEntityNpc {
    private static final Logger LOGGER = LogManager.getLogger();
    private Player conservingPlayer;
    private static final EntityDataAccessor<Boolean> IS_REMOVED = SynchedEntityData.defineId(TudiGongEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> REMOVE_TIMER = SynchedEntityData.defineId(TudiGongEntity.class, EntityDataSerializers.INT);
    public final int maxRemoveTime = 60;
    public final int maxLifeTime = 1200;
    private Vec3 from = Vec3.ZERO;
    private Vec3 dir = Vec3.ZERO;
    private int startTick = 0;
    public BlockPos homePos;
    public TudiGongEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookAtConservingPlayerGoal<>(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(REMOVE_TIMER, 0);
        this.entityData.define(IS_REMOVED, false);
    }

    public void setMarkRemoved() {
        this.setRemoveTimer(maxRemoveTime);
        this.getEntityData().set(IS_REMOVED, true);
    }

    public boolean isMarkRemoved() {
        return getEntityData().get(IS_REMOVED);
    }

    public void countRemove() {
        if(!isMarkRemoved()) {
            return;
        }
        int timer = this.getRemoveTimer();
        if (timer > 0) {
            this.setRemoveTimer(timer - 1);
        }
        if (timer <= 1) {
            this.noticeHomeAndDiscard();
        }
    }

    public void setHomePos(BlockPos homePos) {
        this.homePos = homePos;
    }

    public void setRemoveTimer(int timer) {
        this.entityData.set(REMOVE_TIMER, timer);
    }

    public int getRemoveTimer() {
        return this.entityData.get(REMOVE_TIMER);
    }

    public boolean canInteract() {
        return !this.isMarkRemoved() && this.tickCount > maxRemoveTime;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if(!this.canInteract()) {
                // 生成往上乱飞的烟雾粒子
                for(int i = 0; i < 5; i++) { // 每次生成5个粒子
                    double offsetX = (this.random.nextDouble() - 0.5) * 0.5; // X轴随机偏移
                    double offsetZ = (this.random.nextDouble() - 0.5) * 0.5; // Z轴随机偏移
                    double velocityY = this.random.nextDouble() * 0.2 + 0.1; // 向上的随机速度

                    level().addParticle(
                            ParticleTypes.LARGE_SMOKE, // 烟雾粒子
                            this.getX(),         // 当前位置X
                            this.getY(),   // 当前位置Y）
                            this.getZ(),         // 当前位置Z
                            offsetX,             // X轴随机偏移
                            velocityY,           // 向上的速度
                            offsetZ              // Z轴随机偏移
                    );
                    level().addParticle(
                            ParticleTypes.CLOUD, // 云粒子
                            this.getX(),         // 当前位置X
                            this.getY(),   // 当前位置Y）
                            this.getZ(),         // 当前位置Z
                            offsetX,             // X轴随机偏移
                            velocityY,           // 向上的速度
                            offsetZ              // Z轴随机偏移
                    );
                }
            } else {
                Vec3 position = this.position();
                level().addParticle(ParticleTypes.CLOUD, position.x, position.y + 0.5, position.z, 0, 0, 0);
            }

        } else {

            if(!this.canInteract()) {
                level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ROOTED_DIRT_STEP, SoundSource.BLOCKS, this.random.nextFloat() + 0.5F, this.random.nextFloat() + 0.5F);
            }

            if(this.getRemoveTimer() > 1) {
                ParticleOptions particle = ParticleTypes.END_ROD;
                int particleCount = 20;
                double step = 5.0 / particleCount;

                for (int i = tickCount - startTick; i <= particleCount; i++) {
                    double distance = i * step;
                    Vec3 particlePos = from.add(dir.scale(distance).add(0, i * 0.1, 0));
                    ((ServerLevel) level()).sendParticles(
                            particle,
                            particlePos.x,
                            particlePos.y,
                            particlePos.z,
                            0,
                            dir.x, dir.y, dir.z,
                            0.1f
                    );
                }
            }
            countRemove();

            if(canInteract() && tickCount > TDGConfig.TUDIGONG_LIFETIME_TICKS.get()) {
                this.setMarkRemoved();
            }

            if(this.conservingPlayer != null) {
                this.tickCount = maxRemoveTime;//刷新计时
            }

        }
    }

    public void noticeHomeAndDiscard() {
        if(this.homePos != null) {
            if(level().getBlockEntity(this.homePos) instanceof TuDiTempleBlockEntity tuDiTempleBlockEntity) {
                tuDiTempleBlockEntity.reset();
            }
        }
        this.discard();
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        if(!canInteract()) {
            return false;
        }
        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            //彩蛋对话
            if (this.getConversingPlayer() == null) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putBoolean("from_hurt", true);
                this.sendDialogTo(serverPlayer, compoundTag);
            } else {
                return false;
            }
            source.getEntity().hurt(this.damageSources().indirectMagic(this, this), value * 0.5F);
            EntityType.LIGHTNING_BOLT.spawn(serverPlayer.serverLevel(), serverPlayer.getOnPos(), MobSpawnType.MOB_SUMMONED);
        }
        return source.isCreativePlayer();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (canInteract() && player instanceof ServerPlayer serverPlayer) {
            sendDialogTo(serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreenBuilder getDialogueBuilder(CompoundTag compoundTag) {
        DialogueScreenBuilder builder = new DialogueScreenBuilder(this);
        if (compoundTag.getBoolean("from_hurt")) {
            builder.start(0)
                    .addFinalChoice(0);
        } else {
            builder.start(1)
                    .addFinalChoice(1, 2, (dialogueScreen -> Minecraft.getInstance().setScreen(new StructureSearchScreen(this.getId()))))
                    .addFinalChoice(2, 2, (dialogueScreen -> Minecraft.getInstance().setScreen(new BiomeSearchScreen(this.getId()))))
                    .addFinalChoice(3, 3);
        }
        return builder;
    }

    @Override
    public void handleNpcInteraction(ServerPlayer serverPlayer, int i) {
        if (i == 3) {
            this.setMarkRemoved();
        }
        //2表示打开窗口，应保持对话
        if(i != 2) {
            setConversingPlayer(null);
        }
    }

    public void handleSearch(ServerPlayer serverPlayer, String originalSearchTerm, ResourceLocation resourceLocation, boolean isStructure) {
        LOGGER.info("Player {} located {} (isStructure: {}) using term '{}'", serverPlayer.getName().getString(), resourceLocation.toString(), isStructure, originalSearchTerm);
        this.setConversingPlayer(null);
        if (isStructure && TDGConfig.STRUCTURE_BLACKLIST.get().contains(resourceLocation.toString())) {
            serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(this.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.answer5")), false);
            return;
        }
        BlockPos blockpos = isStructure ? StructureUtils.getNearbyStructurePos(serverPlayer, resourceLocation.toString(), -1145) : BiomeUtil.getNearbyBiomePos(serverPlayer, resourceLocation.toString());
        if(blockpos == null) {
            LOGGER.warn("Could not find location for {}, possibly blocked by other mods.", resourceLocation.toString());
            serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(this.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.answer5")), false);
            return;
        }

        // Send successful search result back to client for history
        Component typeComponent = Component.translatable(isStructure ? "history.tudigong.type.structure" : "history.tudigong.type.biome");
        SyncHistoryEntryPacket historyPacket = new SyncHistoryEntryPacket(originalSearchTerm, typeComponent, blockpos, serverPlayer.level().dimension());
        DialoguePacketRelay.sendToPlayer(TDGPacketHandler.INSTANCE, historyPacket, serverPlayer);

        // Display Title
        int distance = (int) Math.sqrt(serverPlayer.blockPosition().distSqr(blockpos));
        Component direction = TextUtil.getCardinalDirection(serverPlayer, blockpos);
        String structureName = TextUtil.tryToGetName(resourceLocation);
        Component message = Component.translatable("message.tudigong.location_found", direction, distance, structureName);
        serverPlayer.sendSystemMessage(ComponentUtils.wrapInSquareBrackets(this.getDisplayName()).append(": ").append(message));

        String s = blockpos.getY() == -1145 ? "~" : String.valueOf(blockpos.getY());
        s = " " + s + " ";
        if(blockpos.getY() == -1145) {
            blockpos = blockpos.atY((int) serverPlayer.getEyeY());
        }
        if(TDGConfig.MARK_LOCATION.get()) {
            BlockPos finalBlockpos = blockpos;
            String finalS = s;
            Component location = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockpos.getX(), s, blockpos.getZ())).withStyle((p_214489_) -> p_214489_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + finalBlockpos.getX() + finalS + finalBlockpos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
            serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(this.getDisplayName()).append(": ").append(location), false);
            XaeroMapCompat.sendWaypoint(serverPlayer, resourceLocation.toString(), blockpos);
            JourneyMapCompat.sendWaypoint(serverPlayer, resourceLocation.toString(), blockpos);
        }

        if (TDGConfig.SPAWN_GUIDER.get()) {
            net.minecraft.world.level.levelgen.structure.BoundingBox boundingBox = new net.minecraft.world.level.levelgen.structure.BoundingBox(blockpos).inflatedBy(16);
            XianQiEntity xianQiEntity = new XianQiEntity(level(), blockpos.getCenter(), serverPlayer, boundingBox);
            level().addFreshEntity(xianQiEntity);
        }

        serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(this.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.answer3")), false);
        serverPlayer.displayClientMessage(ComponentUtils.wrapInSquareBrackets(this.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.answer4")), false);
        from = this.getEyePosition();
        Vec3 target = blockpos.atY((int) this.getEyeY()).getCenter();
        dir = target.subtract(from).normalize();
        startTick = this.tickCount;
        level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0F, 1.0F);
        setMarkRemoved();
    }

    @Override
    public void setConversingPlayer(@Nullable Player player) {
        this.conservingPlayer = player;
    }

    @Override
    public @Nullable Player getConversingPlayer() {
        return conservingPlayer;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }
}
