package com.minecraftcitiesnetwork.knockknock;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class KnockConfig {

    private final boolean knockWithTool;
    private final Map<Material, List<KnockStep>> soundsByMaterial;

    private KnockConfig(
            boolean knockWithTool,
            Map<Material, List<KnockStep>> soundsByMaterial
    ) {
        this.knockWithTool = knockWithTool;
        this.soundsByMaterial = soundsByMaterial;
    }

    public boolean knockWithTool() {
        return knockWithTool;
    }

    public List<KnockStep> resolve(Material type) {
        return soundsByMaterial.getOrDefault(type, List.of());
    }

    public static KnockConfig load(Path configPath, Logger log) throws IOException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .path(configPath)
                .build();
        ConfigurationNode root = loader.load();
        RawConfig rawConfig;
        try {
            rawConfig = root.get(RawConfig.class);
        } catch (SerializationException e) {
            throw new IOException("Invalid config.yml structure: " + e.getMessage(), e);
        }
        if (rawConfig == null) {
            throw new IOException("Invalid config.yml structure: file is empty");
        }

        Map<Material, List<KnockStep>> soundsByMaterial = new EnumMap<>(Material.class);
        loadSectionInto(rawConfig.doors, "doors", log, soundsByMaterial);
        if (rawConfig.settings.knockOnTrapdoors) {
            loadSectionInto(rawConfig.trapdoors, "trapdoors", log, soundsByMaterial);
        }
        if (rawConfig.settings.knockOnWindows) {
            loadSectionInto(rawConfig.windows, "windows", log, soundsByMaterial);
        }

        return new KnockConfig(rawConfig.settings.knockWithTool, soundsByMaterial);
    }

    private static void loadSectionInto(
            Map<String, List<RawSoundStep>> section,
            String label,
            Logger log,
            Map<Material, List<KnockStep>> out
    ) {
        if (section == null || section.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<RawSoundStep>> entry : section.entrySet()) {
            String key = entry.getKey();
            Material material = parseMaterial(key);
            if (material == null) {
                log.warning("Unknown material in " + label + ": " + key);
                continue;
            }
            List<KnockStep> steps = parseSteps(entry.getValue(), key, log);
            if (!steps.isEmpty()) {
                out.put(material, List.copyOf(steps));
            }
        }
    }

    private static List<KnockStep> parseSteps(List<RawSoundStep> rawSteps, String materialKey, Logger log) {
        if (rawSteps == null || rawSteps.isEmpty()) {
            return List.of();
        }
        List<KnockStep> steps = new ArrayList<>();
        for (RawSoundStep entry : rawSteps) {
            String soundName = entry.sound;
            if (soundName == null || soundName.isBlank()) {
                log.warning("Missing sound for " + materialKey + ", skipping entry.");
                continue;
            }
            NamespacedKey soundKey = NamespacedKey.fromString(soundName);
            if (soundKey == null || Registry.SOUNDS.get(soundKey) == null) {
                log.warning("Invalid sound '" + soundName + "' for " + materialKey + ", skipping entry.");
                continue;
            }
            steps.add(new KnockStep(soundName, entry.volume, entry.pitch));
        }
        return steps;
    }

    private static @Nullable Material parseMaterial(String name) {
        NamespacedKey key = NamespacedKey.fromString(name);
        if (key == null) {
            return null;
        }
        return Registry.MATERIAL.get(key);
    }

    @ConfigSerializable
    private static final class RawConfig {
        @Setting("settings")
        Settings settings = new Settings();
        @Setting("doors")
        Map<String, List<RawSoundStep>> doors = Map.of();
        @Setting("trapdoors")
        Map<String, List<RawSoundStep>> trapdoors = Map.of();
        @Setting("windows")
        Map<String, List<RawSoundStep>> windows = Map.of();
    }

    @ConfigSerializable
    private static final class Settings {
        @Setting("knock-with-tool")
        boolean knockWithTool = false;
        @Setting("knock-on-trapdoors")
        boolean knockOnTrapdoors = true;
        @Setting("knock-on-windows")
        boolean knockOnWindows = true;
    }

    @ConfigSerializable
    private static final class RawSoundStep {
        @Setting("sound")
        String sound;
        @Setting("volume")
        float volume = 1f;
        @Setting("pitch")
        float pitch = 1f;
    }
}
