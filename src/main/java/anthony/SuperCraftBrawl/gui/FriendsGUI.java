package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.friends.FriendProfile;
import anthony.util.ItemHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class FriendsGUI implements InventoryProvider {

    private final Core main;
    public final SmartInventory inv;

    public FriendsGUI(Core main) {
        this(main, null);
    }

    public FriendsGUI(Core main, SmartInventory parent) {
        this.main = main;

        SmartInventory.Builder builder = SmartInventory.builder()
                .id("friends")
                .provider(this)
                .size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Friends");

        if (parent != null) {
            builder.parent(parent);
        }

        this.inv = builder.build();
    }

    private String checkIfInGame(Player onlineFriend) {
        String msg = main.color("&e&nLeft-Click&e to ");
        GameInstance game = main.getGameManager().GetInstanceOfPlayer(onlineFriend);

        if (game != null) {
            if (game.state == GameState.WAITING)
                msg += "Join";
            else if (game.state == GameState.STARTED)
                msg += "Spectate";
        }

        game = main.getGameManager().GetInstanceOfSpectator(onlineFriend);

        if (game != null) {
            if (game.state == GameState.STARTED)
                msg += "Spectate";
        }

        if (onlineFriend.getWorld() == main.getLobbyWorld()) {
            msg += "Teleport";
        }

        return msg;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem border = ClickableItem.of(ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7), " "), e -> {});

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                if (row == 0 || row == 4 || col == 0 || col == 8) {
                    contents.set(row, col, border);
                }
            }
        }

        List<FriendProfile> friends = main.getFriendsManager().getFriends(player.getUniqueId());

        //Sorting friends online first in GUI
        friends.sort((first, second) -> {
            if (first.isOnline() && !second.isOnline()) {
                return -1;
            }

            if (!first.isOnline() && second.isOnline()) {
                return 1;
            }

            return first.getName().compareToIgnoreCase(second.getName());
        });

        int onlineCount = 0;

        for (FriendProfile friend : friends) {
            if (friend.isOnline()) {
                onlineCount++;
            }
        }

        contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.SKULL_ITEM, 1, (short) 3),
                main.color("&a&lYour Friends"),
                main.color("&fOnline: &a" + onlineCount),
                main.color("&fTotal: &a" + friends.size())), e -> {}));

        int slot = 0;

        for (FriendProfile friend : friends) {
            if (slot >= 21) {
                break;
            }

            int row = 1 + (slot / 7);
            int col = 1 + (slot % 7);
            slot++;

            String status = friend.isOnline() ? "&aOnline" : "&7Offline";
            Player onlineFriend = org.bukkit.Bukkit.getPlayer(friend.getUuid());

            List<String> lore = new java.util.ArrayList<>();

            lore.add(main.color("&fStatus: " + status));

            //This below checks if friend is in a game to display to player
            if (friend.isOnline()) {
                GameInstance game = main.getGameManager().GetInstanceOfPlayer(onlineFriend);

                if (game != null) {
                    if (game.state == GameState.STARTED)
                        lore.add(main.color("&rPlaying: &a" + game.getMap().getName()));
                    else if (game.state == GameState.WAITING)
                        lore.add(main.color("&rWaiting: &a" + game.getMap().getName()));
                }

                game = main.getGameManager().GetInstanceOfSpectator(onlineFriend);

                if (game != null) {
                    lore.add(main.color("&rSpectating: &a" + game.getMap().getName()));
                }

                if (onlineFriend.getWorld() == main.getLobbyWorld()) {
                    lore.add(main.color("&rIn Lobby"));
                }
            }

            lore.add("");

            if (friend.isOnline()) {
                lore.add(checkIfInGame(onlineFriend)); //Checks where player is if in lobby or a game
            }

            lore.add(main.color("&e&nRight-click&e to remove friend"));

            ItemStack skull = ItemHelper.createSkullHeadPlayer(1, friend.getName(),
                    main.color((friend.isOnline() ? "&a" : "&7") + friend.getName()),
                    lore);

            contents.set(row, col, ClickableItem.of(skull, e -> {
                if (e.isRightClick()) {
                    main.getFriendsManager().removeFriend(player.getUniqueId(), friend.getUuid());
                    player.sendMessage(main.color("&a&l(!) &rRemoved &e" + friend.getName() + " &rfrom your friends list."));

                    if (contents.inventory().getParent().isPresent()) {
                        new FriendsGUI(main, contents.inventory().getParent().get()).inv.open(player);
                    } else {
                        new FriendsGUI(main).inv.open(player);
                    }

                    return;
                }

                if (onlineFriend == null) {
                    player.sendMessage(main.color("&c&l(!) &rThat friend is currently offline."));
                    return;
                }

                player.closeInventory();

                GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

                if (onlineFriend.getWorld() == main.getLobbyWorld()) {
                    if (game != null) {
                        player.sendMessage(main.color("&c&l(!) &rYou cannot teleport to friends while in game!"));
                        return;
                    }
                    if (main.getParkour().hasPlayer(onlineFriend)) {
                        player.sendMessage(main.color("&c&l(!) &rYou cannot teleport to &a" + onlineFriend.getName() +
                                " &rwhile in parkour!"));
                        return;
                    }
                    if (main.getParkour().hasPlayer(player)) {
                        player.sendMessage(main.color("&c&l(!) &rYou cannot teleport while in parkour!"));
                        return;
                    }
                    player.teleport(onlineFriend.getLocation());
                }

                game = main.getGameManager().GetInstanceOfPlayer(onlineFriend);

                if (game != null) {
                    if (game.state == GameState.STARTED)
                        main.getGameManager().SpectatorJoinMap(player, game.getMap());
                    else if (game.state == GameState.WAITING)
                        if (!game.isLobbyFull(player))
                            main.getGameManager().JoinMap(player, game.getMap());

                    return;
                }

                game = main.getGameManager().GetInstanceOfSpectator(onlineFriend);

                if (game != null) {
                    if (game.state == GameState.STARTED) {
                        main.getGameManager().SpectatorJoinMap(player, game.getMap());
                    }
                }
            }));
        }

        if (friends.isEmpty()) {
            contents.set(2, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                    main.color("&c&lNo Friends Yet"),
                    main.color("&7Add someone with:"),
                    main.color("&e/friends add <player>")), e -> {}));
        }

        int pending = main.getFriendsManager().getPendingRequestCount(player.getUniqueId());

        contents.set(4, 2, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EMERALD),
                main.color("&a&lFriend Requests"),
                main.color("&fPending: &a" + pending),
                "",
                main.color("&eClick to view requests")), e -> {
            new FriendRequestsGUI(main, contents.inventory()).inv.open(player);
        }));

        contents.set(4, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.PAPER),
                main.color("&e&lHow To Add Friends"),
                main.color("&7Use these commands:"),
                main.color("&a/friends add <player>"),
                main.color("&a/friends accept <player>"),
                main.color("&a/friends reject <player>"),
                main.color("&a/friends remove <player>")), e -> {}));

        contents.set(4, 6, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.REDSTONE_COMPARATOR),
                main.color("&b&lRefresh"),
                main.color("&7Click to refresh your friends list.")), e -> {
            if (contents.inventory().getParent().isPresent()) {
                new FriendsGUI(main, contents.inventory().getParent().get()).inv.open(player);
            } else {
                new FriendsGUI(main).inv.open(player);
            }
        }));

        contents.set(4, 8, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.ARROW),
                main.color("&7Go Back")), e -> {
            if (contents.inventory().getParent().isPresent()) {
                contents.inventory().getParent().get().open(player);
            } else {
                player.closeInventory();
            }
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }
}