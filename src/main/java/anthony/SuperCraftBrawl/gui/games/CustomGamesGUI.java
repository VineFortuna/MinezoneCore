package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.SmartInvsPlugin;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Custom Games NPC: every active custom game (public ones as emerald blocks - anyone can join; private ones as
 * redstone blocks - only the host's party can join), and Create Custom Game, which takes you (and your party)
 * straight to a new custom game on the default map.
 */
public class CustomGamesGUI implements InventoryProvider {

    private static final int ROWS = 4;

    private final Core main;
    public final SmartInventory inv;

    public CustomGamesGUI(Core main) {
        this.main = main;

        this.inv = SmartInventory.builder()
                .id("customGames")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Custom Games")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();

        List<ClickableItem> items = new ArrayList<>();
        for (GameInstance game : main.getGameManager().games) {
            if (!game.isCustomGame() || game.getMap() == null) continue;
            if (game.state != GameState.WAITING && game.state != GameState.STARTED) continue;
            items.add(ClickableItem.of(getGameItem(game), e -> handleGameClick(player, game)));
        }

        pagination.setItems(items.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(27);

        for (int column = 0; column < 9; column++) {
            contents.set(ROWS - 1, column, ClickableItem.of(ItemHelper.getGlassFiller(), e -> {}));
        }

        if (!pagination.isFirst()) {
            contents.set(ROWS - 1, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.previous().getPage());
            }));
        }
        if (!pagination.isLast()) {
            contents.set(ROWS - 1, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.next().getPage());
            }));
        }

        contents.set(ROWS - 1, 4, ClickableItem.of(getCreateItem(player), e -> handleCreateClick(player)));

        if (items.isEmpty()) {
            contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                    "&cNo Custom Games", "&7Nobody is running a custom game", "&7right now. Create one below!"), e -> {}));
        }

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(0, 0));
        iterator.allowOverride(false);
        pagination.addToIterator(iterator);
    }

    // ===== Items =====

    private ItemStack getGameItem(GameInstance game) {
        boolean isPublic = game.isCustomGamePublic();
        String host = getHostName(game);
        boolean playing = game.state == GameState.STARTED;

        List<String> lore = new ArrayList<>();
        lore.add(isPublic ? "&7Public - anyone can join" : "&7Private - only " + host + "'s party can join");
        lore.add("");
        lore.add("&fPlayers: &e" + game.players.size());
        lore.add("&fStatus: " + (playing ? "&ePlaying" : "&aLobby"));
        lore.add("&fMap: &e" + game.getMap().toString());
        lore.add("&fMode: &e" + game.getClassSelectionName());
        lore.add("");
        lore.add(playing ? "&eClick to spectate!" : "&eClick to join!");

        ItemStack item = new ItemStack(isPublic ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK);
        return ItemHelper.setDetails(item, (isPublic ? "&a&l" : "&c&l") + host + "'s Custom Game",
                lore.toArray(new String[0]));
    }

    private ItemStack getCreateItem(Player player) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Create a private/public game");
        lore.add("&7with custom modes and rules");
        lore.add("");
        if (main.canHostCustomGames(player)) {
            lore.add("&eClick to create!");
        } else {
            lore.add("&cYou need &6&lLEGENDARY &cto");
            lore.add("&ccreate a custom game!");
        }
        return ItemHelper.setDetails(new ItemStack(Material.WORKBENCH), "&aCreate", lore.toArray(new String[0]));
    }

    // ===== Clicks =====

    private void handleGameClick(Player player, GameInstance game) {
        if (!main.getGameManager().games.contains(game)) { // ended since the menu was opened
            SoundManager.playErrorSound(player);
            inv.open(player);
            return;
        }
        if (!game.canAccessCustomGame(player)) {
            SoundManager.playErrorSound(player);
            player.sendMessage(main.color("&c&l(!) &rThat custom game is private - only &e" + getHostName(game)
                    + "&r's party can join it."));
            return;
        }

        SoundManager.playClickSound(player);
        inv.close(player);
        if (game.state == GameState.STARTED) main.getGameManager().SpectatorJoinGame(player, game);
        else main.getGameManager().JoinGame(player, game);
    }

    private void handleCreateClick(Player player) {
        if (!main.canHostCustomGames(player)) {
            SoundManager.playErrorSound(player);
            player.sendMessage(main.color("&c&l(!) &rCustom Games need &6&lLEGENDARY&r!"));
            return;
        }

        SoundManager.playClickSound(player);
        inv.close(player);
        main.getGameManager().createCustomGame(player);
    }

    /** Reopens every player's open Custom Games menu, so it shows the latest custom games (e.g. one went public). */
    public static void refreshOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Optional<SmartInventory> open = SmartInvsPlugin.manager().getInventory(player);
            if (!open.isPresent() || !(open.get().getProvider() instanceof CustomGamesGUI)) continue;

            int page = SmartInvsPlugin.manager().getContents(player)
                    .map(contents -> contents.pagination().getPage()).orElse(0);
            open.get().open(player, page);
        }
    }

    private static String getHostName(GameInstance game) {
        if (game.getCustomGameHost() == null) return "Unknown";
        OfflinePlayer host = Bukkit.getOfflinePlayer(game.getCustomGameHost());
        return host.getName() != null ? host.getName() : "Unknown";
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
