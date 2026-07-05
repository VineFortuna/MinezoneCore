package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticManager;
import anthony.util.ChatColorHelper;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Shared click-handling for cosmetics GUIs */
public final class CosmeticGuiUtil {

    private CosmeticGuiUtil() {
    }

    /** Clones the icon and appends a status line as extra lore (blank line, then the given text).
     *  Used to mark a category tab as currently viewed, or a cosmetic as currently equipped -
     *  an enchant glow can't be reused for this since some cosmetics already glow on their own. */
    public static ItemStack withStatusLore(ItemStack icon, String statusLine) {
        ItemStack copy = icon.clone();
        ItemMeta meta = copy.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(ChatColorHelper.color(statusLine));
        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    /** Same message for every category. */
    private static String toggleMessage(Cosmetic cosmetic, boolean nowEquipped) {
        return "&9&l(!) &rYou " + (nowEquipped ? "selected" : "removed")
                + " &e" + cosmetic.displayName;
    }

    /** Click-to-toggle: equip if not equipped, unequip if it is. Used by every cosmetics GUI
     *  except Win Effects */
    public static ClickableItem toggleItem(Core main, CosmeticManager manager, Cosmetic cosmetic, Player player,
                                            ItemStack icon, SmartInventory closeOnSuccess) {
        return ClickableItem.of(icon, e -> {
            if (!cosmetic.isUnlocked(player)) {
                player.sendMessage(cosmetic.getUnlockMessage(player));
                return;
            }
            boolean nowEquipped = manager.toggle(player, cosmetic);
            player.sendMessage(main.color(toggleMessage(cosmetic, nowEquipped)));
            if (closeOnSuccess != null) closeOnSuccess.close(player);
        });
    }

    /** Click-to-select: always (re)selects this cosmetic, no unequip-by-reclick. Used by Win Effects. */
    public static ClickableItem selectItem(Core main, CosmeticManager manager, Cosmetic cosmetic, Player player,
                                            ItemStack icon, SmartInventory closeOnSuccess) {
        return ClickableItem.of(icon, e -> {
            if (!cosmetic.isUnlocked(player)) {
                player.sendMessage(cosmetic.getUnlockMessage(player));
                return;
            }
            manager.equip(player, cosmetic);
            player.sendMessage(main.color(toggleMessage(cosmetic, true)));
            if (closeOnSuccess != null) closeOnSuccess.close(player);
        });
    }

    /** Same as {@link #toggleItem}, but for GUIs (like the unified one) that stay open and just
     *  redraw in place afterward instead of closing. */
    public static ClickableItem toggleItem(Core main, CosmeticManager manager, Cosmetic cosmetic, Player player,
                                            ItemStack icon, Runnable onChange) {
        return ClickableItem.of(icon, e -> {
            if (!cosmetic.isUnlocked(player)) {
                player.sendMessage(cosmetic.getUnlockMessage(player));
                return;
            }
            boolean nowEquipped = manager.toggle(player, cosmetic);
            player.sendMessage(main.color(toggleMessage(cosmetic, nowEquipped)));
            if (onChange != null) onChange.run();
        });
    }

    /** Same as {@link #selectItem}, but for GUIs (like the unified one) that stay open and just
     *  redraw in place afterward instead of closing. */
    public static ClickableItem selectItem(Core main, CosmeticManager manager, Cosmetic cosmetic, Player player,
                                            ItemStack icon, Runnable onChange) {
        return ClickableItem.of(icon, e -> {
            if (!cosmetic.isUnlocked(player)) {
                player.sendMessage(cosmetic.getUnlockMessage(player));
                return;
            }
            manager.equip(player, cosmetic);
            player.sendMessage(main.color(toggleMessage(cosmetic, true)));
            if (onChange != null) onChange.run();
        });
    }
}
