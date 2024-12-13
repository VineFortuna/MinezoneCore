package anthony.SuperCraftBrawl.Game.classes.all;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.util.ItemHelper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class NoteblockClass extends BaseClass {
	
	private boolean sharp = false, fire = false, knock = false, speed = false, res = false;
	private ItemStack redstone = new ItemStack(Material.REDSTONE);
	private String notes = "";
	private final List<String> songs = Arrays.asList("ABD", "ACB", "BCD", "DAC", "CADB");
	int count = 0;

	public NoteblockClass(GameInstance instance, Player player) {
		super(instance, player);
		createArmor(
				null,
				"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlZWI3N2Q0ZDI1NzI0YTljYWYyYzdjZGYyZDg4Mzk5YjE0MTdjNmI5ZmY1MjEzNjU5YjY1M2JlNDM3NmUzIn19fQ==",
				"82533B",
				6,
				"Noteblock"
		);
	}

	@Override
	public ClassType getType() {
		return ClassType.Noteblock;
	}

	@Override
	public void setArmor(EntityEquipment playerEquip) {
		setArmorNew(playerEquip);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.removePotionEffect(PotionEffectType.JUMP);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 0));
	}

	@Override
	public void SetNameTag() {

	}

	private void noteList(Player player) {
		player.sendMessage("" + ChatColor.BOLD + "===============================");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "        " + ChatColor.YELLOW + ChatColor.BOLD
				+ ChatColor.UNDERLINE + "  Song List:");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  A, B, D: Sharpness III");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  A, C, B: Fire Aspect I");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  B, C, D: Knockback II");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  D, A, C: Speed II");
		player.sendMessage("" + ChatColor.BOLD + "|| " + "   " + ChatColor.YELLOW + "  C, A, D, B: Resistance I");
		player.sendMessage("" + ChatColor.BOLD + "||");
		player.sendMessage("" + ChatColor.BOLD + "===============================");
	}

	@Override
	public void SetItems(Inventory playerInv) {
		sharp = false;
		fire = false;
		knock = false;
		speed = false;
		res = false;
		player.getInventory()
				.setItem(0,
						ItemHelper
								.addEnchant(
										ItemHelper.setDetails(redstone,
												instance.getGameManager().getMain()
														.color("&rRedstone Dust &7(Right Click)")),
										Enchantment.DAMAGE_ALL, 2));
		redstone.removeEnchantment(Enchantment.DAMAGE_ALL);
		redstone.removeEnchantment(Enchantment.KNOCKBACK);
		redstone.removeEnchantment(Enchantment.FIRE_ASPECT);
		noteItems();
	}

	private void noteItems() {
		ItemStack a = new ItemStack(Material.NOTE_BLOCK);
		a.setDurability((short) 1);
		ItemStack b = new ItemStack(Material.NOTE_BLOCK);
		b.setDurability((short) 2);
		ItemStack c = new ItemStack(Material.NOTE_BLOCK);
		c.setDurability((short) 3);
		ItemStack d = new ItemStack(Material.NOTE_BLOCK);
		d.setDurability((short) 4);
		count = 0; // To reset count

		player.getInventory().setItem(1, ItemHelper.setDetails(a, instance.getGameManager().getMain().color("&eA")));
		player.getInventory().setItem(2, ItemHelper.setDetails(b, instance.getGameManager().getMain().color("&eB")));
		player.getInventory().setItem(3, ItemHelper.setDetails(c, instance.getGameManager().getMain().color("&eC")));
		player.getInventory().setItem(4, ItemHelper.setDetails(d, instance.getGameManager().getMain().color("&eD")));
		/*player.getInventory().setItem(5, ItemHelper.setDetails(new ItemStack(Material.BUCKET),
				instance.getGameManager().getMain().color("&2&lErase Your Work")));*/
	}
	
	public void Tick(int gameTicks) {
		if (instance.classes.containsKey(player) && instance.classes.get(player).getType() == ClassType.Noteblock
				&& instance.classes.get(player).getLives() > 0) {
			String msg = instance.getGameManager().getMain()
					.color("&rNotes played: &e&l" + notes);
			getActionBarManager().setActionBar(player, "noteblock.notes", msg, 2);
		}
		if (!(player.getActivePotionEffects().contains(PotionEffectType.SPEED)) && !res) {
			if (speed)
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
			else
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 0));
		}
		if (!(player.getActivePotionEffects().contains(PotionEffectType.JUMP)))
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 999999999, 0));
	}
	

	@Override
	public void UseItem(PlayerInteractEvent event) {
		ItemStack item = event.getItem();
		
		if (item != null && event.getAction().toString().contains("RIGHT_CLICK")) {
			if (item.getType() == Material.REDSTONE) {
				noteList(player); // Shows the notes to the player when right clicking melee
			}
			else if (item.getType() == Material.NOTE_BLOCK) {
				playNote(player.getInventory().getHeldItemSlot());
			} /*else if (item.getType() == Material.BUCKET) {
				player.sendMessage(
						instance.getGameManager().getMain().color("&r&l(!) &rYou have reset all of your work. Rip :("));
				clearNotes();
			}*/
		}
	}

	private void incorrectSong() {
		player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rWow, you're not that good are you?"));
		player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 0);
		clearNotes();
	}
	
	public void playNote(int slot) {
		switch (slot) {
			case 1:
				notes += "A";
				break;
			case 2:
				notes += "B";
				break;
			case 3:
				notes += "C";
				break;
			case 4:
				notes += "D";
				break;
		}
		if (!verifySong()) {
			incorrectSong();
		} else {
			count++;
			switch (count) {
				case 1:
					player.getInventory().setItem(slot, ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, (short) 5),
							instance.getGameManager().getMain().color("&e&lFirst Note")));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.0f);
					break;
				case 2:
					player.getInventory().setItem(slot, ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, (short) 14),
							instance.getGameManager().getMain().color("&e&lSecond Note")));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.1225f);
					break;
				case 3:
					player.getInventory().setItem(slot, ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, (short) 11),
							instance.getGameManager().getMain().color("&e&lThird Note")));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.2599f);
					break;
				case 4:
					player.getInventory().setItem(slot, ItemHelper.setDetails(new ItemStack(Material.WOOL, 1, (short) 1),
							instance.getGameManager().getMain().color("&e&lFourth Note")));
					player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1.3348f);
					break;
			}
		}
		
		for (int i = 0; i < songs.size(); i++) {
			if (songs.get(i).equals(notes)) {
				giveReward(i);
				clearNotes();
				break;
			}
		}
	}
	
	public void giveReward(int i) {
		switch (i) {
			case 0:
				if (!sharp) {
					player.getInventory().setItem(0, ItemHelper.addEnchant(redstone, Enchantment.DAMAGE_ALL, 3));
					player.sendMessage(instance.getGameManager().getMain()
							.color("&r&l(!) &rYour song skills rewarded you with &eSharpness 3 &ron your weapon"));
					sharp = true;
				} else
					player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rYou played this song already!"));
				break;
			case 1:
				if (!fire) {
					player.getInventory().setItem(0, ItemHelper.addEnchant(redstone, Enchantment.FIRE_ASPECT, 1));
					if (!(redstone.containsEnchantment(Enchantment.DAMAGE_ALL)))
						player.getInventory().setItem(0,
								ItemHelper.addEnchant(ItemHelper.addEnchant(redstone, Enchantment.FIRE_ASPECT, 1),
										Enchantment.DAMAGE_ALL, 2));
					player.sendMessage(instance.getGameManager().getMain()
							.color("&r&l(!) &rYour song skills rewarded you with &eFire Aspect 1 &ron your weapon"));
					fire = true;
				} else
					player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rYou played this song already!"));
				break;
			case 2:
				if (!knock) {
					player.getInventory().setItem(0, ItemHelper.addEnchant(redstone, Enchantment.KNOCKBACK, 2));
					if (!(redstone.containsEnchantment(Enchantment.DAMAGE_ALL)))
						player.getInventory().setItem(0, ItemHelper.addEnchant(
								ItemHelper.addEnchant(redstone, Enchantment.DAMAGE_ALL, 2), Enchantment.DAMAGE_ALL, 2));
					player.sendMessage(instance.getGameManager().getMain()
							.color("&r&l(!) &rYour song skills rewarded you with &eKnockback 2 &ron your weapon"));
					knock = true;
				} else
					player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rYou played this song already!"));
				break;
			case 3:
				if (!speed) {
					player.removePotionEffect(PotionEffectType.SPEED);
					if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
						player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
						player.sendMessage(instance.getGameManager().getMain().color(
								"&r&l(!) &rYour song skills rewarded you with &eSpeed 2&r, but unfortunately you had to give up Resistance 1 for it"));
						res = false;
					} else {
						player.sendMessage(instance.getGameManager().getMain()
								.color("&r&l(!) &rYour song skills rewarded you with &eSpeed 2"));
					}
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999999, 1));
					
				} else
					player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rYou played this song already!"));
				break;
			case 4:
				if (!res) {
					player.removePotionEffect(PotionEffectType.SPEED);
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999999, 0));
					player.sendMessage(instance.getGameManager().getMain().color(
							"&r&l(!) &rYour song skills rewarded you with &eResistance 1&r, but unfortunately you had to give up your Speed effect for it"));
					speed = false;
					res = true;
				} else
					player.sendMessage(instance.getGameManager().getMain().color("&r&l(!) &rYou played this song already!"));
				break;
		}
	}
	
	public boolean verifySong() {
		for (String song : songs) {
			if (notes.length() <= song.length()) {
				if (song.substring(0, notes.length()).equals(notes)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void clearNotes() {
		notes = "";
		count = 0;
		noteItems();
	}

	@Override
	public ItemStack getAttackWeapon() {
		ItemStack item = this.redstone;
		return item;
	}

}
