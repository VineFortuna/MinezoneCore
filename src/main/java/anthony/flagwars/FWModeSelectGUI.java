package anthony.flagwars;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Opened from the lobby NPC - lets a player pick one of the available modes (see FWGameMode#isAvailable) before queuing for Flag Wars. */
public class FWModeSelectGUI implements InventoryProvider {

	private static final List<String> SOLO_DESCRIPTION = Arrays.asList(
			"&7Every player for themselves, steal enemy",
			"&7flags, defend your own, and be the first",
			"&7to capture 4 flags to be victorious!"
	);

	private static final List<String> DUO_DESCRIPTION = Arrays.asList(
			"&7Team up with a partner and take on the",
			"&7rest in Duos. Up to 8 teams, 16 players",
			"&7battling it out for 4 flags!"
	);

	private final FWGameManager fwGameManager;
	private final SmartInventory inv;

	public FWModeSelectGUI(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
		this.inv = SmartInventory.builder()
				.id("fwModeSelect")
				.provider(this)
				.size(3, 9)
				.title(fwGameManager.getMain().color("&8&lFlag Wars"))
				.build();
	}

	public void open(Player player) {
		inv.open(player);
	}

	/**
	 * What clicking Flag Wars in the lobby does: with only one mode available there is nothing to choose, so the
	 * player is queued for it right away, and the menu only opens once there is more than one.
	 */
	public static void openOrJoin(FWGameManager fwGameManager, Player player) {
		List<FWGameMode> modes = FWGameMode.availableModes();
		if (modes.size() == 1) {
			player.closeInventory(); // the game selector, if that is where they clicked
			fwGameManager.joinGame(player, modes.get(0));
			return;
		}
		new FWModeSelectGUI(fwGameManager).open(player);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		render(player, contents);
	}

	@Override
	public void update(Player player, InventoryContents contents) {
		render(player, contents);
	}

	private void render(Player player, InventoryContents contents) {
		Core main = fwGameManager.getMain();

		ItemStack pane = ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " ");
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				if (row == 0 || row == 2 || col == 0 || col == 8) {
					contents.set(row, col, ClickableItem.of(pane, e -> {}));
				}
			}
		}

		// Only the modes that can be joined right now. One sits in the middle, two sit either side of it.
		List<FWGameMode> modes = FWGameMode.availableModes();
		int[] columns = modes.size() == 1 ? new int[] {4} : new int[] {3, 5};

		for (int i = 0; i < modes.size() && i < columns.length; i++) {
			final FWGameMode mode = modes.get(i);
			ItemStack icon = mode == FWGameMode.SOLO ? blueBanner() : steveHead();
			List<String> description = mode == FWGameMode.SOLO ? SOLO_DESCRIPTION : DUO_DESCRIPTION;

			contents.set(1, columns[i], ClickableItem.of(modeIcon(main, icon, mode, description), e -> {
				inv.close(player);
				fwGameManager.joinGame(player, mode);
			}));
		}
	}

	private ItemStack blueBanner() {
		ItemStack banner = new ItemStack(Material.BANNER);
		BannerMeta meta = (BannerMeta) banner.getItemMeta();
		meta.setBaseColor(DyeColor.BLUE);
		banner.setItemMeta(meta);
		return banner;
	}

	/** Plain player head with no owner set - renders as the default Steve skin. */
	private ItemStack steveHead() {
		return new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	}

	private ItemStack modeIcon(Core main, ItemStack icon, FWGameMode mode, List<String> description) {
		List<String> lore = new ArrayList<>();
		lore.add(main.color("&rMode: &a" + mode.getLabel()));
		lore.add("");
		for (String line : description) {
			lore.add(main.color(line));
		}
		lore.add("");
		lore.add(main.color("&eClick to Play!"));
		lore.add("");
		lore.add(main.color("&a" + playersInMode(mode) + " &rplaying!"));

		return ItemHelper.setDetails(icon, main.color("&6&lFLAG WARS"), lore);
	}

	/** Summed across every concurrent instance of this mode - FWGameManager can run more than one at once. */
	private int playersInMode(FWGameMode mode) {
		int total = 0;
		for (FWGameInstance instance : fwGameManager.getActiveInstances()) {
			if (instance.getMode() == mode) {
				total += instance.countPlayers();
			}
		}
		return total;
	}
}
