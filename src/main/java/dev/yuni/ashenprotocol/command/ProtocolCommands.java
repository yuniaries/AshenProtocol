package dev.yuni.ashenprotocol.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.yuni.ashenprotocol.protocol.ProtocolSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class ProtocolCommands {

    public static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("protocol")

                        .then(
                                Commands.literal("status")
                                        .executes(context -> {
                                            ProtocolSavedData data =
                                                    ProtocolSavedData.get(
                                                            context.getSource()
                                                                    .getServer()
                                                    );

                                            context.getSource()
                                                    .sendSuccess(
                                                            () -> Component
                                                                    .literal(
                                                                            "Integrity="
                                                                                    + data.getIntegrity()
                                                                                    + " | Entropy="
                                                                                    + data.getEntropy()
                                                                                    + " | Phase="
                                                                                    + data.getPhase().zh()
                                                                    )
                                                                    .withStyle(
                                                                            ChatFormatting.AQUA
                                                                    ),
                                                            false
                                                    );

                                            return 1;
                                        })
                        )

                        .then(
                                Commands.literal("integrity")
                                        .requires(source ->
                                                source.hasPermission(2)
                                        )
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument(
                                                                                "value",
                                                                                IntegerArgumentType.integer(
                                                                                        0,
                                                                                        10000
                                                                                )
                                                                        )
                                                                        .executes(context -> {
                                                                            int value =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "value"
                                                                                    );

                                                                            ProtocolSavedData data =
                                                                                    ProtocolSavedData.get(
                                                                                            context.getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            data.setIntegrity(value);

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )

                        .then(
                                Commands.literal("entropy")
                                        .requires(source ->
                                                source.hasPermission(2)
                                        )
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument(
                                                                                "value",
                                                                                IntegerArgumentType.integer(
                                                                                        0,
                                                                                        10000
                                                                                )
                                                                        )
                                                                        .executes(context -> {
                                                                            int value =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "value"
                                                                                    );

                                                                            ProtocolSavedData data =
                                                                                    ProtocolSavedData.get(
                                                                                            context.getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            data.setEntropy(value);

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument(
                                                                                "value",
                                                                                IntegerArgumentType.integer(
                                                                                        -10000,
                                                                                        10000
                                                                                )
                                                                        )
                                                                        .executes(context -> {
                                                                            int value =
                                                                                    IntegerArgumentType.getInteger(
                                                                                            context,
                                                                                            "value"
                                                                                    );

                                                                            ProtocolSavedData data =
                                                                                    ProtocolSavedData.get(
                                                                                            context.getSource()
                                                                                                    .getServer()
                                                                                    );

                                                                            data.addEntropy(value);

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
        );
    }

    private ProtocolCommands() {}
}
