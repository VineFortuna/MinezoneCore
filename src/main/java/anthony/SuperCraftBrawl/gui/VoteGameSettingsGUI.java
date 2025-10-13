package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Bars;
import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoteGameSettingsGUI implements InventoryProvider {

	private final Core main;
	public SmartInventory inv;

	public VoteGameSettingsGUI(Core main) {
		this.main = main;
		this.inv = SmartInventory.builder().id("voteGameSettings").provider(this).size(4, 9)
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
			addVoteGameStartButton(contents, player, game);
			addVoteTimeButton(contents, player, game);
			addLightningRateButton(contents, player, game);

			if (game.gameType != GameType.DUEL) // Don't let players change game mode if duels
				addVoteGameTypeButton(contents, player, data, game);
		}
	}

	/**
	 * Adds a "Vote for Game Start" item to the specified inventory.
	 * 
	 * @param contents The inventory contents where the item will be added.
	 * @param player   The player who is interacting with the inventory.
	 * @param game     The current game instance in which the player is involved.
	 */
	private void addVoteGameStartButton(InventoryContents contents, Player player, GameInstance game) {
		ItemStack voteGameStart = ItemHelper.setDetails(new ItemStack(Material.BEACON), ChatColor.YELLOW + "Game Start",
				"", "" + ChatColor.RESET + "(" + (game != null ? game.getGameSettings().totalStartVotes : "0") + "/"
						+ (game != null ? game.players.size() : "0") + ")",
				"",
				"&7Start the game immediately",
				"&7when all players are ready");
		contents.set(1, 3, ClickableItem.of(voteGameStart, event -> {
			if (event.getWhoClicked() instanceof Player) {
				Player clickingPlayer = (Player) event.getWhoClicked();
				SoundManager.playSuccessfulHit(player);
				game.getGameSettings().handleVoteGameStart(clickingPlayer, game);
				openForAll(game);
				inv.close(player);
			}
		}));
	}

	/**
	 * Adds a "Vote for Time of Day" item to the specified inventory.
	 * 
	 * @param contents The inventory contents where the item will be added.
	 * @param player   The player who is interacting with the inventory.
	 * @param game     The current game instance in which the player is involved.
	 */
	private void addVoteTimeButton(InventoryContents contents, Player player, GameInstance game) {
		String timeSetting = "";
		if (game.getMapWorld().getTime() == 1000)
			timeSetting = "Night";
		else
			timeSetting = "Day";

		ItemStack voteTime = ItemHelper.setDetails(new ItemStack(Material.WATCH),
				ChatColor.YELLOW + "Time Of Day -> " + timeSetting, "",
				"" + ChatColor.RESET + "(" + (game != null ? game.getGameSettings().totalTimeVotes : "0") + "/"
						+ (game != null ? game.players.size() : "0") + ")",
				"",
				"&7Change the time of day to",
				"&7" + timeSetting.toLowerCase() + " for the entire game");
		contents.set(1, 5, ClickableItem.of(voteTime, event -> {
			if (event.getWhoClicked() instanceof Player) {
				SoundManager.playSuccessfulHit(player);
				game.getGameSettings().handleVoteTime(player, game);
				openForAll(game);
			}
		}));
	}

	/**
	 * Adds a "Vote for Game Type" item to the specified inventory.
	 * 
	 * @param contents The inventory contents where the item will be added.
	 * @param player   The player who is interacting with the inventory.
	 * @param game     The current game instance in which the player is involved.
	 */
	private void addVoteGameTypeButton(InventoryContents contents, Player player, PlayerData data, GameInstance game) {
	    // Determine the "next" type the player would vote for
	    GameType nextType = (game != null && game.gameType == GameType.CLASSIC) ? GameType.FRENZY : GameType.CLASSIC;

	    // Basic counts (safe if game is null)
	    int votes = (game != null) ? game.getGameSettings().totalGameTypeVotes : 0;
	    int total = (game != null) ? Math.max(game.players.size(), 1) : 1; // avoid /0
	    int percent = Math.min(100, Math.max(0, (int) Math.round((votes * 100.0) / total)));

	    // Pick a material that “reads” the mode
	    Material icon = (nextType == GameType.FRENZY) ? Material.TNT : Material.COMPASS;

	    // Title + subtitle line
	    String title = ChatColor.GOLD + "" + ChatColor.BOLD + "Change Game Type";
	    String subtitle = ChatColor.YELLOW + "→ " + ChatColor.WHITE + nextType.getName();

	    // Pretty progress bar: keep [ ] and % just like you wanted
	    String bar = Bars.progressBar(votes, total, 12, org.bukkit.ChatColor.GREEN, org.bukkit.ChatColor.DARK_GRAY, '■', true);

	    // Build lore (center-ish spacing with subtle separators)
	    List<String> lore = Arrays.asList(
	        ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "------------------------",
	        ChatColor.GRAY + "Votes: " + ChatColor.WHITE + "(" + votes + "/" + total + ")",
	        "",
	        ChatColor.GRAY + "Mode Details:",
	        ChatColor.DARK_AQUA + "  • " + ChatColor.WHITE + "Random class each life, even ones",
	        ChatColor.DARK_AQUA + "  • " + ChatColor.WHITE + "you have not unlocked yet",
	        ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "------------------------",
	        ChatColor.GREEN + "" + ChatColor.BOLD + "Click to cast your vote"
	    );

	    ItemStack stack = new ItemStack(icon);
	    ItemMeta meta = stack.getItemMeta();
	    meta.setDisplayName(title + ChatColor.RESET + "  " + subtitle);
	    meta.setLore(lore);
	    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
	    stack.setItemMeta(meta);

	    // Add a subtle glow
	    makeGlow(stack);

	    contents.set(3, 5, ClickableItem.of(stack, event -> {
	        if (event.getWhoClicked() instanceof Player) {
	            SoundManager.playSuccessfulHit(player);
	            if (game != null) {
	                game.getGameSettings().handleVoteGameType(player, game);
	                openForAll(game);
	            }
	        }
	    }));
	}

	/** Adds a cosmetic glow without showing enchant text (we hide it with ItemFlag). */
	private void makeGlow(ItemStack item) {
	    try {
	        item.addUnsafeEnchantment(Enchantment.LUCK, 1); // any low-level enchant works
	        ItemMeta m = item.getItemMeta();
	        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	        item.setItemMeta(m);
	    } catch (Exception ignored) {}
	}

	/**
	 * Adds a "Lightning Drop Rate" item to the specified inventory.
	 * 
	 * @param contents The inventory contents where the item will be added.
	 * @param player   The player who is interacting with the inventory.
	 * @param game     The current game instance in which the player is involved.
	 */
	private void addLightningRateButton(InventoryContents contents, Player player, GameInstance game) {
		ItemStack lightningRate = ItemHelper.setDetails(new ItemStack(Material.NETHER_STAR),
				ChatColor.YELLOW + "Lightning Drop Rate -> 2x", "",
				"" + ChatColor.RESET + "(" + (game != null ? game.getGameSettings().getLightningVotes() : "0") + "/"
						+ (game != null ? game.players.size() : "0") + ")",
				"",
				"&7Items spawn every 15 seconds",
				"&7instead of every 30");
		contents.set(3, 3, ClickableItem.of(lightningRate, event -> {
			if (event.getWhoClicked() instanceof Player) {
				SoundManager.playSuccessfulHit(player);
				game.getGameSettings().handleLightningRate(player, game);
				openForAll(game);
			}
		}));
	}

	@Override
	public void update(Player player, InventoryContents contents) {

	}

	private void openForAll(GameInstance game) {
		for (Player p : game.players) {
			if (p.getOpenInventory() != null && p.getOpenInventory().getTitle().equals(inv.getTitle())) {
				inv.open(p);
			}
		}
	}
}