package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class GameSettings {

	public enum HealthPotMode {
		DISABLED("Disabled"), HALF_HEALTH("Potion - Half Health"), ALWAYS("Potion - Always"), FULL_HEAL("Instant Full Heal");

		private final String name;

		HealthPotMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/** One For All: everyone plays the same class, chosen by the host/co-host or voted on by everyone. */
	public enum OneForAllMode {
		DISABLED("Disabled"), HOST("Host/Co-Host"), VOTING("Voting");

		private final String name;

		OneForAllMode(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private GameInstance game;
	public int totalStartVotes; // how many players are ready
	public int dropTimer;
	public boolean santaFlyover;
	public List<Player> startVotes; // the players who are ready (the ready-up dye)
    public int strengthOnKillSeconds; // 0 = off
    public HealthPotMode healthPotMode; // Healing on Kill: when killers get a health pot, or a full heal
    public int maxHealthHearts; // each life's max health
    public OneForAllMode oneForAllMode; // everyone plays the same class
    public int spawnProtectionSeconds; // 0 = no spawn protection
    public boolean randomClasses;
    public int randomClassCycleSeconds;
    public int startingLives;
    public int respawnSeconds;
    public boolean lowGravity;
    public boolean passiveHealing; // natural regeneration (custom games only)
    public Set<ClassType> enabledClasses; // classes players may pick in a private game
    public Set<GameLootDrops> enabledDrops; // items lightning drops can be (none = all of them)

	public GameSettings(GameInstance game) {
		this.game = game;
		this.totalStartVotes = 0;
		this.dropTimer = 30; // Default lightning drop time
		this.santaFlyover = false;
		this.startVotes = new ArrayList<Player>();
        this.strengthOnKillSeconds = 0;
        this.healthPotMode = HealthPotMode.HALF_HEALTH;
        this.maxHealthHearts = 10;
        this.oneForAllMode = OneForAllMode.DISABLED;
        this.spawnProtectionSeconds = 3; // the Spawn Protection modifier (custom games only)
        this.randomClasses = false;
        this.randomClassCycleSeconds = 15;
        this.startingLives = 5; // Default lives
        this.respawnSeconds = 3; // Default respawn countdown
        this.lowGravity = false;
        this.passiveHealing = true;
        this.enabledClasses = EnumSet.noneOf(ClassType.class);
        for (ClassType type : ClassType.getAvailableClasses()) {
            this.enabledClasses.add(type); // default: every non-vaulted class
        }
        this.enabledDrops = EnumSet.allOf(GameLootDrops.class);
	}

	/** Takes over another game's Game Modifiers - a custom game's next round keeps the leader's settings. */
	public void copyModifiersFrom(GameSettings other) {
		this.dropTimer = other.dropTimer;
		this.strengthOnKillSeconds = other.strengthOnKillSeconds;
		this.healthPotMode = other.healthPotMode;
		this.maxHealthHearts = other.maxHealthHearts;
		this.oneForAllMode = other.oneForAllMode;
		this.spawnProtectionSeconds = other.spawnProtectionSeconds;
		this.randomClasses = other.randomClasses;
		this.randomClassCycleSeconds = other.randomClassCycleSeconds;
		this.startingLives = other.startingLives;
		this.respawnSeconds = other.respawnSeconds;
		this.lowGravity = other.lowGravity;
		this.passiveHealing = other.passiveHealing;
		this.enabledClasses.clear();
		this.enabledClasses.addAll(other.enabledClasses);
		this.enabledDrops.clear();
		this.enabledDrops.addAll(other.enabledDrops);
	}

	/** The Class List is untouched: every non-vaulted class (none enabled also falls back to those). */
	public boolean isDefaultClassList() {
		Set<ClassType> playable = EnumSet.noneOf(ClassType.class);
		for (ClassType type : enabledClasses) {
			if (type.isPlayable()) playable.add(type);
		}
		return playable.isEmpty() || playable.equals(EnumSet.copyOf(Arrays.asList(ClassType.getAvailableClasses())));
	}

	/** The Item List is untouched: every item (none enabled also falls back to every item). */
	public boolean isDefaultItemList() {
		return enabledDrops.isEmpty() || enabledDrops.size() == GameLootDrops.values().length;
	}

	// GETTER METHODS:

	public int getTotalStartVotes() {
		return this.totalStartVotes;
	}

	/*
	 * This function starts the game if the command /startgame is used or if
	 * everyone in the waiting lobby is ready
	 */
	public void forceStartGame(boolean startGameCommand) {
		// Custom games only start from their host's or co-host's countdown, never from votes
		if (!startGameCommand && game.isCustomGame() && !game.isCustomGameCountdownStarted()) return;
		if (!startGameCommand) {
			if (game.gameStartTime == null && game.timeToStartSeconds > 60) {
				return;
			}
		}
		game.TellAll(color("&2&l(!) &rGame is now starting"));
		if (game.gameStartTime != null) {
			game.gameStartTime.cancel();
		}

		game.StartGame();
	}

	public void changeGameType() {
		if (game == null) return;
		game.gameType = (game.gameType == GameType.FRENZY) ?  GameType.CLASSIC : GameType.FRENZY;
		game.TellAll(color("&2&l(!) &rThe game mode has been set to &e&l" + game.gameType.getName()));
		updateModeOnBoard();
	}
	
	private void updateModeOnBoard() {
		game.updateModeLine();
	}

	/** The player clicked the ready-up dye: ready <-> not ready. Everyone being ready starts the game. */
	public void toggleReady(Player player, GameInstance game) {
		GameSettings gs = null;

		if (game != null && game.getGameSettings() != null) {
			gs = game.getGameSettings();
			if (!(gs.startVotes.contains(player))) {
				gs.totalStartVotes++;
				gs.startVotes.add(player);
				updateGameStartVoteStatus(player, game, ChatColor.GREEN);
			} else {
				gs.totalStartVotes--;
				gs.startVotes.remove(player);
				updateGameStartVoteStatus(player, game, ChatColor.RED);
			}
		}
	}

	/** Nobody is ready anymore (the countdown stopped). */
	public void clearReady() {
		startVotes.clear();
		totalStartVotes = 0;
	}

	public void updateGameStartVoteStatus(Player player, GameInstance game, ChatColor color) {
		String status = color + (color == ChatColor.GREEN ? " is Ready " : " is no longer Ready ");
        String message = game.color("&2&l(!) &r" + player.getName() + status +
                "&a(" + game.getGameSettings().totalStartVotes + "/" + game.players.size() + ")");
		game.TellAll(message);

		if (game.getGameSettings().totalStartVotes == game.players.size())
			game.getGameSettings().forceStartGame(false);
	}

	/*
	 * This function handles removing players from the ready players if they leave the
	 * server or the game lobby
	 */
	public void removeFromStartVotes(Player player) {
		if (game != null && game.getGameSettings() != null) {
			GameSettings gs = game.getGameSettings();

			if (gs.startVotes.contains(player)) {
				gs.totalStartVotes--;
				gs.startVotes.remove(player);
			}
		}
	}

	private String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}
}
