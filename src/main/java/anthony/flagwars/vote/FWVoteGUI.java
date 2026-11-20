package anthony.flagwars.vote;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWGameState;
import anthony.flagwars.FWTeam;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Flag Wars's lobby vote menu - mirrors SuperCraftBros' VoteGameSettingsGUI,
 * trimmed to FD's own settings: force-starting the queued countdown, voting
 * the match to take place at night, and doubling the emerald/diamond
 * generator spawn rate. All three are majority votes (more than half of the
 * players currently in the lobby), tallied in FWGameSettings.
 */
public class FWVoteGUI implements InventoryProvider {

	private final Core main;
	private final FWGameInstance instance;
	private final SmartInventory inv;

	public FWVoteGUI(FWGameInstance instance) {
		this.instance = instance;
		this.main = instance.getManager().getMain();
		this.inv = SmartInventory.builder().id("fdVoteGameSettings").provider(this).size(3, 9)
				.title(main.color("&8&lVote")).build();
	}

	public void open(Player player) {
		inv.open(player);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		FWGameSettings settings = instance.getGameSettings();

		if (instance.getState() == FWGameState.STARTING) {
			addGameStartButton(contents, player, settings);
		}
		addNightTimeButton(contents, player, settings);
		addDoubleGeneratorButton(contents, player, settings);
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	private void addGameStartButton(InventoryContents contents, Player player, FWGameSettings settings) {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.BEACON),
				main.color("&eGame Start"),
				"&7Skip the rest of the countdown",
				"&7and start the game immediately",
				"",
				main.color("&fVotes: &a(" + settings.getStartVotes() + "/" + instance.countPlayers() + ")"));

		contents.set(0, 2, ClickableItem.of(item, event -> {
			SoundManager.playSuccessfulHit(player);
			settings.handleVoteGameStart(player);
			refreshForAll();
		}));
	}

	private void addNightTimeButton(InventoryContents contents, Player player, FWGameSettings settings) {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.WATCH),
				main.color("&eSet Time To Night"),
				"&7Vote to make this match",
				"&7take place at night",
				"",
				main.color("&fVotes: &a(" + settings.getNightVotes() + "/" + instance.countPlayers() + ")"));

		contents.set(0, 4, ClickableItem.of(item, event -> {
			SoundManager.playSuccessfulHit(player);
			settings.handleVoteNightTime(player);
			refreshForAll();
		}));
	}

	private void addDoubleGeneratorButton(InventoryContents contents, Player player, FWGameSettings settings) {
		ItemStack item = ItemHelper.setDetails(new ItemStack(Material.DIAMOND),
				main.color("&eGenerator Spawn Rate -> 2x"),
				"&7Emerald & diamond generators",
				"&7spawn twice as often",
				"",
				main.color("&fVotes: &a(" + settings.getDoubleGeneratorVotes() + "/" + instance.countPlayers() + ")"));

		contents.set(0, 6, ClickableItem.of(item, event -> {
			SoundManager.playSuccessfulHit(player);
			settings.handleVoteDoubleGenerators(player);
			refreshForAll();
		}));
	}

	/** Re-opens the menu for everyone currently viewing it so every vote count updates live, same as SCB's openForAll. */
	private void refreshForAll() {
		for (FWTeam team : instance.getTeams().values()) {
			for (UUID uuid : team.getMembers()) {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) reopenIfViewing(p);
			}
		}
		for (Player p : instance.getSpectators()) {
			reopenIfViewing(p);
		}
	}

	private void reopenIfViewing(Player p) {
		if (p.getOpenInventory() != null && p.getOpenInventory().getTitle().equals(inv.getTitle())) {
			inv.open(p);
		}
	}
}
