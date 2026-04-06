package com.minecraftcitiesnetwork.knockknock;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.util.List;

public final class KnockKnockPlugin extends JavaPlugin {

    private KnockConfig knockConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!reloadKnockConfig()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands registrar = event.registrar();
            registrar.register(
                    Commands.literal("knockknock")
                            .then(Commands.literal("reload")
                                    .requires(ctx -> ctx.getSender().hasPermission("knockknock.reload"))
                                    .executes(ctx -> {
                                        var sender = ctx.getSource().getSender();
                                        if (reloadKnockConfig()) {
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
        getServer().getPluginManager().registerEvents(new KnockListener(this), this);
        getLogger().info("KnockKnock enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("KnockKnock disabled.");
    }

    public boolean reloadKnockConfig() {
        try {
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                    .path(getDataFolder().toPath().resolve("config.yml"))
                    .build();
            ConfigurationNode root = loader.load();
            KnockConfig loaded = root.get(KnockConfig.class);
            if (loaded == null) {
                throw new IOException("Invalid config.yml structure: file is empty");
            }
            loaded.sanitize(getLogger());
            this.knockConfig = loaded;
            return true;
        } catch (SerializationException e) {
            getLogger().severe("Invalid config.yml structure: " + e.getMessage());
            return false;
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            return false;
        }
    }

    public KnockConfig knockConfig() {
        return knockConfig;
    }
}
