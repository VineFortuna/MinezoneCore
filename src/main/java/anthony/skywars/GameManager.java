package anthony.skywars;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import anthony.SuperCraftBrawl.Core;
import anthony.skywars.gui.KitSelectorGUI;

public class GameManager implements Listener {

	private Core main;
	public Map<Maps, GameInstance> gameMap = new HashMap<Maps, GameInstance>();

	public GameManager(Core main) {
		this.main = main;
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	// Getters:

	public Core getMain() {
		return this.main;
	}

	public GameInstance getInstanceOfPlayer(Player player) { // Gets the game of a player that they're in
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet())
			if (entry.getValue().hasPlayer(player))
				return entry.getValue();
		return null;
	}

	public GameInstance getInstanceOfSpectator(Player player) { // Gets the game of a player that they're in
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet())
			if (entry.getValue().hasSpectator(player))
				return entry.getValue();
		return null;
	}

	public boolean removePlayer(Player player) { // Removes player from game
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().hasPlayer(player)) {
				entry.getValue().removePlayer(player);
				return true;
			}
		}

		return false;
	}

	public void JoinGame(Player player, Maps map) { // Adds player to game
		GameInstance i = null;

		if (this.getInstanceOfPlayer(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are already in a game!"));
		} else {
			if (gameMap.get(map) != null) {
				i = gameMap.get(map);
			} else {
				i = new GameInstance(this, map);
				gameMap.put(map, i);
			}

			i.addPlayer(player);
		}
	}

	public void SpectateMap(Player player, Maps map) {
		GameInstance i = null;

		if (this.getInstanceOfSpectator(player) != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are already in a game!"));
		} else {
			if (gameMap.get(map) != null) {
				i = gameMap.get(map);
				i.addSpectator(player);
			} else {
				player.sendMessage(main.color("&c&l(!) &rThis game is not playing!"));
			}
		}
	}

	public void removeMap(Maps map) { // Removes map from active games if game over or no players left in lobby
		this.gameMap.remove(map);
	}

	public void onTestEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GameInstance i = this.getInstanceOfPlayer(player);
			if (i != null) {
				if (i.getState() == anthony.skywars.GameState.STARTED)
					event.setCancelled(false);
				else
					event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityDamage2(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GameInstance i = this.getInstanceOfPlayer(player);

			if (i != null) {
				if (i.getState() == anthony.skywars.GameState.STARTED)
					event.setCancelled(false);
				else
					event.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			GameInstance i = this.getInstanceOfPlayer(player);
			GameInstance i2 = null; // For the player killer if there is one

			if (player.getKiller() != null)
				i2 = this.getInstanceOfPlayer(player);

			if (i != null && i.getState() == anthony.skywars.GameState.STARTED) {
				player.setHealth(20.0);
				player.setGameMode(GameMode.SPECTATOR);
				player.sendTitle(this.getMain().color("&cYou died"), this.getMain().color("&rYou are now a Spectator"));
				i.players.remove(player); // Remove player from game then add as spectator next line
				i.spectators.add(player);
				for (ItemStack item : player.getInventory().getContents()) {
					if (item != null && item.getType() != Material.AIR) {
						// Drop the item as an entity at the player's location
						Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), item);
						droppedItem.setPickupDelay(20); // Set a delay before the item can be picked up (in ticks)
					}
				}

				String tag = this.getMain().getRankManager().getRank(player).getTagWithSpace();
				if (player.getKiller() != null) {
					player.teleport(player.getKiller());
					String kTag = this.getMain().getRankManager().getRank(player.getKiller()).getTagWithSpace();
					i.TellAll(this.getMain().color("&2&l(!) " + tag + "&r" + player.getName() + " &cwas killed by "
							+ kTag + "&r" + player.getKiller().getName()));

					if (i2 != null) {
						i2.playersStats.get(player.getKiller()).kills += 1;
						i2.boards.get(player.getKiller()).updateLine(5,
								" " + i2.playersStats.get(player.getKiller()).getKills());
					}
				} else {
					i.TellAll(
							this.getMain().color("&2&l(!) " + tag + "&r" + player.getName() + " &cjust died SO badly"));
				}
				i.checkForWin(); // Checking if game is over
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player player = e.getPlayer();
		GameInstance instance = this.getInstanceOfPlayer(player);

		if (instance != null) {
			if (instance.getState() == GameState.STARTED) {
				if (player.getLocation().getY() < 5 && player.getGameMode() != GameMode.SPECTATOR) {
					EntityDamageEvent damageEvent = new EntityDamageEvent(e.getPlayer(), DamageCause.VOID, 1000);
					main.getServer().getPluginManager().callEvent(damageEvent);
					PlayerDeathEvent de = new PlayerDeathEvent(player, null, 0, null);
					this.onDeath(de);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();

			GameInstance instance = this.getInstanceOfPlayer(player);
			if (instance != null) {
				if (!event.isCancelled() && event.getFinalDamage() >= player.getHealth() - 0.2) {
					event.setCancelled(true);
					if (event instanceof EntityDamageByEntityEvent) {
						EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
						if (damageEvent.getDamager() instanceof Player) {
							Player damager = (Player) damageEvent.getDamager();
							player.setLastDamageCause(new EntityDamageByEntityEvent(damager, player, event.getCause(),
									event.getDamage()));
						}
					}
					PlayerDeathEvent de = new PlayerDeathEvent(player, null, 0, null);
					this.onDeath(de);
					return;
				}
			}
		}

	}

	// Player interacts:

	@EventHandler
	public void interact(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Player player = event.getPlayer();
		GameInstance i = this.getInstanceOfPlayer(player);

		if (i != null) {
			if (item != null
					&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				ItemMeta meta = item.getItemMeta();

				switch (item.getType()) {
				case COMPASS:
					if (i.getState() == anthony.skywars.GameState.LOBBY) {
						new KitSelectorGUI(main).inv.open(player);
					}
					break;
				}
			}
		}
	}
}
