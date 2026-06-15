package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.classes.HealTask;
import anthony.SuperCraftBrawl.Timer;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class EnderdragonClass extends BaseClass {

	private int cooldownSec;

	public Timer pearlTimer = new Timer();

	public EnderdragonClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.3;
		createArmor(
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2U0ZDM1YTJmNmJkNzM5NjA1ZmE0ZWFhNmUxNWQwMzgwMDlmM2YyMGIxYTIwNDYyODAxNjA1ODczNmU3Yjk1ZSJ9fX0=",
				6,
				"000000",
				"000000",
				"000000"
		);
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
	}

	@Override
	public void SetItems(Inventory playerInv) {
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.ENDER_PEARL, 5),
				"" + ChatColor.BLACK + ChatColor.BOLD + "Teleporters"));
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void Tick(int gameTicks) {
		if (!(player.getActivePotionEffects().contains(PotionEffectType.WEAKNESS)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 999999999, 1));

		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.EnderDragon
				&& instance.classes.get(player).getLives() > 0) {
			this.cooldownSec = (10000 - pearlTimer.getTime()) / 1000 + 1;

			if (pearlTimer.getTime() < 10000) {
				String msg = instance.getGameManager().getMain()
						.color("&c&lTeleporter &rregenerates in: &e" + this.cooldownSec + "s");
				getActionBarManager().setActionBar(player, "teleport.cooldown", msg, 2);
			} else {
				String msg = instance.getGameManager().getMain().color("&rYou can use &c&lTeleporter");
				getActionBarManager().setActionBar(player, "teleport.cooldown", msg, 2);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		/*
		 * ItemStack item = event.getItem(); if (item != null && item.getType() ==
		 * Material.STONE_SWORD && (event.getAction() == Action.RIGHT_CLICK_AIR ||
		 * event.getAction() == Action.RIGHT_CLICK_BLOCK)) { double boosterStrength =
		 * 1.4; for (Player gamePlayer : instance.players)
		 * gamePlayer.playSound(player.getLocation(), Sound.BAT_TAKEOFF, 1, 1); Vector
		 * vel = player.getLocation().getDirection().multiply(boosterStrength);
		 * player.setVelocity(vel); }
		 */
	}

	@Override
	public ClassType getType() {
		return ClassType.EnderDragon;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.setUnbreakable(new ItemStack(Material.STONE_SWORD));
		return item;
	}

	@Override
	public void killEvent(Player damagerPlayer) {
		if (instance.classes.containsKey(damagerPlayer) && !checkIfDead(player, instance)) {
			Location pLoc = damagerPlayer.getLocation();
			EnderCrystal crystal = (EnderCrystal) pLoc.getWorld().spawnEntity(pLoc, EntityType.ENDER_CRYSTAL);
			HealTask task = new HealTask(damagerPlayer, crystal, instance.getGameManager().getMain());
			BukkitTask bukkit = Bukkit.getScheduler().runTaskTimerAsynchronously(instance.getGameManager().getMain(), task, 0, 20L);
			task.set(bukkit);
		}
	}
}

class HealTask implements Runnable {
	private EnderCrystal crystal;
	private Player p;
	private int count;
	private Core main;
	private int expired;
	private BukkitTask te;

	public HealTask(Player p, EnderCrystal crystal, Core main) {
		this.crystal = crystal;
		this.p = p;
		count = 0;
		this.main = main;
		expired = 0;
	}

	public void set(BukkitTask te){
		this.te = te;
	}

	@Override
	public void run() {
		if (expired == 0){
			expired = 1;
			expireCrystal();
		}
		Location pLoc = p.getLocation();
		Location kLoc = crystal.getLocation();
		kLoc.setY(kLoc.getY() + 1);
		Vector vec = pLoc.toVector().subtract(kLoc.toVector()).normalize();
		vec = vec.divide(new Vector(5,5,5));
		Location clone = kLoc.clone();
		if (clone.distance(pLoc) > 10) {
			return;
		}
		PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.ENCHANTMENT_TABLE, true,
				(float) clone.getX(), (float) clone.getY(), (float) clone.getZ(), 0F, 0F, 0F, 0F, 3);
		for (Player pl : Bukkit.getOnlinePlayers()) {
			((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet);
		}
		for (int i = 0; i < 200; i++) {
			clone.add(vec);
			packet = new PacketPlayOutWorldParticles(EnumParticle.ENCHANTMENT_TABLE, true, (float) clone.getX(),
					(float) clone.getY(), (float) clone.getZ(), 0F, 0F, 0F, 0F, 3);
			for (Player pl : Bukkit.getOnlinePlayers()) {
				((CraftPlayer) pl).getHandle().playerConnection.sendPacket(packet);
			}
			if (((int) clone.distance(pLoc)) == 0) {
				return;
			}
			if (count < 3){
				if (p.getHealth() < 20) {
					p.setHealth(Math.min(p.getHealth() + 1, p.getMaxHealth()));
					count++;
				}
			}
		}

	}

	private void expireCrystal(){
		Bukkit.getScheduler().runTaskLater(main, () -> {
			if (crystal != null){
				crystal.remove();
			}
			if (te == null){
				System.out.println("Oh ur in fucking trouble theres no way to cancel this task");
				return;
			}
			te.cancel();
		}, 20*3);
	}

}
