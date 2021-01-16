package net.justminecraft.minigames.gungame;

import net.justminecraft.minigames.minigamecore.Game;
import net.justminecraft.minigames.minigamecore.Minigame;

public class JustGunsGame extends Game {
    public int taskId = 0;
    Scoreboard scoreboard;
    HashMap<Player, Integer> playerScore = new HashMap<>();
    HashMap<Player, Integer> killStreak = new HashMap<>();
    HashMap<Player, Integer> playerKills = new HashMap<>();

    public JustGunsGame(Minigame mg) {
        super(mg);
        justguns = (JustGuns) mg;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
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
