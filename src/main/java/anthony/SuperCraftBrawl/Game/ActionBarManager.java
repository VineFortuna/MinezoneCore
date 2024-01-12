package anthony.SuperCraftBrawl.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.Core;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class ActionBarManager {

	private final Core corePlugin;
	private final Map<UUID, ActionBarData> actionBars = new HashMap<>();

	public ActionBarManager(Core corePlugin) {
		this.corePlugin = corePlugin;
		new BukkitRunnable() {

			@Override
			public void run() {
				tick();
			}
		}.runTaskTimer(corePlugin, 0, 1);
	}

	/**
	 * Sets the action bar of a player.
	 * 
	 * @param p       This is the player who has their action bar set.
	 * @param id      The ID of the action bar. Make sure this is unique.
	 * @param message The actual message.
	 * @param ticks   The number of ticks it will last for.
	 */
	public final void setActionBar(Player p, String id, String message, int ticks) {
		ActionBarData data = actionBars.computeIfAbsent(p.getUniqueId(), t -> new ActionBarData());
		data.setActionBar(id, message, ticks);
	}

	private final void tick() {
		actionBars.entrySet().removeIf(e -> {
			UUID uuid = e.getKey();
			ActionBarData data = e.getValue();

			Player p = corePlugin.getServer().getPlayer(uuid);
			if (p == null)
				return true;

			String message = data.tickAndGetActionBar();
			if (message.isEmpty())
				return false;
			PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + message + "\"}"),
					(byte) 2);
			CraftPlayer craft = (CraftPlayer) p;
			craft.getHandle().playerConnection.sendPacket(packet);
			return false;
		});
	}

	private static class ActionBarData {
		private final Map<String, ActionBarMessage> actionBarMessages = new HashMap<>();

		public String tickAndGetActionBar() {
			if (actionBarMessages.size() == 0)
				return "";
			String msg = actionBarMessages.values().stream().map(m -> m.getMessage())
					.collect(Collectors.joining(ChatColor.DARK_GRAY + " ┃ "));
			actionBarMessages.values().removeIf(ActionBarMessage::shouldRemove);
			return msg;
		}

		public void setActionBar(String id, String message, int ticks) {
			actionBarMessages.put(id, new ActionBarMessage(ticks, message));
		}
	}

	private static class ActionBarMessage {
		int remainingTicks;
		String message;

		public ActionBarMessage(int totalTicks, String message) {
			this.remainingTicks = totalTicks;
			this.message = message;
		}

		public boolean shouldRemove() {
			return --remainingTicks <= 0;
		}

		public String getMessage() {
			return message;
		}
	}

}
