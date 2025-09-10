package com.p1nero.tudigong.block;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.block.custom.TuDiTempleBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TDGBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TuDiGongMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<TuDiTempleBlockEntity>> TUDI_TEMPLE_ENTITY =
            REGISTRY.register("tudi_temple_entity", () ->
                    BlockEntityType.Builder.of(TuDiTempleBlockEntity::new, TDGBlocks.TUDI_TEMPLE.get()).build(null));
}
