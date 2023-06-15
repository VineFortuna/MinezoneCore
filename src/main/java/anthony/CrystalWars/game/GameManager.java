package anthony.CrystalWars.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import anthony.CrystalWars.game.classes.BaseClass;
import anthony.SuperCraftBrawl.Core;

public class GameManager implements Listener {
	
	private Core main;
	public Map<Maps, GameInstance> gameMap = new HashMap<Maps, GameInstance>();
	
	public GameManager(Core main) {
		this.main = main;
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}
	
	public Core getMain() {
		return this.main;
	}
	
	public GameInstance getInstanceOfPlayer(Player player) {
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet())
			if (entry.getValue().hasPlayer(player))
				return entry.getValue();
		return null;
	}
	
	public boolean removePlayer(Player player) {
		for (Entry<Maps, GameInstance> entry : gameMap.entrySet()) {
			if (entry.getValue().hasPlayer(player)) {
				entry.getValue().removePlayer(player);
				return true;
			}
		}
		
		return false;
	}
	
	public void JoinGame(Player player, Maps map) {
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
	
	public void removeMap(Maps map) {
		this.gameMap.remove(map);
	}
	
	//Death:
	
	/*@EventHandler
	public void onDeath(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			GameInstance i = this.getInstanceOfPlayer(p);
			BaseClass bc = null;
			
			if (i != null)
				bc = i.classes.get(p);
			
			if (!event.isCancelled() && event.getFinalDamage() >= p.getHealth() - 0.2) {
				event.setCancelled(true);
				
				if (bc != null) {
					bc.PlayerDeath(p);
					return;
				}
			}
		}
	}*/

}
