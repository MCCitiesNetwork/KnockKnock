package com.minecraftcitiesnetwork.knockknock;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class KnockKnockBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands registrar = event.registrar();
            registrar.register(
                    Commands.literal("knockknock")
                            .then(Commands.literal("reload")
                                    .executes(ctx -> {
                                        var sender = ctx.getSource().getSender();
                                        if (!sender.hasPermission("knockknock.reload")) {
                                            sender.sendPlainMessage("You do not have permission to use this command.");
                                            return 0;
                                        }
                                        KnockKnockPlugin plugin = JavaPlugin.getPlugin(KnockKnockPlugin.class);
                                        if (plugin.reloadKnockConfig()) {
                                            sender.sendPlainMessage("KnockKnock config reloaded.");
                                            return Command.SINGLE_SUCCESS;
                                        }
                                        sender.sendPlainMessage("Failed to reload KnockKnock config. Check console logs.");
                                        return 0;
                                    }))
                            .build(),
                    "KnockKnock commands",
                    List.of("kk")
            );
        });
    }
}
