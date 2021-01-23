package net.justminecraft.minigames.gungame;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import net.justminecraft.minigames.minigamecore.ActionBar;
import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.MG;
import net.justminecraft.minigames.minigamecore.Minigame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JustGunsGame extends Game {
    public int taskId = 0;
    private JustGuns justguns;

    Scoreboard scoreboard;
    HashMap<Player, Double> playerScore = new HashMap<>();
    HashMap<Player, Double> killStreak = new HashMap<>();
    HashMap<Player, Integer> playerKills = new HashMap<>();

    public JustGunsGame(Minigame mg) {
        super(mg, false);
        justguns = (JustGuns) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public String getMapSize(int p) {
        if(p < 5) return "small";
        if(p > 5) return "large";
        return "small";
    }

    public String getRandomMap(Game game) {
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
        return maps.get((int) (Math.random() * maps.size()));
    }

    public void onPlayerDeath(Player p) {
        p.setVelocity(new Vector(0, 0, 0));
        p.setHealth(20);
        p.setFallDistance(0);
        p.setGameMode(GameMode.SPECTATOR);
        resetKills(p);
        updateExperience(p);
        if (p.getLocation().getY() < 30) {
            p.teleport(new Location(p.getWorld(), 0, 90, 0, 135, 45));
        }
        if(isGameOver()) {
            endGame();
        } else {
            new PlayerRespawn((JustGuns) minigame, p);
        }
    }

    public boolean isGameOver() {
        boolean over = false;
        int killsNeeded = neededKills();
        for(Player p : players) {
            if(playerKills.get(p) >= killsNeeded) over = true;
        }
        return over;
    }

    public void endGame() {
        Player winner = getWinner();
        for(Player p : players) {
            if(p != winner) {
                playerLeave(p);
            }
        }
        finishGame();
    }

    public Player getWinner() {
        Player p = null;
        int killsNeeded = neededKills();
        for(Player player : players) {
            if(playerKills.get(player) >= killsNeeded) p = player;
        }
        return p;
    }

    public int neededKills() {
        int killsNeeded = 20;
        int playersInt = players.size();
        if(playersInt <= 2) killsNeeded = 10;
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
        double KillSteak = killStreak.get(p);
        double scoreMultiplier = getMultiplier(p);
        ActionBar killStreakMulti = new ActionBar(ChatColor.GREEN + "Points Multiplier: " + ChatColor.UNDERLINE.toString() + ChatColor.DARK_GREEN + scoreMultiplier);
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
        double multi = 1 + (KillSteak / 10);
        return multi;
    }

    public void addScore(Player p) {
        double score = playerScore.get(p);
        double multiplier = getMultiplier(p);
        double newScore = score + (100 * multiplier);
        playerScore.replace(p, newScore);
    }

    public void setPlayer(Player p) {
        playerKills.put(p, 0);
        playerScore.put(p, Double.valueOf(0));
        killStreak.put(p, Double.valueOf(0));
    }

    public void addKillStreak(Player p) {
        double kills = killStreak.get(p);
        kills = kills + 1;
        killStreak.replace(p, kills);
    }

    public void resetKills(Player p) {
        killStreak.replace(p, Double.valueOf(0));
    }
}
