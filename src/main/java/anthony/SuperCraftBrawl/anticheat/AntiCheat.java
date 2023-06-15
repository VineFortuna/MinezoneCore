package anthony.SuperCraftBrawl.anticheat;

/*import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.Main;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

public class AntiCheat implements Listener {

	private Main main;
	private BukkitRunnable flyDetect;
	private int ticks = 0;
	private int speedTicks = 0;
	private BukkitRunnable noSlowDetect;
	private List<Player> checkPlayers;
	private HashMap<Player, BukkitRunnable> flyRunnable = new HashMap<>();

	public AntiCheat(Main main) {
		this.main = main;
		this.checkPlayers = new ArrayList<Player>();
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	public Main getMain() {
		return main;
	}

	public void TellAll(String message) {
		for (Player onlinePlayers : Bukkit.getOnlinePlayers()) {
			onlinePlayers.sendMessage(message);
		}
	}

	public void warnMac(Player player, String message) {
		checkPlayers.add(player);
	}

	@EventHandler
	public void FlyDetect(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PlayerData data = main.getDataManager().getPlayerData(player);
		int ping = ((CraftPlayer) player).getHandle().ping;

		// Gamemode check
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
			return;
		// Potion effect check
		if (player.hasPotionEffect(PotionEffectType.JUMP) && !(player.isOnGround()))
			return;

		if (data.magicbroom == 0) {
			if (event.getTo().getY() - event.getFrom().getY() >= 1.5) {
				if (ping <= 60) {
					//if (checkPlayers.contains(player)) {
						BukkitRunnable runnable = flyRunnable.get(player);
						if (runnable == null) {
							runnable = new BukkitRunnable() {
								int ticks = 0;
								int count = 0;

								@Override
								public void run() {
									if (player.isOnGround()) {
										flyRunnable.remove(player);
										this.cancel();
									}
									if (count == 4) {
										//int newPing = ((CraftPlayer) player).getHandle().ping;
										//if (newPing <= 60) {
											TellAll(main.color("&9MAC: &e" + player.getName()
													+ " &7was removed due to suspicious activity"));
											warnMac(player, "" + player.getName()
													+ " was kicked due to Suspicious Activity. No longer spectating player");
											player.kickPlayer("You were kicked due to possible Fly Hacks");
											flyRunnable.remove(player);
											this.cancel();
										//} else {
											// FIX LATER TO CHECK PING
										//}
									}

									if (ticks % 3 == 0) {
										for (Player staff : Bukkit.getOnlinePlayers()) {
											if (staff.hasPermission("scb.anticheat")) {
												staff.sendMessage(main.color("&9MAC: &e" + player.getName()
														+ " &7detected of &cFly Hacking (&e" + ping
														+ "ms&c)&7. Please investigate"));
											}
										}
										count++;
									}
									ticks++;
								}
							};
							runnable.runTaskTimer(main, 0, 20);
							flyRunnable.put(player, runnable);
						}
					} else {
						warnMac(player, "" + player.getName() + " suspected of possibly cheating. Now spectating..");
					}
				//}
			}
		}
	}

	@EventHandler
	public void ReachDetect(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Entity player = event.getEntity();
			Player damager = (Player) event.getDamager();
			int ping = ((CraftPlayer) damager).getHandle().ping;

			if (ping <= 50) {
				if (damager.getLocation().distanceSquared(player.getLocation()) > 5.00) {
					for (Player staff : Bukkit.getOnlinePlayers()) {
						if (staff.hasPermission("scb.anticheat")) {
							staff.sendMessage(main.color("&9MAC: &e" + damager.getName() + " &7detected of &cReach (&e"
									+ ping + "ms&c)&7. Please investigate"));
						}
					}
				}
			} else {
				if (damager.getLocation().distanceSquared(player.getLocation()) > 5.00) {
					for (Player staff : Bukkit.getOnlinePlayers()) {
						if (staff.hasPermission("scb.anticheat")) {
							staff.sendMessage(main.color(
									"&9MAC: &e" + damager.getName() + " &7detected of &cReach&7. Please investigate"));
							staff.sendMessage(main.color(
									"&9MAC: &e" + damager.getName() + "&7's ping is &e" + ping + "ms &c&l(HIGH)"));
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void NoSlowDetect(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.BOW && event.getAction() == Action.RIGHT_CLICK_AIR) {
			if (player.getWalkSpeed() < 0.2F) {
				return;
			} else {
				if (noSlowDetect == null) {
					noSlowDetect = new BukkitRunnable() {
						int ticks = 0;

						@Override
						public void run() {
							if (ticks == 1) {
								if (player.getWalkSpeed() >= 0.2F) {
									for (Player staff : Bukkit.getOnlinePlayers()) {
										if (staff.hasPermission("scb.anticheat")) {
											staff.sendMessage(main.color("&9MAC: &e" + player.getName()
													+ " &7detected of &cNo Slowdown&7. Please investigate"));
										}
									}
								}
								noSlowDetect = null;
								this.cancel();
							}

							ticks++;
						}

					};
					noSlowDetect.runTaskTimer(main, 0, 20);
				}
			}
		}
	}

	@EventHandler
	public void Broom(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PlayerData data = main.getDataManager().getPlayerData(player);
		if (data.magicbroom == 1) {
			if (player.isOnGround()) {
				data.magicbroom = 0;
			}
		}
	}
}*/
