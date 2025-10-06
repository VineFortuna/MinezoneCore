package anthony.SuperCraftBrawl;

import net.md_5.bungee.api.ChatColor;

public enum Announcements {

	Lightning(color("&2&l(!) &rLook out for lightning as they can spawn powerups!")),
	Discord(color("&2&l(!) &rJoin our Discord by using &e/socials")),
	Maps(color(
			"&2&l(!) &rNot all maps are listed in lobby. Use &e/maps &rfor a list of maps and &e/join <map> &rto play!")),
	Store(color("&2&l(!) &rConsider purchasing a rank at &e&nminezone.club&r for some awesome perks!"));

	public String name;

	Announcements(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}

	public static String color(String c) {
		return ChatColor.translateAlternateColorCodes('&', c);
	}

}
