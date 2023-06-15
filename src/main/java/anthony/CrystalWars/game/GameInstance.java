package anthony.CrystalWars.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import anthony.CrystalWars.game.classes.BaseClass;
import anthony.CrystalWars.game.classes.PlayerStuff;
import anthony.SuperCraftBrawl.npcs.MapNPC;
import anthony.SuperCraftBrawl.npcs.NPCManager;
import anthony.SuperCraftBrawl.worldgen.VoidGenerator;
import net.jitse.npclib.api.skin.Skin;

public class GameInstance {

	private GameManager gm;
	private Maps map;
	private List<Player> players;
	private List<Player> spectators;
	public Map<Player, String> team;
	public Map<Player, BaseClass> classes;
	public Map<Player, Boolean> crystal;
	public Set<Vector> blocksPlaced;
	private GameState state;
	private World mapWorld;
	private BukkitRunnable timer;
	private BukkitRunnable iron;
	private List<Location> genLocs;

	public GameInstance(GameManager gm, Maps map) {
		this.gm = gm;
		this.map = map;
		this.state = GameState.LOBBY;
		this.players = new ArrayList<Player>();
		this.spectators = new ArrayList<Player>();
		this.team = new HashMap<Player, String>();
		this.classes = new HashMap<Player, BaseClass>();
		this.crystal = new HashMap<Player, Boolean>();
		this.blocksPlaced = new HashSet<Vector>();
		this.genLocs = new ArrayList<Location>();
		this.initialiseMap();
		this.genLocs.add(new Location(mapWorld, 0.500, 66, 70.510));
	}

	public void initialiseMap() {
		WorldCreator w = new WorldCreator(map.getMapInstance().worldName).environment(World.Environment.NORMAL);
		w.generator(new VoidGenerator());
		mapWorld = Bukkit.getServer().createWorld(w);
	}

	public void addPlayer(Player player) {
		if (state == GameState.LOBBY || state == GameState.STARTING) {
			if (hasPlayer(player) == false) {
				players.add(player);
				gameLobby(player);
				checkForStart();

				if (!(team.containsValue("Blue"))) {
					team.put(player, "Blue");
				} else {
					team.put(player, "Red");
				}
				player.sendMessage(getManager().getMain().color("&2&l(!) &rYou have joined &r&l" + map.toString()));
				player.sendMessage(getManager().getMain().color("&2&l(!) &rYou have joined Team " + team.get(player)));
			}
		}
	}

	public void checkForStart() {
		// DELETE LATER
		for (Entity e : mapWorld.getEntities())
			if (!(e instanceof Player))
				e.remove();

		if (getPlayers().size() == 2)
			startTimer();
	}

	public void startTimer() {
		if (timer == null) {
			timer = new BukkitRunnable() {
				int ticks = 20;

				@Override
				public void run() {
					if (ticks == 20) {
						state = GameState.STARTING;
						TellAll(getManager().getMain().color("&2&l(!) &rGame starting in &e" + ticks + "s"));
					} else if (ticks == 0) {
						state = GameState.IN_PROGRESS;
						for (Player player : getPlayers()) {
							spawn(player);
							loadClasses(player);
							crystal.put(player, true);
							player.setAllowFlight(false);
						}

						ironGoldGen();
						spawnCrystal();
						spawnNpcs();
						TellAll(getManager().getMain().color("&2&l(!) &rGame started"));
						timer = null;
						this.cancel();
					}

					ticks--;
				}
			};
			timer.runTaskTimer(getManager().getMain(), 0, 20);
		}
	}

	public void spawnNpcs() {
		Skin skin = new Skin(
				"ewogICJ0aW1lc3RhbXAiIDogMTYzOTc5ODIxNjM4OCwKICAicHJvZmlsZUlkIiA6ICI1NWEyZTcyZTAxNjE0Yzg1YjE5MWEwYjIyN2ZmNzU4YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXRoQmxpbmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWEzZTg3OTcxYTZmYjhlM2Q5NzZjN2RhYjYzMjU3MmVlMjAwZDI4MmE0NGVkYTVkMGEyYTNjNDE1MjQ2NGQyZiIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTc5MTI3OTBmZjE2NGI5MzE5NmYwOGJhNzFkMGU2MjEyOTMwNDc3NmQwZjM0NzMzNGY4YTZlYWU1MDlmOGE1NiIKICAgIH0KICB9Cn0=",
				"tSB+tKhVQKKut1lm4VETD1l/ulDTI40F2f//L3d6pMZP2LGRp/B8BNbvqYjWdzY80aqw9t2zlvkAGQGFLOF1QPlCoA+OgG9kSS+OP7CZhM3fzTLjpIVyQQtKxCTJuh664Oj4JuzgfEYq/Yc7emUj7/asazPlNvgpY0BbS0pxXrhiN47UoNYjkxoBuyOX+uIkhmRzO+bQH1rRrMMdY6CYgubyz6QJXNJqg4zBgeRaUZHbtxlpy6FRcQPrvteacznCyfFa/cpkbbVLMPD9NQxCuB4bt29ygfVAgcO6+o5O/1JMJfA3yKpp0xuayZ6ILgIRzIBPP1943IAmegspjH8cs08Hhu7G9HuP7fyhvE3e8jMGzhhoeFmB4nZXDJxzT5ALsHjhzEzZFs3VazdiiaJy+7GwkqxQD6Y5NNUrMuHhvLo3nK9vs0kLjS/aQhBl4CkRIaqA3fJoE/ctpniJ5KVyaoSrF5YOn3QFp4qUg9JFr0KU7Ld3L2bcPwSFEIU7V3nIi7Kza9yx2JDOXOzmApfdDz1pYRB/g3D7WdVk1rRb/xPVUTlAjss4b9ijLhIHhcKGQyNl2dXPiIVnlx+FmJACnR40YZExLXEiFeutfUw1T8IP/Qqkg5158sKfqRPLwhvCqAjjqbDYtscPrNB1ApQCHTaL+UepPei+TFeHproVUHU=");
		NPCManager npc = this.getManager().getMain().getNPCManager();
		//npc.addNpcs(new MapNPC(npc, new Location(mapWorld, 21.442, 72.06250, -93.527, -90, 1), skin));
	}

	public void gameLobby(Player player) {
		Vector v = map.getMapInstance().lobbyLoc;
		Location loc = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
		player.teleport(loc);
	}

	public void spawn(Player player) {
		MapInstance i = map.getMapInstance();
		Vector v = new Vector(0, 100, 0);
		Location loc = null;

		if (team.get(player).equals("Blue")) {
			v = i.bluePos;
			loc = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
		} else if (team.get(player).equals("Red")) {
			v = i.redPos;
			loc = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
		}

		player.teleport(loc);
	}

	public void spawnCrystal() {
		Location loc = new Location(mapWorld, -29.533, 72, -81.031);
		mapWorld.spawnEntity(loc, EntityType.ENDER_CRYSTAL);
		loc = new Location(mapWorld, 30.457, 72, -81.031);
		mapWorld.spawnEntity(loc, EntityType.ENDER_CRYSTAL);
	}

	public void loadClasses(Player player) {
		BaseClass bc = new PlayerStuff(this, player);
		classes.put(player, bc);
		player.getInventory().clear();
		bc.loadEquipment();
		player.setHealth(20.0);
		player.setFoodLevel(20);
		player.setGameMode(GameMode.SURVIVAL);
	}

	public void ironGoldGen() {
		if (iron == null) {
			iron = new BukkitRunnable() {
				int ticks = 0;

				@Override
				public void run() {
					for (Location loc : genLocs) {
						if (ticks % 10 == 0) {
							ItemStack gold = new ItemStack(Material.GOLD_INGOT);
							mapWorld.dropItemNaturally(loc, gold);
						}
						ItemStack iron = new ItemStack(Material.IRON_INGOT);
						mapWorld.dropItemNaturally(loc, iron);
						ticks++;
					}
				}
			};
			iron.runTaskTimer(getManager().getMain(), 0, 20);
		}
	}

	public boolean isInBlue(Location loc) {
		MapInstance mapInstance = map.getMapInstance();
		Vector v = mapInstance.center;
		Location centre = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
		double boundsX = mapInstance.boundsX;
		double boundsZ = mapInstance.boundsZ;

		if (Math.abs(centre.getX() - loc.getX()) > boundsX)
			return false;
		if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
			return false;
		return true;
	}

	public boolean isInRed(Location loc) {
		MapInstance mapInstance = map.getMapInstance();
		Vector v = mapInstance.center2;
		Location centre = new Location(mapWorld, v.getX(), v.getY(), v.getZ());
		double boundsX = mapInstance.boundsX2;
		double boundsZ = mapInstance.boundsZ2;

		if (Math.abs(centre.getX() - loc.getX()) > boundsX)
			return false;
		if (Math.abs(centre.getZ() - loc.getZ()) > boundsZ)
			return false;
		return true;
	}

	public boolean hasPlayer(Player player) {
		return this.players.contains(player);
	}

	public void checkForWin() {
		Player alivePlayer = null;

		if (this.getPlayers().size() == 1) {
			alivePlayer = this.getPlayers().get(0);

			if (alivePlayer != null)
				winGame(alivePlayer);
		}
	}

	@SuppressWarnings("deprecation")
	public void winGame(Player winner) {
		this.state = GameState.FINISHED;
		winner.sendTitle(this.getManager().getMain().color("&e&lVICTORY"), "&eYou won the game!");
		Bukkit.broadcastMessage(this.getManager().getMain()
				.color("&2&l(!) &e" + winner.getName() + " &rjust won on &r&l" + this.map.toString()));
		getManager().getMain().ResetPlayer(winner);
		getManager().removeMap(map);

		for (Player spec : this.getSpectators())
			getManager().getMain().ResetPlayer(spec);
	}

	public void removePlayer(Player player) {
		if (getPlayers().contains(player)) {
			this.players.remove(player);

			if (getState() == GameState.LOBBY) {
				getManager().removeMap(this.map);
			} else if (getState() == GameState.STARTING) {
				if (this.timer != null) {
					state = GameState.LOBBY;
					this.timer.cancel();
					this.timer = null;
					TellAll(getManager().getMain().color("&c&l(!) &rGame start cancelled. Not enough players!"));
				}
			} else if (getState() == GameState.IN_PROGRESS) {
				TellAll(getManager().getMain().color("&2&l(!) &e" + player.getName() + " &chas left the game!"));
				checkForWin();
			}
		}
	}

	public void TellAll(String msg) {
		for (Player p : getPlayers())
			p.sendMessage(msg);
	}

	// Getter methods:

	public GameManager getManager() {
		return this.gm;
	}

	public List<Player> getPlayers() {
		return this.players;
	}

	public List<Player> getSpectators() {
		return this.spectators;
	}

	public String getTeam(Player player) {
		return this.team.get(player);
	}

	public GameState getState() {
		return this.state;
	}

	public World getMapWorld() {
		return this.mapWorld;
	}
}
