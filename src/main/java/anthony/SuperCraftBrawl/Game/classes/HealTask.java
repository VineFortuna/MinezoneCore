package anthony.SuperCraftBrawl.Game.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Main;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;

public class HealTask implements Runnable {
	private EnderCrystal crystal;
	private Player p;
    private int count;
	private Main main;
    private int expired;
    private BukkitTask te;

	public HealTask(Player p, EnderCrystal crystal, Main main) {
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
                	p.setHealth(p.getHealth() + 1);
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
