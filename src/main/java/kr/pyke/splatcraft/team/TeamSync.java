package kr.pyke.splatcraft.team;

import kr.pyke.splatcraft.manager.PlayerTeamManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeamSync {
    private static final String TEAM_NAME_PREFIX = "splatcraft_team_";

    private TeamSync() { }

    public static String teamName(byte teamID) { return TEAM_NAME_PREFIX + teamID; }

    public static PlayerTeam ensureTeam(MinecraftServer server, byte teamID) {
        Scoreboard scoreboard = server.getScoreboard();
        String name = teamName(teamID);
        PlayerTeam team = scoreboard.getPlayerTeam(name);
        if (team == null) {
            team = scoreboard.addPlayerTeam(name);
            team.setSeeFriendlyInvisibles(true);
        }

        return team;
    }

    public static void assignTeam(MinecraftServer server, ServerPlayer player, byte teamID) {
        Scoreboard scoreboard = server.getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (currentTeam != null && currentTeam.getName().startsWith(TEAM_NAME_PREFIX)) {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), currentTeam);
        }

        if (teamID == 0) { return; }

        PlayerTeam team = ensureTeam(server, teamID);
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
    }

    public static void removeTeam(ServerPlayer player) {
        Scoreboard scoreboard = player.level().getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (currentTeam != null && currentTeam.getName().startsWith(TEAM_NAME_PREFIX)) {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), currentTeam);
        }
    }

    public static void syncAllOnLogin(MinecraftServer server, ServerPlayer player) {
        byte teamID = PlayerTeamManager.getTeamID(player);
        if (teamID != 0) {
            assignTeam(server, player, teamID);
        }
    }
}
