package anthony.util;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.ContainerAnvil;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Asks a player for a line of text with an anvil, which is how it is done on 1.8 (there is no API for it): a name tag
 * with the current text is put in the anvil, the player renames it, and clicking the result gives what they typed.
 * Nothing in the anvil can be taken, and what is in it is cleared when it closes, so nothing ends up in the inventory.
 *
 * Needs to be registered as a listener once. Only for 1.8.8 (v1_8_R3), like the rest of the plugin's server code.
 */
public final class AnvilPrompt implements Listener {

    private static final int INPUT_SLOT = 0;
    private static final int RESULT_SLOT = 2;

    private final Plugin plugin;
    /** The players that have a prompt open. Removed when it closes or they leave. */
    private final Map<UUID, Consumer<String>> open = new HashMap<UUID, Consumer<String>>();

    public AnvilPrompt(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the prompt. {@code onSubmit} is called (a tick after the anvil closes) with the text the player typed,
     * and not at all if they close it without. It can't be asked for the same text again: an unchanged name has no result.
     *
     * @return false if it couldn't be opened (nothing is open then)
     */
    public boolean open(Player player, String text, Consumer<String> onSubmit) {
        try {
            EntityPlayer handle = ((CraftPlayer) player).getHandle();
            ContainerAnvil container = new ContainerAnvil(handle.inventory, handle.world, new BlockPosition(0, 0, 0), handle);
            container.checkReachable = false; // there is no anvil block
            container.windowId = handle.nextContainerCounter();

            ItemStack tag = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = tag.getItemMeta();
            meta.setDisplayName(text);
            tag.setItemMeta(meta);
            container.getBukkitView().getTopInventory().setItem(INPUT_SLOT, tag);

            // Like Player#openInventory: whatever is open is closed first, and plugins can cancel it
            Container opened = CraftEventFactory.callInventoryOpenEvent(handle, container);
            if (opened == null) return false;

            open.put(player.getUniqueId(), onSubmit);
            handle.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, "minecraft:anvil",
                    new ChatMessage("Repairing", new Object[0]), 0));
            handle.activeContainer = container;
            handle.activeContainer.addSlotListener(handle);
            return true;
        } catch (Throwable t) {
            open.remove(player.getUniqueId());
            plugin.getLogger().severe("Couldn't open an anvil prompt: " + t);
            t.printStackTrace();
            return false;
        }
    }

    private boolean isOurs(Player player, Inventory inventory) {
        // A player with a prompt open can only have its anvil open
        return open.containsKey(player.getUniqueId()) && inventory != null && inventory.getType() == InventoryType.ANVIL;
    }

    private static void empty(Inventory anvil) {
        for (int slot = 0; slot <= RESULT_SLOT; slot++) anvil.setItem(slot, null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        final Player player = (Player) event.getWhoClicked();
        if (!isOurs(player, event.getInventory())) return;

        event.setCancelled(true); // nothing is ever taken or moved
        if (event.getRawSlot() != RESULT_SLOT) return;

        Inventory anvil = event.getInventory();
        ItemStack result = anvil.getItem(RESULT_SLOT);
        if (result == null || !result.hasItemMeta() || !result.getItemMeta().hasDisplayName()) return; // nothing typed yet

        final String typed = result.getItemMeta().getDisplayName();
        final Consumer<String> onSubmit = open.remove(player.getUniqueId());
        empty(anvil);
        player.closeInventory();

        // After the anvil is closed, so that what it does next (open a menu, say) isn't undone by it
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) onSubmit.accept(typed);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player && isOurs((Player) event.getWhoClicked(), event.getInventory())) {
            event.setCancelled(true);
        }
    }

    /** Closed without a result (escape): the name tag is cleared so the anvil doesn't hand it to the player. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (!isOurs(player, event.getInventory())) return;

        empty(event.getInventory());
        open.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        open.remove(event.getPlayer().getUniqueId());
    }
}
