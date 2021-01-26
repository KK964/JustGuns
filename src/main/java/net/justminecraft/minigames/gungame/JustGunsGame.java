package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JustGunsGame extends Game {
    private final JustGuns justguns;

    Scoreboard scoreboard;
    int neededKills;
    HashMap<Player, Double> playerScore = new HashMap<>();
    HashMap<Player, Double> killStreak = new HashMap<>();
    HashMap<Player, Integer> playerKills = new HashMap<>();
    ArrayList<Location> spawnLocations = new ArrayList<>();

    public JustGunsGame(Minigame mg) {
        super(mg, false);
        justguns = (JustGuns) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public String getMapSize(int p) {
        if(p <= 2) return JustGuns.TINY_MAPS_STRING;
        if(p < JustGuns.SMALL_MAP_PLAYERS) return JustGuns.SMALL_MAPS_STRING;
        if(p > JustGuns.SMALL_MAP_PLAYERS) return JustGuns.LARGE_MAPS_STRING;
        return JustGuns.SMALL_MAPS_STRING;
    }

    public String getRandomMap(Game game) {
        try {
            ArrayList<String> maps = new ArrayList<>();
            String size = getMapSize(game.players.size());
            for(File fileEntry : JustGuns.SCHEMATIC_FOLDER.listFiles()) {
                if(fileEntry.isFile()) {
                    String FileName = fileEntry.getName();
                    String regex = "(" + size + ")_\\w+\\.schematic";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(FileName);
                    if(m.find()) {
                        maps.add(FileName);
                    }
                }
            }
            if(maps.size() == 0) new IOException("Schematic File is missing a \"" + size + "\" Size map.");
            return maps.get((int) (Math.random() * maps.size()));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Vector getLookDirection(Location loc, Location loc2) {
        loc2.add(0.5, 0, 0.5);
        return loc2.toVector().subtract(loc.toVector());
    }

    @Override
    public void onPlayerDeath(Player p) {
        p.setVelocity(new Vector(0, 0, 0));
        p.setHealth(20);
        p.setFallDistance(0);
        p.setGameMode(GameMode.SPECTATOR);
        resetKills(p);
        updateExperience(p);
        updateScore(p);
        if (p.getLocation().getY() < 30) {
            p.teleport(new Location(p.getWorld(), 0, 90, 0, 135, 45));
        }
        if(!gameOver()) new PlayerRespawn((JustGuns) minigame, p);
    }

    public boolean gameOver() {
        boolean over = false;
        for(Player p : players) {
            if(playerKills.get(p) >= neededKills) over = true;
        }
        int playersNeeded = 2;
        if(JustGuns.TESTING_MODE) {
            playersNeeded = 1;
        }
        if(players.size() < playersNeeded) over = true;
        return over;
    }

    public void endGame() {
        Player winner = getWinner();
        ArrayList<Player> toKick = new ArrayList<>(players);
        for(Player p : toKick) {
            if(p != winner) {
                playerLeave(p);
            }
        }
    }

    public Player getWinner() {
        Player p = null;
        for(Player player : players) {
            if(playerKills.get(player) >= neededKills) p = player;
        }
        return p;
    }

    public int neededKills() {
        int killsNeeded = JustGuns.WIN_DEFAULT_KILLS;
        int playersInt = players.size();
        if(playersInt <= JustGuns.WIN_LESS_PLAYERS) killsNeeded = JustGuns.WIN_LESS_PLAYERS_KILLS;
        return killsNeeded;
    }

    public void updateScore(Player p) {
        scoreboard.resetScores(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + (playerKills.get(p) - 1));
        scoreboard.resetScores(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + (playerKills.get(p)));
        Game g = MG.core().getGame(p);
        if(g != null && g.minigame == justguns) {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + playerKills.get(p)).setScore(3);
        } else {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + "✗").setScore(3);
        }
    }

    public void updateActionBar(Player p) {
        double KillStreak = killStreak.get(p);
        double scoreMultiplier = getMultiplier(p);
        ActionBar killStreakMulti = new ActionBar(ChatColor.RED + "Kill Streak: " + ChatColor.DARK_RED + ChatColor.UNDERLINE.toString() + KillStreak + ChatColor.RESET + ChatColor.GREEN + " Points Multiplier: " + ChatColor.UNDERLINE.toString() + ChatColor.DARK_GREEN + scoreMultiplier);
        updateExperience(p);
        killStreakMulti.send(p);
    }

    public void updateExperience(Player p) {
        double points = playerScore.get(p);
        points = (int) Math.round(points);
        p.setLevel((int) points);
    }

    public double getMultiplier(Player p) {
        double KillSteak = killStreak.get(p);
        if(KillSteak == 0) return 1;
        if(KillSteak > 9) return 2;
        return 1 + (KillSteak / 10);
    }

    public void addScore(Player p) {
        double score = playerScore.get(p);
        double multiplier = getMultiplier(p);
        double newScore = score + (100 * multiplier);
        playerScore.replace(p, newScore);
    }

    public void setPlayer(Player p) {
        playerKills.put(p, 0);
        playerScore.put(p, (double) 0);
        killStreak.put(p, (double) 0);
    }

    public void addKillStreak(Player p) {
        double kills = killStreak.get(p);
        kills = kills + 1;
        killStreak.replace(p, kills);
    }

    public void resetKills(Player p) {
        killStreak.replace(p, (double) 0);
    }
}
