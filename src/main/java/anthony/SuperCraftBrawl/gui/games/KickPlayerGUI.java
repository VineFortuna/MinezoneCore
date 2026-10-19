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
 * Game Settings > Kick Player: the host or co-host picks a player to remove from the custom game, who then can't
 * join it again. The host can't be kicked. Laid out like the Co-Host menu.
 */
public class KickPlayerGUI implements InventoryProvider {

    private static final int ROWS = 4;

    private final Core main;
    private final GameInstance game;
    private final GameModifiersGUI parent;
    public final SmartInventory inv;

    public KickPlayerGUI(Core main, GameInstance game, GameModifiersGUI parent) {
        this.main = main;
        this.game = game;
        this.parent = parent;

        this.inv = SmartInventory.builder()
                .id("kickPlayer")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Kick a Player")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        List<ClickableItem> heads = new ArrayList<>();
        for (Player target : game.players) {
            if (target == player || game.isCustomGameHost(target)) continue;
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
                    "&cNobody to Kick", "&7There's nobody else in this", "&7game you can kick."), e -> {}));
        }

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
        iterator.allowOverride(false);
        pagination.addToIterator(iterator);
    }

    private ItemStack getHead(Player target) {
        boolean isCoHost = game.isCustomGameCoHost(target);
        return ItemHelper.setDetails(ItemHelper.createSkullHeadPlayer(1, target.getName()),
                "&e" + target.getName(),
                "",
                isCoHost ? "&aCo-Host" : "&7Player",
                "",
                "&cClick to kick");
    }

    private void handleClick(Player kicker, Player target) {
        if (game.state != GameState.WAITING || game.isCustomGameCountdownStarted() || !game.canEditCustomGame(kicker)) {
            kicker.sendMessage(main.color("&c&l(!) &rPlayers can't be kicked anymore."));
            inv.close(kicker);
            return;
        }
        if (!game.players.contains(target)) { // left since the menu was opened
            SoundManager.playErrorSound(kicker);
            inv.open(kicker);
            return;
        }

        SoundManager.playClickSound(kicker);
        game.kickFromCustomGame(kicker, target);

        inv.open(kicker); // pick another, or Go Back
        game.refreshEditorMenus(kicker);
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
