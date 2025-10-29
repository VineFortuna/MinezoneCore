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

    /** Use to open: new LeaderboardScopeGUI(main).inv().open(player); */
    public SmartInventory inv() { return this.inv; }

    @Override
    public void init(Player player, InventoryContents contents) {
        // Nice frame
        ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) {
            if (r == 1 && c == 4) continue;
            contents.set(r, c, ClickableItem.empty(pane));
        }
        // place the clock
        setClock(contents, player);
    }

    @Override
    public void update(Player player, InventoryContents contents) { }

    private void setClock(InventoryContents contents, Player player) {
        LeaderboardScope current = main.leaderboardScopeByViewer
                .getOrDefault(player.getUniqueId(), LeaderboardScope.LIFETIME);

        ItemStack clock = buildClock(current);
        contents.set(1, 4, ClickableItem.of(clock, (InventoryClickEvent e) -> {
            LeaderboardScope next = nextScope(
                    main.leaderboardScopeByViewer.getOrDefault(player.getUniqueId(), LeaderboardScope.LIFETIME));

            // Save choice
            main.leaderboardScopeByViewer.put(player.getUniqueId(), next);
            SoundManager.playClickSound(player);
            player.sendMessage(main.color("&eShowing " + next.display() + " leaderboards"));
            setClock(contents, player);
            repaintFor(player, next);
        }));
    }

    /** Build a WATCH with checkmarks in lore. */
    private ItemStack buildClock(LeaderboardScope selected) {
        ItemStack it = new ItemStack(Material.WATCH);
        ItemMeta meta = it.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(main.color("&eLeaderboard Scope"));
            String check = ChatColor.GREEN + "✔ ";
            String dot   = ChatColor.DARK_GRAY + "• ";

            String l1 = (selected == LeaderboardScope.DAILY   ? check : dot) + ChatColor.YELLOW + "Daily";
            String l2 = (selected == LeaderboardScope.WEEKLY  ? check : dot) + ChatColor.YELLOW + "Weekly";
            String l3 = (selected == LeaderboardScope.MONTHLY ? check : dot) + ChatColor.YELLOW + "Monthly";
            String l4 = (selected == LeaderboardScope.LIFETIME? check : dot) + ChatColor.YELLOW + "Lifetime";

            meta.setLore(Arrays.asList(ChatColor.GRAY + "Click to cycle", "", l1, l2, l3, l4));
            it.setItemMeta(meta);
        }
        return it;
    }

    /** Cycle order: DAILY → WEEKLY → MONTHLY → LIFETIME → DAILY */
    private LeaderboardScope nextScope(LeaderboardScope s) {
        switch (s) {
            case DAILY:   return LeaderboardScope.WEEKLY;
            case WEEKLY:  return LeaderboardScope.MONTHLY;
            case MONTHLY: return LeaderboardScope.LIFETIME;
            default:      return LeaderboardScope.DAILY;
        }
    }

    /** Repaint THIS viewer’s leaderboards. Uses reflection so it compiles even if some boards aren’t converted yet. */
    private void repaintFor(Player player, LeaderboardScope scope) {
        Object kills = main.getKillsLeaderboard();
        Object wins  = main.getLeaderboard();       // your Wins board
        Object flaw  = main.getFlawlessWinsBoard(); // may or may not have paintFor/clearViewerHologram yet

        if (scope == LeaderboardScope.LIFETIME) {
            Bukkit.getScheduler().runTask(main, () -> {
                // clear per-viewer overlays (if those methods exist)
                invokeIfExists(kills, "clearViewerHologram", new Class[]{Player.class}, new Object[]{player});
                invokeIfExists(wins,  "clearViewerHologram", new Class[]{Player.class}, new Object[]{player});
                invokeIfExists(flaw,  "clearViewerHologram", new Class[]{Player.class}, new Object[]{player});

                // redraw global (only shows to Lifetime viewers)
                invokeIfExists(kills, "updateLeaderboard", new Class[]{boolean.class}, new Object[]{false});
                invokeIfExists(wins,  "updateLeaderboard", new Class[]{boolean.class}, new Object[]{false});
                invokeIfExists(flaw,  "updateLeaderboard", new Class[]{boolean.class}, new Object[]{false});
            });
            return;
        }

        // Scoped: refresh caches async, then paint per-viewer on main thread
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            invokeIfExists(kills, "asyncUpdate", new Class[]{}, new Object[]{});
            invokeIfExists(wins,  "asyncUpdate", new Class[]{}, new Object[]{});
            invokeIfExists(flaw,  "asyncUpdate", new Class[]{}, new Object[]{});

            Bukkit.getScheduler().runTask(main, () -> {
                invokeIfExists(kills, "paintFor",
                        new Class[]{Player.class, LeaderboardScope.class}, new Object[]{player, scope});
                invokeIfExists(wins,  "paintFor",
                        new Class[]{Player.class, LeaderboardScope.class}, new Object[]{player, scope});
                invokeIfExists(flaw,  "paintFor",
                        new Class[]{Player.class, LeaderboardScope.class}, new Object[]{player, scope});
            });
        });
    }

    /* Small reflection helper: call a method if it exists; ignore if not. */
    private void invokeIfExists(Object target, String name, Class<?>[] sig, Object[] args) {
        if (target == null) return;
        try {
            java.lang.reflect.Method m = target.getClass().getMethod(name, sig);
            m.setAccessible(true);
            m.invoke(target, args);
        } catch (Throwable ignored) { }
    }
}