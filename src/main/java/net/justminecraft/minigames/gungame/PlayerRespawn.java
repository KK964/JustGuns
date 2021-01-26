package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class PlayerRespawn implements Runnable {
    private final JustGuns plugin;
    private final Player player;
    private int seconds;

    public PlayerRespawn(JustGuns plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.seconds = plugin.RESPAWN_TIME;
        run();
    }

    @Override
    public void run() {
        if(seconds == 0) {
            Game g = MG.core().getGame(player);
            JustGunsGame game = (JustGunsGame) g;
            if(g == null || g.minigame != plugin) return;
            
            Location spawnLocation;
            ArrayList<Location> spawnLoc = new ArrayList<>(game.spawnLocations);
            ArrayList<Location> spawnLoc2 = new ArrayList<>(game.spawnLocations);
            for(Location possibleLoc : spawnLoc2) {
                possibleLoc.setWorld(game.world);
                for(Entity ent : possibleLoc.getWorld().getNearbyEntities(possibleLoc, 5, 5, 5)) {
                    if(ent instanceof Player && ((Player) ent).getGameMode() != GameMode.SPECTATOR) spawnLoc.remove(possibleLoc);
                }
            }
            if(spawnLoc.size() > 0) {
                spawnLocation = spawnLoc.get((int) (Math.random() * spawnLoc.size()));
            } else {
                spawnLocation = game.spawnLocations.get((int) (Math.random() * game.spawnLocations.size()));
            }

            Vector lookDirection = game.getLookDirection(spawnLocation, new Location(g.world, 0, spawnLocation.getY(), 0));
            spawnLocation.setDirection(lookDirection);
            player.teleport(spawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }

        TitleAPI.sendTitle(player, 0, seconds > 1 ? 30 : 20, 0, ChatColor.GREEN + "Respawning in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...","");
        seconds--;
        plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
    }
}
