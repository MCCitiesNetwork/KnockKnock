package com.minecraftcitiesnetwork.knockknock;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@ConfigSerializable
public final class KnockConfig {

    @Setting("settings")
    public Settings settings = new Settings();
    @Setting("doors")
    public Map<String, List<KnockStep>> doors = Map.of();
    @Setting("trapdoors")
    public Map<String, List<KnockStep>> trapdoors = Map.of();
    @Setting("windows")
    public Map<String, List<KnockStep>> windows = Map.of();

    public boolean knockWithTool() {
        return settings.knockWithTool;
    }

    public List<KnockStep> resolve(Material type) {
        String materialKey = type.getKey().toString();

        List<KnockStep> fromDoors = doors.get(materialKey);
        if (fromDoors != null) {
            return fromDoors;
        }
        if (settings.knockOnWindows) {
            List<KnockStep> fromWindows = windows.get(materialKey);
            if (fromWindows != null) {
                return fromWindows;
            }
        }
        if (settings.knockOnTrapdoors) {
            List<KnockStep> fromTrapdoors = trapdoors.get(materialKey);
            if (fromTrapdoors != null) {
                return fromTrapdoors;
            }
        }
        return List.of();
    }

    public void sanitize(Logger log) {
        this.doors = sanitizeSection(this.doors, "doors", log);
        this.trapdoors = sanitizeSection(this.trapdoors, "trapdoors", log);
        this.windows = sanitizeSection(this.windows, "windows", log);
    }

    private static Map<String, List<KnockStep>> sanitizeSection(Map<String, List<KnockStep>> section, String label, Logger log) {
        if (section == null || section.isEmpty()) {
            return Map.of();
        }
        Map<String, List<KnockStep>> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, List<KnockStep>> entry : section.entrySet()) {
            String materialKey = entry.getKey();
            NamespacedKey parsedMaterialKey = NamespacedKey.fromString(materialKey);
            if (parsedMaterialKey == null || Registry.MATERIAL.get(parsedMaterialKey) == null) {
                log.warning("Unknown material in " + label + ": " + materialKey);
                continue;
            }
            List<KnockStep> steps = entry.getValue();
            if (steps == null || steps.isEmpty()) {
                continue;
            }
            List<KnockStep> validSteps = new ArrayList<>();
            for (KnockStep step : steps) {
                if (step == null || step.sound() == null || step.sound().isBlank()) {
                    log.warning("Missing sound for " + materialKey + ", skipping entry.");
                    continue;
                }
                NamespacedKey soundKey = NamespacedKey.fromString(step.sound());
                if (soundKey == null || Registry.SOUNDS.get(soundKey) == null) {
                    log.warning("Invalid sound '" + step.sound() + "' for " + materialKey + ", skipping entry.");
                    continue;
                }
                validSteps.add(step);
            }
            if (!validSteps.isEmpty()) {
                sanitized.put(materialKey, List.copyOf(validSteps));
            }
        }
        return sanitized;
    }

    @ConfigSerializable
    public static final class Settings {
        @Setting("knock-with-tool")
        public boolean knockWithTool = false;
        @Setting("knock-on-trapdoors")
        public boolean knockOnTrapdoors = true;
        @Setting("knock-on-windows")
        public boolean knockOnWindows = true;
    }
}
