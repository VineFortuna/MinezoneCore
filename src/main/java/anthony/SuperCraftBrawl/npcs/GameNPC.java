package anthony.SuperCraftBrawl.npcs;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import anthony.SuperCraftBrawl.Core;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.skin.MineSkinFetcher;
import net.md_5.bungee.api.ChatColor;

public class GameNPC implements Listener{
	
	private Core main;
	private NPCLib npcLib;
	
	private NPC gameLobbyNPC;
	
	public GameNPC(Core main, NPC gameLobbyNPC) {
		this.main = main;
		this.npcLib = new NPCLib(main);
		this.gameLobbyNPC = gameLobbyNPC;
		load();
		Bukkit.getPluginManager().registerEvents(this, main);
	}
	
	private void load() {
		int skinId = 277513;
		
		MineSkinFetcher.fetchSkinFromIdAsync(skinId, skin -> {
			gameLobbyNPC = npcLib.createNPC(Arrays.asList(ChatColor.GOLD + "Game Lobbies"));
			gameLobbyNPC.setLocation(new Location(main.getLobbyWorld(), 10.548, 144, 19.533, 0, 0));
			gameLobbyNPC.setSkin(skin);
			gameLobbyNPC.create();
		});
	}
	
	@EventHandler
	public void onNPCInteract(NPCInteractEvent event) {
		Player player = event.getWhoClicked();
		
		if (event.getNPC() == gameLobbyNPC) {
			player.sendMessage("Test!");
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		gameLobbyNPC.show(player);
	}
	
}
