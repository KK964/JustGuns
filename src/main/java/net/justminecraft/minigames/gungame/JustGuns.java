package main.java.net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class JustGuns extends Minigame implements Listener {

    upgradesGui upgradesGui = new upgradesGui();

    public void onEnable() {
        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("JustGuns Enabled");
    }

    public void onDisable() {
        getLogger().info("JustGuns Disabled");
    }

    @Override
    public int getMaxPlayers() {
        return 4;
    }

    @Override
    public int getMinPlayers() {
        return 2;
    }

    @Override
    public String getMinigameName() {
        return "Guns";
    }

    //Listeners

    @EventHandler
    public void onGunUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g != null && g.minigame == this) {
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && e.getItem() != null) {
                if(e.getItem().getType() == Material.DIAMOND_HOE && e.getItem().getItemMeta().getDisplayName().contentEquals(ChatColor.GOLD + "Gun")) {
                    Location loc = p.getLocation();
                    double yAdd = 1.62;
                    if(p.isSneaking())
                        yAdd = 1.50; //sneaking
                    loc = loc.add(loc.getDirection().getX(),loc.getDirection().getY()+yAdd,loc.getDirection().getZ());

                    int dmg = getDamage(e.getItem());
                    int range = getRange(e.getItem()) * 100;

                    for(int i = 0; i < range; i++) { //total distance travel
                        loc = loc.add(loc.getDirection().getX()/1.5, loc.getDirection().getY()/1.5, loc.getDirection().getZ()/1.5);
                        p.getWorld().spigot().playEffect(loc, Effect.FLAME, 1, 0, 0, 0 ,0, 0, 1, 100);
                        if(loc.getBlock() != null && loc.getBlock().getType().isSolid()) {
                            break;
                        }
                        for(Entity ent : loc.getWorld().getNearbyEntities(loc, 0.2, 0.2, 0.2)) {
                            if(ent != p && ent.getType().isAlive()) {
                                ((LivingEntity) ent).damage(dmg, p);
                                i = range;
                                break;
                            }
                        }
                    }
                    p.getLocation().getWorld().playSound(p.getLocation(), Sound.IRONGOLEM_HIT, 2, 0.5f);
                }
                if(e.getItem().getType() == Material.NETHER_STAR && e.getItem().getItemMeta().getDisplayName().contentEquals(ChatColor.GOLD + "Upgrades")) {
                    openUpgradesInventory(p);
                }
            }
        }
    }

    @EventHandler
    public void dropHandler(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Game g = MG.core().getGame(p);
        if(g != null && g.minigame == this) {
            e.setCancelled(true);
        }
    }

    //game

    @Override
    public Game newGame() {
        return new JustGunsGame(this);
    }

    @Override
    public void startGame(Game game) {
        JustGunsGame g = (JustGunsGame) game;

        for(Player p : g.players) {
            giveGun(p, 1, 1);
            giveUpgrade(p);
        }
    }

    @Override
    public void generateWorld(Game g, WorldBuffer w) {
        g.moneyPerDeath = 5;
        g.moneyPerWin= 30;
        g.disableBlockBreaking = true;
        g.disableBlockPlacing = true;
        g.disableHunger = true;

        //generate world with schem and barriers
    }


    //weapon stuff
    public void giveGun(Player p, int dmg, int range) {
        ItemStack gun = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta gunMeta = gun.getItemMeta();
        List<String> loreList = Arrays.asList(ChatColor.GRAY + "Damage " + dmg, ChatColor.GRAY + "Range " + range);
        gunMeta.setDisplayName(ChatColor.GOLD + "Gun");
        gunMeta.setLore(loreList);
        gun.setItemMeta(gunMeta);
        p.getInventory().setItem(0, gun);
    }

    public void giveUpgrade(Player p) {
        ItemStack upgrades = new ItemStack(Material.NETHER_STAR);
        ItemMeta upgradesMeta = upgrades.getItemMeta();
        List<String> loreList = Arrays.asList(ChatColor.GRAY + "Upgrade Your Gun");
        upgradesMeta.setDisplayName(ChatColor.GOLD + "Upgrades");
        upgradesMeta.setLore(loreList);
        upgrades.setItemMeta(upgradesMeta);
        p.getInventory().setItem(8, upgrades);
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

    /*
      * Upgrades
     */
    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        e.setCancelled(true);
        if(e.getClick() == ClickType.NUMBER_KEY) return;
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        if(clickedItem.getType() == Material.STAINED_GLASS_PANE && clickedItem.getDurability() == 14) {
            p.playSound(p.getLocation(), Sound.BURP, 100, 1);
            p.sendMessage(ChatColor.RED + "That Upgrade is Maxed!");
            return;
        }
        switch(e.getRawSlot()) {
            case 0: {
                addDmg(p);
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 100, 1);
                break;
            }
            case 4: {
                addRange(p);
                p.playSound(p.getLocation(), Sound.LEVEL_UP, 100, 1);
                break;
            }
        }
    }

    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        Player p = (Player) e.getWhoClicked();
        Game g = MG.core().getGame(p);
        if(g == null || g.minigame != this) return;
        e.setCancelled(true);
    }

    public void addDmg(Player p) {
        ItemStack gun = p.getInventory().getItem(0);
        int dmg = getDamage(gun) + 1;
        int range = getRange(gun);
        p.getInventory().setItem(0, new ItemStack(Material.AIR));
        giveGun(p, dmg, range);
        p.sendMessage(ChatColor.GREEN + "Upgraded Your Damage to " + ChatColor.DARK_GREEN + dmg + ChatColor.GREEN + " Damage and " + ChatColor.DARK_GREEN + range + ChatColor.GREEN + " Range.");
        openUpgradesInventory(p);
    }
    public void addRange(Player p) {
        ItemStack gun = p.getInventory().getItem(0);
        int dmg = getDamage(gun);
        int range = getRange(gun) + 1;
        p.getInventory().setItem(0, new ItemStack(Material.AIR));
        giveGun(p, dmg, range);
        p.sendMessage(ChatColor.GREEN + "Upgraded Your Damage to " + ChatColor.DARK_GREEN + dmg + ChatColor.GREEN + " Damage and " + ChatColor.DARK_GREEN + range + ChatColor.GREEN + " Range.");
        openUpgradesInventory(p);
    }

    public void openUpgradesInventory(Player p) {
        p.closeInventory();
        upgradesGui.openInventory(p);
    }
}
