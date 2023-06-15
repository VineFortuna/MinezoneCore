package anthony.SuperCraftBrawl;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class Events implements Listener {

	private Core main;
	private BukkitRunnable event;
	private final Vector vec1;
	private final Location loc1;
	private final Location loc2;
	private ConcurrentHashMap<Integer, Integer> stages;
	private HashSet<BukkitTask> tasks;
	private Vector vec;

	public Events(Core main) {
		this.main = main;
		loc2 = new Location(Bukkit.getWorld("world"), 1.5, 142, 19.5);
		tasks = new HashSet<>();
		stages = new ConcurrentHashMap<>();
		vec1 = new Vector(-0.6479183039533765, -0.008541055842897546, -0.7616619471703104);
		loc1 = new Location(Bukkit.getWorld("world"), 29, 207, 83);
		this.main.getServer().getPluginManager().registerEvents(this, main);
	}

	public void startEvent() {
		// main.listener = null;
		main.smmmanager = null;
		main.gameManager = null;
		// main.commands = new Commands(this);
		//main.databaseManager = null;
		//main.dataManager = null;
		main.npcManager = null;
		main.rankManager = null;
		main.ag = null;
		//main.dm = null;
		main.p = null;

		for (Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(new Location(main.getLobbyWorld(), 12.488, 153, 26.499));
			p.sendMessage(main.color("&b&lIce Dragon> &cBahahaha, nice try, very nice try indeed..."));
		}

		sendMsgs();
	}

	public void sendMsgs() {
		if (event == null) {
			event = new BukkitRunnable() {
				int ticks = 0;

				@Override
				public void run() {
					if (ticks == 3) {
						TellAll(main.color("&b&lIce Dragon> &cI did warn you about this.. And you didn't listen"));
					} else if (ticks == 8) {
						TellAll(main.color(
								"&b&lIce Dragon> &cThe last time a server went through a Spring Cleaning, it ended in disaster"));
					} else if (ticks == 15) {
						TellAll(main.color("&b&lIce Dragon> &cNow, you shall pay the price...."));
					} else if (ticks == 25) {
						World w = main.getServer().createWorld(new WorldCreator("world_the_end"));
						Location loc = new Location(w, -61.502, 47, 29.499);

						for (Player p : Bukkit.getOnlinePlayers()) {
							p.setGameMode(GameMode.SURVIVAL);
							p.teleport(loc);
							p.getInventory().clear();
						}
					} else if (ticks == 30) {
						event = null;
						this.cancel();
						TellAll(main.color(
								"&b&lIce Dragon> &cMy brother will be taking care of you, should be a piece of cake hahaha good luck"));
						selectClass();
					}

					ticks++;
				}
			};
			event.runTaskTimer(main, 0, 20);
		}
	}

	public void selectClass() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.setAllowFlight(true);
			//new FinalEventGUI(main, p).inv.open(p);
		}
	}

	public void sendMsgs2() {
		if (event == null) {
			event = new BukkitRunnable() {
				int ticks = 0;

				@Override
				public void run() {
					if (ticks == 2) {
						TellAll(main.color("&c&lIce Dragon> &cBWAGHHHH! HOW DID YOU SURVIVE?!?!?"));
						main.finalEvent = true;
					} else if (ticks == 5) {
						TellAll(main.color("&c&lIce Dragon> &cYou have no idea what you've gotten yourself into"));
						
						for (Player p : Bukkit.getOnlinePlayers())
							main.LobbyBoard(p);
					} else if (ticks == 10) {
						TellAll(main.color("&c&lIce Dragon> &cTryna save your precious server?? WELL HOW'S THIS!"));
						
						for (Player p : Bukkit.getOnlinePlayers())
							p.setGameMode(GameMode.SPECTATOR);
					} else if (ticks == 12) {
						Location loc = new Location(main.getLobbyWorld(), 504.917, 165, -497.944);

						for (Player p : Bukkit.getOnlinePlayers())
							p.teleport(loc);
					} else if (ticks == 20) {
						Location loc = new Location(main.getLobbyWorld(), 504.917, 158, -497.944);

						main.getLobbyWorld().strikeLightning(loc);
						TNTPrimed tnt = main.getLobbyWorld().spawn(loc.add(0, 0, 0), TNTPrimed.class);
						tnt.setFuseTicks(0);
					} else if (ticks == 28) {
						TellAll(main.color("&c&lIce Dragon> &cYou wanna see some more?"));
						Location loc = new Location(main.getLobbyWorld(), 1499.488, 189, 983.501);

						for (Player p : Bukkit.getOnlinePlayers())
							p.teleport(loc);
					} else if (ticks == 30) {
						Location loc = new Location(main.getLobbyWorld(), 1499.488, 181, 983.501);

						main.getLobbyWorld().strikeLightning(loc);
						TNTPrimed tnt = main.getLobbyWorld().spawn(loc.add(0, 0, 0), TNTPrimed.class);
						tnt.setFuseTicks(0);
					} else if (ticks == 40) {
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.teleport(new Location(main.getLobbyWorld(), 12.488, 153, 26.499));
							p.setGameMode(GameMode.CREATIVE);
						}
						
						TellAll(main.color(
								"&c&lIce Dragon> &cYou shouldn't have played with fire.... Hope you enjoyed your time here"));
					} else if (ticks == 43) {
						loadFirst();
					} else if (ticks == 100) {
						event = null;
						this.cancel();
					}

					ticks++;
				}
			};
			event.runTaskTimer(main, 0, 20);
		}
	}
	
	private void loadFirst() {
		stages.put(1, 1);
		Bukkit.getScheduler().runTask(main, () -> {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				Vector v = ((loc2.toVector().subtract(pl.getLocation().toVector())).multiply(-1)).normalize();
				Vector ve = (v.setY((v.getY() * -1) + 20)).multiply(20);
				pl.setVelocity(ve);
				Bukkit.getScheduler().runTaskLater(main, () -> {
					pl.setGameMode(GameMode.CREATIVE);
				}, 50L);
			}
		});
		loc1.getWorld().playSound(loc1, Sound.ENDERDRAGON_GROWL, 10000000, 1);
		BukkitTask t1 = Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
			Location clone;
			clone = loc1.clone();
			clone.add(vec1);
			clone.add(vec1);
			int i = stages.get(1);
			for (int b = 0; b < i; b++) {
				clone.add(vec1);
				PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME, true,
						(float) clone.getX(), (float) clone.getY(), (float) clone.getZ(), 0.2F, 0.2F, 0.2F, 0F, 20);
				Bukkit.getOnlinePlayers()
						.forEach(a -> ((CraftPlayer) a).getHandle().playerConnection.sendPacket(packet));
			}
			if ((int) clone.getX() != 1) {
				stages.put(1, i + 1);
			}
			if ((int) clone.getX() == 1) {
				if (vec == null) {
					vec = loc2.toVector().subtract(clone.toVector()).normalize();
					loadsecond(clone.clone());
				}
			}
		}, 0L, 5L);
		tasks.add(t1);
	}

	private void loadsecond(Location loc) {
		tasks.forEach(a -> a.cancel());
		stages.put(2, 1);
		BukkitTask t2 = Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
			Location clone = loc.clone();
			for (int z = 0; z < stages.get(2); z++) {
				clone.add(vec);
				PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.FLAME, true,
						(float) clone.getX(), (float) clone.getY(), (float) clone.getZ(), 0.2F, 0.2F, 0.2F, 0.4F, 20);
				Bukkit.getOnlinePlayers()
						.forEach(a -> ((CraftPlayer) a).getHandle().playerConnection.sendPacket(packet));
			}
			clone.getWorld().playSound(clone, Sound.FIREWORK_BLAST, 10000, 1);
			if (stages.get(2) == 70) {
				explode();
			}
			stages.put(2, stages.get(2) + 1);
		}, 0L, 2L);
		tasks.add(t2);
	}

	private void explode() {
		tasks.forEach(a -> a.cancel());
		Bukkit.getScheduler().runTask(main, () -> {
			loc2.getWorld().createExplosion(loc2, 25);
		});
		Bukkit.getScheduler().runTask(main, () -> {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				Vector v = ((loc2.toVector().subtract(pl.getLocation().toVector())).multiply(-50)).normalize();
				Vector ve = (v.setY((v.getY() * -1) * 500)).multiply(20);
				pl.setVelocity(ve);
			}
		});
	}

	private void TellAll(String msg) {
		for (Player p : Bukkit.getOnlinePlayers())
			p.sendMessage(msg);
	}

}
