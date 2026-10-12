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

    /**
     * Creates a copy of an item icon with an additional status line in its lore.
     * Used to show selected categories or equipped cosmetics.
     */
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

    /** Creates the message shown when a cosmetic is equipped or removed. */
    private static String toggleMessage(Cosmetic cosmetic, boolean nowEquipped) {
        return "&9&l(!) &rYou " + (nowEquipped ? "selected" : "removed")
                + " &e" + cosmetic.displayName;
    }

    /**
     * Creates a clickable cosmetic item that toggles between equipped and unequipped.
     * Used by most cosmetic menus.
     */
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

    /**
     * Creates a clickable cosmetic item that equips the cosmetic when clicked.
     * Unlike toggle items, clicking again does not unequip it.
     * Used for WinEffects.
     */
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

    /**
     * Creates a toggle item for menus that update their contents after selection
     * instead of closing.
     */
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

    /**
     * Creates a select item for menus that update their contents after selection
     * instead of closing.
     */
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
