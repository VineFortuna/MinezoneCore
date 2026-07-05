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

public class UnifiedCosmeticsGUI implements InventoryProvider {

    private static final CosmeticCategory[] CATEGORIES = {
            CosmeticCategory.GADGET,
            CosmeticCategory.OUTFIT,
            CosmeticCategory.TRAIL,
            CosmeticCategory.PET,
            CosmeticCategory.TITLE,
            CosmeticCategory.HOOK_TRAIL,
            CosmeticCategory.DEATH_EFFECT,
            CosmeticCategory.WIN_EFFECT
    };

    /** Categories before this index are lobby cosmetics; the rest are game cosmetics. A divider pane sits between them. */
    private static final int LOBBY_CATEGORY_COUNT = 6;

    public Core main;
    public SmartInventory inv;

    private final Map<UUID, CosmeticCategory> selectedCategory = new HashMap<>();

    public UnifiedCosmeticsGUI(Core main) {
        inv = SmartInventory.builder().id("myInventory").provider(this).size(6, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Cosmetics").build();
        this.main = main;
    }

    public UnifiedCosmeticsGUI(Core main, SmartInventory parent) {
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

    /** Maps a category index to its tab column, leaving a gap at LOBBY_CATEGORY_COUNT for the divider pane. */
    private static int tabColumn(int index) {
        return index < LOBBY_CATEGORY_COUNT ? index : index + 1;
    }

    private static ItemStack categoryIcon(CosmeticCategory category) {
        switch (category) {
            case GADGET: return new ItemStack(Material.GOLD_BARDING);
            case OUTFIT: return new ItemStack(Material.LEATHER_CHESTPLATE);
            case TRAIL: return new ItemStack(Material.BLAZE_POWDER);
            case PET: return ItemHelper.createMonsterEgg(EntityType.PIG, 1);
            case TITLE: return new ItemStack(Material.NAME_TAG);
            case HOOK_TRAIL: return new ItemStack(Material.FISHING_ROD);
            case DEATH_EFFECT: return new ItemStack(Material.REDSTONE);
            case WIN_EFFECT: return new ItemStack(Material.FIREWORK);
            default: return new ItemStack(Material.STAINED_GLASS_PANE);
        }
    }

    /** Some categories fall back to a specific cosmetic rather than nothing when unequipped. */
    private static String defaultCosmeticId(CosmeticCategory category) {
        switch (category) {
            case WIN_EFFECT: return "default";
            case DEATH_EFFECT: return "redstone";
            default: return null;
        }
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        CosmeticCategory selected = selectedCategory.computeIfAbsent(player.getUniqueId(), id -> CATEGORIES[0]);
        CosmeticManager manager = main.getCosmeticManager();

        int selectedCol = tabColumn(indexOf(selected));

        // divider row under the category tabs, lime under selected category
        for (int col = 0; col < 9; col++) {
            short paneData = col == selectedCol ? (short) 5 : (short) 0;
            contents.set(1, col, ClickableItem
                    .of(ItemHelper.setDetails(new ItemStack(Material.STAINED_GLASS_PANE, 1, paneData), " "), e -> {
                    }));
        }

        for (int i = 0; i < CATEGORIES.length; i++) {
            CosmeticCategory category = CATEGORIES[i];
            int col = tabColumn(i);
            boolean isSelected = category == selected;

            ItemStack tabIcon = ItemHelper.setDetails(categoryIcon(category), ChatColor.YELLOW + category.pluralLabel);
            if (isSelected) tabIcon = CosmeticGuiUtil.withStatusLore(tabIcon, "&aCurrently Viewing");

            contents.set(0, col, ClickableItem.of(tabIcon, e -> {
                selectedCategory.put(player.getUniqueId(), category);
                SoundManager.playClickSound(player);
                inv.open(player);
            }));
        }

        ItemStack divider = ItemHelper.setDetails(
                new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1),
                ChatColor.GOLD + "" + ChatColor.BOLD + "Categories",
                "&6« &eLobby Cosmetics", "&eGame Cosmetics &6»");
        contents.set(0, LOBBY_CATEGORY_COUNT, ClickableItem.of(divider, e -> {
        }));

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
                    player.sendMessage(main.color("&9&l(!) &rYou reset your " + selected.pluralLabel));
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