package anthony.SuperCraftBrawl;

import net.md_5.bungee.api.ChatColor;

public enum Announcements {

	Website(color("&2&l(!) &rVisit &e&nwww.minezone.club&r for more info, statistics and our store!")),
	
	Discord(color("&2&l(!) &rJoin our Discord by using &e/socials")),
	
	Store(color(
			"&2&l(!) &rConsider purchasing a rank at &e&nminezone.club/store&r to support the server & for some awesome perks!"));

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
