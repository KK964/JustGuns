package net.justminecraft.minigames.gungame;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;
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

import java.util.HashMap;

public class JustGunsGame extends Game {
    public int taskId = 0;
    private JustGuns justguns;

    Scoreboard scoreboard;
    HashMap<Player, Integer> playerScore = new HashMap<>();
    HashMap<Player, Integer> killStreak = new HashMap<>();
    HashMap<Player, Integer> playerKills = new HashMap<>();

    public JustGunsGame(Minigame mg) {
        super(mg, false);
        justguns = (JustGuns) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    public void onPlayerDeath(Player p) {
        p.setVelocity(new Vector(0, 0, 0));
        p.setHealth(20);
        p.setFallDistance(0);
        p.setGameMode(GameMode.SPECTATOR);
        new PlayerRespawn((JustGuns) minigame, p);

        if (p.getLocation().getY() < 30) {
            p.teleport(new Location(p.getWorld(), 20, 90, 20, 135, 45));
        }
    }

    public  void updateScore(Player p) {
        scoreboard.resetScores(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + (playerKills.get(p) - 1));
        scoreboard.resetScores(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + (playerKills.get(p)));
        Game g = MG.core().getGame(p);
        if(g != null && g.minigame == justguns) {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + playerKills.get(p)).setScore(3);
        } else {
            scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(ChatColor.RED + p.getName() + ": " + ChatColor.DARK_RED + "✗").setScore(3);
        }
    }
}
