package com.minecraftcitiesnetwork.knockknock;

import org.bukkit.plugin.java.JavaPlugin;

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
            this.knockConfig = KnockConfig.load(getDataFolder().toPath().resolve("config.yml"), getLogger());
            return true;
        } catch (IOException e) {
            getLogger().severe("Failed to load config.yml: " + e.getMessage());
            return false;
        }
    }

    public KnockConfig knockConfig() {
        return knockConfig;
    }
}
