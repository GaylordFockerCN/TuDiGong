package com.p1nero.tudigong.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ExportStructuresCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> tudigongCommand = Commands.literal("tudigong");

        ExportCommand.register(tudigongCommand);
        TagCommand.register(tudigongCommand);
        ReloadTagsCommand.register(tudigongCommand);

        dispatcher.register(tudigongCommand);
    }
}
