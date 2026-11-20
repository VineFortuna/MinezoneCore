package anthony.flagwars.vote;

import anthony.SuperCraftBrawl.Core;
import anthony.flagwars.FWGameConstants;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWTeam;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.UUID;

/** Mirrors SuperCraftBros' own SpectatorGUI: a clickable list of every active player, teleporting the viewer to whoever they pick. */
public class FWSpectatorGUI implements InventoryProvider {

	private final Core main;
	private final FWGameInstance instance;
	private final SmartInventory inv;

	public FWSpectatorGUI(FWGameInstance instance) {
		this.instance = instance;
		this.main = instance.getManager().getMain();
		this.inv = SmartInventory.builder().id("fwSpectate").provider(this).size(3, 9)
				.title(main.color("&8&lSpectate")).build();
	}

	public void open(Player player) {
		inv.open(player);
	}

	@Override
	public void init(Player player, InventoryContents contents) {
		int slot = 0;

		for (FWTeam team : instance.getTeams().values()) {
			for (UUID uuid : team.getMembers()) {
				Player target = Bukkit.getPlayer(uuid);
				if (target == null) continue;
				if (slot >= 27) return; // only 3 rows to work with

				contents.set(slot / 9, slot % 9, ClickableItem.of(playerIcon(target, team), e -> {
					player.teleport(target);
					inv.close(player);
				}));
				slot++;
			}
		}
	}

	@Override
	public void update(Player player, InventoryContents contents) {
	}

	private ItemStack playerIcon(Player target, FWTeam team) {
		String teamColor = FWGameConstants.chatColorCodeFor(team.getColor());
		String health = new DecimalFormat("#.#").format(target.getHealth());

		return ItemHelper.setDetails(ItemHelper.createSkullHeadPlayer(1, target.getName()),
				main.color("&fPlayer: &a" + target.getName()),
				main.color("&fTeam: " + teamColor + team.getName()),
				main.color("&fKills: &a" + instance.getKills(target)),
				main.color("&fFlags Captured: &a" + instance.getFlagCaptures(target)),
				main.color("&fHealth: &a" + health + "/20"));
	}
}
