package anthony.SuperCraftBrawl.gui.leaderboard;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.leaderboards.LeaderboardScope;
import anthony.util.ChatColorHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LeaderboardScopeGUI implements InventoryProvider {

    private final Core main;
    private final SmartInventory inv;

    public LeaderboardScopeGUI(Core main) {
        this.main = main;
        this.inv = SmartInventory.builder()
                .id("scb-leaderboard-scope")
                .provider(this)
                .size(3, 9)
                .title(ChatColorHelper.color("&eLeaderboard Scope"))
                .build();
    }

    /** Use to open: new LeaderboardScopeGUI(main).inv().open(player); */
    public SmartInventory inv() {
        return this.inv;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        LeaderboardScope current = main.leaderboardScopeByViewer
                .getOrDefault(player.getUniqueId(), LeaderboardScope.LIFETIME);

        for (LeaderboardScope scope : LeaderboardScope.values()) {
            boolean selected = scope == current;

            ItemStack icon = new ItemStack(selected ? Material.NETHER_STAR : Material.PAPER);
            setName(icon, (selected ? ChatColor.AQUA + "● " : ChatColor.GRAY + "○ ")
                    + ChatColor.YELLOW + scope.display());

            contents.add(ClickableItem.of(icon, e -> {
                // save preference
                main.leaderboardScopeByViewer.put(player.getUniqueId(), scope);
                player.sendMessage(ChatColor.YELLOW + "Showing " + scope.display() + " leaderboards.");

                // close the GUI now (SmartInvs-safe)
                contents.inventory().close(player);

                // Only wiring Kills here since that's what you're testing.
                // (You can replicate the pattern for your other boards later.)
                if (main.getKillsLeaderboard() == null) return;

                if (scope == LeaderboardScope.LIFETIME) {
                    // Lifetime -> clear any per-view lines and redraw global so the viewer sees Lifetime again
                    Bukkit.getScheduler().runTask(main, () -> {
                        try {
                            main.getKillsLeaderboard().clearViewerHologram(player);
                            main.getKillsLeaderboard().updateLeaderboard(false); // redraw global for everyone (simple & safe)
                        } catch (Throwable ignored) {}
                    });
                } else {
                    // Scoped -> refresh cache async, then show scoped view to THIS player
                    Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
                        try { main.getKillsLeaderboard().asyncUpdate(); } catch (Throwable ignored) {}
                        Bukkit.getScheduler().runTask(main, () -> {
                            try { main.getKillsLeaderboard().showToViewer(player, scope); } catch (Throwable ignored) {}
                        });
                    });
                }
            }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) { }

    private void setName(ItemStack item, String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
    }
}