package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticCategory;
import anthony.SuperCraftBrawl.cosmetics.CosmeticManager;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticsGUI implements InventoryProvider {

    private static final CosmeticCategory[] CATEGORIES = {
            CosmeticCategory.GADGET,
            CosmeticCategory.OUTFIT,
            CosmeticCategory.TRAIL,
            CosmeticCategory.PET,
            CosmeticCategory.TITLE,
            CosmeticCategory.HOOK_TRAIL,
            CosmeticCategory.DOUBLE_JUMP,
            CosmeticCategory.DEATH_EFFECT,
            CosmeticCategory.WIN_EFFECT
    };

    public Core main;
    public SmartInventory inv;

    private final Map<UUID, CosmeticCategory> selectedCategory = new HashMap<>();

    public CosmeticsGUI(Core main) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Cosmetics").build();
        this.main = main;
    }

    public CosmeticsGUI(Core main, SmartInventory parent) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Cosmetics").parent(parent).build();
        this.main = main;
    }

    private static int indexOf(CosmeticCategory category) {
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i] == category) return i;
        }
        return -1;
    }

    /** Where a category's cosmetics show up, shown in the lore of its tab. */
    private static String categoryScope(CosmeticCategory category) {
        switch (category) {
            case DOUBLE_JUMP: return "&7Lobby & Game Cosmetic";
            case DEATH_EFFECT:
            case WIN_EFFECT: return "&7Game Cosmetic";
            default: return "&7Lobby Cosmetic";
        }
    }

    private static ItemStack categoryIcon(CosmeticCategory category) {
        switch (category) {
            case GADGET: return new ItemStack(Material.GOLD_BARDING);
            case OUTFIT: return new ItemStack(Material.LEATHER_CHESTPLATE);
            case TRAIL: return new ItemStack(Material.BLAZE_POWDER);
            case PET: return ItemHelper.createMonsterEgg(EntityType.PIG, 1);
            case TITLE: return new ItemStack(Material.NAME_TAG);
            case HOOK_TRAIL: return new ItemStack(Material.FISHING_ROD);
            case DOUBLE_JUMP: return new ItemStack(Material.FEATHER);
            case DEATH_EFFECT: return new ItemStack(Material.REDSTONE);
            case WIN_EFFECT: return new ItemStack(Material.FIREWORK);
            default: return new ItemStack(Material.STAINED_GLASS_PANE);
        }
    }

    /** Some categories fall back to a specific cosmetic rather than nothing when unequipped. */
    private static String defaultCosmeticId(CosmeticCategory category) {
        switch (category) {
            case WIN_EFFECT: return "default";
            default: return null;
        }
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        CosmeticCategory selected = selectedCategory.computeIfAbsent(player.getUniqueId(), id -> CATEGORIES[0]);
        CosmeticManager manager = main.getCosmeticManager();

        int selectedCol = indexOf(selected);

        // divider row under the category tabs, lime under selected category
        for (int col = 0; col < 9; col++) {
            short paneData = col == selectedCol ? (short) 5 : (short) 0;
            contents.set(1, col, ClickableItem
                    .of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, paneData), " "), e -> {
                    }));
        }

        for (int i = 0; i < CATEGORIES.length; i++) {
            CosmeticCategory category = CATEGORIES[i];
            boolean isSelected = category == selected;

            ItemStack tabIcon = ItemHelper.setDetails(categoryIcon(category), ChatColor.YELLOW + category.pluralLabel,
                    categoryScope(category));
            if (isSelected) tabIcon = CosmeticGuiUtil.withStatusLore(tabIcon, "&aCurrently Viewing");

            contents.set(0, i, ClickableItem.of(tabIcon, e -> {
                selectedCategory.put(player.getUniqueId(), category);
                SoundManager.playClickSound(player);
                inv.open(player);
            }));
        }

        int i = 0;
        for (Cosmetic cosmetic : manager.registry().getAll(selected)) {
            int row = 2 + (i / 7);
            if (row > 4) break;
            int col = 1 + (i % 7);

            boolean equipped = cosmetic.id.equals(manager.getEquippedId(player, selected));
            ItemStack icon = cosmetic.getIcon(player);
            if (equipped) icon = CosmeticGuiUtil.withStatusLore(icon, "&aSelected");

            ClickableItem item = selected == CosmeticCategory.WIN_EFFECT
                    ? CosmeticGuiUtil.selectItem(main, manager, cosmetic, player, icon, () -> inv.open(player))
                    : CosmeticGuiUtil.toggleItem(main, manager, cosmetic, player, icon, () -> inv.open(player));
            contents.set(row, col, item);
            i++;
        }

        String defaultId = defaultCosmeticId(selected);
        Cosmetic equippedCosmetic = manager.getEquipped(player, selected);
        if (equippedCosmetic == null && defaultId != null) {
            equippedCosmetic = manager.registry().get(selected, defaultId);
        }
        String equippedLabel = equippedCosmetic != null
                ? equippedCosmetic.getIcon(player).getItemMeta().getDisplayName()
                : ChatColor.GRAY + "Nothing";

        contents.set(5, 4, ClickableItem.of(
                ItemHelper.setDetails(new ItemStack(Material.BARRIER), ChatColor.RED + "Reset",
                        "", "&7Currently selected:", equippedLabel), e -> {
                    if (defaultId != null) {
                        Cosmetic defaultCosmetic = manager.registry().get(selected, defaultId);
                        if (defaultCosmetic != null) manager.equip(player, defaultCosmetic);
                    } else {
                        manager.unequip(player, selected);
                    }
                    player.sendMessage(main.color("&9&l(!) &rYou reset your " + selected.label));
                    SoundManager.playClickSound(player);
                    inv.open(player);
                }));

        if (inv.getParent().isPresent()) {
            contents.set(5, 8, ClickableItem
                    .of(ItemHelper.setDetails(new ItemStack(Material.ARROW), ChatColor.GRAY + "Go Back"), e -> {
                        inv.getParent().get().open(player);
                    }));
        }
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}