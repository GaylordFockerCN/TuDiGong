package com.p1nero.tudigong.item;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.item.custom.TuDiCommandSpellItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TDGItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, TuDiGongMod.MOD_ID);
    public static final RegistryObject<Item> TUDI_COMMAND_SPELL = REGISTRY.register("tudi_command_spell", () -> new TuDiCommandSpellItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

}
