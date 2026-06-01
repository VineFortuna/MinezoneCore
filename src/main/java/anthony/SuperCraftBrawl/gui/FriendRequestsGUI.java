package anthony.SuperCraftBrawl.gui;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.friends.FriendProfile;
import anthony.SuperCraftBrawl.friends.FriendsManager;
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

public class FriendRequestsGUI implements InventoryProvider {

    private final Core main;
    public final SmartInventory inv;

    public FriendRequestsGUI(Core main) {
        this(main, null);
    }

    public FriendRequestsGUI(Core main, SmartInventory parent) {
        this.main = main;

        SmartInventory.Builder builder = SmartInventory.builder()
                .id("friendRequests")
                .provider(this)
                .size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Friend Requests");

        if (parent != null) {
            builder.parent(parent);
        }

        this.inv = builder.build();
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

        List<FriendProfile> requests = main.getFriendsManager().getPendingRequests(player.getUniqueId());

        contents.set(0, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.EMERALD),
                main.color("&a&lFriend Requests"),
                main.color("&fPending: &a" + requests.size())), e -> {}));

        int slot = 0;

        for (FriendProfile request : requests) {
            if (slot >= 21) {
                break;
            }

            int row = 1 + (slot / 7);
            int col = 1 + (slot % 7);
            slot++;

            ItemStack skull = ItemHelper.createSkullHeadPlayer(1, request.getName(),
                    main.color("&e" + request.getName()),
                    Arrays.asList(
                            main.color("&7Wants to be your friend."),
                            "",
                            main.color("&aLeft-click &7to accept"),
                            main.color("&cRight-click &7to reject")
                    ));

            contents.set(row, col, ClickableItem.of(skull, e -> {
                if (e.isRightClick()) {
                    FriendsManager.FriendResult result = main.getFriendsManager()
                            .rejectRequest(player.getUniqueId(), request.getUuid());

                    if (result == FriendsManager.FriendResult.REQUEST_REJECTED) {
                        player.sendMessage(main.color("&c&l(!) &rRejected &e" + request.getName() + "'s &rfriend request."));
                    } else {
                        player.sendMessage(main.color("&c&l(!) &rCould not reject that request."));
                    }

                    new FriendRequestsGUI(main).inv.open(player);
                    return;
                }

                FriendsManager.FriendResult result = main.getFriendsManager()
                        .acceptRequest(player.getUniqueId(), request.getUuid());

                if (result == FriendsManager.FriendResult.REQUEST_ACCEPTED) {
                    player.sendMessage(main.color("&a&l(!) &rYou are now friends with &e" + request.getName() + "&r!"));
                } else {
                    player.sendMessage(main.color("&c&l(!) &rCould not accept that request."));
                }

                new FriendRequestsGUI(main).inv.open(player);
            }));
        }

        if (requests.isEmpty()) {
            contents.set(2, 4, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.BARRIER),
                    main.color("&c&lNo Pending Requests"),
                    main.color("&7You do not have any friend requests right now.")), e -> {}));
        }

        contents.set(4, 7, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.ARROW),
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