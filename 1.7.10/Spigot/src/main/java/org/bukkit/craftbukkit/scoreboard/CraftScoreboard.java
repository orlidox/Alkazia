package org.bukkit.craftbukkit.scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.Scoreboard;
import net.minecraft.server.ScoreboardObjective;
import net.minecraft.server.ScoreboardTeam;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import com.google.common.collect.ImmutableSet;

public final class CraftScoreboard implements org.bukkit.scoreboard.Scoreboard {
	final Scoreboard board;
	final Map<String, CraftObjective> objectives = new HashMap<String, CraftObjective>();
	final Map<String, CraftTeam> teams = new HashMap<String, CraftTeam>();

	CraftScoreboard(Scoreboard board) {
		this.board = board;

		for (ScoreboardObjective objective : (Iterable<ScoreboardObjective>) board.getObjectives()) {
			new CraftObjective(this, objective); // It adds itself to map
		}
		for (ScoreboardTeam team : (Iterable<ScoreboardTeam>) board.getTeams()) {
			new CraftTeam(this, team); // It adds itself to map
		}
	}

	@Override
	public CraftObjective registerNewObjective(String name, String criteria) throws IllegalArgumentException {
		Validate.notNull(name, "Objective name cannot be null");
		Validate.notNull(criteria, "Criteria cannot be null");
		Validate.isTrue(name.length() <= 16, "The name '" + name + "' is longer than the limit of 16 characters");
		Validate.isTrue(board.getObjective(name) == null, "An objective of name '" + name + "' already exists");

		CraftCriteria craftCriteria = CraftCriteria.getFromBukkit(criteria);
		ScoreboardObjective objective = board.registerObjective(name, craftCriteria.criteria);
		return new CraftObjective(this, objective);
	}

	@Override
	public Objective getObjective(String name) throws IllegalArgumentException {
		Validate.notNull(name, "Name cannot be null");
		return objectives.get(name);
	}

	@Override
	public ImmutableSet<Objective> getObjectivesByCriteria(String criteria) throws IllegalArgumentException {
		Validate.notNull(criteria, "Criteria cannot be null");

		ImmutableSet.Builder<Objective> objectives = ImmutableSet.builder();
		for (CraftObjective objective : this.objectives.values()) {
			if (objective.getCriteria().equals(criteria)) {
				objectives.add(objective);
			}
		}
		return objectives.build();
	}

	@Override
	public ImmutableSet<Objective> getObjectives() {
		return ImmutableSet.copyOf((Collection<? extends Objective>) objectives.values());
	}

	@Override
	public Objective getObjective(DisplaySlot slot) throws IllegalArgumentException {
		Validate.notNull(slot, "Display slot cannot be null");
		ScoreboardObjective objective = board.getObjectiveForSlot(CraftScoreboardTranslations.fromBukkitSlot(slot));
		if (objective == null)
			return null;
		return objectives.get(objective.getName());
	}

	@Override
	public ImmutableSet<Score> getScores(OfflinePlayer player) throws IllegalArgumentException {
		Validate.notNull(player, "OfflinePlayer cannot be null");

		ImmutableSet.Builder<Score> scores = ImmutableSet.builder();
		for (CraftObjective objective : objectives.values()) {
			scores.add(objective.getScore(player));
		}
		return scores.build();
	}

	@Override
	public ImmutableSet<Score> getScores(String entry) throws IllegalArgumentException {
		Validate.notNull(entry, "Entry cannot be null");

		ImmutableSet.Builder<Score> scores = ImmutableSet.builder();
		for (CraftObjective objective : objectives.values()) {
			scores.add(objective.getScore(entry));
		}
		return scores.build();
	}

	@Override
	public void resetScores(OfflinePlayer player) throws IllegalArgumentException {
		Validate.notNull(player, "OfflinePlayer cannot be null");

		board.resetPlayerScores(player.getName());
	}

	@Override
	public void resetScores(String entry) throws IllegalArgumentException {
		Validate.notNull(entry, "Entry cannot be null");

		board.resetPlayerScores(entry);
	}

	@Override
	public Team getPlayerTeam(OfflinePlayer player) throws IllegalArgumentException {
		Validate.notNull(player, "OfflinePlayer cannot be null");

		ScoreboardTeam team = board.getPlayerTeam(player.getName());
		return team == null ? null : teams.get(team.getName());
	}

	@Override
	public Team getTeam(String teamName) throws IllegalArgumentException {
		Validate.notNull(teamName, "Team name cannot be null");

		return teams.get(teamName);
	}

	@Override
	public ImmutableSet<Team> getTeams() {
		return ImmutableSet.copyOf((Collection<? extends Team>) teams.values());
	}

	@Override
	public Team registerNewTeam(String name) throws IllegalArgumentException {
		Validate.notNull(name, "Team name cannot be null");
		Validate.isTrue(name.length() <= 16, "Team name '" + name + "' is longer than the limit of 16 characters");
		Validate.isTrue(board.getTeam(name) == null, "Team name '" + name + "' is already in use");

		return new CraftTeam(this, board.createTeam(name));
	}

	@Override
	public ImmutableSet<OfflinePlayer> getPlayers() {
		ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
		for (Object playerName : board.getPlayers()) {
			players.add(Bukkit.getOfflinePlayer(playerName.toString()));
		}
		return players.build();
	}

	@Override
	public ImmutableSet<String> getEntries() {
		ImmutableSet.Builder<String> entries = ImmutableSet.builder();
		for (Object entry : board.getPlayers()) {
			entries.add(entry.toString());
		}
		return entries.build();
	}

	@Override
	public void clearSlot(DisplaySlot slot) throws IllegalArgumentException {
		Validate.notNull(slot, "Slot cannot be null");
		board.setDisplaySlot(CraftScoreboardTranslations.fromBukkitSlot(slot), null);
	}

	// CraftBukkit method
	public Scoreboard getHandle() {
		return board;
	}
}
