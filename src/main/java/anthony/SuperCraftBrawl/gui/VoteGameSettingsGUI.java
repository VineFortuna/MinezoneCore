package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class VoteGameSettingsGUI implements InventoryProvider {

	private final Core main;
	public SmartInventory inv;

	public VoteGameSettingsGUI(Core main) {
		this.main = main;
		this.inv = SmartInventory.builder().id("voteGameSettings").provider(this).size(3, 9)
				.title(ChatColor.DARK_GRAY.toString() + ChatColor.BOLD + "Vote").build();
	}

	/*
	 * This function initializes the inventory GUI & adds contents to it with
	 * clickable actions to vote for game start, time of day or change the game mode
	 */
	@Override
	public void init(Player player, InventoryContents contents) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (game != null && game.state == GameState.WAITING && game.players.size() >= 2) {
			addVoteGameStartButton(contents, player, data, game);
			addVoteTimeButton(contents, player, data, game);
			addVoteGameTypeButton(contents, player, data, game);
		}
	}

	private void addVoteGameStartButton(InventoryContents contents, Player player, PlayerData data, GameInstance game) {
		ItemStack voteGameStart = ItemHelper.setDetails(new ItemStack(Material.BEACON), ChatColor.YELLOW + "Game Start",
				"", "(" + (game != null ? game.getGameSettings().totalStartVotes : "0") + "/"
						+ (game != null ? game.players.size() : "0") + ")");
		contents.set(1, 3, ClickableItem.of(voteGameStart, event -> {
			if (event.getWhoClicked() instanceof Player) {
				Player clickingPlayer = (Player) event.getWhoClicked();
				inv.close(clickingPlayer);
				handleVoteGameStart(clickingPlayer, data, game);
			}
		}));
	}

	private void addVoteTimeButton(InventoryContents contents, Player player, PlayerData data, GameInstance game) {
		ItemStack voteTime = ItemHelper.setDetails(new ItemStack(Material.WATCH),
				ChatColor.YELLOW + "Time of Day -> Night", "",
				"(" + (game != null ? game.getGameSettings().totalTimeVotes : "0") + "/"
						+ (game != null ? game.players.size() : "0") + ")");
		contents.set(1, 5, ClickableItem.of(voteTime, event -> {
			if (event.getWhoClicked() instanceof Player) {
				Player clickingPlayer = (Player) event.getWhoClicked();
				inv.close(clickingPlayer);
			}
		}));
	}

	private void addVoteGameTypeButton(InventoryContents contents, Player player, PlayerData data, GameInstance game) {
		ItemStack voteGameType = ItemHelper.setDetails(new ItemStack(Material.BEACON),
				ChatColor.YELLOW + "Vote Game Type -> Frenzy", "",
				"(" + (game != null ? game.getGameSettings().totalGameTypeVotes : "0") + "/"
						+ (game != null ? game.players.size() : "0") + ")");
		contents.set(2, 4, ClickableItem.of(voteGameType, event -> {
			if (event.getWhoClicked() instanceof Player) {
				Player clickingPlayer = (Player) event.getWhoClicked();
				inv.close(clickingPlayer);
				handleVoteGameType(game);
			}
		}));
	}

	private void handleVoteGameStart(Player player, PlayerData playerData, GameInstance game) {
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

	private void handleVoteGameType(GameInstance game) {
		
	}

	private void updateGameStartVoteStatus(Player player, GameInstance game, ChatColor color) {
		String status = color + (color == ChatColor.GREEN ? " is Ready " : " is no longer Ready ");
		String message = ChatColor.DARK_GREEN + ChatColor.BOLD.toString() + "(!) " + ChatColor.RESET + ChatColor.YELLOW
				+ player.getName() + ChatColor.BOLD + status + ChatColor.RED + "(" + ChatColor.GREEN
				+ game.getGameSettings().totalStartVotes + "/" + game.players.size() + ChatColor.RED + ")";
		game.TellAll(message);

		if (game.getGameSettings().totalStartVotes == game.players.size())
			game.getGameSettings().forceStartGame();
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}
}