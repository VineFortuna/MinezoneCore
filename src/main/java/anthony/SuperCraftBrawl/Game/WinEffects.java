package anthony.SuperCraftBrawl.Game;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class WinEffects {

	private Player player;
	private GameInstance i;
	private EnderDragon dragon;
	private boolean defaultEffect = false;

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
					else
						defaultEffect();
				} else
					defaultEffect();
			}
		}
	}

	// ALL WIN EFFECTS:

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
							int chance = r.nextInt(3);

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
		} else if (this.defaultEffect == true)
			this.defaultEffect = false;
	}
}
