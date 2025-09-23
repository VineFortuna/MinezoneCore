package anthony.SuperCraftBrawl.practice;

import org.bukkit.event.Listener;

import anthony.SuperCraftBrawl.Core;

public class PracticeManager implements Listener {
	
	private Core core;

	public PracticeManager(Core core) {
		this.core = core;
		this.core.getServer().getPluginManager().registerEvents(this, core);
	}
	
}
