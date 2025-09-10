package com.p1nero.tudigong.item;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.block.TDGBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class TDGItemTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TuDiGongMod.MOD_ID);
    public static final RegistryObject<CreativeModeTab> ITEMS = REGISTRY.register("items",
            () -> CreativeModeTab.builder().title(Component.translatable("entity.tudigong.tudigong")).icon(() -> TDGBlocks.TUDI_TEMPLE.get().asItem().getDefaultInstance()).displayItems((params, output) -> {
                output.accept(TDGBlocks.TUDI_TEMPLE.get());
                output.accept(TDGItems.TUDI_COMMAND_SPELL.get());
            }).build());
}
