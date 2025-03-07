package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.Game.map.MapInstance;
import anthony.util.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class WinEffects {

	private Player player;
	private GameInstance instance;
	private EnderDragon dragon;
	private boolean defaultEffect = false;
	private boolean rainEffect = false;
	private boolean floodEffect = false;
	private ArrayList<Item> fish = new ArrayList<>();

	public WinEffects(Player player, GameInstance instance) {
		this.player = player;
		this.instance = instance;
	}

	public void checkWinEffect() { // Database checking here
		if (instance != null) {
			PlayerData data = instance.getGameManager().getMain().getDataManager().getPlayerData(player);

			if (data != null) {
				if (data.enderDragonEffect == 1)
					enderDragonEffect();
				else if (data.santaEffect == 1)
					santaEffect();
				else if (data.fireParticlesEffect == 1)
					fireParticlesEffect();
				else if (data.broomWinEffect == 1)
					magicBroomEffect();
				else if (data.fishRainEffect == 1)
					fishRainEffect();
				else if (data.floodEffect == 1)
					floodEffect();
				else
					defaultEffect();
			}
		}
	}

	// ALL WIN EFFECTS:

	//This spawns an Ender Dragon at the player & makes the player ride it
	private void enderDragonEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld()) return;
		startFireworksRunnable(world);
		this.dragon = (EnderDragon) world.spawnEntity(this.player.getLocation(), EntityType.ENDER_DRAGON);
		this.dragon.setPassenger(this.player);
	}

	private void magicBroomEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld()) return;
		startFireworksRunnable(world);
		ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT),"&2&lMagic Broom");
		player.getInventory().setItem(0, broom);
	}

	private ItemStack makeRed(ItemStack armour) { // FOR SANTA EFFECT
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.RED);
		armour.setItemMeta(lm);
		return armour;
	}

	private void santaEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld()) return;
		startFireworksRunnable(world);
		ItemStack playerskull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
		meta.setOwner("Santa");
		meta.setDisplayName("");
		playerskull.setItemMeta(meta);

		player.getInventory().setHelmet(playerskull);
		player.getInventory().setChestplate(makeRed(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		player.getInventory().setLeggings(makeRed(new ItemStack(Material.LEATHER_LEGGINGS)));
		player.getInventory().setBoots(makeRed(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));

		final Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
		horse.setTamed(true);
		horse.setOwner(player);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		player.teleport(horse.getLocation());
		horse.setPassenger(player);
	}

	private void fireParticlesEffect() {
		
	}
	
	public Location getItemRainLoc() {
		Random rand = new Random();
		int attempts = 0;
		Location respawnLoc = instance.GetRespawnLoc();
		while (true) {
			Location loc = respawnLoc.clone().add(rand.nextFloat() * 50 - 25, 35, rand.nextFloat() * 50 - 25);
			Location loc2 = loc.clone();
			while (true) {
				loc2.setY(loc2.getY() - 1);
				Material mat = loc2.getBlock().getType();
				if (mat.isSolid()) {
					return loc;
				}
				if (loc2.getY() < 40) // Too low down without finding block
					break;
			}
			if (attempts > 100)
				return respawnLoc.add(0, 35, 0);
			attempts++;
		}
	}

	private void fishRainEffect() {
		this.rainEffect = true;
		World world = player.getWorld();
		if (world != instance.getMapWorld()) return;
		startFireworksRunnable(world);
		world.setStorm(true);
		world.setThundering(true);
		BukkitRunnable runnable = new BukkitRunnable() {
			int rep = 0;
			Random rand = new Random();
			@Override
			public void run() {
				if (rep == 240) {
					this.cancel();
				} else {
					int chance = rand.nextInt(4);
					Item i = world.dropItem(getItemRainLoc(),
							new ItemStack(Material.RAW_FISH, 1, (short) chance));
					fish.add(i);
				}
				rep++;
			}
		};
		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 1);
	}

	private void floodEffect() {
		World world = player.getWorld();
		if (world != instance.getMapWorld()) return;
		startFireworksRunnable(world);
		world.setStorm(true);
		world.setThundering(true);

		// Define the bounds for flooding (center and size)
		MapInstance map = instance.getMap().GetInstance();
		Vector center = map.center.clone();
		double centerX = center.getX();
		double centerY = player.getLocation().clone().getY() - 5;
		double centerZ = center.getZ();
		double width = map.boundsX; // Width of the flooded area (along X axis)
		double length = map.boundsZ; // Length of the flooded area (along Z axis)

		final Boat boat = (Boat) player.getWorld().spawnEntity(player.getLocation(), EntityType.BOAT);
		player.teleport(boat.getLocation());
		boat.setPassenger(player);

		BukkitRunnable runnable = new BukkitRunnable() {
			int rep = 0;
			double y = centerY;
			@Override
			public void run() {
				if (rep == 120) {
					this.cancel();
				} else {
					// Loop through the blocks in the defined bounds
					for (int x = (int) (centerX - width); x <= centerX + width; x++) {
						for (int z = (int) (centerZ - length); z <= centerZ + length; z++) {
							Block block = world.getBlockAt(x, (int) y, z);
							Block blockBelow = block.getRelative(BlockFace.DOWN);

							if (blockBelow.getType() != Material.AIR && block.getType() == Material.AIR) {
								block.setType(Material.WATER);
							}
						}
					}
					y++;
					boat.teleport(boat.getLocation().add(0, 1, 0)); // Move boat up
				}
				rep++;
			}
		};

		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
	}

	private void defaultEffect() {
		this.defaultEffect = true;

		if (this.defaultEffect == true) {
			World world = player.getWorld();
			if (world != instance.getMapWorld()) return;
			startFireworksRunnable(world);
		}
	}

	private void startFireworksRunnable(World world) {
		BukkitRunnable runnable = new BukkitRunnable() {
			int sec = 0;

			@Override
			public void run() {
				if (sec == 9) {
					this.cancel();
				} else {
					playFireworks(world);
				}

				sec++;
			}

		};
		runnable.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
	}

	private void playFireworks(World world) {
		Firework fw = (Firework) world.spawnEntity(player.getLocation(), EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.setPower(1);

		Color c = null;
		Random r = new Random();
		int chance = r.nextInt(4);

		if (chance == 0)
			c = Color.BLUE;
		else if (chance == 1)
			c = Color.LIME;
		else if (chance == 2)
			c = Color.GREEN;
		else
			c = Color.YELLOW;
		fwm.addEffect(FireworkEffect.builder().withColor(c).flicker(true).build());
		fw.setFireworkMeta(fwm);
	}

	// REMOVE WIN EFFECTS:

	public void removeWinEffects() {
		if (this.dragon != null && !(this.dragon.isDead())) {
			this.dragon.remove();
		} else if (this.defaultEffect) {
			this.defaultEffect = false;
		} else if (this.rainEffect) {
			player.getWorld().setStorm(false);
			player.getWorld().setThundering(false);
			for (Item i : fish) {
				i.remove();
			}
			this.rainEffect = false;
		} else if (this.floodEffect) {
			player.getWorld().setStorm(false);
			player.getWorld().setThundering(false);
			this.floodEffect = false;
		}
	}
}
