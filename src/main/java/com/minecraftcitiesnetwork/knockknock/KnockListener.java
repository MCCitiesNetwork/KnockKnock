package com.minecraftcitiesnetwork.knockknock;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public final class KnockListener implements Listener {

    private final KnockKnockPlugin plugin;

    public KnockListener(KnockKnockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (!event.getPlayer().isSneaking()) {
            return;
        }
        var block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        KnockConfig config = plugin.knockConfig();
        if (!event.getPlayer().getInventory().getItemInMainHand().isEmpty() && !config.knockWithTool()) {
            return;
        }
        List<KnockStep> steps = config.resolve(block.getType());
        if (steps.isEmpty()) {
            return;
        }
        var world = block.getWorld();
        var location = block.getLocation();
        for (KnockStep step : steps) {
            world.playSound(location, step.sound(), step.volume(), step.pitch());
        }
    }
}
