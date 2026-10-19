package anthony.flagwars;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

/**
 * Invisibility Potion: vanilla invisibility still shows armor and (on 1.8) the name tag, so while a player has it
 * everyone else is sent empty armor slots for them and their name tag is hidden. The armor is re-sent every tick,
 * since the server sends their real armor again whenever it changes or someone comes into range. Once the effect
 * is gone (ran out, died, left) or an enemy hits them (see reveal), their armor and name tag come back.
 */
public class FWInvisibilityManager {

	private final FWGameManager fwGameManager;
	private final Set<UUID> hidden = new HashSet<>();

	public FWInvisibilityManager(FWGameManager fwGameManager) {
		this.fwGameManager = fwGameManager;
		Bukkit.getScheduler().runTaskTimer(fwGameManager.getMain(), this::tick, 1L, 1L);
	}

	/** The player was just splashed with an Invisibility Potion. */
	public void track(Player player) {
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;
		hidden.add(player.getUniqueId());
		instance.getScoreboardManager().hideNameTag(player);
	}

	/** An enemy hit them: the potion ends right away and their armor and name tag show again. */
	public void reveal(Player player) {
		if (!hidden.remove(player.getUniqueId())) return;
		player.removePotionEffect(PotionEffectType.INVISIBILITY);
		show(player);
	}

	private void tick() {
		if (hidden.isEmpty()) return;
		Iterator<UUID> it = hidden.iterator();
		while (it.hasNext()) {
			Player player = Bukkit.getPlayer(it.next());
			if (player == null) {
				it.remove();
				continue;
			}
			if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)
					|| fwGameManager.getInstanceOfPlayer(player) == null) {
				it.remove();
				show(player);
				continue;
			}
			sendArmor(player, true);
		}
	}

	/** Their real armor, and their team's name tag if they're still in the game. */
	private void show(Player player) {
		sendArmor(player, false);
		FWGameInstance instance = fwGameManager.getInstanceOfPlayer(player);
		if (instance == null) return;
		FWTeam team = instance.findTeamOf(player);
		if (team != null) instance.getScoreboardManager().applyNameTag(player, team);
	}

	/** Sends everyone else in the player's world their armor slots: empty, or what they're really wearing. */
	private void sendArmor(Player player, boolean empty) {
		ItemStack[] armor = player.getInventory().getArmorContents(); // boots, leggings, chestplate, helmet
		int entityId = player.getEntityId();
		PacketPlayOutEntityEquipment[] packets = new PacketPlayOutEntityEquipment[4];
		for (int i = 0; i < 4; i++) {
			packets[i] = new PacketPlayOutEntityEquipment(entityId, i + 1,
					empty ? null : CraftItemStack.asNMSCopy(armor[i]));
		}
		for (Player viewer : player.getWorld().getPlayers()) {
			if (viewer == player) continue;
			for (PacketPlayOutEntityEquipment packet : packets) {
				((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}
}
