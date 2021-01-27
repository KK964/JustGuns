package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.ActionBar;
import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Reload implements Runnable {
    private final JustGuns plugin;
    private final Player player;
    private final int clips;
    private final int ammo;
    private int seconds;

    public Reload(JustGuns plugin, Player player, int seconds, int ammo, int clips) {
        this.plugin = plugin;
        this.player = player;
        this.seconds = seconds;
        this.clips = clips;
        this.ammo = ammo;
        run();
    }

    @Override
    public void run() {
        Game g = MG.core().getGame(player);
        JustGunsGame game = (JustGunsGame) g;
        if(g == null || g.minigame != plugin) return;
        if(player.getGameMode() == GameMode.SPECTATOR) return;
        if(plugin.Ammo.getClips(player) > 0) {
            if(seconds == 0) {
                plugin.Ammo.removeClip(player);
                plugin.Ammo.setAmmo(player, JustGuns.BASE_AMMO);
                ActionBar reloaded = new ActionBar(ChatColor.GREEN + "Reloaded!");
                reloaded.send(player);
                game.reloading.remove(player);
                return;
            }
            ActionBar timeLeft = new ActionBar(ChatColor.GREEN + "Reloading in " + seconds + " second" + (seconds == 1 ? "" : "s") + "...");
            timeLeft.send(player);
            seconds--;
            plugin.getServer().getScheduler().runTaskLater(plugin, this, 20);
        } else {
            ActionBar outOfAmmo = new ActionBar(ChatColor.DARK_RED + "Out Of Ammo!");
            outOfAmmo.send(player);
            game.reloading.remove(player);
        }
    }


}
