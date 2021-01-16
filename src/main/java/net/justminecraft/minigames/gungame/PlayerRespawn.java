package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.titleapi.TitleAPI;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
            Location spawnLocation = new Location(g.world,0, 64, 0);//unknown
            while (spawnLocation.getBlock().getType() != Material.AIR) {
                spawnLocation = spawnLocation.add(0,1,0);
            }

            player.teleport(spawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
            return;
        }

        TitleAPI.sendTitle(player, 0, seconds > 1 ? 30 : 20, 0, ChatColor.GREEN + "Respawning in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...","");
        seconds--;
        plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
    }
}
