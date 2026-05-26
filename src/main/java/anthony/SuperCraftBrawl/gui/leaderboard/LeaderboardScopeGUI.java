package anthony.SuperCraftBrawl.gui.leaderboard;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.leaderboards.LeaderboardScope;
import anthony.util.ChatColorHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class LeaderboardScopeGUI implements InventoryProvider {

    private final Core main;
    private final SmartInventory inv;

    public LeaderboardScopeGUI(Core main) {
        this.main = main;
        this.inv = SmartInventory.builder()
                .id("scb-leaderboard-scope")
                .provider(this)
                .size(3, 9)
                .title(ChatColorHelper.color("&8Leaderboard Scope"))
                .build();
    }

    public SmartInventory inv() {
        return this.inv;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                if (r == 1 && c == 4) {
                    continue;
                }

                contents.set(r, c, ClickableItem.empty(pane));
            }
        }

        setClock(contents, player);
    }

    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void setClock(InventoryContents contents, Player player) {
        LeaderboardScope current = main.leaderboardScopeByViewer.getOrDefault(
                player.getUniqueId(),
                LeaderboardScope.LIFETIME
        );

        ItemStack clock = buildClock(current);

        contents.set(1, 4, ClickableItem.of(clock, (InventoryClickEvent e) -> {
            LeaderboardScope next = nextScope(main.leaderboardScopeByViewer.getOrDefault(
                    player.getUniqueId(),
                    LeaderboardScope.LIFETIME
            ));

            main.leaderboardScopeByViewer.put(player.getUniqueId(), next);

            SoundManager.playClickSound(player);
            player.sendMessage(main.color("&r&l(!) &rShowing &e" + next.display() + " &rleaderboards"));

            setClock(contents, player);
            repaintAllBoardsFor(player, next);
        }));
    }

    private ItemStack buildClock(LeaderboardScope selected) {
        ItemStack it = new ItemStack(Material.WATCH);
        ItemMeta meta = it.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(main.color("&eLeaderboard Scope"));

            String check = ChatColor.GREEN + "✔ ";
            String dot = ChatColor.DARK_GRAY + "• ";

            String l1 = (selected == LeaderboardScope.DAILY ? check : dot) + ChatColor.YELLOW + "Daily";
            String l2 = (selected == LeaderboardScope.WEEKLY ? check : dot) + ChatColor.YELLOW + "Weekly";
            String l3 = (selected == LeaderboardScope.MONTHLY ? check : dot) + ChatColor.YELLOW + "Monthly";
            String l4 = (selected == LeaderboardScope.LIFETIME ? check : dot) + ChatColor.YELLOW + "Lifetime";

            meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to cycle", "", l1, l2, l3, l4));
            it.setItemMeta(meta);
        }

        return it;
    }

    private LeaderboardScope nextScope(LeaderboardScope s) {
        switch (s) {
            case DAILY:
                return LeaderboardScope.WEEKLY;
            case WEEKLY:
                return LeaderboardScope.MONTHLY;
            case MONTHLY:
                return LeaderboardScope.LIFETIME;
            default:
                return LeaderboardScope.DAILY;
        }
    }

    private void repaintAllBoardsFor(Player player, LeaderboardScope scope) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            asyncUpdateAllBoards();

            Bukkit.getScheduler().runTask(main, () -> {
                /*
                 * Important:
                 * First rebuild the global lifetime packet holograms.
                 * updateLeaderboard only sends lifetime holograms to players whose scope is LIFETIME.
                 * Since this player's scope has already changed, Daily/Weekly/Monthly players will not
                 * receive the lifetime stands again.
                 *
                 * Then repaintLeaderboardsFor paints the selected scoped holograms for this player.
                 */
                redrawGlobalBoards();
                main.repaintLeaderboardsFor(player, scope);
            });
        });
    }

    private void asyncUpdateAllBoards() {
        try {
            if (main.getLeaderboard() != null) {
                main.getLeaderboard().asyncUpdate();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getKillsLeaderboard() != null) {
                main.getKillsLeaderboard().asyncUpdate();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getFlawlessWinsBoard() != null) {
                main.getFlawlessWinsBoard().asyncUpdate();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getWinstreakBoard() != null) {
                main.getWinstreakBoard().asyncUpdate();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getFishingLeaderboard() != null) {
                main.getFishingLeaderboard().asyncUpdate();
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getParkourLeaderboards() != null) {
                for (anthony.SuperCraftBrawl.leaderboards.ParkourBoard parkourBoard : main.getParkourLeaderboards()) {
                    if (parkourBoard != null) {
                        parkourBoard.asyncUpdate();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void redrawGlobalBoards() {
        try {
            if (main.getLeaderboard() != null) {
                main.getLeaderboard().updateLeaderboard(false);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getKillsLeaderboard() != null) {
                main.getKillsLeaderboard().updateLeaderboard(false);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getFlawlessWinsBoard() != null) {
                main.getFlawlessWinsBoard().updateLeaderboard(false);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getWinstreakBoard() != null) {
                main.getWinstreakBoard().updateLeaderboard(false);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getFishingLeaderboard() != null) {
                main.getFishingLeaderboard().updateLeaderboard(false);
            }
        } catch (Throwable ignored) {
        }

        try {
            if (main.getParkourLeaderboards() != null) {
                for (anthony.SuperCraftBrawl.leaderboards.ParkourBoard parkourBoard : main.getParkourLeaderboards()) {
                    if (parkourBoard != null) {
                        parkourBoard.updateLeaderboard(false);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}