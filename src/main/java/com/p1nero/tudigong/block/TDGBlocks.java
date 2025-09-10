package com.p1nero.tudigong.block;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.block.custom.TuDiTempleBlock;
import com.p1nero.tudigong.item.TDGItems;
import com.p1nero.tudigong.item.custom.SimpleDescriptionBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class TDGBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, TuDiGongMod.MOD_ID);

    public static final RegistryObject<Block> TUDI_TEMPLE = registerBlock("tudi_temple",
            () -> new TuDiTempleBlock(BlockBehaviour.Properties.copy(Blocks.BRICKS).noOcclusion()));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = REGISTRY.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return TDGItems.REGISTRY.register(name, () -> new SimpleDescriptionBlockItem(block.get(), new Item.Properties()));
    }

}
