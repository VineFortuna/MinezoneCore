package anthony.SuperCraftBrawl.Game.classes.all;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.SuperCraftBrawl.ItemHelper;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;

public class BedrockClass extends BaseClass {

	private BukkitRunnable lava;
	private BukkitRunnable bedrock;
	private List<Material> blockList;
	private List<Block> blockList2;
	private boolean used;
	private int cooldownSec;

	public BedrockClass(GameInstance instance, Player player) {
		super(instance, player);
		this.blockList = new ArrayList<>();
		this.blockList2 = new ArrayList<>();
	}

	@Override
	public ClassType getType() {
		return ClassType.Bedrock;
	}

	public ItemStack makeBlack(ItemStack armor) {
		LeatherArmorMeta lm = (LeatherArmorMeta) armor.getItemMeta();
		lm.setColor(Color.BLACK);
		armor.setItemMeta(lm);
		return armor;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		playerEquip.setHelmet(new ItemStack(Material.BEDROCK));
		playerEquip.setChestplate(makeBlack(ItemHelper.addEnchant(new ItemStack(Material.LEATHER_CHESTPLATE),
				Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
		playerEquip.setLeggings(makeBlack(new ItemStack(Material.LEATHER_LEGGINGS)));
		playerEquip.setBoots(makeBlack(
				ItemHelper.addEnchant(new ItemStack(Material.LEATHER_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 4)));
	}

	@Override
	public void SetNameTag() {

	}

	@Override
	public void SetItems(Inventory playerInv) {
		this.bedrockInvincibility = false; // To reset each life
		this.used = false; // Same here
		playerInv.setItem(0, this.getAttackWeapon());
		playerInv.setItem(1, ItemHelper.setDetails(new ItemStack(Material.LAVA_BUCKET), "", "",
				instance.getManager().getMain().color("&7Right click to set lava on opponents!")));
	}

	@Override
	public void Tick(int gameTicks) {
		this.cooldownSec = (10000 - bedrockLava.getTime()) / 1000 + 1;

		if (bedrockLava.getTime() < 10000) {
			String msg = instance.getManager().getMain()
					.color("&6&lBedrock Lava &rregenerates in: &e" + this.cooldownSec + "s");
			PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
			CraftPlayer craft = (CraftPlayer) player;
			craft.getHandle().playerConnection.sendPacket(packet);
		} else {
			if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Bedrock
					&& instance.classes.get(player).getLives() > 0) {
				String msg = instance.getManager().getMain().color("&rYou can use &6&lBedrock Lava");
				PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
						(byte) 2);
				CraftPlayer craft = (CraftPlayer) player;
				craft.getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();

		if (item != null && item.getType() == Material.BEDROCK
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (this.used != false) {
				player.sendMessage(instance.getManager().getMain()
						.color("&c&l(!) &rYou have already used your invincibility for this life!"));
				return;
			}
			BaseClass bc = instance.classes.get(player);
			bc.bedrockInvincibility = true;
			player.sendMessage(instance.getManager().getMain().color(
					"&6&l(!) &rYou are now Invincible for &e10 seconds &r, but you are unable to hit other players"));
			this.used = true;

			if (bedrock == null) {
				bedrock = new BukkitRunnable() {
					int ticks = 10;

					@Override
					public void run() {
						if (ticks == 0) {
							bedrock = null;
							this.cancel();
							bedrockInvincibility = false;
						}

						ticks--;
					}
				};
				bedrock.runTaskTimer(instance.getManager().getMain(), 0, 20);
			}
		} else if (item != null && item.getType() == Material.LAVA_BUCKET
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			if (bedrockLava.getTime() < 10000) {
				int seconds = (10000 - bedrockLava.getTime()) / 1000 + 1;
				event.setCancelled(true);
				player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
						+ "Your Lava is still on cooldown for " + ChatColor.YELLOW + seconds + "s");
			} else {
				bedrockLava.restart();
				player.getInventory().clear(player.getInventory().getHeldItemSlot());
				for (Player gamePlayer : instance.players) {
					if (gamePlayer != player) {
						if (instance.classes.containsKey(gamePlayer)
								&& instance.classes.get(gamePlayer).getLives() > 0) {
							if (gamePlayer.getGameMode() != GameMode.SPECTATOR) {
								if (instance.duosMap != null) {
									if (!(instance.team.get(gamePlayer).equals(instance.team.get(player)))) {
										Block block = instance.getMapWorld().getBlockAt(
												gamePlayer.getLocation().getBlockX(),
												gamePlayer.getLocation().getBlockY() + 1,
												gamePlayer.getLocation().getBlockZ());
										blockList.add(block.getType());
										blockList2.add(block);
										if (!(block.getType() == Material.STATIONARY_WATER))
											block.setType(Material.LAVA);
									}
								} else {
									Block block = instance.getMapWorld().getBlockAt(
											gamePlayer.getLocation().getBlockX(),
											gamePlayer.getLocation().getBlockY() + 1,
											gamePlayer.getLocation().getBlockZ());
									blockList.add(block.getType());
									blockList2.add(block);
									if (!(block.getType() == Material.STATIONARY_WATER))
										block.setType(Material.LAVA);
								}
							}
						}
					}

					if (lava == null) {
						lava = new BukkitRunnable() {
							int ticks = 1;

							@Override
							public void run() {
								if (ticks == 0 || instance.state == GameState.ENDED) {
									this.cancel();
								}
								ticks--;
							}

							@Override
							public void cancel() {
								int count = 0;
								for (Block blocks : blockList2) {
									blocks.setType(blockList.get(count));
									count++;
								}
								lava = null;
							}
						};
						lava.runTaskTimer(instance.getManager().getMain(), 0, 20);
					}
				}
			}
		}
	}

	@Override
	public void GameEnd() {
		if (lava != null) {
			this.bedrockInvincibility = false;
			lava.cancel();
		}
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = ItemHelper.addEnchant(ItemHelper.addEnchant(
				ItemHelper.setDetails(new ItemStack(Material.BEDROCK), ChatColor.BLACK + "Bedrock"),
				Enchantment.DAMAGE_ALL, 3), Enchantment.KNOCKBACK, 1);
		return item;
	}

}
