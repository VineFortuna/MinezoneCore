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

/** Opened from the lobby NPC - lets a player pick Solo or Duos before queuing for Flag Wars. */
public class FWModeSelectGUI implements InventoryProvider {

	private static final List<String> SOLO_DESCRIPTION = Arrays.asList(
			"&7Every player for themselves - steal",
			"&7enemy flags, defend your own, and",
			"&7outlast the rest in this fast-paced brawl!"
	);

	private static final List<String> DUO_DESCRIPTION = Arrays.asList(
			"&7Team up with a partner and take on the",
			"&7rest in Duos - up to 8 teams, 16 players",
			"&7battling it out at once!"
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

		contents.set(1, 3, ClickableItem.of(modeIcon(main, blueBanner(), FWGameMode.SOLO, SOLO_DESCRIPTION), e -> {
			inv.close(player);
			fwGameManager.joinGame(player, FWGameMode.SOLO);
		}));

		contents.set(1, 5, ClickableItem.of(modeIcon(main, steveHead(), FWGameMode.DUO, DUO_DESCRIPTION), e -> {
			inv.close(player);
			fwGameManager.joinGame(player, FWGameMode.DUO);
		}));
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
		lore.add("");
		for (String line : description) {
			lore.add(main.color(line));
		}
		lore.add("");
		lore.add(main.color("&eClick to Play!"));
		lore.add("");
		lore.add(main.color("&a" + playersInMode(mode) + " &rplaying!"));

		return ItemHelper.setDetails(icon, main.color("&6&lFLAG WARS&r &e&o" + mode.getLabel()), lore);
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
