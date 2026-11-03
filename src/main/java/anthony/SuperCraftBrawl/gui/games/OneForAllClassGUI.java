package anthony.SuperCraftBrawl.gui.games;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameSettings.OneForAllMode;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import fr.minuskube.inv.content.SlotPos;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/*
 * One For All's class selector (the enchanted book once the custom game is started): every enabled class, which the
 * host / co-host click to choose the class everyone plays (Host/Co-Host), or everyone clicks to vote for (Voting).
 * Each class's stack size is its votes. Laid out like the Class List: page arrows on the sides, Random at the bottom.
 */
public class OneForAllClassGUI implements InventoryProvider {

    static final int ROWS = 6;
    static final String RANDOM_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCJ9fX0=";
    static final String RANDOM_NAME = GameInstance.ONE_FOR_ALL_RANDOM;

    private final Core main;
    private final GameInstance game;
    public final SmartInventory inv;

    public OneForAllClassGUI(Core main, GameInstance game) {
        this.main = main;
        this.game = game;

        this.inv = SmartInventory.builder()
                .id("oneForAllClasses")
                .provider(this)
                .size(ROWS, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Class Selector")
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        Map<ClassType, Integer> votes = game.getOneForAllVoteCounts();

        List<ClassType> classes = new ArrayList<>(Arrays.asList(game.getSelectableClasses()));
        classes.sort(Comparator.comparing(ClassType::toString));

        ClickableItem[] items = new ClickableItem[classes.size()];
        for (int i = 0; i < classes.size(); i++) {
            ClassType type = classes.get(i);
            Integer count = votes.get(type);
            items[i] = getOptionItem(game, player, type, count == null ? 0 : count);
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(28);

        contents.fillBorders(ClickableItem.of(ItemHelper.getGlassFiller(), e -> {}));
        contents.set(0, 4, ClickableItem.of(getInfoItem(player), e -> {}));

        Integer randomVotes = votes.get(null);
        contents.set(ROWS - 1, 4, getOptionItem(game, player, null, randomVotes == null ? 0 : randomVotes));

        // Voting: a shortcut to the Voting Paper's menu in the bottom corner
        if (game.getGameSettings().oneForAllMode == OneForAllMode.VOTING) {
            contents.set(ROWS - 1, 8, ClickableItem.of(ItemHelper.setDetails(new ItemStack(Material.PAPER),
                    "&e&lClass Votes",
                    "&7See the classes with votes",
                    "",
                    "&eClick to open"), e -> {
                SoundManager.playClickSound(player);
                new OneForAllVotesGUI(main, game).inv.open(player);
            }));
        }

        // Previous / Next Page
        if (!pagination.isFirst()) {
            contents.set(2, 0, ClickableItem.of(ItemHelper.getPreviousPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.previous().getPage());
            }));
        }
        if (!pagination.isLast()) {
            contents.set(2, 8, ClickableItem.of(ItemHelper.getNextPageItem(), e -> {
                SoundManager.playClickSound(player);
                inv.open(player, pagination.next().getPage());
            }));
        }

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, SlotPos.of(1, 1));
        iterator.allowOverride(false);
        pagination.addToIterator(iterator);
    }

    private ItemStack getInfoItem(Player viewer) {
        if (game.getGameSettings().oneForAllMode == OneForAllMode.HOST) {
            ClassType chosen = game.getOneForAllClass();
            return ItemHelper.setDetails(new ItemStack(Material.ARMOR_STAND),
                    "&e&lOne For All",
                    "&7Everyone plays the class chosen",
                    "&7by the host or co-host",
                    "",
                    "&fCurrent: " + (chosen == null ? RANDOM_NAME : chosen.getTag()),
                    "",
                    "&7Do &e/class random &7to go",
                    "&7back to " + RANDOM_NAME);
        }

        String vote = !game.hasOneForAllVote(viewer) ? "&7None"
                : game.getOneForAllVote(viewer) == null ? RANDOM_NAME : game.getOneForAllVote(viewer).getTag();
        return ItemHelper.setDetails(new ItemStack(Material.ARMOR_STAND),
                "&e&lOne For All",
                "&7Everyone plays the class",
                "&7with the most votes",
                "",
                "&fYour Vote: " + vote);
    }

    /*
     * A class (or Random, when type is null) to choose or vote for. Shared with the Voting Paper's menu: the stack
     * size is its votes, and it glows when it's the viewer's vote (or the host's pick).
     */
    static ClickableItem getOptionItem(GameInstance game, Player viewer, ClassType type, int votes) {
        ItemStack item = type == null ? ItemHelper.createSkullTexture(RANDOM_TEXTURE) : type.getItem();
        if (item == null) item = new ItemStack(Material.WOOD);
        item = ItemHelper.setHideFlags(item, true);

        boolean voting = game.getGameSettings().oneForAllMode == OneForAllMode.VOTING;
        boolean picked = voting ? game.hasOneForAllVote(viewer) && game.getOneForAllVote(viewer) == type
                : game.getOneForAllClass() == type;

        List<String> lore = new ArrayList<>();
        if (type != null) {
            List<String> description = type.buildDescription();
            if (description != null) lore.addAll(description);
        } else {
            lore.add("&7A random enabled class");
        }
        lore.add("");
        if (voting) {
            lore.add("&fVotes: &a" + votes);
            lore.add("");
            lore.add(picked ? "&aYou voted for this class" : "&eClick to vote");
            if (picked) lore.add("&eClick to &cremove &eyour vote");
        } else if (picked) {
            lore.add("&aEveryone is playing this class");
        } else {
            lore.add(game.canEditCustomGame(viewer) ? "&eClick to choose for everyone"
                    : "&cOnly the host and co-host can choose");
        }

        item = ItemHelper.setDetails(item, type == null ? RANDOM_NAME : type.getTag(), lore.toArray(new String[0]));
        if (picked) item = ItemHelper.setGlowing(item, true);
        if (voting) item.setAmount(Math.max(1, Math.min(64, votes)));

        // The pick refreshes everyone's open One For All menus, this one included
        return ClickableItem.of(item, e -> game.chooseOneForAllClass(viewer, type, true));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
