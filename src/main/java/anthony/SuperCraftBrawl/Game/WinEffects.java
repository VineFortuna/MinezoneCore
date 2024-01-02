package anthony.SuperCraftBrawl.Game;

import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

public class WinEffects {

	private Player player;
	private GameInstance gameInstance;
	private EnderDragon dragon;
	private boolean defaultEffect = false;

	public WinEffects(Player player, GameInstance gameInstance) {
		this.player = player;
		this.gameInstance = gameInstance;
	}

	public void checkWinEffect() { // Database checking here
		if (gameInstance != null) {
			PlayerData playerData = gameInstance.getGameManager().getMain().getPlayerDataManager().getPlayerData(player);

			if (playerData != null) {
				if (player.hasPermission("scb.winEffects")) {
					if (playerData.enderDragonEffect == 1)
						enderDragonEffect();
					else if (playerData.santaEffect == 1)
						santaEffect();
					else if (playerData.fireParticlesEffect == 1)
						fireParticlesEffect();
					else if (playerData.broomWinEffect == 1)
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
				gameInstance.getGameManager().getMain().color("&2&lMagic Broom"));
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
			if (player.getWorld() == gameInstance.getMapWorld()) {
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
				runnable.runTaskTimer(gameInstance.getGameManager().getMain(), 0, 20);
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
