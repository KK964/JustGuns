package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.ActionBar;
import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import net.justminecraft.minigames.minigamecore.worldbuffer.WorldBuffer;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class JustGuns extends Minigame implements Listener {

    public static File DATA_FOLDER;
    public static File SCHEMATIC_FOLDER;

    public int MIN_PLAYERS;
    public int MAX_PLAYERS;

    public int DEFAULT_DAMAGE;
    public int DEFAULT_RANGE;
    public int MAX_DAMAGE;
    public int MAX_RANGE;

    public int RANGE_MULTIPLIER;

    public int WIN_POINTS;
    public int DEATH_POINTS;

    public int RESPAWN_TIME;

    upgradesGui upgradesGui = new upgradesGui(this);

    public void onEnable() {
        DATA_FOLDER = getDataFolder();
        initConfigs();
        MG.core().registerMinigame(this);
        getServer().getPluginManager().registerEvents(this,this);
        getLogger().info("JustGuns Enabled");
    }

    public void onDisable() {
        getLogger().info("JustGuns Disabled");
    }

    @Override
    public int getMaxPlayers() {
        return MAX_PLAYERS;
    }

    @Override
    public int getMinPlayers() {
        return MIN_PLAYERS;
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

                    int dmg = upgradesGui.getDamage(e.getItem());
                    int range = upgradesGui.getRange(e.getItem()) * RANGE_MULTIPLIER;

                    for(int i = 0; i < range; i++) { //total distance travel
                        loc = loc.add(loc.getDirection().getX()/1.5, loc.getDirection().getY()/1.5, loc.getDirection().getZ()/1.5);
                        p.getWorld().spigot().playEffect(loc, Effect.FLAME, 1, 0, 0, 0 ,0, 0, 1, 100);
                        if(loc.getBlock() != null && loc.getBlock().getType().isSolid()) {
                            break;
                        }
                        for(Entity ent : loc.getWorld().getNearbyEntities(loc, 0.2, 0.2, 0.2)) {
                            if(ent != p && ent.getType().isAlive()) {
                                if(ent instanceof Player) {
                                    Player player = (Player) ent;
                                    player.damage(dmg, p);
                                } else {
                                    ((LivingEntity) ent).damage(dmg, p);
                                }
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
    public void entityKill(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        Player p = entity.getKiller();
        Game g = MG.core().getGame(p);
        if(g != null && g.minigame == this) {
            JustGunsGame jg = (JustGunsGame) g;
            jg.playerKills.replace(p, jg.playerKills.get(p) + 1);
            jg.updateScore(p);
            jg.updateActionBar(p);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Game g = MG.core().getGame(p);
        if (g != null && g.minigame == this) {
            g.broadcastRaw(e.getDeathMessage());
            p.closeInventory();
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
        g.world.setDifficulty(Difficulty.PEACEFUL);
        g.world.setSpawnLocation(0, 64, 0);
        g.world.setGameRuleValue("naturalRegeneration", "false");
        g.world.setGameRuleValue("keepInventory", "true");

        Objective kills = g.scoreboard.registerNewObjective("kills", "dummy");
        kills.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "GUNS");
        kills.setDisplaySlot(DisplaySlot.SIDEBAR);

        kills.getScore("  ").setScore(5);
        kills.getScore(ChatColor.GRAY + ChatColor.UNDERLINE.toString() + "Kills:").setScore(4);
        kills.getScore(" ").setScore(2);
        kills.getScore(ChatColor.YELLOW + "justminecraft.net").setScore(1);

        for(Player p : g.players) {
            g.setPlayer(p);
            g.updateScore(p);
            giveGun(p, DEFAULT_DAMAGE, DEFAULT_RANGE);
            giveUpgrade(p);
            p.setScoreboard(g.scoreboard);
            p.teleport(new Location(g.world, 0, 64, 0));
        }
    }

    @Override
    public void generateWorld(Game game, WorldBuffer w) {
        JustGunsGame g = (JustGunsGame) game;

        String map = g.getRandomMap(game);

        g.disablePvP = false;
        g.moneyPerDeath = DEATH_POINTS;
        g.moneyPerWin= WIN_POINTS;
        g.disableBlockBreaking = true;
        g.disableBlockPlacing = true;
        g.disableHunger = true;
        Map m = new Map();
        Location l = new Location(g.world, 0, 64, 0);
        m.placeSchematic(w, l, map);
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
        int dmg = upgradesGui.getDamage(gun) + 1;
        int range = upgradesGui.getRange(gun);
        p.getInventory().setItem(0, new ItemStack(Material.AIR));
        giveGun(p, dmg, range);
        p.sendMessage(ChatColor.GREEN + "Upgraded Your Gun to " + ChatColor.DARK_GREEN + dmg + ChatColor.GREEN + " Damage and " + ChatColor.DARK_GREEN + range + ChatColor.GREEN + " Range.");
        openUpgradesInventory(p);
    }
    public void addRange(Player p) {
        ItemStack gun = p.getInventory().getItem(0);
        int dmg = upgradesGui.getDamage(gun);
        int range = upgradesGui.getRange(gun) + 1;
        p.getInventory().setItem(0, new ItemStack(Material.AIR));
        giveGun(p, dmg, range);
        p.sendMessage(ChatColor.GREEN + "Upgraded Your Gun to " + ChatColor.DARK_GREEN + dmg + ChatColor.GREEN + " Damage and " + ChatColor.DARK_GREEN + range + ChatColor.GREEN + " Range.");
        openUpgradesInventory(p);
    }

    public void openUpgradesInventory(Player p) {
        p.closeInventory();
        upgradesGui.openInventory(p);
    }

    // Other

    private void initConfigs() {
        saveDefaultConfig();
        SCHEMATIC_FOLDER = new File (DATA_FOLDER.getPath() + System.getProperty("file.separator") + "schematics");
        SCHEMATIC_FOLDER.mkdir();

        MIN_PLAYERS = this.getConfig().getInt("minPlayers");
        MAX_PLAYERS = this.getConfig().getInt("maxPlayers");

        WIN_POINTS = this.getConfig().getInt("winPoints");
        DEATH_POINTS = this.getConfig().getInt("deathPoints");

        DEFAULT_DAMAGE = this.getConfig().getInt("defaultDmg");
        DEFAULT_RANGE = this.getConfig().getInt("defaultRange");
        MAX_DAMAGE = this.getConfig().getInt("maxDmg");
        MAX_RANGE = this.getConfig().getInt("maxRange");

        RANGE_MULTIPLIER = this.getConfig().getInt("rangeMultiplier");

        RESPAWN_TIME = this.getConfig().getInt("respawnTime");
    }

}
