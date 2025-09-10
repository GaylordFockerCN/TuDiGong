package com.p1nero.tudigong.item.custom;

import com.p1nero.tudigong.TDGConfig;
import com.p1nero.tudigong.entity.TDGEntities;
import com.p1nero.tudigong.entity.TudiGongEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TuDiCommandSpellItem extends Item {
    public TuDiCommandSpellItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if(context.getLevel() instanceof ServerLevel serverLevel) {
            Vec3 center = context.getClickedPos().getCenter();
            if(serverLevel.getBlockState(context.getClickedPos()).is(BlockTags.DIRT) &&
                    serverLevel.getEntitiesOfClass(TudiGongEntity.class, (new AABB(center, center)).inflate(10)).isEmpty()) {
                TudiGongEntity tudiGongEntity = TDGEntities.TU_DI_GONG.get().spawn(serverLevel, context.getClickedPos().above(1), MobSpawnType.MOB_SUMMONED);
                if(context.getPlayer() != null && tudiGongEntity != null) {
                    context.getPlayer().displayClientMessage(ComponentUtils.wrapInSquareBrackets(context.getPlayer().getDisplayName()).append(": ").append(Component.translatable("tip.tudigong.summon")), false);
                    context.getPlayer().displayClientMessage(ComponentUtils.wrapInSquareBrackets(tudiGongEntity.getDisplayName()).append(": ").append(Component.translatable("entity.tudigong.tudigong.dialog1")), false);
                    context.getPlayer().getCooldowns().addCooldown(context.getItemInHand().getItem(), TDGConfig.SPELL_COOLDOWN.get());
                    context.getItemInHand().shrink(1);
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag p_41424_) {
        super.appendHoverText(itemStack, level, list, p_41424_);
        list.add(Component.translatable("item.tudigong.tudi_command_spell.usage").withStyle(ChatFormatting.GRAY));
    }
}
