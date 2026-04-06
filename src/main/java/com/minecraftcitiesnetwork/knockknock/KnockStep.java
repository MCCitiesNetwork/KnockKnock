package com.minecraftcitiesnetwork.knockknock;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public record KnockStep(
        @Setting("sound") String sound,
        @Setting("volume") float volume,
        @Setting("pitch") float pitch
) {}
