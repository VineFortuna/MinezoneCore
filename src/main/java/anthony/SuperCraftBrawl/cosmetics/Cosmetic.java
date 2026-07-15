package anthony.SuperCraftBrawl.cosmetics;

import anthony.util.ChatColorHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * One equippable cosmetic. Concrete cosmetics are declared as small anonymous subclasses right
 * where they're registered.
 */
public abstract class Cosmetic {

    public final String id;
    public final CosmeticCategory category;
    public final String displayName;

    /** e.g. "PRO Exclusive", "Fishing Reward", "Halloween 2025 Exclusive"; null if always unlocked. */
    private final String requirement;

    protected Cosmetic(String id, CosmeticCategory category, String displayName) {
        this(id, category, displayName, null);
    }

    protected Cosmetic(String id, CosmeticCategory category, String displayName, String requirement) {
        this.id = id;
        this.category = category;
        this.displayName = displayName;
        this.requirement = requirement;
    }

    /** Built per-player/per-open since some icons show live progress (e.g. basket counts). */
    public abstract ItemStack getIcon(Player player);

    public abstract boolean isUnlocked(Player player);

    /** Colorized message sent when the player clicks this while locked. */
    public abstract String getUnlockMessage(Player player);

    /** Apply the runtime effect (set armor, spawn pet, start rendering, etc). */
    public abstract void onEquip(Player player);

    /** Remove the runtime effect. Also called on quit for cleanup, and when another cosmetic
     *  in the same category is equipped in its place. */
    public abstract void onUnequip(Player player);

    /** Override only for cosmetics that need a recurring visual (trails, the snowman's follow
     *  behavior). Returning 0 (the default) means this cosmetic never ticks. */
    public long getTickPeriod() {
        return 0;
    }

    public void tick(Player player) {
    }

    /** Appends the standard requirement-badge + how-to-obtain lore lines to an icon. Call this
     *  last in getIcon() after building the rest of the lore. Does nothing if this cosmetic has
     *  no requirement (always unlocked). */
    protected final ItemStack withRequirementLore(ItemStack icon) {
        if (requirement == null) return icon;

        ItemMeta meta = icon.getItemMeta();
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(ChatColorHelper.color("&c" + requirement));
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }
}