package anthony.SuperCraftBrawl.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.util.ChatColorHelper;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.Core;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import javax.annotation.Nullable;

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

	public static class AbilityActionBar {
		private final BaseClass baseClass;
		private final ActionBarManager actionBarManager;

		public AbilityActionBar(BaseClass baseClass, ActionBarManager actionBarManager) {
			this.baseClass = baseClass;
			this.actionBarManager = actionBarManager;
		}

		public void setActionBarAbility(Player player, Ability ability1, Ability ability2) {
			String name = ability1.getAbilityName();

			if (ability2 != null) name += ability2.getAbilityName();

			actionBarManager.setActionBar(player, name + baseClass, createAbilityMessage(ability1, ability2), 2);
		}

		public String createAbilityMessage(Ability ability1, @Nullable Ability ability2) {
			String message1 = constructAbilityMessage(ability1);

			if (ability2 == null) return message1;

			String message2 = constructAbilityMessage(ability2);
			return message1 + org.bukkit.ChatColor.DARK_GRAY + " ┃ " + message2;
		}

		String constructAbilityMessage(Ability ability) {
			long remainingTime = ability.getCooldownInstance().getRemainingCooldownSeconds();

			if (!ability.isReady()) {
				return ChatColorHelper.color(ability.getAbilityName() + " &rregenerates in &e" + (remainingTime + 1) + "s");
			} else {
				return ChatColorHelper.color("&rYou can use " + ability.getAbilityName());
			}
		}
	}

}
