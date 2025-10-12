package anthony.SuperCraftBrawl.commands;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameLootDrops;
import anthony.SuperCraftBrawl.Game.GameSettings;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.GameType;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.map.Maps;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import anthony.SuperCraftBrawl.gui.ActiveGamesGUI;
import anthony.SuperCraftBrawl.gui.GameSelectorGUI;
import anthony.SuperCraftBrawl.gui.GameStatsGUI;
import anthony.SuperCraftBrawl.gui.cosmetics.CosmeticsGUI;
import anthony.SuperCraftBrawl.gui.fishing.FishingGUI;
import anthony.SuperCraftBrawl.playerdata.ClassDetails;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.practice.Game;
import anthony.SuperCraftBrawl.practice.SCBPractice;
import anthony.SuperCraftBrawl.ranks.Rank;
import anthony.util.SoundManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.lang.WordUtils;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Score;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class Commands implements CommandExecutor, TabCompleter {

	private final Core main;
	public List<Player> players;
	private final java.util.Set<java.util.UUID> candyAuraEnabled = new java.util.HashSet<>();
	private int candyAuraTaskId = -1;

	// how often to render (ticks). 5 = 4x/sec
	private static final long CANDY_AURA_PERIOD_TICKS = 5L;

	public Commands(Core main) {
		this.main = main;
	}

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			switch (cmd.getName().toLowerCase()) {
			case "purchases":
				purchaseCommand(args, player);
				break;

			case "fishing":
				fishingCommand(args, player);
				break;

			case "cosmetics":
				cosmeticsCommand(args, player);
				break;

			case "startgame":
				startGameCommand(player);
				break;

			case "frenzy":
				setFrenzyCommand(player);
				break;

			case "server":
				serverCommand(player);
				break;

			case "fly":
				flyCommand(player);
				break;

			case "items":
				itemsCommand(args, player);
				break;

			case "setlives":
				setLivesCommand(args, player);
				break;

			case "gamestats":
				gameStatsCommand(args, player);
				break;

			case "socials":
				socialsCommand(player);
				break;

			case "maps":
				mapsCommand(args, player);
				break;

			case "join":
				joinCommand(args, player);
				break;

			case "spectate":
				spectateCommand(args, player);
				break;

			case "class":
				classCommand(args, player);
				break;

			case "leave":
				this.leaveGame(player);
				break;

			case "players":
				playersCommand(player);
				break;

			case "duel":
				duelCommand(args, player);
				break;

			case "party":
				partyCommand(args, player);
				break;

			case "color":
				colorCommand(args, player);
				break;

			case "lactate":
				lactateCommand(player);
				break;

			case "sound":
				soundCommand(args, player);
				break;

			case "soundnms":
				soundNMSCommand(args, player);
				break;

			case "heal":
				healCommand(player, args);
				break;

			case "forceclass":
				forceClassCommand(player, args);
				break;

			case "practice":
				new SCBPractice(player, Game.BowPractice, main);
				break;

			case "candyaura":
				candyAuraCommand(args, player);
				break;
			}
		} else
			sender.sendMessage("Hey! You can't use this in the terminal!");
		return true;
	}

	private void candyAuraCommand(String[] args, Player player) {
		// Optional permission – remove this block if you want everyone to use it
		if (!player.hasPermission("cosmetic.candyaura")) {
			player.sendMessage(main.color("&c&l(!) &rYou don't have access to &dCandy Aura&r."));
			return;
		}

		String mode = (args.length == 0) ? "toggle" : args[0].toLowerCase();
		switch (mode) {
		case "on":
			candyAuraEnabled.add(player.getUniqueId());
			player.sendMessage(main.color("&dCandy Aura &aenabled&d. Sweet!"));
			ensureCandyAuraTaskRunning();
			break;
		case "off":
			candyAuraEnabled.remove(player.getUniqueId());
			player.sendMessage(main.color("&dCandy Aura &cdisabled&d."));
			break;
		case "toggle":
		default:
			if (candyAuraEnabled.contains(player.getUniqueId())) {
				candyAuraEnabled.remove(player.getUniqueId());
				player.sendMessage(main.color("&dCandy Aura &cdisabled&d."));
			} else {
				candyAuraEnabled.add(player.getUniqueId());
				player.sendMessage(main.color("&dCandy Aura &aenabled&d. Sweet!"));
				ensureCandyAuraTaskRunning();
			}
			break;
		}
	}

	private void ensureCandyAuraTaskRunning() {
		if (candyAuraTaskId != -1)
			return; // already running
		candyAuraTaskId = Bukkit.getScheduler().runTaskTimer(main, new Runnable() {
			@Override
			public void run() {
				if (candyAuraEnabled.isEmpty())
					return;
				for (java.util.UUID id : new java.util.HashSet<>(candyAuraEnabled)) {
					Player p = Bukkit.getPlayer(id);
					if (p == null || !p.isOnline())
						continue;
					renderCandyAura(p);
				}
			}
		}, CANDY_AURA_PERIOD_TICKS, CANDY_AURA_PERIOD_TICKS).getTaskId();
	}

	/** Draw a small “candy” swirl around the player (1.8-safe Effects). */
	private void renderCandyAura(Player p) {
		Location base = p.getLocation().add(0, 0.1, 0);

		// light sparkles around feet
		p.getWorld().playEffect(base, Effect.HAPPY_VILLAGER, 0, 16);
		p.getWorld().playEffect(base, Effect.CRIT, 0, 16);

		// purple-ish magic near waist
		Location waist = base.clone().add(0, 0.7, 0);
		p.getWorld().playEffect(waist, Effect.WITCH_MAGIC, 0, 16);

		// tiny swirl ring
		final double r = 0.45;
		long t = System.currentTimeMillis();
		for (int i = 0; i < 6; i++) {
			double a = (t / 120.0 + i * Math.PI / 3.0);
			double x = Math.cos(a) * r;
			double z = Math.sin(a) * r;
			Location ring = waist.clone().add(x, 0.1, z);
			p.getWorld().playEffect(ring, Effect.HAPPY_VILLAGER, 0, 8);
		}
	}

	private void serverCommand(Player player) {
		player.sendMessage(main.color("&b&l(!) &rYou are currently connected to &2SCB-1"));
	}

	private void socialsCommand(Player player) {
		player.sendMessage(main.color("&7&m-------&7&l[Social Medias]&7&m-------"));
		player.sendMessage("");
		player.sendMessage(main.color("&f&lDiscord: &ahttps://discord.gg/FSZpmY9FZB"));
		player.sendMessage(main.color("&f&lYouTube: &ahttps://www.youtube.com/@minezone6480"));
		player.sendMessage(main.color("&f&lTikTok: &ahttps://www.tiktok.com/@minezonemc"));
//				player.sendMessage(main.color("&f&lTwitter: &ahttps://twitter.com/MinezoneMC"));
		player.sendMessage("");
		player.sendMessage(main.color("&7&m----------------------------"));
	}

	private void forceClassCommand(Player player, String[] args) {
		if (!player.hasPermission("scb.forceclass")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		Player targetPlayer;

		if (args.length == 1) {
			// /fc className - apply to self
			targetPlayer = player;
		} else if (args.length == 2) {
			// /fc className player - apply to specified player
			targetPlayer = Bukkit.getPlayer(args[1]);

			if (targetPlayer == null) {
				player.sendMessage(main.color("&c&l(!) &rPlayer not found!"));
				return;
			}
		} else {
			player.sendMessage(main.color("&c&l(!) &rUsage: /fc <className> [player]"));
			return;
		}

		GameInstance game = main.getGameManager().GetInstanceOfPlayer(targetPlayer);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &r" + (targetPlayer == player ? "You are" : targetPlayer.getName() + " is") + " not in a game!"));
			return;
		}

		ClassType[] classes = ClassType.values();
		ClassType selectedClass = Arrays.stream(classes)
				.filter(clazz -> clazz.toString().equalsIgnoreCase(args[0]))
				.findFirst()
				.orElse(null);

		if (selectedClass == null) {
			player.sendMessage(main.color("&c&l(!) &rClass not found"));
			return;
		}

		BaseClass oldBaseClass = game.classes.get(targetPlayer);

		if (oldBaseClass == null) {
			player.sendMessage(main.color("&c&l(!) &r" + (targetPlayer == player ? "You don't" : targetPlayer.getName() + " doesn't") + " have a current class!"));
			return;
		}

		// Store old class in oldClasses map
		game.oldClasses.put(targetPlayer, oldBaseClass);

		// Reset old score if it exists
		if (oldBaseClass.score != null) {
			try {
				oldBaseClass.score.getScoreboard().resetScores(oldBaseClass.score.getEntry());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Create new class instance
		BaseClass newBaseClass = selectedClass.GetClassInstance(game, targetPlayer);

		// Transfer all stats from old class
		newBaseClass.lives = oldBaseClass.lives;
		newBaseClass.tokens = oldBaseClass.tokens;
		newBaseClass.totalTokens = oldBaseClass.totalTokens;
		newBaseClass.totalExp = oldBaseClass.totalExp;
		newBaseClass.totalKills = oldBaseClass.totalKills;
		newBaseClass.bountyTarget = oldBaseClass.bountyTarget;

		// Create new scoreboard entry
		String scoreEntry = game.truncateString("" + selectedClass.getTag() + " " + ChatColor.WHITE + targetPlayer.getName(), 40);
		Score newScore = game.livesObjective.getScore(scoreEntry);
		newBaseClass.score = newScore;
		newScore.setScore(newBaseClass.lives);

		// Update class mappings
		game.classes.put(targetPlayer, newBaseClass);
		game.allClasses.put(targetPlayer, newBaseClass);

		// Update scoreboard
		game.sendScoreboardUpdate(targetPlayer);

		// Update display name
		if (targetPlayer.hasPermission("scb.chat"))
			targetPlayer.setDisplayName("" + targetPlayer.getName() + " " + selectedClass.getTag());
		else targetPlayer.setDisplayName("" + targetPlayer.getName() + " " + selectedClass.getTag() + ChatColor.GRAY);

		// Clear inventory and load new class
		for (PotionEffect type : targetPlayer.getActivePotionEffects()) targetPlayer.removePotionEffect(type.getType());
		targetPlayer.getInventory().clear();
		newBaseClass.loadPlayer();

		String message = "&a&l(!) &r&e" + targetPlayer.getName() + "&r's class was set to " + selectedClass.getTag();
		newBaseClass.TellAll(main.color(message));
	}

	private void healCommand(Player player, String[] args) {
		if (!player.hasPermission("scb.heal")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		Player targetPlayer;

		if (args.length == 0) {
			targetPlayer = player;
		} else {
			targetPlayer = Bukkit.getPlayer(args[0]);

			if (targetPlayer == null) {
				player.sendMessage(main.color("&c&l(!) &rPlayer not found!"));
				return;
			}
		}

		// Fully heal the player to their maximum health
		double maxHealth = targetPlayer.getMaxHealth();
		targetPlayer.setHealth(maxHealth);
		targetPlayer.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);

		if (targetPlayer.equals(player)) {
			player.sendMessage(main.color("&a&l(!) &rYou have been healed!"));
		} else {
			player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 1, 1);
			player.sendMessage(main.color("&a&l(!) &rYou have healed &e" + targetPlayer.getName() + "&r!"));
			targetPlayer.sendMessage(main.color("&a&l(!) &rYou have been healed by &e" + player.getName() + "&r!"));
		}
	}

	private void soundCommand(String[] args, Player player) {
		if (!player.hasPermission("scb.sound")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		if (args.length < 1) {
			// Display all sounds in a clickable list
			displaySoundList(player);
			return;
		}

		Location location = player.getLocation();

		try {
			Sound sound = Sound.valueOf(args[0].toUpperCase()); // Get the Sound enum value
			float volume = 1.0f; // Default volume
			float pitch = 1.0f; // Default pitch

			// Parse pitch if provided
			if (args.length >= 2) {
				pitch = Float.parseFloat(args[1]);
			}

			// Parse volume if provided
			if (args.length >= 3) {
				volume = Float.parseFloat(args[2]);
			}

			// Play the sound to everyone in the world
			player.getWorld().playSound(location, sound, volume, pitch);
			player.sendMessage(ChatColor.GREEN + "Played sound: " + sound.name() + " with pitch " + pitch
					+ " and volume " + volume);
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + "Invalid sound name.");
		}
	}

	private void displaySoundList(Player player) {
		player.sendMessage(ChatColor.GOLD + "=== Click a Sound to Play It ===");

		for (Sound sound : Sound.values()) {
			// Create a clickable TextComponent for each sound
			TextComponent soundMessage = new TextComponent(ChatColor.AQUA + "- " + sound.name());
			soundMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sound " + sound.name()));

			// Send the clickable message to the player
			player.spigot().sendMessage(soundMessage);
		}

		player.sendMessage(ChatColor.GOLD + "=== End of Sound List ===");
	}

	private void lactateCommand(Player player) {
		if (player.hasPermission("scb.lactate")) {
			Location loc = player.getLocation();
			loc.setY(loc.getY() + 0.7);
			player.sendMessage(main.color("&r&l(!) &rYou have &r&lLACTATED!"));
			player.getWorld().playSound(loc, Sound.COW_HURT, 1, 1);
			sendParticle(player, loc, EnumParticle.SNOWBALL, 300, 0.8f, 0.0f, -0.3f, 0.0f);
		} else {
			player.sendMessage(main.color("&c&l(!) &rYou need the rank &5&lSUPREME &rto use this command!"));
		}
	}
	
	public static void sendParticle(Player player, Location location, EnumParticle particle, int amount, float speed,
			float offsetX, float offsetY, float offsetZ) {
		PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(particle, true, (float) location.getX(),
				(float) location.getY(), (float) location.getZ(), offsetX, offsetY, offsetZ, speed, amount);

		for (Player players : Bukkit.getOnlinePlayers())
			((CraftPlayer) players).getHandle().playerConnection.sendPacket(particles);
	}

	private void soundNMSCommand(String[] args, Player player) {
		if (!player.hasPermission("scb.soundnms")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		// Handle list command
		if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
			List<String> allSounds = getAllNMSSounds();
			int page = args.length > 1 ? tryParseInt(args[1], 1) : 1;
			displayPaginatedSoundList(player, allSounds, page);
			return;
		}

		// Handle sound playing
		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "Usage: /soundnms (sn) <name> [pitch] [volume]");
			player.sendMessage(ChatColor.GRAY + "Or /soundnms (sn) list [page] to see all sounds");
			return;
		}

		try {
			float volume = 10.0f;
			float pitch = 1.0f;

			if (args.length >= 2)
				pitch = Float.parseFloat(args[1]);
			if (args.length >= 3)
				volume = Float.parseFloat(args[2]);

			// Play sound using NMS
			String soundName = args[0].toLowerCase();
			SoundManager.playNMSSoundToAll(player, soundName, volume, pitch);

			player.sendMessage(
					ChatColor.GREEN + "Played sound: " + soundName + " (Pitch: " + pitch + ", Volume: " + volume + ")");

		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "Invalid number format for pitch/volume");
		} catch (Exception e) {
			player.sendMessage(ChatColor.RED + "Error playing sound: " + e.getMessage());
			if (player.hasPermission("scb.sound.debug")) {
				player.sendMessage(ChatColor.GRAY + "Technical: " + e);
			}
		}
	}

	private void displayPaginatedSoundList(Player player, List<String> sounds, int page) {
		if (sounds.isEmpty()) {
			player.sendMessage(ChatColor.RED + "No sounds found in registry!");
			return;
		}

		int perPage = 25;
		int totalPages = (int) Math.ceil((double) sounds.size() / perPage);
		page = Math.max(1, Math.min(page, totalPages));

		player.sendMessage(ChatColor.GOLD + "=== Available Sounds (Page " + page + "/" + totalPages + ") ===");

		int start = (page - 1) * perPage;
		int end = Math.min(start + perPage, sounds.size());

		for (int i = start; i < end; i++) {
			// Create a clickable TextComponent for each sound
			TextComponent soundMessage = new TextComponent(ChatColor.YELLOW + "- " + sounds.get(i));
			soundMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/soundnms " + sounds.get(i)));

			// Send the clickable message to the player
			player.spigot().sendMessage(soundMessage);
		}

		player.sendMessage(ChatColor.GOLD + "=====================");
		player.sendMessage(ChatColor.GRAY + "Use /soundnms list <page> to see more sounds");
	}

	public List<String> getAllNMSSounds() {
		return Arrays.asList(
				// Ambient sounds
				"ambient.cave.cave", "ambient.weather.rain", "ambient.weather.thunder",

				// Player/entity hurt sounds
				"game.player.hurt.fall.big", "game.neutral.hurt.fall.big", "game.hostile.hurt.fall.big",
				"game.player.hurt.fall.small", "game.neutral.hurt.fall.small", "game.hostile.hurt.fall.small",
				"game.player.hurt", "game.neutral.hurt", "game.hostile.hurt", "game.player.die", "game.neutral.die",
				"game.hostile.die",

				// Dig/break sounds
				"dig.cloth", "dig.glass", "game.potion.smash", "dig.grass", "dig.gravel", "dig.sand", "dig.snow",
				"dig.stone", "dig.wood",

				// Fire sounds
				"fire.fire", "fire.ignite", "item.fireCharge.use",

				// Firework sounds
				"fireworks.blast", "fireworks.blast_far", "fireworks.largeBlast", "fireworks.largeBlast_far",
				"fireworks.launch", "fireworks.twinkle", "fireworks.twinkle_far",

				// Swim sounds
				"game.player.swim.splash", "game.neutral.swim.splash", "game.hostile.swim.splash", "game.player.swim",
				"game.neutral.swim", "game.hostile.swim",

				// Liquid sounds
				"liquid.lava", "liquid.lavapop", "liquid.water",

				// Minecart sounds
				"minecart.base", "minecart.inside",

				// Note block sounds
				"note.bass", "note.bassattack", "note.bd", "note.harp", "note.hat", "note.pling", "note.snare",

				// Portal sounds
				"portal.portal", "portal.travel", "portal.trigger",

				// Random sounds
				"random.anvil_break", "random.anvil_land", "random.anvil_use", "random.bow", "random.bowhit",
				"random.break", "random.burp", "random.chestclosed", "random.chestopen", "gui.button.press",
				"random.click", "random.door_open", "random.door_close", "random.drink", "random.eat", "random.explode",
				"random.fizz", "game.tnt.primed", "creeper.primed", "random.levelup", "random.orb", "random.pop",
				"random.splash", "random.successful_hit", "random.wood_click",

				// Step sounds
				"step.cloth", "step.grass", "step.gravel", "step.ladder", "step.sand", "step.snow", "step.stone",
				"step.wood",

				// Piston sounds
				"tile.piston.in", "tile.piston.out",

				// Mob sounds
				"mob.bat.death", "mob.bat.hurt", "mob.bat.idle", "mob.bat.loop", "mob.bat.takeoff", "mob.blaze.breathe",
				"mob.blaze.death", "mob.blaze.hit", "mob.cat.hiss", "mob.cat.hitt", "mob.cat.meow", "mob.cat.purr",
				"mob.cat.purreow", "mob.chicken.hurt", "mob.chicken.plop", "mob.chicken.say", "mob.chicken.step",
				"mob.cow.hurt", "mob.cow.say", "mob.cow.step", "mob.creeper.death", "mob.creeper.say",
				"mob.enderdragon.end", "mob.enderdragon.growl", "mob.enderdragon.hit", "mob.enderdragon.wings",
				"mob.endermen.death", "mob.endermen.hit", "mob.endermen.idle", "mob.endermen.portal",
				"mob.endermen.scream", "mob.endermen.stare", "mob.ghast.affectionate_scream", "mob.ghast.charge",
				"mob.ghast.death", "mob.ghast.fireball", "mob.ghast.moan", "mob.ghast.scream", "mob.guardian.hit",
				"mob.guardian.idle", "mob.guardian.death", "mob.guardian.elder.hit", "mob.guardian.elder.idle",
				"mob.guardian.elder.death", "mob.guardian.land.hit", "mob.guardian.land.idle",
				"mob.guardian.land.death", "mob.guardian.curse", "mob.guardian.attack", "mob.guardian.flop",
				"mob.horse.angry", "mob.horse.armor", "mob.horse.breathe", "mob.horse.death", "mob.horse.donkey.angry",
				"mob.horse.donkey.death", "mob.horse.donkey.hit", "mob.horse.donkey.idle", "mob.horse.gallop",
				"mob.horse.hit", "mob.horse.idle", "mob.horse.jump", "mob.horse.land", "mob.horse.leather",
				"mob.horse.skeleton.death", "mob.horse.skeleton.hit", "mob.horse.skeleton.idle", "mob.horse.soft",
				"mob.horse.wood", "mob.horse.zombie.death", "mob.horse.zombie.hit", "mob.horse.zombie.idle",
				"mob.irongolem.death", "mob.irongolem.hit", "mob.irongolem.throw", "mob.irongolem.walk",
				"mob.magmacube.big", "mob.magmacube.jump", "mob.magmacube.small", "mob.pig.death", "mob.pig.say",
				"mob.pig.step", "mob.rabbit.hurt", "mob.rabbit.idle", "mob.rabbit.hop", "mob.rabbit.death",
				"mob.sheep.say", "mob.sheep.shear", "mob.sheep.step", "mob.silverfish.hit", "mob.silverfish.kill",
				"mob.silverfish.say", "mob.silverfish.step", "mob.skeleton.death", "mob.skeleton.hurt",
				"mob.skeleton.say", "mob.skeleton.step", "mob.slime.attack", "mob.slime.big", "mob.slime.small",
				"mob.spider.death", "mob.spider.say", "mob.spider.step", "mob.villager.death", "mob.villager.haggle",
				"mob.villager.hit", "mob.villager.idle", "mob.villager.no", "mob.villager.yes", "mob.wither.death",
				"mob.wither.hurt", "mob.wither.idle", "mob.wither.shoot", "mob.wither.spawn", "mob.wolf.bark",
				"mob.wolf.death", "mob.wolf.growl", "mob.wolf.howl", "mob.wolf.hurt", "mob.wolf.panting",
				"mob.wolf.shake", "mob.wolf.step", "mob.wolf.whine", "mob.zombie.death", "mob.zombie.hurt",
				"mob.zombie.infect", "mob.zombie.metal", "mob.zombie.remedy", "mob.zombie.say", "mob.zombie.step",
				"mob.zombie.unfect", "mob.zombie.wood", "mob.zombie.woodbreak", "mob.zombiepig.zpig",
				"mob.zombiepig.zpigangry", "mob.zombiepig.zpigdeath", "mob.zombiepig.zpighurt",

				// Music/record sounds
				"records.11", "records.13", "records.blocks", "records.cat", "records.chirp", "records.far",
				"records.mall", "records.mellohi", "records.stal", "records.strad", "records.wait", "records.ward",
				"music.menu", "music.game", "music.game.creative", "music.game.end", "music.game.end.dragon",
				"music.game.end.credits", "music.game.nether");
	}

	public void colorCommand(String[] args, Player player) {
		List<ChatColor> colors = Arrays.asList(ChatColor.WHITE, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.GREEN,
				ChatColor.DARK_GREEN, ChatColor.AQUA, ChatColor.DARK_AQUA, ChatColor.BLUE, ChatColor.DARK_BLUE,
				ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE, ChatColor.RED, ChatColor.DARK_RED, ChatColor.GRAY,
				ChatColor.DARK_GRAY, ChatColor.BLACK, ChatColor.RESET);

		PlayerData data = main.getDataManager().getPlayerData(player);

		if (player.hasPermission("scb.color")) {
			if (data != null) {
				try {
					if (args.length == 0 || !colors.contains(ChatColor.valueOf(args[0].toUpperCase()))) {
						colorMessage(player, colors);
					} else if (args[0].equalsIgnoreCase("reset")) {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.RESET + player.getName());
						data.color = "";
					} else {
						player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET + "Changed your prefix to "
								+ ChatColor.valueOf(args[0].toUpperCase()) + player.getName());
						data.color = args[0].toUpperCase();
					}
				} catch (IllegalArgumentException e) {
					colorMessage(player, colors);
				}
			}
		} else {
			player.sendMessage(
					"" + ChatColor.DARK_RED + ChatColor.BOLD + "(!) " + ChatColor.RESET + "You need the rank "
							+ ChatColor.BLUE + ChatColor.BOLD + "CAPTAIN " + ChatColor.RESET + "to use this command");
		}
	}

	private void colorMessage(Player player, List<ChatColor> colors) {
		player.sendMessage(main.color("&f&l----------------------------------------"));
		player.sendMessage(main.color("&r&l(!) &rClick on a color below to select it:"));
		TextComponent[] colorText = new TextComponent[colors.size() - 1];
		int i = 0;
		for (ChatColor color : colors) {
			if (color != ChatColor.RESET) {
				TextComponent message = new TextComponent(WordUtils.capitalizeFully(color.name().replace('_', ' ')));
				if (i < colorText.length)
					message.addExtra(ChatColor.GRAY + ", ");
				message.setColor(color.asBungee());
				message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color " + color.name()));
				colorText[i] = message;
				i++;
			}
		}
		player.spigot().sendMessage(colorText);
		TextComponent message = new TextComponent("Click here to reset color");
		message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color RESET"));
		player.spigot().sendMessage(message);
		player.sendMessage(main.color("&f&l----------------------------------------"));
	}

	private void partyCommand(String[] args, Player player) {
		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing:"));
			player.sendMessage(main.color(ChatColor.STRIKETHROUGH + "&7-------------------------------------"));
			player.sendMessage(main.color("&e&lPARTY COMMANDS:"));
			player.sendMessage(main.color("&e/party invite <player> &7-> &bInvites a player to your party"));
			player.sendMessage(main.color("&e/party accept <player> &7-> &bAccepts a party invite from a player"));
		}
	}

	private void purchaseCommand(String[] args, Player player) {
		if (!player.hasPermission("scb.purchases")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		if (args.length == 0) {
			player.sendMessage("Not enough arguments!");
			return;
		}

		switch (args[0].toLowerCase()) {
		case "get":
			purchaseGetCommand(player);
			break;
		case "buy":
			if (args.length == 1) {
				player.sendMessage("Not enough arguments");
				return;
			}

			purchaseBuyCommand(args, player);
			break;
		}
	}

	private void purchaseGetCommand(Player player) {
		PlayerData data = main.getDataManager().getPlayerData(player);
		player.sendMessage("Listing purchases...");

		int size = 0;
		for (Entry<Integer, ClassDetails> entry : data.playerClasses.entrySet()) {
			player.sendMessage(" - " + entry.getKey() + ": " + entry.getValue().toString());
			size++;
		}

		if (size == 0) {
			player.sendMessage("You have no Class Stats");
		}
	}

	private void purchaseBuyCommand(String[] args, Player player) {
		int classID = Integer.parseInt(args[1]);
		PlayerData playerData = main.getDataManager().getPlayerData(player);
		ClassDetails details = playerData.playerClasses.get(classID);

		if (details == null) {
			details = new ClassDetails();
			playerData.playerClasses.put(classID, details);
		}
		details.setPurchased();
		player.sendMessage("Class Purchased!");
	}

	private void startGameCommand(Player player) {
		if (!player.hasPermission("scb.startGame")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
			return;
		}

		if (game.state != GameState.WAITING) {
			if (game.state == GameState.STARTED)
				player.sendMessage(main.color("&c&l(!) &rGame is already in progress!"));
			else if (game.state == GameState.ENDED)
				player.sendMessage(main.color("&c&l(!) &rGame has already ended!"));
			return;
		}

		if (game.players.size() == 0) {
			player.sendMessage(main.color("&c&l(!) &rNot enough players to start!"));
			return;
		}

		game.getGameSettings().forceStartGame(true);
	}

	private void setFrenzyCommand(Player player) {
		if (!player.hasPermission("scb.frenzy")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
			return;
		}

		if (game.state != GameState.WAITING) {
			if (game.state == GameState.STARTED)
				player.sendMessage(main.color("&c&l(!) &rGame is already in progress!"));
			else if (game.state == GameState.ENDED)
				player.sendMessage(main.color("&c&l(!) &rGame has already ended!"));
			return;
		}

		game.getGameSettings().changeGameType(true);
	}

	private void flyCommand(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
		PlayerData flyData = main.getDataManager().getPlayerData(player);

		if (game != null) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot use this in game!"));
			return;
		}

		if (main.getParkour().players.containsKey(player)) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot use this now!"));
			return;
		}

		if (!player.hasPermission("scb.fly")) {
			player.sendMessage(main.color("&c&l(!) &rYou need the rank &9&lCAPTAIN &rto use this command!"));
			return;
		}

		if (flyData != null) {
			resetDoubleJump(player);
			if (flyData.fly == 0) {
				player.sendMessage(main.color("&e&l(!) &rYou have enabled flight!"));
				flyData.fly = 1;
			} else {
				player.sendMessage(main.color("&e&l(!) &rYou have disabled flight!"));
				flyData.fly = 0;
			}
			main.getDataManager().saveData(flyData); // Save even when server restarts
		}
	}

	private void itemsCommand(String[] args, Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/items"));
			return;
		}

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game"));
			return;
		}

		if (!player.hasPermission("scb.items")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		for (GameLootDrops loot : GameLootDrops.values()) {
			player.getInventory().addItem(loot.getItem());
		}
	}

	private void setLivesCommand(String[] args, Player player) {
		if (args.length < 2) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/setlives <player> <num>"));
			return;
		}

		int num;

		try {
			num = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			player.sendMessage(main.color("&r&l(!) &rPlease specify number of lives"));
			return;
		}

		if (num <= 0) {
			player.sendMessage(main.color("&c&l(!) &rNumber of lives must be greater than zero!"));
			return;
		}

		Player target = Bukkit.getServer().getPlayerExact(args[0]);

		if (target == null) {
			player.sendMessage(main.color("&c&l(!) &rPlease specify a player!"));
			return;
		}

		GameInstance game = main.getGameManager().GetInstanceOfPlayer(target);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rThis player is not in a game!"));
			return;
		}

		if (game.state != GameState.STARTED) {
			player.sendMessage(main.color("&c&l(!) &rGame must be started to use!"));
			return;
		}

		BaseClass baseClass = game.classes.get(target);

		if (baseClass == null) {
			player.sendMessage(main.color("&c&l(!) &rTarget player doesn't have a class assigned."));
			return;
		}

		if (!player.hasPermission("scb.setlives")) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have permission for that!"));
			return;
		}

		baseClass.lives = num;
		player.sendMessage(main.color("&2&l(!) &rYou set &e" + target.getName() + "&r's lives to &e" + num));
		baseClass.score.setScore(baseClass.lives);
	}

	private void gameStatsCommand(String[] args, Player player) {
		GameInstance currentGame = main.getGameManager().GetInstanceOfPlayer(player);

		// Player is in a game
		if (currentGame != null && currentGame.state == GameState.STARTED) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot use this in a game!"));
			return;
		}

		GameInstance i = main.gameStats.get(player);
		// No stats available
		if (i == null) {
			player.sendMessage(main.color("&c&l(!) &rThis game's stats have expired or are unavailable."));
			return;
		}

		// Game in progress
		if (i.state != GameState.ENDED) {
			player.sendMessage(main.color("&c&l(!) &rThis game is still in progress. No stats available."));
			return;
		}

		// Open stats
		try {
			new GameStatsGUI(main, i).inv.open(player);
		} catch (NullPointerException ex) {
			player.sendMessage(main.color("&c&l(!) &rThis game's stats cannot be viewed. Did a player leave early?"));
		}
	}

	private void mapsCommand(String[] args, Player player) {
		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/maps"));
			return;
		}

		new GameSelectorGUI(main).inv.open(player);

//		// Send the message to the player
//		player.sendMessage(ChatColorHelper.color(createMapsString()));
	}

	private void fishingCommand(String[] args, Player player) {
		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/fishing"));
			return;
		}

		new FishingGUI(main, null).inv.open(player);
	}

	private void cosmeticsCommand(String[] args, Player player) {
		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/cosmetics"));
			return;
		}

		new CosmeticsGUI(main).inv.open(player);
	}

	private String createMapsString() {
		StringBuilder stringBuilder = new StringBuilder();
		String HEADERCOLOR = "&e&l";

		List<Maps> classicCuratedMaps = Maps.filterMaps(GameType.CLASSIC, Maps.Category.CURATED, null, null);
		List<Maps> duelCuratedMaps = Maps.filterMaps(GameType.DUEL, Maps.Category.CURATED, null, null);
		List<Maps> curatedMaps = new ArrayList<>(classicCuratedMaps);
		curatedMaps.addAll(duelCuratedMaps);

		List<Maps> classicCasualMaps = Maps.filterMaps(GameType.CLASSIC, Maps.Category.CASUAL, null, null);
		List<Maps> duelCasualMaps = Maps.filterMaps(GameType.DUEL, Maps.Category.CASUAL, null, null);
		List<Maps> casualMaps = new ArrayList<>(classicCasualMaps);
		casualMaps.addAll(duelCasualMaps);

		List<Maps> classicVaultedMaps = Maps.filterMaps(GameType.CLASSIC, Maps.Category.VAULTED, null, null);
		List<Maps> duelVaultedMaps = Maps.filterMaps(GameType.DUEL, Maps.Category.VAULTED, null, null);
		List<Maps> vaultedMaps = new ArrayList<>(classicVaultedMaps);
		vaultedMaps.addAll(duelVaultedMaps);

		stringBuilder.append("&f&l----------------------------------------");
		stringBuilder.append(HEADERCOLOR).append("\nCURATED MAPS: (").append(curatedMaps.size()).append(")\n");

		// Get Classic maps for Curated category
		appendMaps(GameType.CLASSIC, Maps.Category.CURATED, stringBuilder);

		// Get Duels maps for Curated category
		appendMaps(GameType.DUEL, Maps.Category.CURATED, stringBuilder);

		// Now for Casual Maps section
		stringBuilder.append(HEADERCOLOR).append(" \nCASUAL MAPS: (").append(casualMaps.size()).append(")\n");

		// Get Classic maps for Casual category
		appendMaps(GameType.CLASSIC, Maps.Category.CASUAL, stringBuilder);

		// Get Duels maps for Casual category
		appendMaps(GameType.DUEL, Maps.Category.CASUAL, stringBuilder);

		// Now for Vaulted Maps section
		stringBuilder.append(HEADERCOLOR).append(" \nVAULTED MAPS: (").append(vaultedMaps.size()).append(")\n");

		// Get Classic maps for Vaulted category
		appendMaps(GameType.CLASSIC, Maps.Category.VAULTED, stringBuilder);

		// Get Duels maps for Vaulted category
		appendMaps(GameType.DUEL, Maps.Category.VAULTED, stringBuilder);

		stringBuilder.append("&f&l----------------------------------------");

		return stringBuilder.toString();
	}

	private void appendMaps(GameType gameMode, Maps.Category category, StringBuilder stringBuilder) {
		List<Maps> maps = Maps.filterMaps(gameMode, category, null, null);
		Maps.sortMaps(maps, Maps.Sorter.ALPHABETICAL);

		appendColorAndComma(stringBuilder, maps, gameMode);
	}

	private void appendColorAndComma(StringBuilder stringBuilder, List<Maps> mapsList, GameType gameMode) {
		String color = "";

		if (gameMode == GameType.CLASSIC) {
			color = "&a";
		} else if (gameMode == GameType.DUEL)
			color = "&b";

		for (int i = 0; i < mapsList.size(); i++) {
			stringBuilder.append(color).append(mapsList.get(i).getName());

			if (i < mapsList.size() - 1) {
				stringBuilder.append("&7, ");
			}
		}
		stringBuilder.append("\n"); // Newline after each map group
	}

	private void duelCommand(String[] args, Player player) {
		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/duel <player>"));
			return;
		}

		Player targetPlayer = Bukkit.getPlayer(args[0]);

		if (targetPlayer != null) {
			if (targetPlayer != player) {

			} else
				player.sendMessage(main.color("&c&l(!) &rSilly you! You can't duel yourself!"));
		} else
			player.sendMessage(main.color("&c&l(!) &rThe specified player is not online!"));
	}

	private void joinCommand(String[] args, Player player) {
		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/join <map>"));
			return;
		}

		String mapName = args[0];
		GameInstance gameSpectator = main.getGameManager().GetInstanceOfSpectator(player);

		if (gameSpectator != null) {
			player.sendMessage(main.color("&c&l(!) &rYou are currently spectating a game!"));
			return;
		}

		Maps map = null;

		for (Maps maps : Maps.values()) {
			if (maps.toString().equalsIgnoreCase(mapName)) {
				map = maps;
				main.getListener().removeCosmetics(player);
				break;
			}
		}

		if (map == null) {
			player.sendMessage(main.color("&c&l(!) &rThis map does not exist! Use &e/maps &rfor a list of maps"));
			return;
		}

		main.getGameManager().JoinMap(player, map);
	}

	private void spectateCommand(String[] args, Player player) {
		if (args.length != 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/spectate"));
			return;
		}

		new ActiveGamesGUI(main).inv.open(player);

//		String mapName = args[0];
//		Maps map = null;
//
//		for (Maps maps : Maps.values()) {
//			if (maps.toString().equalsIgnoreCase(mapName)) {
//				map = maps;
//				break;
//			}
//		}
//
//		if (map == null) {
//			player.sendMessage(main.color("&c&l(!) &rThis map does not exist! Use &e/maps &rfor a list of maps"));
//			return;
//		}
//
//		main.getGameManager().SpectatorJoinMap(player, map);
	}

	private void playersCommand(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
			return;
		}

		String players = "";
		for (Player gamePlayer : game.players) {
			if (!players.isEmpty()) {
				players += ", ";
			}

			players += gamePlayer.getName() + "";
		}

		player.sendMessage(main.color("&l(!) &aPlayers in your game (" + game.players.size() + "): "));
		player.sendMessage(main.color("l--> " + players));
	}

	private void classCommand(String[] args, Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);

		PlayerData playerData = main.getDataManager().getPlayerData(player);

		if (args.length == 0) {
			player.sendMessage(main.color("&c&l(!) &rIncorrect usage! Try doing: &e/class <classname>"));
			return;
		}

		String className = args[0];
		if (className.equalsIgnoreCase("random") && game != null && game.state == GameState.WAITING) {
			selectRandomClass(player, playerData);
			player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
			return;
		}

		ClassType selectedClassType = getClassType(className);
		if (selectedClassType == null) {
			player.sendMessage(
					main.color("&c&l(!) &rThis class does not exist! Use &e/classes &rfor a list of playable classes"));
			return;
		}

		handleClassSelection(player, game, playerData, selectedClassType);
	}

	private void handleClassSelection(Player player, GameInstance game, PlayerData playerData, ClassType type) {
		ClassDetails classDetails = playerData.playerClasses.get(type.getID());

		if (!isClassUnlocked(player, classDetails, type) || !isLevelUnlocked(player, playerData, type)
				|| !isFishermanClassUnlocked(player, type) || !isFreddyUnlocked(player, type)
				|| !isRankRequirementMet(player, type) || (game != null && !isGameStateWaiting(game, player))
				|| (game != null && !isFrenzyGameType(game, player))) {
			return;
		}

		displayClassSelectionMessage(player, type);

		if (game != null) {
			main.getGameManager().playerSelectClass(player, type);
			player.playSound(player.getLocation(), Sound.SUCCESSFUL_HIT, 0.5f, 1);
		}
	}

	private void displayClassSelectionMessage(Player player, ClassType type) {
		player.sendMessage(main.color("&2&l============================================="));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l|| " + "&e&lSelected Class: " + type.getTag()));
		player.sendMessage(main.color("&2&l|| " + "&e&lClass Desc: &e" + type.getClassDesc()));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l|| "));
		player.sendMessage(main.color("&2&l============================================="));
	}

	private void selectRandomClass(Player player, PlayerData playerData) {
		Random random = new Random();
		ClassType classType = ClassType.getAvailableClasses()[random.nextInt(ClassType.getAvailableClasses().length)];

		if (playerData.playerClasses.get(classType.getID()) != null
				&& playerData.playerClasses.get(classType.getID()).purchased || classType.getTokenCost() == 0) {
			Rank donor = classType.getMinRank();

			if (donor == null || player.hasPermission("scb." + donor.toString().toLowerCase())) {
				player.sendMessage(main.color("&2&l(!) " + "&eYou have selected to go a &lRandom class"));
				main.getGameManager().playerSelectClass(player, classType);
				GameInstance game = main.getGameManager().GetInstanceOfPlayer(player);
				game.board.updateLine(5, main.color(" &cR&6a&en&ad&bo&3m"));
				player.setDisplayName(player.getName());
			}
		}
	}

	private boolean isRankRequirementMet(Player player, ClassType type) {
		Rank donor = type.getMinRank();
		if (donor != null && !player.hasPermission("scb." + donor.toString().toLowerCase())) {
			player.sendMessage(main.color("&c&l(!) &rYou need a rank to use this class"));
			return false;
		}
		return true;
	}

	private boolean isLevelUnlocked(Player player, PlayerData playerData, ClassType type) {
		// Level Classes
		if (type.getLevel() > 0 && playerData.level < type.getLevel()) {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
			return false;
		}
		return true;
	}
	
	private boolean isFreddyUnlocked(Player player, ClassType type) {
		if (type == ClassType.Freddy && !main.getHalloweenManager().hasUnlockedFreddy(player) && !player.isOp()) {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
			return false;
		}
		return true;
	}

	private boolean isFishermanClassUnlocked(Player player, ClassType type) {
		if (type == ClassType.Fisherman && !main.fishing.hasUnlockedFisherman(player) && !player.isOp()) {
			player.sendMessage(main.color("&c&l(!) &rYou have not unlocked this class yet!"));
			return false;
		}
		return true;
	}

	private boolean isClassUnlocked(Player player, ClassDetails classDetails, ClassType type) {
		boolean isPurchased = classDetails != null && classDetails.purchased;

		if (!isPurchased && type.getTokenCost() != 0) {
			player.sendMessage(main.color("&c&l(!) &rYou do not have this class unlocked"));
			return false;
		}
		return true;
	}

	private boolean isFrenzyGameType(GameInstance game, Player player) {
		if (game.gameType == GameType.FRENZY) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot select a class in a Frenzy game!"));
			return false;
		}
		return true;
	}

	private ClassType getClassType(String className) {
		for (ClassType type : ClassType.getAvailableClasses()) {
			if (className.equalsIgnoreCase(type.toString())) {
				return type;
			}
		}
		return null;
	}

	public void leaveGame(Player player) {
		GameInstance game = main.getGameManager().GetInstanceOfSpectator(player);
		player.spigot().setCollidesWithEntities(true);
		player.setAllowFlight(false);
		player.setAllowFlight(true);
		for (Player p : Bukkit.getOnlinePlayers()) {
			player.showPlayer(p);
			p.showPlayer(player);
		}

		if (game != null && game.state == GameState.ENDED)
			return;
		else if (main.getGameManager().RemovePlayerFromAll(player)) {
			main.ResetPlayer(player);
			/*
			 * main.getScoreboardManager().lobbyBoard(player);
			 * main.sendScoreboardUpdate(player);
			 */
			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have left your game");

			if (game != null && game.getGameSettings() != null) {
				GameSettings gs = game.getGameSettings();

				if (gs.startVotes.contains(player)) {
					gs.totalStartVotes--;
					gs.startVotes.remove(player);
				}
			}

			for (PotionEffect type : player.getActivePotionEffects())
				player.removePotionEffect(type.getType());

			// main.sendScoreboardUpdate(player);
			removeArmor(player);
		} else if (game != null && game.spectators.contains(player)) {
			String mapName = "";
			if (game.duosMap != null)
				mapName = game.duosMap.toString();
			else
				mapName = game.getMap().toString();

			player.sendMessage("" + ChatColor.RESET + ChatColor.DARK_GREEN + ChatColor.BOLD + "(!) " + ChatColor.RESET
					+ "You have left " + mapName);
			main.ResetPlayer(player);
			main.getScoreboardManager().lobbyBoard(player);
			main.sendScoreboardUpdate(player);
			game.spectators.remove(player);
			player.setDisplayName("" + player.getName());
		} else
			player.sendMessage(main.color("&c&l(!) &rYou are not in a game!"));
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("join") || cmd.getName().equalsIgnoreCase("spectate")) {
			List<Maps> maps = Arrays.asList(Maps.values());
			List<String> mapsString = Lists.newArrayList();
			if (args.length == 1) {
				for (Maps map : maps) {
					if (map.getName().toLowerCase().startsWith(args[0].toLowerCase()))
						mapsString.add(map.getName());
				}
				return mapsString;
			}
		} else if (cmd.getName().equalsIgnoreCase("class")) {
			List<ClassType> a = Arrays.asList(ClassType.getAvailableClasses());
			List<String> f = Lists.newArrayList();
			if (args.length == 1) {
				for (ClassType s : a) {
					if (s.name().toLowerCase().startsWith(args[0].toLowerCase()))
						f.add(s.name());
				}
				return f;
			}
		} else if (cmd.getName().equalsIgnoreCase("sound")) {
			if (args.length == 1) {
				List<String> soundNames = new ArrayList<>();
				for (Sound sound : Sound.values()) {
					if (sound.name().toLowerCase().startsWith(args[0].toLowerCase())) {
						soundNames.add(sound.name());
					}
				}
				return soundNames;
			}
		}
		return null;
	}

	private boolean isGameStateWaiting(GameInstance game, Player player) {
		if (game.state != GameState.WAITING) {
			player.sendMessage(main.color("&c&l(!) &rYou cannot select a class while in a game!"));
			return false;
		}
		return true;
	}

	private boolean isPlayerInGame(Player player, GameInstance game) {
		if (game == null) {
			player.sendMessage(main.color("&c&l(!) &rYou have to be in a game to select a class!"));
			return false;
		}
		return true;
	}

	private void removeArmor(Player player) {
		ItemStack air = new ItemStack(Material.AIR, 1);
		player.getInventory().setHelmet(air);
		player.getInventory().setChestplate(air);
		player.getInventory().setLeggings(air);
		player.getInventory().setBoots(air);
	}

	private void resetDoubleJump(Player player) {
		player.setAllowFlight(false);
		player.setAllowFlight(true);
	}

	private int tryParseInt(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}
}
