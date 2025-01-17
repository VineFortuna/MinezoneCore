package anthony.SuperCraftBrawl.party;

import java.util.Map;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;

public class Party {

	private Core core;
	public Map<Player, PartyManager> party;
	
	public Party(Core core) {
		this.core = core;
	}
	
}
