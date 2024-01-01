package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BedrockClass extends BaseClass {

	private ItemStack weapon;
	private ItemStack lavaBucket;
	private Ability invincibility = new Ability("Invincibility", 25, player);
	private int invincibilityDuration = 3; // 3 Seconds Duration

	private ItemStack[] originalArmor;

//	private Ability lavaAbility = new Ability("Lava Ability", player);

	private BukkitRunnable lava;
	private BukkitRunnable bedrock;
	private List<BlockState> blockList;
	private List<Block> blockList2;
	private int lavaCooldownSec;

	public BedrockClass(GameInstance instance, Player player) {
		super(instance, player);
		this.blockList = new ArrayList<>();
		this.blockList2 = new ArrayList<>();
	}

	@Override
	public ClassType getType() {
		return ClassType.Bedrock;
	}

	@Override
	public void SetArmour(EntityEquipment playerEquip) {
		// Head (helmet)
		ItemStack playerHead = ItemHelper.create(Material.BEDROCK, "&0Bedrock Head");

		// Chestplate
		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.BLACK, "&0Bedrock Chestplate");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Leggings
		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.BLACK,"&0Bedrock Leggings");

		// Boots
		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.BLACK, "&0Bedrock Boots");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Setting Armor
		playerEquip.setHelmet(playerHead);
		playerEquip.setChestplate(chestplate);
		playerEquip.setLeggings(leggings);
		playerEquip.setBoots(boots);
	}

	public void modifyArmorDuringInvincibility() {
		// Getting original armor
		originalArmor = player.getInventory().getArmorContents();

		// Invincibility armor
		// Chestplate
		ItemStack chestplate = ItemHelper.createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE, "&0Bedrock Chestplate");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Leggings
		ItemStack leggings = ItemHelper.createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE, "&0Bedrock Leggings");

		// Boots
		ItemStack boots = ItemHelper.createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE, "&0Bedrock Boots");
		chestplate.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

		// Setting Armor
		player.getInventory().setChestplate(chestplate);
		player.getInventory().setLeggings(leggings);
		player.getInventory().setBoots(boots);
	}

	public void restoreOriginalArmor() {
		player.getInventory().setArmorContents(originalArmor);
	}

	@Override
	public void SetNameTag() {

	}

	// Setting items
	@Override
	public void SetItems(Inventory playerInv) {
		this.bedrockInvincibility = false; // To reset each life

		// Weapon
		ItemStack weapon = ItemHelper.create(Material.BEDROCK, ChatColor.BLACK + "Bedrock");
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3); // Sharpness 3
		weapon.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1); // Knockback 1

		this.weapon = weapon;

		// Lava
		ItemStack lavaBucket = ItemHelper.create(Material.LAVA_BUCKET, ChatColor.GOLD + "Lava Bucket", ChatColor.GRAY + "Right click to set lava on opponents!");

		this.lavaBucket = lavaBucket;

		// Settings Items
		playerInv.setItem(0, weapon);
		playerInv.setItem(1, lavaBucket);
	}

	@Override
	public void Tick(int gameTicks) {
		this.lavaCooldownSec = (10000 - bedrockLava.getTime()) / 1000 + 1;

		if (bedrockLava.getTime() < 10000) {
			String msg = instance.getGameManager().getMain()
					.color("&6&lBedrock Lava &rregenerates in: &e" + this.lavaCooldownSec + "s");
			PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a("{\"text\":\"" + msg + "\"}"), (byte) 2);
			CraftPlayer craft = (CraftPlayer) player;
			craft.getHandle().playerConnection.sendPacket(packet);
		} else {
			if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Bedrock
					&& instance.classes.get(player).getLives() > 0) {
				String msg = instance.getGameManager().getMain().color("&rYou can use &6&lBedrock Lava");
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

		if (item != null) {
			if (player.getGameMode() != GameMode.SPECTATOR) {
				// BEDROCK INVINCIBILITY ABILITY
				if (item.equals(weapon)) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
						// If ability is on cooldown
						if (!invincibility.isReady()) {
							invincibility.sendPlayerRemainingCooldownChatMessage();
						}
						// If ability is available
						else {
							// Setting cooldown
							invincibility.use();
							// Sending return message
							invincibility.sendPlayerCustomUseAbilityChatMessage("&d&l(!) &rYou are now invincible for &e" + invincibilityDuration + " &rseconds");
							invincibility.sendPlayerCustomUseAbilityChatMessage("&d&l(!) &rYou are also unable to hit other players");
							// Setting invincibility
							BaseClass bc = instance.classes.get(player);
							bc.bedrockInvincibility = true;

							// Playing sound
							SoundManager.playSoundToAllGamePlayersFromAPlayerLocation(instance, player, Sound.ZOMBIE_INFECT, 20, 1);

							// Setting invincibility runnable
							if (bedrock == null) {
								bedrock = new BukkitRunnable() {
									// Setting invincibility/runnable duration
									int ticks = invincibilityDuration;
									boolean armorModified = false; // Flag: armor is modified

									@Override
									public void run() {
										// When invincibility is over
										if (ticks == 0) {
											bedrock = null;
											this.cancel();
											bedrockInvincibility = false;

											// Restoring armor to original
											if (armorModified) {
												restoreOriginalArmor();
											}

											// Playing sound when invincibility is over
											SoundManager.playSoundToAllGamePlayersFromAPlayerLocation(instance, player, Sound.ZOMBIE_UNFECT, 20, 1);
										} else {
											if (!armorModified) {
												// Modifying armor when invincibility starts
												modifyArmorDuringInvincibility();
												armorModified = true;
											}
										}

										ticks--;
									}
								};
								bedrock.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
							}
						}
					}
				}
				// LAVA ABILITY
				if (item.equals(lavaBucket)) {
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
						if (bedrockLava.getTime() < 10000) {
							int seconds = (10000 - bedrockLava.getTime()) / 1000 + 1;
							event.setCancelled(true);
							player.sendMessage("" + ChatColor.BOLD + "(!) " + ChatColor.RESET
									+ "Your Lava is still on cooldown for " + ChatColor.YELLOW + seconds + "s");
						} else {
							bedrockLava.restart();
							player.getInventory().clear(player.getInventory().getHeldItemSlot());
							for (Player gamePlayer : instance.players) {
								//if (gamePlayer != player) {
									if (instance.classes.containsKey(gamePlayer)
											&& instance.classes.get(gamePlayer).getLives() > 0) {
										if (gamePlayer.getGameMode() != GameMode.SPECTATOR) {
											if (instance.duosMap != null) {
												if (!(instance.team.get(gamePlayer)
														.equals(instance.team.get(player)))) {
													Block block = instance.getMapWorld().getBlockAt(
															gamePlayer.getLocation().getBlockX(),
															gamePlayer.getLocation().getBlockY() + 1,
															gamePlayer.getLocation().getBlockZ());
													blockList.add(block.getState());
													blockList2.add(block);
													block.setType(Material.LAVA);
												}
											} else {
												Block block = instance.getMapWorld().getBlockAt(
														gamePlayer.getLocation().getBlockX(),
														gamePlayer.getLocation().getBlockY() + 1,
														gamePlayer.getLocation().getBlockZ());
												
												blockList.add(block.getState());
												blockList2.add(block);
												block.setType(Material.LAVA);
											}
										//}
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
												BlockState blockState = blocks.getState();
												blockState.setType(blockList.get(count).getType());
												blockState.setData(blockList.get(count).getData());
												blockState.update(true);
												count++;
											}
											lava = null;
										}
									};
									lava.runTaskTimer(instance.getGameManager().getMain(), 0, 20);
								}
							}
						}
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
		return weapon;
	}
}
