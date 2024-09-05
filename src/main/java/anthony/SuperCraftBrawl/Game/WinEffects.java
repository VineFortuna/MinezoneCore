package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Random;

public class WinEffects {

	private Player player;
	private GameInstance i;
	private EnderDragon dragon;
	private boolean defaultEffect = false;
	private boolean rainEffect = false;
	private ArrayList<Item> fish = new ArrayList<>();

	public WinEffects(Player player, GameInstance i) {
		this.player = player;
		this.i = i;
	}

	public void checkWinEffect() { // Database checking here
		if (i != null) {
			PlayerData data = i.getGameManager().getMain().getDataManager().getPlayerData(player);

			if (data != null) {
				if (player.hasPermission("scb.winEffects")) {
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
					else
						defaultEffect();
				} else
					defaultEffect();
			}
		}
	}

	// ALL WIN EFFECTS:

	//This spawns an Ender Dragon at the player & makes the player ride it
	private void enderDragonEffect() {
		this.dragon = (EnderDragon) this.player.getWorld().spawnEntity(this.player.getLocation(),
				EntityType.ENDER_DRAGON);
		this.dragon.setPassenger(this.player);
	}

	private void magicBroomEffect() {
		ItemStack broom = ItemHelper.setDetails(new ItemStack(Material.WHEAT),
				i.getGameManager().getMain().color("&2&lMagic Broom"));
		player.getInventory().setItem(0, broom);
	}

	private ItemStack makeRed(ItemStack armour) { // FOR SANTA EFFECT
		LeatherArmorMeta lm = (LeatherArmorMeta) armour.getItemMeta();
		lm.setColor(Color.RED);
		armour.setItemMeta(lm);
		return armour;
	}

	private void santaEffect() {
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
		Location respawnLoc = i.GetRespawnLoc();
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
		if (player.getWorld() == i.getMapWorld()) {
			World w = player.getWorld();
			w.setStorm(true);
			w.setThundering(true);
			BukkitRunnable runnable = new BukkitRunnable() {
				int rep = 0;
				Random rand = new Random();
				@Override
				public void run() {
					if (rep == 240) {
						this.cancel();
					} else {
						int chance = rand.nextInt(4);
						Item i = player.getWorld().dropItem(getItemRainLoc(),
								new ItemStack(Material.RAW_FISH, 1, (short) chance));
						fish.add(i);
					}
					rep++;
				}
			};
			runnable.runTaskTimer(i.getGameManager().getMain(), 0, 1);
		}
	}

	private void defaultEffect() {
		this.defaultEffect = true;

		if (this.defaultEffect == true) {
			if (player.getWorld() == i.getMapWorld()) {
				BukkitRunnable runnable = new BukkitRunnable() {
					int sec = 0;

					@Override
					public void run() {
						if (sec == 9) {
							this.cancel();
						} else {
							Firework fw = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
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
						
						sec++;
					}
					
				};
				runnable.runTaskTimer(i.getGameManager().getMain(), 0, 20);
			}
		}
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
		}
	}
}
