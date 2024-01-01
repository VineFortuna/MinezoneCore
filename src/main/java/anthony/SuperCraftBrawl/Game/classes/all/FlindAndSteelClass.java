package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class FlindAndSteelClass extends BaseClass {

	private BukkitRunnable r;
	private boolean isUsed = false;
	private int initialLives = 0;
	private ItemStack flint = ItemHelper.addEnchant(
			ItemHelper.setDetails(new ItemStack(Material.FLINT), instance.getGameManager().getMain().color("&4&lFlint")),
			Enchantment.DAMAGE_ALL, 3);
	private ItemStack steel = ItemHelper.addEnchant(ItemHelper.setDetails(new ItemStack(Material.IRON_INGOT),
			instance.getGameManager().getMain().color("&b&lSteel")), Enchantment.KNOCKBACK, 2);

	public FlindAndSteelClass(GameInstance instance, Player player) {
		super(instance, player);
		baseVerticalJump = 1.1;
	}

	@Override
	public ClassType getType() {
		return ClassType.FlintAndSteel;
	}

	public ItemStack makeBlack(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.BLACK);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public ItemStack getAttackWeapon() {
		return this.steel;
	}

	@Override
	public void SetNameTag() {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void DoDamage2(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player) event.getEntity();
			if (instance.duosMap != null)
				if (instance.team.get(p).equals(instance.team.get(player)))
					return;

			if (flintUsed == true) {
				if (player.getInventory().getItemInHand().equals(steel)) {
					flintUsed = false;
					isUsed = false;
					BaseClass bc = instance.classes.get(player);
					if (bc != null)
						initialLives = bc.getLives();

					r.cancel();
					r = null;
					player.sendMessage(instance.getGameManager().getMain()
							.color("&2&l(!) &rYou made &4Flint&7And&bSteel&r! You have &e15 seconds &rto go nuts"));
					player.getInventory().remove(this.flint);
					player.getInventory().remove(this.steel);
					player.getInventory()
							.addItem(ItemHelper.addEnchant(
									ItemHelper.addEnchant(ItemHelper.addEnchant(new ItemStack(Material.FLINT_AND_STEEL),
											Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 2),
									Enchantment.FIRE_ASPECT, 1));
					
					for (Player gamePlayer : instance.players)
						gamePlayer.playEffect(player.getLocation(), Effect.BLAZE_SHOOT, 1);
					expire();
				}
			}
		}
	}

	public void expire() {
		if (r == null) {
			r = new BukkitRunnable() {
				int ticks = 15;

				@Override
				public void run() {
					BaseClass bc = instance.classes.get(player);
					if (bc != null) {
						if ((initialLives != bc.getLives()) || instance.state == GameState.ENDED) {
							flintUsed = false;
							isUsed = false;
							r = null;
							this.cancel();
						}
					}
					if (ticks == 0) {
						player.sendMessage(instance.getGameManager().getMain()
								.color("&2&l(!) &rYour &4Flint&7And&bSteel&r ran out of power!"));
						player.getInventory().remove(Material.FLINT_AND_STEEL);
						player.getInventory().addItem(flint);
						player.getInventory().addItem(getAttackWeapon());
						r = null;
						this.cancel();
						cooldown();
					}

					ticks--;
				}

			};
			r.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
		}
	}

	private void cooldown() {
		if (r == null) {
			r = new BukkitRunnable() {
				int ticks = 10;

				@Override
				public void run() {
					if (player.getGameMode() == GameMode.SPECTATOR || instance.state == GameState.ENDED) {
						r = null;
						this.cancel();
					}
					if (ticks == 0) {
						flintUsed = false;
						isUsed = false;
						r = null;
						this.cancel();
					} else {
						String msg = instance.getGameManager().getMain()
								.color("&9&l(!) &eAbility Cooldown: " + ticks + "s");
						PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
								(byte) 2);
						CraftPlayer craft = (CraftPlayer) player;
						craft.getHandle().playerConnection.sendPacket(packet);
					}

					ticks--;
				}

			};
			r.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
		}
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (r == null) {
			if (isUsed == false) {
				if (flintUsed == false) {
					if (event.getEntity() instanceof Player) {
						Player p = (Player) event.getEntity();
						if (instance.getGameManager().spawnProt.containsKey(p)
								|| instance.getGameManager().spawnProt.containsKey(player))
							return;
							
							if (instance.duosMap != null)
								if (instance.team.get(p).equals(instance.team.get(player)))
									return;

						if (player.getInventory().getItemInHand().equals(flint)) {
							flintUsed = true;
							isUsed = true;
							if (r == null) {
								r = new BukkitRunnable() {
									int ticks = 0;

									@Override
									public void run() {
										if (ticks == 2) {
											r = null;
											this.cancel();
											flintUsed = false;
											isUsed = false;
											return;
										}

										ticks++;
									}

								};
								r.runTaskTimer(instance.getGameManager().getMain(), 0, 50);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		flintUsed = false; // Default state
		isUsed = false; // Default state
		playerInv.setItem(0, this.flint);
		playerInv.setItem(1, this.getAttackWeapon());
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {

	}

}
