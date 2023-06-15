package anthony.CrystalWars.game.classes;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import anthony.CrystalWars.game.GameInstance;

public abstract class BaseClass {

	public GameInstance i;
	public Player player;

	public BaseClass(GameInstance i, Player player) {
		this.i = i;
		this.player = player;
	}

	public abstract void SetArmour(EntityEquipment playerEquip);

	public abstract void SetItems(Inventory playerInv);

	public void loadEquipment() {
		Inventory inv = player.getInventory();
		this.SetArmour(player.getEquipment());
		this.SetItems(inv);
	}

	public void PlayerDeath(Player p) {
		Player killer = p.getKiller();

		if (p != null && p.getGameMode() != GameMode.SPECTATOR) {
			String p1 = i.getManager().getMain().getRankManager().getRank(p).getTagWithSpace();
			if (killer != null) {
				String k = i.getManager().getMain().getRankManager().getRank(killer).getTagWithSpace();
				TellAll(i.getManager().getMain()
						.color("&2&l(!) &r" + p1 + p.getName() + " &cwas killed by &r" + k + killer.getName()));
			} else {
				TellAll(i.getManager().getMain().color("&2&l(!) &r" + p1 + p.getName() + " &cjust died SO badly"));
			}

			if (!(i.crystal.containsKey(p))) {
				player.setGameMode(GameMode.SPECTATOR);
				p.sendTitle(i.getManager().getMain().color("&cYou have died!"),
						i.getManager().getMain().color("&rYou are now a Spectator"));
				TellAll(i.getManager().getMain()
						.color("&2&l(!) &r&l" + i.team.get(p) + " Team &chas been eliminated!"));
				i.gameLobby(p);
				i.getPlayers().remove(p);
				i.team.remove(p);
				i.getSpectators().add(p);
				i.checkForWin();
			} else {
				player.setGameMode(GameMode.SPECTATOR);
				player.setHealth(20.0);
				i.gameLobby(p);
				BukkitRunnable r = new BukkitRunnable() {
					int ticks = 5;

					@Override
					public void run() {
						if (ticks == 0) {
							i.spawn(p);
							player.sendTitle(i.getManager().getMain().color("&e&lRespawned"), "");
							player.setGameMode(GameMode.SURVIVAL);
							player.setHealth(20.0);
							this.cancel();
						} else {
							player.sendTitle(i.getManager().getMain().color("&eRespawning In:"),
									i.getManager().getMain().color("&c") + ticks);
						}

						ticks--;
					}
				};
				r.runTaskTimer(i.getManager().getMain(), 0, 20);
			}
		}
	}

	private void TellAll(String msg) {
		for (Player gamePlayer : i.getPlayers())
			gamePlayer.sendMessage(msg);
	}

}
