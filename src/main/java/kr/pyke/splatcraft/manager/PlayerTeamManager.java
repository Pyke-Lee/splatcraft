package kr.pyke.splatcraft.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kr.pyke.splatcraft.SplatCraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTeamManager {
    private static final Map<UUID, Byte> playerTeams = new ConcurrentHashMap<>();
    private static final Map<Byte, String> teamNames = new ConcurrentHashMap<>();

    private PlayerTeamManager() { }

    public static byte getTeamID(UUID playerID) { return playerTeams.getOrDefault(playerID, (byte) 0); }

    public static byte getTeamID(ServerPlayer player) { return getTeamID(player.getUUID()); }

    public static boolean hasTeam(UUID playerID) { return playerTeams.containsKey(playerID) && playerTeams.get(playerID) != 0; }

    public static void setTeamID(UUID playerID, byte teamID) {
        if (teamID == 0) {
            playerTeams.remove(playerID);
        }
        else {
            playerTeams.put(playerID, teamID);
        }
    }

    public static void removePlayer(UUID playerID) {
        playerTeams.remove(playerID);
    }

    public static int clearTeam(byte teamID) {
        int removed = 0;

        var iterator = playerTeams.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() == teamID) {
                iterator.remove();
                removed++;
            }
        }

        return removed;
    }

    public static List<UUID> getPlayersInTeam(byte teamID) {
        List<UUID> result = new ArrayList<>();
        for (var entry : playerTeams.entrySet()) {
            if (entry.getValue() == teamID) {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    public static void setTeamName(byte teamID, String name) {
        teamNames.put(teamID, name);
    }

    public static String getTeamName(byte teamID) { return teamNames.getOrDefault(teamID, "팀 " + teamID); }

    public static boolean hasTeamName(byte teamID) { return teamNames.containsKey(teamID); }

    public static void clear() {
        playerTeams.clear();
        teamNames.clear();
    }

    public static void loadFromSavedData(MinecraftServer server) {
        TeamSavedData savedData = server.overworld().getDataStorage().computeIfAbsent(TeamSavedData.TYPE);
        playerTeams.clear();
        teamNames.clear();

        for (PlayerTeamEntry entry : savedData.getPlayerEntries()) {
            playerTeams.put(entry.playerID, entry.teamID);
        }
        for (TeamNameEntry entry : savedData.getNameEntries()) {
            teamNames.put(entry.teamID, entry.name);
        }
    }

    public static void markSavedDataDirty(MinecraftServer server) {
        TeamSavedData savedData = server.overworld().getDataStorage().computeIfAbsent(TeamSavedData.TYPE);

        List<PlayerTeamEntry> playerEntries = new ArrayList<>();
        for (var entry : playerTeams.entrySet()) {
            playerEntries.add(new PlayerTeamEntry(entry.getKey(), entry.getValue()));
        }

        List<TeamNameEntry> nameEntries = new ArrayList<>();
        for (var entry : teamNames.entrySet()) {
            nameEntries.add(new TeamNameEntry(entry.getKey(), entry.getValue()));
        }

        savedData.setEntries(playerEntries, nameEntries);
        savedData.setDirty();
    }

    private record PlayerTeamEntry(UUID playerID, byte teamID) {
        static final Codec<PlayerTeamEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("player_id").forGetter(PlayerTeamEntry::playerID),
            Codec.BYTE.fieldOf("team_id").forGetter(PlayerTeamEntry::teamID)
        ).apply(instance, PlayerTeamEntry::new));
    }

    private record TeamNameEntry(byte teamID, String name) {
        static final Codec<TeamNameEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("team_id").forGetter(TeamNameEntry::teamID),
            Codec.STRING.fieldOf("name").forGetter(TeamNameEntry::name)
        ).apply(instance, TeamNameEntry::new));
    }

    private record TeamSavedDataContent(List<PlayerTeamEntry> players, List<TeamNameEntry> names) {
        static final Codec<TeamSavedDataContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerTeamEntry.CODEC.listOf().fieldOf("players").forGetter(TeamSavedDataContent::players),
            TeamNameEntry.CODEC.listOf().fieldOf("names").forGetter(TeamSavedDataContent::names)
        ).apply(instance, TeamSavedDataContent::new));
    }

    public static class TeamSavedData extends SavedData {
        private static final Identifier FILE_NAME = Identifier.fromNamespaceAndPath(SplatCraft.MOD_ID, "splatcraft_teams");

        public static final Codec<TeamSavedData> CODEC = TeamSavedDataContent.CODEC.xmap(
            content -> {
                TeamSavedData data = new TeamSavedData();
                data.playerEntries = new ArrayList<>(content.players);
                data.nameEntries = new ArrayList<>(content.names);

                return data;
            },
            data -> new TeamSavedDataContent(data.playerEntries, data.nameEntries)
        );

        public static final SavedDataType<TeamSavedData> TYPE = new SavedDataType<>(FILE_NAME, TeamSavedData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

        private List<PlayerTeamEntry> playerEntries = new ArrayList<>();
        private List<TeamNameEntry> nameEntries = new ArrayList<>();

        public TeamSavedData() { }

        List<PlayerTeamEntry> getPlayerEntries() { return playerEntries; }

        List<TeamNameEntry> getNameEntries() { return nameEntries; }

        void setEntries(List<PlayerTeamEntry> players, List<TeamNameEntry> names) {
            this.playerEntries = players;
            this.nameEntries = names;
        }
    }
}