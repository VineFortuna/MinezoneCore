package anthony.SuperCraftBrawl.halloween;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import java.lang.reflect.Field;
import java.util.*;

public class HalloweenHuntManager implements Listener {

	public static final String EVENT_KEY = "HALLOWEEN2025";
	public static final int TOTAL = 10;
	private static final int FULL_MASK = (1 << TOTAL) - 1;

	private final Core core;
	private final HalloweenDAO dao;

	// ===== EDIT THESE =====
	private static final String WORLD = "lobby-1";
	private static final List<int[]> BASKETS = Arrays.asList(new int[] { 197, 105, 689 },
			new int[] { 126,111, 702 }, 
			new int[] { 205, 105, 574 },
			new int[] { 229, 86, 569 },
			new int[] { 115, 114, 643 }, 
			new int[] { 72, 116, 929 },
			new int[] { 8, 112, 841 }, 
			new int[] { 282, 112, 660 }, 
			new int[] { 295, 94, 532 },
			new int[] { 253, 143, 626 }
	);

	public HalloweenHuntManager(Core core) {
		this.core = core;
		this.dao = new HalloweenDAO(core, EVENT_KEY);
		Bukkit.getPluginManager().registerEvents(this, core);
	}

	private int indexOfBasket(Block block) {
		if (block == null)
			return -1;
		if (!block.getWorld().getName().equalsIgnoreCase(WORLD))
			return -1;
		int bx = block.getX(), by = block.getY(), bz = block.getZ();
		for (int i = 0; i < BASKETS.size(); i++) {
			int[] a = BASKETS.get(i);
			if (a[0] == bx && a[1] == by && a[2] == bz)
				return i;
		}
		return -1;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		int idx = indexOfBasket(e.getClickedBlock());
		if (idx < 0)
			return; // not a basket
		e.setCancelled(true);

		Player p = e.getPlayer();
		UUID id = p.getUniqueId();

		if (dao.hasFound(id, idx)) {
			p.sendMessage(ChatColor.GRAY + "🎃 You already found this basket.");
			p.playEffect(e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5), Effect.SMOKE, 1);
			return;
		}

		int newMask = dao.markFound(id, idx, TOTAL);
		int found = Integer.bitCount(newMask);

		p.playSound(p.getLocation(), Sound.LEVEL_UP, 1f, 1.2f);
		p.playEffect(e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5), Effect.HAPPY_VILLAGER, 1);
		p.sendMessage(
				ChatColor.GOLD + "🍬 Basket found! (" + ChatColor.GREEN + found + ChatColor.GOLD + "/" + TOTAL + ")");

		//Gives players the rewards for finding baskets
		grantMilestoneReward(p, found);

		if (newMask == FULL_MASK)
			reward(p);

		//Updates lobby scoreboard with new Basket count
		refreshLobbyBoard(p);
	}

	/*
	 * This function gives token rewards based on the amount that is called
	 */
	private void giveTokensReward(Player p, int amount) {
		PlayerData data = core.getDataManager().getPlayerData(p);

		if (data != null) {
			data.tokens += amount;
			p.sendMessage(core.color("&6&l(!) &rYou were given &e25 tokens!"));
		}
	}

	/*
	 * This function gives EXP rewards based on the amount that is called
	 */
	private void giveExpReward(Player p, int amount) {
		PlayerData data = core.getDataManager().getPlayerData(p);

		if (data != null) {
			data.exp += amount;
			p.sendMessage(core.color("&6&l(!) &rYou were given &e500 EXP!"));
			core.getListener().checkIfLevelUp(p);
		}
	}

	/**
	 * Called after each successful find. Use this to grant per-milestone prizes.
	 * For now it just sends a themed message; swap with your prize logic later.
	 */
	private void grantMilestoneReward(Player p, int found) {
		switch (found) {
		case 1:
			p.sendMessage(core.color("&6&l(!) &6🎃 &rYou found &e1&r/10 baskets!"));
			giveTokensReward(p, 25); // Gives 25 tokens for getting 1/10 baskets
			break;
		case 2:
			p.sendMessage(core.color("&6&l(!) &6🎃 &rYou found &e2&r/10 baskets!"));
			p.sendMessage(core
					.color("&6&l(!) &rYou unlocked the &6&lTrick-or-Treat &rtitle cosmetic! Open 'Cosmetics' to view"));
			break;
		case 3:
			p.sendMessage(core.color("&6&l(!) &6🎃 &rYou found &e3&r/10 baskets!"));
			p.sendMessage(core.color("&6&l(!) &rYou unlocked the &6Ritual &rwin effect! Open 'Cosmetics' to view"));
			break;
		case 4:
			p.sendMessage(core.color("&6&l(!) &6🎃 Milestone 4/10! &rYou unlocked: &9&lCandy Aura &rcosmetic!"));
			break;
		case 5:
			p.sendMessage(core.color("&6&l(!) &rYou found &e5&r/10 baskets!"));
			giveExpReward(p, 500); // Gives 500 EXP reward for getting 5/10 baskets
			break;
		case 6:
			p.sendMessage(core.color("&6&l(!) &rYou found &e6&r/10 baskets!"));
			p.sendMessage(
					core.color("&6&l(!) &rYou unlocked the &6&lFreddy &routfit cosmetic! Open 'Cosmetics' to view"));
			break;
		case 7:
			p.sendMessage(core.color("&6&l(!) &rYou found &e7&r/10 baskets!"));
			p.sendMessage(
					core.color("&6&l(!) &rYou unlocked the &6Pumpkin Pie &rdeath particles! Open 'Cosmetics' to view"));
			break;
		case 8:
			p.sendMessage(ChatColor.DARK_AQUA + "▶ " + ChatColor.AQUA + "Milestone 8/10! (sample prize here)");
			break;
		case 9:
			p.sendMessage(ChatColor.DARK_AQUA + "▶ " + ChatColor.AQUA + "Milestone 9/10! (sample prize here)");
			break;
		case 10:
			p.sendMessage(ChatColor.GREEN + "✔ " + ChatColor.GOLD + "Milestone 10/10! (final prize here)");
			break;
		default:

		}
	}

	private void reward(Player p) {
		p.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.DIAMOND, 5));
		Bukkit.broadcastMessage(
				core.color("&6&l[HALLOWEEN HUNT] &e" + p.getName() + "&6 has found all 10 Baskets! Happy Halloween!"));
	}

	/** For scoreboard: number found from DB. */
	public int getFoundCount(UUID uuid) {
		return Integer.bitCount(dao.getProgress(uuid));
	}

	/** Expose DAO for admin cmd. */
	public HalloweenDAO getDao() {
		return dao;
	}

	/** Rebuild your lobby board. Replace with your actual call if needed. */
	public void refreshLobbyBoard(Player p) {
		try {
			core.getScoreboardManager().lobbyBoard(p); // uses your existing method
		} catch (Throwable ignored) {
		}
	}

	public void placeHeadsOnEnable(final String base64Texture) {
		Bukkit.getScheduler().runTask(core, () -> {
			World w = Bukkit.getWorld(WORLD);
			if (w == null) {
				core.getLogger().severe("[Halloween] World not found: " + WORLD);
				return;
			}
			for (int[] c : BASKETS) {
				Location loc = new Location(w, c[0], c[1], c[2]);
				placeCustomHeadBlock(loc, base64Texture);
			}
			core.getLogger().info("[Halloween] Placed " + BASKETS.size() + " basket heads in " + WORLD + ".");
		});
	}

	private static void placeCustomHeadBlock(Location loc, String base64Texture) {
		Block b = loc.getBlock();
		b.setType(Material.SKULL);
		b.setData((byte) 1, false);

		Skull skull = (Skull) b.getState();
		try {
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", base64Texture));
			Field f = skull.getClass().getDeclaredField("profile");
			f.setAccessible(true);
			f.set(skull, profile);
		} catch (Exception ignored) {
		}
		skull.update(true, false);
	}
}