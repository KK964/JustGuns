package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class upgradesGui implements Listener {

    private final JustGuns plugin;

    public Inventory inv;

    public upgradesGui(JustGuns plugin) {
        this.plugin = plugin;
        inv = Bukkit.createInventory(null, 9, ChatColor.GOLD + "Upgrades!");
    }

    private void initializeItems(Player p) {
        inv.setItem(0, createGuiItemI(getItemType("dmg", getDamage(invItem(p)), p), ChatColor.RED + "Up Damage", ChatColor.AQUA + "Current Damage: " + getDamage(invItem(p)), ChatColor.AQUA + loreTwo("dmg", getDamage(invItem(p)), p)));
        // set 0 with upgrade damage

        inv.setItem(4, createGuiItemI(getItemType("range", getRange(invItem(p)), p), ChatColor.RED + "Up Range", ChatColor.AQUA + "Current Range: " + getRange(invItem(p)), ChatColor.AQUA + loreTwo("range", getRange(invItem(p)), p)));
        // set 4 with upgrade range

        inv.setItem(8, createGuiItemM(Material.GOLD_NUGGET, ChatColor.GOLD + "Stats", ChatColor.GOLD + "Points: " + playerScore(p), ChatColor.RED + "Damage: " + getDamage(invItem(p)), ChatColor.AQUA + "Range: " + getRange(invItem(p))));
        // set 8 with stats

        /*
          * | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | // 9
          * | 9 |10 |11 |12 |13 |14 |15 |16 |17 |
          * |18 |19 |20 |21 |22 |23 |24 |25 |26 | // 27
         */
    }

    public ItemStack invItem(Player p) {
        return p.getInventory().getItem(0);
    }

    public ItemStack getItemType(String type, int level, Player p) {
        short color = 5;
        boolean isMax = isMax(type, level);
        if(isMax) color = 14;
        if(!canBuy(type,level,p)) color = 14;
        ItemStack res = new ItemStack(Material.STAINED_GLASS_PANE, 1, color);
        return res;
    }

    public String loreTwo(String type, int level, Player p) {
        String res = "Upgrade to level " + (level + 1) + " for " + itemCost(type, level);
        if(!canBuy(type, level, p)) {
            res = type + " costs too much!";
        }
        if(isMax(type, level)) {
            res = type + " is maxed out!";
        }
        return res;
    }

    public boolean isMax(String type, int level) {
        boolean res = false;
            if(type == "dmg") {
                if(level >= plugin.MAX_DAMAGE) res = true;
            }
            if(type == "range") {
                if(level >= plugin.MAX_RANGE) res = true;
            }
        return res;
    }

    public double itemCost(String type, int level) {
        double cost = 0;
        if(type == "dmg") {
            cost = plugin.DAMAGE_BASE_COST;
            cost = cost + (level * plugin.DAMAGE_UPGRADE_MULTIPLIER);
        }
        if(type == "range") {
            cost = plugin.RANGE_BASE_COST;
            cost = cost + (level * plugin.RANGE_UPGRADE_MULTIPLIER);
        }
        return cost;
    }

    public boolean canBuy(String type, int level, Player player) {
        boolean res = false;
        double playerScore = playerScore(player);
        double itemCost = itemCost(type, level);
        if(itemCost < playerScore) res = true;
        return res;
    }

    public double playerScore(Player player) {
        Game game = MG.core().getGame(player);
        JustGunsGame g = (JustGunsGame) game;
        return g.playerScore.get(player);
    }

    protected ItemStack createGuiItemM(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
    protected ItemStack createGuiItemI(final ItemStack item, final String name, final String... lore) {
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public void openInventory(Player p) {
        initializeItems(p);
        p.openInventory(inv);
    }

    public void updateInventory(Player p) {
        initializeItems(p);
    }

    public String[] getLore(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        String[] loreArray = new String[lore.size()];
        loreArray = lore.toArray(loreArray);
        return loreArray;
    }
    public int getDamage(ItemStack item) {
        String[] loreArray = getLore(item);
        String[] dmg = loreArray[0].split(" ");
        return Integer.parseInt(dmg[1]);
    }
    public int getRange(ItemStack item) {
        String[] loreArray = getLore(item);
        String[] range = loreArray[1].split(" ");
        return Integer.parseInt(range[1]);
    }
}
