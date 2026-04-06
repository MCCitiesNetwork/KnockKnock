package com.minecraftcitiesnetwork.knockknock;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.spongepowered.configurate.ConfigurationNode;
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

        ConfigurationNode settings = root.node("settings");
        boolean knockWithTool = settings.node("knock-with-tool").getBoolean(false);
        boolean knockOnTrapdoors = settings.node("knock-on-trapdoors").getBoolean(true);
        boolean knockOnWindows = settings.node("knock-on-windows").getBoolean(true);

        Map<Material, List<KnockStep>> soundsByMaterial = new EnumMap<>(Material.class);
        loadSectionInto(root.node("doors"), "doors", log, soundsByMaterial);
        if (knockOnTrapdoors) {
            loadSectionInto(root.node("trapdoors"), "trapdoors", log, soundsByMaterial);
        }
        if (knockOnWindows) {
            loadSectionInto(root.node("windows"), "windows", log, soundsByMaterial);
        }

        return new KnockConfig(knockWithTool, soundsByMaterial);
    }

    private static void loadSectionInto(
            ConfigurationNode section,
            String label,
            Logger log,
            Map<Material, List<KnockStep>> out
    ) {
        if (section.virtual()) {
            return;
        }
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : section.childrenMap().entrySet()) {
            String key = String.valueOf(entry.getKey());
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

    private static List<KnockStep> parseSteps(ConfigurationNode stepsNode, String materialKey, Logger log) {
        List<? extends ConfigurationNode> children = stepsNode.childrenList();
        if (children.isEmpty()) {
            return List.of();
        }
        List<KnockStep> steps = new ArrayList<>();
        for (ConfigurationNode entry : children) {
            String soundName = entry.node("sound").getString();
            if (soundName == null || soundName.isBlank()) {
                log.warning("Missing sound for " + materialKey + ", skipping entry.");
                continue;
            }
            NamespacedKey soundKey = NamespacedKey.fromString(soundName);
            if (soundKey == null || Registry.SOUNDS.get(soundKey) == null) {
                log.warning("Invalid sound '" + soundName + "' for " + materialKey + ", skipping entry.");
                continue;
            }
            float volume = entry.node("volume").getFloat(1f);
            float pitch = entry.node("pitch").getFloat(1f);
            steps.add(new KnockStep(soundName, volume, pitch));
        }
        return steps;
    }

    private static Material parseMaterial(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
