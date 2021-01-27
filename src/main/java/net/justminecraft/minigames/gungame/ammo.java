package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ammo {

    private final JustGuns plugin;

    ammo(JustGuns plugin) {
        this.plugin = plugin;
    }

    public ItemStack getBullets() {
        return getItem(Material.STONE_BUTTON, ChatColor.WHITE + "Bullet");
    }

    public ItemStack getClips() {
        return getItem(Material.IRON_INGOT, ChatColor.WHITE + "Clip");
    }

    public void setAmmo(Player player, int ammo) {
        Game g = MG.core().getGame(player);
        JustGunsGame game = (JustGunsGame) g;
        if(g == null || g.minigame != plugin) return;
        game.playerAmmo.replace(player, ammo);
        if(ammo > 0) {
            ItemStack bullets = getBullets();
            bullets.setAmount(ammo);
            player.getInventory().setItem(6, bullets);
        } else {
            player.getInventory().setItem(6, new ItemStack(Material.AIR));
        }
    }

    public void setClips(Player player, int clips) {
        Game g = MG.core().getGame(player);
        JustGunsGame game = (JustGunsGame) g;
        if(g == null || g.minigame != plugin) return;
        game.playerClips.replace(player, clips);
        if(clips > 0) {
            ItemStack mags = getClips();
            mags.setAmount(clips);
            player.getInventory().setItem(7, mags);
        } else {
            player.getInventory().setItem(7, new ItemStack(Material.AIR));
        }
    }

    public int getAmmo(Player player) {
        Game g = MG.core().getGame(player);
        JustGunsGame game = (JustGunsGame) g;
        if(g == null || g.minigame != plugin) return 0;
        return game.playerAmmo.get(player);
    }

    public int getClips(Player player) {
        Game g = MG.core().getGame(player);
        JustGunsGame game = (JustGunsGame) g;
        if(g == null || g.minigame != plugin) return 0;
        return game.playerClips.get(player);
    }

    public void removeAmmo(Player player) {
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != plugin) return;
        int ammo = getAmmo(player) - 1;
        setAmmo(player, ammo);
    }

    public void removeClip(Player player) {
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != plugin) return;
        int clips = getClips(player) - 1;
        setClips(player, clips);
    }

    public void addReceivedAmmo(Player player, int ammo, int clips) {
        Game g = MG.core().getGame(player);
        if(g == null || g.minigame != plugin) return;
        clips = clips + getClips(player);
        ammo = ammo + getAmmo(player);
        if(ammo > JustGuns.BASE_AMMO) {
            clips = clips + (int) Math.floor(ammo / JustGuns.BASE_AMMO);
            ammo = JustGuns.BASE_AMMO % ammo;
        }
        setAmmo(player, ammo);
        setClips(player, clips);
    }

    public void setBaseAmmo(Player player) {
        Game g = MG.core().getGame(player);
        JustGunsGame game = (JustGunsGame) g;
        if(g == null || g.minigame != plugin) return;
        if(!game.playerAmmo.containsKey(player) && !game.playerClips.containsKey(player)) {
            game.playerAmmo.put(player, JustGuns.BASE_AMMO);
            game.playerClips.put(player, JustGuns.BASE_CLIPS);
            setAmmo(player, JustGuns.BASE_AMMO);
            setClips(player, JustGuns.BASE_CLIPS);
        } else {
            setAmmo(player, JustGuns.BASE_AMMO);
            setClips(player, JustGuns.BASE_CLIPS);
        }
    }

    public ItemStack getItem(Material mat, String name, String ...lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        itemMeta.setLore(Arrays.asList(lore));
        item.setItemMeta(itemMeta);
        return item;
    }
}
