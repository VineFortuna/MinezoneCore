package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.Ability;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Game.projectile.ItemProjectile;
import anthony.SuperCraftBrawl.Game.projectile.ProjectileOnHit;
import anthony.util.ItemHelper;
import anthony.util.SoundManager;
import anthony.util.TitleHelper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.texture.ItemTexture;

import java.awt.Color;
import java.util.Random;

public class SpiderClass extends BaseClass {

	private ItemStack weapon;
	private ItemStack webItem;
	private final Ability mutateAbility = new Ability("&c&lMutate", 6, player);
	private final Ability webAbility = new Ability("&f&lWeb", 4, player);
	private static final double WEB_ABILITY_DURATION = 2;
	private static final int MAX_WEB_AMOUNT = 4;
	private static final double WEB_PLAYER_RADIUS = 1.0;
	private final PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 6 * 20, 1, false, true);
	private final PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6 * 20, 0, false, true);
	private final PotionEffect regen = new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 1, false, true);
	private final PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, 6 * 20, 0, false, false);
	private final PotionEffect poison = new PotionEffect(PotionEffectType.POISON, 4 * 20, 0, false, true);
	private static final int VENOM_ABILITY_DURATION = 6;
	public int invisTaskId;
	private int venomTaskId;

	public SpiderClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg3YTk2YThjMjNiODNiMzJhNzNkZjA1MWY2Yjg0YzJlZjI0ZDI1YmE0MTkwZGJlNzRmMTExMzg2MjliNWFlZiJ9fX0=",
				"4C453B",
				"5C0000",
				"4C453B",
				6,
				"Spider"
		);

		initializeItems();
	}

	private void initializeItems() {
		// Weapon
		weapon = ItemHelper.setDetails(
				new ItemStack(Material.SPIDER_EYE),
				"&c&lEye",
				"",
				"&7Mutate to gain one of these effects:",
				"&7▶ &b&oSpeed &e" + (speed.getAmplifier() + 1) + " &7for &e" + speed.getDuration() / 20 + "s",
				"&7▶ &2&oVenom &7for &e" + poison.getDuration() / 20 + "s",
				"&7▶ &4&oStrength &e" + (strength.getAmplifier() + 1) + " &7for &e" + strength.getDuration() / 20 + "s",
				"&7▶ &d&oRegeneration &e" + (regen.getAmplifier() + 1) + " &7for &e" + regen.getDuration() / 20 + "s",
				"&7▶ &f&oInvisibility &e" + (invis.getAmplifier() + 1) + " &7for &e" + invis.getDuration() / 20 + "s"
		);
		weapon.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 3);

		// Web Ability
		String durationDisplay = ItemHelper.formatDouble(WEB_ABILITY_DURATION);

		webItem = ItemHelper.setDetails(
				new ItemStack(Material.STRING),
				webAbility.getAbilityNameRightClickMessage(),
				"&7Throw an attachable cobweb to trap enemies",
				"",
				"&7Duration: &a" + durationDisplay + "s"
		);
	}

	@Override
	public void DoDamage(EntityDamageByEntityEvent event) {
		if (!isPlayerAlive()) return;
		if (venomTaskId == -1) return;

		ItemStack heldItem = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
		boolean isWeaponMelee =
				event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
						&& heldItem != null
						&& heldItem.equals(weapon);

		if (!isWeaponMelee) return;
		Entity damagedEntity = event.getEntity();
		if (!(damagedEntity instanceof Player)) return;

		Player damagedPlayer = (Player) damagedEntity;
		damagedPlayer.addPotionEffect(poison);
	}

	@Override
	public void Tick(int gameTicks) {
		if (!isPlayerAlive()) return;
		handleAddingWebItem();
		// ActionBar
		ActionBarManager actionBarManager = this.getActionBarManager();
		ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(this, actionBarManager);
		abilityActionBar.setActionBarAbilityWhite(player, mutateAbility, webAbility);
	}

	private void handleAddingWebItem() {
		ItemStack item = player.getInventory().getItem(1);
		if (item == null) return;

		// Handle barrier case
		if (item.getType() == Material.BARRIER && webAbility.isReady()) {
			player.getInventory().setItem(1, webItem.clone());
			webAbility.use();
			SoundManager.playItemPickup(player);
			return;
		}

		// Only proceed if it's a web item
		if (!item.isSimilar(webItem)) return;

		// Add web if we have less than max and timer has passed
		int currentAmount = item.getAmount();
		if (currentAmount < MAX_WEB_AMOUNT && webAbility.isReady()) {
			int newAmount = currentAmount + 1;
			item.setAmount(newAmount);
			SoundManager.playItemPickup(player);
			if (newAmount == MAX_WEB_AMOUNT) return;
			webAbility.use();
		}
	}

	@Override
	public void SetItems(Inventory playerInv) {
		// Resetting Dance Ability CD
		mutateAbility.getCooldownInstance().reset();
		webAbility.getCooldownInstance().reset();

		// Resetting Webs on Death
		ItemStack initialWebItem = webItem.clone();
		initialWebItem.setAmount(MAX_WEB_AMOUNT);

		playerInv.setItem(0, weapon);
		playerInv.setItem(1, initialWebItem);
	}

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		Action action = event.getAction();
		if (item == null) return;
		if (player.getGameMode() == GameMode.SPECTATOR) return;
		if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;
		if (item.equals(weapon)) {
			onMutateAbility();
			return;
		}
		if (item.isSimilar(webItem)) onWebAbility(item);
	}

	private void onMutateAbility() {
		if (!mutateAbility.isReady()) return;
		applyRandomEffect();
		mutateAbility.use();
		SoundManager.playSoundToAll(player, Sound.SPIDER_IDLE, 1, 1);
		spawnMutateParticles();
	}

	public void spawnMutateParticles() {
		Location loc = player.getLocation().add(0, 0.6, 0);
		ItemStack spiderEye = new ItemStack(Material.SPIDER_EYE);
		ItemTexture texture = new ItemTexture(spiderEye);
		Random random = new Random();

		for (int i = 0; i < 20; i++) {
			// Calculate random offsets in a spherical pattern around the player
			double radius = 0.3 + random.nextDouble() * 0.7; // 1.0 to 2.5 blocks from player
			double angle = random.nextDouble() * 2 * Math.PI; // Random angle around player

			// Convert polar coordinates to Cartesian offsets
			float offsetX = (float) (radius * Math.cos(angle));
			float offsetZ = (float) (radius * Math.sin(angle));
			float offsetY = (float) (random.nextDouble() * 1.8); // 0 to 2 blocks high

			Location particleLoc = loc.clone().add(offsetX, offsetY, offsetZ);

			ParticleEffect.ITEM_CRACK.display(
					particleLoc,
					0.1f, 0.1f, 0.1f,
					0.03f,
					1,
					texture
			);
		}
	}


	@Override
	public void Death(PlayerDeathEvent e) {
		super.Death(e);
		removeInvisibility();
		removeVenom();
	}

	private void applyRandomEffect() {
		Random random = new Random();
		int chance = random.nextInt(5) + 1;

		String effect = "";
		switch (chance) {
			case 1:
				player.addPotionEffect(speed);
				effect = "&bSpeed";
				break;
			case 2:
				player.addPotionEffect(strength);
				effect = "&4Strength";
				break;
			case 3:
				player.addPotionEffect(regen);
				effect = "&dRegen";
				break;
			case 4:
				addVenom();
				effect = "&2Venom";
				break;
			case 5:
				addInvisibility();
				effect = "&7Invis";
				break;
		}

		TitleHelper.sendTitle(player, "", effect, 0, 20, 10);
	}

	private void addVenom() {
		venomTaskId = Bukkit.getScheduler().runTaskTimer(
				instance.getGameManager().getMain(),
				this::spawnVenomParticles,
				0, // Delay
				10 // Repeat every x ticks
		).getTaskId();

		Bukkit.getScheduler().runTaskLater(
				instance.getGameManager().getMain(),
				() -> {
					if (venomTaskId != -1) {
						Bukkit.getScheduler().cancelTask(venomTaskId);
						venomTaskId = -1;
					}
				},
				VENOM_ABILITY_DURATION * 20
		).getTaskId();
	}

	private void spawnVenomParticles() {
		Location loc = player.getLocation().add(0, 0.6, 0);
		ParticleBuilder particleBuilder = new ParticleBuilder(ParticleEffect.REDSTONE);
		particleBuilder.setColor(Color.GREEN);
		particleBuilder.setOffset(0.1f, 0.1f, 0.1f);

		Random random = new Random();

		for (int i = 0; i < 5; i++) {
			// Calculate random offsets in a spherical pattern around the player
			double radius = 0.3 + random.nextDouble() * 0.7; // 1.0 to 2.5 blocks from player
			double angle = random.nextDouble() * 2 * Math.PI; // Random angle around player

			// Convert polar coordinates to Cartesian offsets
			float offsetX = (float) (radius * Math.cos(angle));
			float offsetZ = (float) (radius * Math.sin(angle));
			float offsetY = (float) (random.nextDouble() * 1.8); // 0 to 1.8 blocks high

			Location particleLoc = loc.clone().add(offsetX, offsetY, offsetZ);
			particleBuilder.setLocation(particleLoc);
			particleBuilder.display(instance.players);
		}
	}

	private void removeVenom() {
		if (venomTaskId != -1) {
			Bukkit.getScheduler().cancelTask(venomTaskId);
			venomTaskId = -1;
		}
	}

	private void addInvisibility() {
		removeInvisibility();
		player.addPotionEffect(invis);
		removeArmor();

		invisTaskId = Bukkit.getScheduler().runTaskLater(
				instance.getGameManager().getMain(),
				() -> {removeInvisibility(); setArmorBack();},
				invis.getDuration()
		).getTaskId();
	}

	private void removeArmor () {
		player.getEquipment().getHelmet().addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 8);
		player.getEquipment().setChestplate(null);
		player.getEquipment().setLeggings(null);
		player.getEquipment().setBoots(null);
	}

	private void setArmorBack() {
		if (player.getEquipment().getHelmet().getType() == Material.PUMPKIN) {
			ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
			pumpkin.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 6);
			player.getEquipment().setHelmet(pumpkin);
		} else player.getEquipment().setHelmet(playerHead);
		player.getEquipment().setChestplate(chestplate);
		player.getEquipment().setLeggings(leggings);
		player.getEquipment().setBoots(boots);
	}

	private void removeInvisibility() {
		if (invisTaskId != -1) {
			Bukkit.getScheduler().cancelTask(invisTaskId);
			invisTaskId = -1;
		}
	}

	private void onWebAbility(ItemStack webStack) {
		int amount = webStack.getAmount();
		if (amount <= 0) return;

		int remainingWebs = amount - 1;
		webStack.setAmount(remainingWebs);

		handleEmptyWebStackCase(remainingWebs);
		handleTriggeringWebCooldown(remainingWebs);
		throwWebProjectile();
		SoundManager.playSoundToAll(player, Sound.SPIDER_WALK, 1, 1);
	}

	private void handleEmptyWebStackCase(int remainingWebs) {
		if (remainingWebs == 0) {
			player.getInventory().setItem(1, new ItemStack(Material.BARRIER));
		}
	}

	private void handleTriggeringWebCooldown(int remainingWebs) {
		if (remainingWebs == MAX_WEB_AMOUNT - 1) {
			webAbility.use();
		}
	}

	private void throwWebProjectile() {
		ItemProjectile projectile = new ItemProjectile(instance, player, new ProjectileOnHit() {
			@Override
			public void onHit(Player playerHit) {
				if (playerHit != null && playerHit.getGameMode() != GameMode.SPECTATOR) {
					handleWebSpawn(playerHit.getLocation());
				} else {
					handleWebSpawn(this.getBaseProj().getEntity().getLocation());
				}
			}

		}, new ItemStack(Material.WEB));
		instance.getGameManager().getProjManager().shootProjectile(projectile, player.getEyeLocation(),
				player.getLocation().getDirection().multiply(1.5D));
	}

	private void handleWebSpawn(Location hitLoc) {
		// First check for nearby non-spectator players
		Player nearestPlayer = getNearestPlayer(hitLoc, WEB_PLAYER_RADIUS);
		if (nearestPlayer != null) {
			spawnWebAtPlayer(nearestPlayer);
			return;
		}

		// If no player found, spawn at block location
		spawnWebAtBlock(hitLoc);
	}

	private Player getNearestPlayer(Location location, double radius) {
		Player nearestPlayer = null;
		double nearestDistance = Double.MAX_VALUE;

		for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
			if (entity instanceof Player) {
				Player nearbyPlayer = (Player) entity;
				if (nearbyPlayer == player) continue;
				if (nearbyPlayer.getGameMode() == GameMode.SPECTATOR) continue;

				double distance = nearbyPlayer.getLocation().distance(location);
				if (distance < nearestDistance) {
					nearestPlayer = nearbyPlayer;
					nearestDistance = distance;
				}
			}
		}
		return nearestPlayer;
	}

	private void spawnWebAtPlayer(Player targetPlayer) {
		Location playerLoc = targetPlayer.getLocation().add(0, 1, 0);
		Block targetBlock = playerLoc.getBlock();

		// Try to find suitable block around player
		Block[] potentialBlocks = {
				targetBlock,
				targetBlock.getRelative(BlockFace.DOWN),
				targetBlock.getRelative(BlockFace.UP)
		};

		for (Block block : potentialBlocks) {
			if (block.getType() == Material.AIR) {
				setWebBlock(block);
				return;
			}
		}
	}

	private void spawnWebAtBlock(Location hitLoc) {
		Block hitBlock = hitLoc.getBlock();
		if (hitBlock == null) {
			return;
		}

		if (hitBlock.getType() == Material.AIR) {
			setWebBlock(hitBlock);
			return;
		}

		// Try to find suitable adjacent block
		BlockFace[] faces = {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
				BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

		for (BlockFace face : faces) {
			Block relativeBlock = hitBlock.getRelative(face);
			if (relativeBlock.getType() == Material.AIR) {
				setWebBlock(relativeBlock);
				return;
			}
		}
	}

	private void setWebBlock(Block webBlock) {
		webBlock.setType(Material.WEB);
		SoundManager.playSoundToAll(player, webBlock.getLocation(), Sound.DIG_WOOL, 1, 1);
		spawnWebParticles(webBlock, 20);

		Bukkit.getScheduler().runTaskLater(
				instance.getGameManager().getMain(),
				() -> {
					webBlock.setType(Material.AIR);
					SoundManager.playSoundToAll(player, webBlock.getLocation(), Sound.DIG_WOOL, 1, 0.5f);

					// Create multiple breaking particles
					spawnWebParticles(webBlock, 45);
				},
				(long) WEB_ABILITY_DURATION * 20
		);
	}

	private void spawnWebParticles(Block webBlock, int amount) {
		Location center = webBlock.getLocation().add(0.5, 0.5, 0.5);
		for (int i = 0; i < amount; i++) {
			double offsetX = (Math.random() - 0.5) * 0.7;
			double offsetY = (Math.random() - 0.5) * 0.7;
			double offsetZ = (Math.random() - 0.5) * 0.7;

			player.getWorld().playEffect(
					center.clone().add(offsetX, offsetY, offsetZ),
					Effect.TILE_BREAK,
					Material.WEB.getId()
			);
		}

	}

	@Override
	public ClassType getType() {
		return ClassType.Spider;
	}

	@Override
	public ItemStack getAttackWeapon() {
		return weapon;
	}
}
