package anthony.SuperCraftBrawl.gui.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LobbyCosmeticsGUI implements InventoryProvider {

    public Core main;
    public SmartInventory inv;

    public LobbyCosmeticsGUI(Core main) {
        inv = SmartInventory.builder()
                .id("myInventory")
                .provider(this)
                .size(5, 9)
                .title("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Lobby Cosmetics")
                .build();
        this.main = main;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        PlayerData data = main.getDataManager().getPlayerData(player);

        // Icon Items
        ItemStack particles = ItemHelper.create(Material.NETHER_STAR, ChatColor.YELLOW + "Particle effects");

        ItemStack gadgets = ItemHelper.create(Material.GOLD_BARDING, ChatColor.YELLOW + "Gadgets");

        ItemStack pets = ItemHelper.createMonsterEgg(EntityType.WOLF, 1,ChatColor.YELLOW + "Pets");

        ItemStack lobbyOutfits = ItemHelper.create(Material.GOLD_HELMET, ChatColor.YELLOW + "Outfits");

        ItemStack morphs = ItemHelper.createSkullHead(1, SkullType.ZOMBIE, ChatColor.YELLOW + "Morphs");


        // Setting Icons
        contents.set(1, 2, ClickableItem.of(
                particles,
                e -> {
                    inv.close(player);
                    new ParticlesEffectsGUI(main).inv.open(player);
                }));

        contents.set(1, 4, ClickableItem.of(
                gadgets,
                e -> {
                    inv.close(player);
                    new GadgetsGUI(main).inv.open(player);
                }));

        contents.set(1, 6, ClickableItem.of(
                pets,
                e -> {
                    inv.close(player);
                    new PetsGUI(main).inv.open(player);
                }));

        contents.set(3, 3, ClickableItem.of(
                lobbyOutfits,
                e -> {
                    inv.close(player);
                    new LobbyOutfitsGUI(main).inv.open(player);
                }));

        contents.set(3, 5, ClickableItem.of(
                morphs,
                e -> {
                    inv.close(player);
                    new MorphsGUI(main).inv.open(player);
                }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
