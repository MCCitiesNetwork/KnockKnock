package com.minecraftcitiesnetwork.knockknock;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;

public final class KnockKnockPlugin extends JavaPlugin {

    private KnockConfig knockConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!reloadKnockConfig()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
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
