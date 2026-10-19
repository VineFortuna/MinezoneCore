package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/*
 * Game Modifiers > Co-Host: the host picks one player in the game to change the Game Modifiers with them.
 * Clicking the current co-host removes them; clicking anyone else makes them the co-host instead.
 * Laid out like the maps GUI: heads inside the border, page arrows on the sides, Go Back in the bottom middle.
 */
public class CoHostGUI implements InventoryProvider {

    private static final int ROWS = 4;

    private final Core main;
    private final GameInstance game;
    private final GameModifiersGUI parent;
    public final SmartInventory inv;

    public CoHostGUI(Core main, GameInstance game, GameModifiersGUI parent) {
        this.main = main;
        this.game = game;
        this.parent = parent;

        this.inv = SmartInventory.builder()
                .id("coHost")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Choose a Co-Host")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        List<ClickableItem> heads = new ArrayList<>();
        for (Player target : game.players) {
            if (game.isCustomGameHost(target)) continue;
            heads.add(ClickableItem.of(getHead(target), e -> handleClick(player, target)));
        }

        pagination.setItems(heads.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage((ROWS - 2) * 7);

        contents.fillBorders(ClickableItem.of(ItemHelper.getGlassFiller(), e -> {}));

        if (!pagination.isFirst()) {
            contents.set(1, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.previous().getPage());
            }));
        }
        if (!pagination.isLast()) {
            contents.set(1, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.next().getPage());
            }));
        }
        contents.set(ROWS - 1, 4, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
            SoundManager.playClickSound(player);
            parent.inv.open(player);
        }));

        if (heads.isEmpty()) {
            contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                    "&cNobody Else Here", "&7There's nobody in this game", "&7to make co-host yet."), e -> {}));
        }

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
        iterator.allowOverride(false);
        pagination.addToIterator(iterator);
    }

    private ItemStack getHead(Player target) {
        boolean isCoHost = game.isCustomGameCoHost(target);
        ItemStack head = ItemHelper.createSkullHeadPlayer(1, target.getName());
        if (isCoHost) head = ItemHelper.setGlowing(head, true);

        return ItemHelper.setDetails(head,
                (isCoHost ? "&a" : "&e") + target.getName(),
                "",
                isCoHost ? "&aCurrent Co-Host" : "&7Player",
                "",
                isCoHost ? "&eClick to remove as co-host" : "&eClick to make co-host");
    }

    private void handleClick(Player host, Player target) {
        if (game.state != GameState.WAITING || game.isCustomGameCountdownStarted() || !game.isCustomGameHost(host)) {
            host.sendMessage(main.color("&c&l(!) &rThe co-host can't be changed anymore."));
            inv.close(host);
            return;
        }
        if (!game.players.contains(target)) { // left since the menu was opened
            SoundManager.playErrorSound(host);
            inv.open(host);
            return;
        }

        SoundManager.playClickSound(host);
        if (game.isCustomGameCoHost(target)) {
            game.setCustomGameCoHost(null);
            game.TellAll(main.color("&2&l(!) &e" + target.getName() + " &ris no longer the co-host."));
        } else {
            game.setCustomGameCoHost(target);
            game.TellAll(main.color("&2&l(!) &e" + target.getName() + " &ris now the co-host and can change the game settings."));
        }

        parent.inv.open(host);
        game.refreshEditorMenus(host);
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
