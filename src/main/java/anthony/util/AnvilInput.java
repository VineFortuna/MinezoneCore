package anthony.util;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.ContainerAnvil;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/*
 * A text prompt in an anvil window: the player types into the anvil's name field and clicks the result to confirm,
 * or closes the window to cancel. 1.8.8 has no API for it, so the window is opened with packets. Nothing can be
 * taken out of or put into it, and whatever is left in it is cleared when it closes (so no item is handed back).
 * A prompt is forgotten as soon as its window closes or the player quits.
 */
public final class AnvilInput implements Listener {

    private static final int INPUT_SLOT = 0, RESULT_SLOT = 2;

    private static AnvilInput instance; // registered the first time a prompt is opened

    private final Plugin plugin;
    private final Map<UUID, Prompt> prompts = new HashMap<>();

    private static final class Prompt {
        final Inventory anvil;
        final Consumer<String> onSubmit;
        final Runnable onCancel;
        boolean submitted;

        Prompt(Inventory anvil, Consumer<String> onSubmit, Runnable onCancel) {
            this.anvil = anvil;
            this.onSubmit = onSubmit;
            this.onCancel = onCancel;
        }
    }

    private AnvilInput(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the prompt (on the next tick, so it never replaces the window in the middle of the click that asked
     * for it). initialText is what the name field starts with. onSubmit gets the confirmed text (empty if they
     * cleared it) with the window already closed; onCancel runs if they close the window instead.
     */
    public static void open(Plugin plugin, Player player, String initialText, Consumer<String> onSubmit,
                            Runnable onCancel) {
        if (instance == null || instance.plugin != plugin) { // a re-enabled plugin has a new listener
            instance = new AnvilInput(plugin);
            Bukkit.getPluginManager().registerEvents(instance, plugin);
        }

        AnvilInput input = instance;
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) input.show(player, initialText, onSubmit, onCancel);
            }
        });
    }

    private void show(Player player, String initialText, Consumer<String> onSubmit, Runnable onCancel) {
        player.closeInventory(); // e.g. the menu this was opened from (also forgets any earlier prompt)

        EntityPlayer nms = ((CraftPlayer) player).getHandle();
        ContainerAnvil container = new ContainerAnvil(nms.inventory, nms.world, new BlockPosition(0, 0, 0), nms);
        container.checkReachable = false; // it isn't an anvil in the world

        int windowId = nms.nextContainerCounter();
        nms.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, "minecraft:anvil",
                new ChatMessage("Repairing"), 0));
        nms.activeContainer = container;
        nms.activeContainer.windowId = windowId;
        nms.activeContainer.addSlotListener(nms);

        Inventory anvil = container.getBukkitView().getTopInventory();
        prompts.put(player.getUniqueId(), new Prompt(anvil, onSubmit, onCancel));

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(initialText);
        paper.setItemMeta(meta);
        anvil.setItem(INPUT_SLOT, paper);
    }

    private Prompt getPrompt(Player player, Inventory open) {
        Prompt prompt = prompts.get(player.getUniqueId());
        return prompt != null && prompt.anvil == open ? prompt : null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Prompt prompt = getPrompt(player, event.getView().getTopInventory());
        if (prompt == null) return;

        event.setCancelled(true);
        if (prompt.submitted || event.getRawSlot() != RESULT_SLOT) return;

        ItemStack result = event.getCurrentItem(); // the paper, renamed to what they typed
        if (result == null || result.getType() == Material.AIR) return; // nothing changed yet

        ItemMeta meta = result.getItemMeta();
        String text = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        prompt.submitted = true;

        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.closeInventory();
                prompt.onSubmit.accept(text);
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (getPrompt((Player) event.getWhoClicked(), event.getView().getTopInventory()) != null)
            event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Prompt prompt = getPrompt((Player) event.getPlayer(), event.getInventory());
        if (prompt == null) return;

        prompts.remove(event.getPlayer().getUniqueId());
        prompt.anvil.clear();
        if (!prompt.submitted) prompt.onCancel.run();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Prompt prompt = prompts.remove(event.getPlayer().getUniqueId());
        if (prompt != null) prompt.anvil.clear();
    }
}
