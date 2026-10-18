package anthony.SuperCraftBrawl.party.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.party.Party;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PartySettingsGUI implements InventoryProvider {

    private final Core main;
    public final SmartInventory inv;
    private final SmartInventory parent;

    public PartySettingsGUI(Core main) {
        this(main, null);
    }

    public PartySettingsGUI(Core main, SmartInventory parent) {
        this.main = main;
        this.parent = parent;

        SmartInventory.Builder builder = SmartInventory.builder()
                .id("partySettings")
                .provider(this)
                .size(3, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Party Settings");

        if (parent != null) {
            builder.parent(parent);
        }

        this.inv = builder.build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem border = ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {});

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                if (row == 0 || row == 2 || col == 0 || col == 8) {
                    contents.set(row, col, border);
                }
            }
        }

        if (main.getPartyManager() == null) {
            contents.set(1, 4, ClickableItem.of(ItemHelper.setDetails(
                    new ItemStack(Material.BARRIER),
                    main.color("&cParty System Unavailable"),
                    main.color("&7The party system is not loaded.")
            ), e -> {}));
            return;
        }

        // Players without a party can still change settings - a party just for them is made on the first change
        Party party = main.getPartyManager().getParty(player);
        boolean isLeader = party == null || party.isLeader(player.getUniqueId());
        PlayerData data = main.getDataManager().getPlayerData(player);
        // No party yet: show the saved choice the next party will start with
        boolean privateGames = party != null ? party.isPrivateGames()
                : data != null && data.customGames && main.canHostPrivateGames(player);
        boolean chatMuted = party != null && party.isPartyChatMuted();

        contents.set(1, 3, ClickableItem.of(ItemHelper.setDetails(
                ItemHelper.setGlowing(new ItemStack(Material.EYE_OF_ENDER), privateGames),
                main.color("&eCustom Games"),
                "",
                main.color("&fStatus: " + getStatus(privateGames)),
                "",
                main.color("&7When enabled, your party games"),
                main.color("&7can play around with game"),
                main.color("&7settings and add cool things"),
                main.color("&7Requires &6&lLEGENDARY &7or higher"),
                "",
                isLeader ? main.color("&eClick to toggle") : main.color("&cOnly the party leader can change this.")
        ), e -> {
            if (!privateGames && !main.canHostPrivateGames(player)) {
                player.sendMessage(main.color("&c&l(!) &rCustom Games need &6&lLEGENDARY &ror higher!"));
                return;
            }

            Party p = getPartyToModify(player);
            if (p == null) return;

            p.setPrivateGames(!p.isPrivateGames());

            // Remember it across restarts
            PlayerData leaderData = main.getDataManager().getPlayerData(player);
            if (leaderData != null) {
                leaderData.customGames = p.isPrivateGames();
                main.getDataManager().saveData(leaderData);
            }

            if (p.isPrivateGames()) {
                player.sendMessage(main.color("&a&l(!) &rPrivate games have been &aenabled&r."));
            } else {
                player.sendMessage(main.color("&a&l(!) &rPrivate games have been &cdisabled&r."));
            }

            inv.open(player);
        }));

        contents.set(1, 5, ClickableItem.of(ItemHelper.setDetails(
                ItemHelper.setGlowing(new ItemStack(Material.NOTE_BLOCK), chatMuted),
                main.color("&eMute Party Chat"),
                "",
                main.color("&fStatus: " + getStatus(chatMuted)),
                "",
                main.color("&7When enabled, party members"),
                main.color("&7cannot send party chat messages."),
                "",
                isLeader ? main.color("&eClick to toggle") : main.color("&cOnly the party leader can change this.")
        ), e -> {
            Party p = getPartyToModify(player);
            if (p == null) return;

            p.setPartyChatMuted(!p.isPartyChatMuted());

            if (p.isPartyChatMuted()) {
                player.sendMessage(main.color("&a&l(!) &rParty chat has been &cmuted&r."));
            } else {
                player.sendMessage(main.color("&a&l(!) &rParty chat has been &aunmuted&r."));
            }

            inv.open(player);
        }));

        contents.set(3, 8, ClickableItem.of(ItemHelper.getGoBackItem(), e -> {
            if (parent != null) {
                parent.open(player);
            } else {
                inv.close(player);
            }
        }));
    }

    /** The party whose settings the player may change (made on the spot if they have none), or null if they're not its leader. */
    private Party getPartyToModify(Player player) {
        Party party = main.getPartyManager().getOrCreateParty(player);
        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage(main.color("&c&l(!) &rOnly the party leader can change party settings."));
            return null;
        }
        return party;
    }

    private String getStatus(boolean enabled) {
        return enabled ? "&aEnabled" : "&cDisabled";
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}