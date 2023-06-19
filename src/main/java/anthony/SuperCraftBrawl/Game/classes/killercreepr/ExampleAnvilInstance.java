package anthony.SuperCraftBrawl.Game.classes.killercreepr;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.classes.ClassType;
import anthony.SuperCraftBrawl.Timer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class ExampleAnvilInstance extends ClassInstance{
    private final Timer timer = new Timer();
    private int cooldownSec;
    private int num;
    private boolean used;
    public ExampleAnvilInstance(GameInstance gameInstance, UUID user, SuperClass superClass) {
        super(gameInstance, user, superClass);
    }

    @Override
    public boolean t(Player p, int gameTicks) {
        if (used) {
            if (p.isOnGround()) {
                this.used = false;
                List<Entity> players = p.getNearbyEntities(3.0, 1.0, 3.0);
                for (Entity e : players) {
                    if (e instanceof Player) {
                        Player gamePlayer = (Player) e;
                        if (gamePlayer != p) {
                            gamePlayer.setVelocity(new Vector(0, 1, 0).multiply(0.5D));
                            if (this.num >= 15) this.num = 15;

                            EntityDamageEvent damageEvent = new EntityDamageEvent(gamePlayer, EntityDamageEvent.DamageCause.VOID, this.num);
                            instance.getManager().getMain().getServer().getPluginManager().callEvent(damageEvent);
                            gamePlayer.damage(this.num, p);
                            this.num = 0; // To reset
                        }
                    }
                }
                for (Player gamePlayer : instance.players) {
                    gamePlayer.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    p.playEffect(p.getLocation(), Effect.TILE_BREAK, 1);
                }
            }
        }

        //Don't think this is needed.
        /*if (instance.classes.containsKey(p) && instance.classes.get(p).getType() == ClassType.Anvil
                && instance.classes.get(p).getLives() > 0) {
        }*/

        this.cooldownSec = (15000 - timer.getTime()) / 1000 + 1;

        if (timer.getTime() < 15000) {
            String msg = String.valueOf(ChatColor.RESET) + ChatColor.YELLOW + ChatColor.BOLD + "Goomba Stomp "
                    + ChatColor.RESET + " regenerates in: " + ChatColor.YELLOW + cooldownSec + "s";
            PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
                    (byte) 2);
            CraftPlayer craft = (CraftPlayer) p;
            craft.getHandle().playerConnection.sendPacket(packet);
        } else {
            String msg = ChatColor.RESET + "You can use " + ChatColor.YELLOW + ChatColor.BOLD + "Goomba Stomp";
            PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
                    (byte) 2);
            CraftPlayer craft = (CraftPlayer) p;
            craft.getHandle().playerConnection.sendPacket(packet);
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = getPlayer();
        if(!event.getPlayer().equals(p)) return;
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.ANVIL) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (timer.getTime() < 15000) {
                    int seconds = (15000 - timer.getTime()) / 1000 + 1;
                    event.setCancelled(true);
                    p.sendMessage(ChatColor.BOLD + "(!) " + ChatColor.RESET
                            + "Your Goomba Stomp is still on cooldown for " + ChatColor.YELLOW + seconds
                            + " more seconds ");
                    return;
                }
                timer.restart();
                if(p.isOnGround()){
                    p.sendMessage(
                            instance.getManager().getMain().color("&c&l(!) &rYou cannot use this on the ground!"));
                }else{
                    int y = 1;
                    //Simplified this
                    Block b = p.getLocation().getBlock();
                    while (true) {
                        Block b2 = p.getWorld().getBlockAt(b.getX(), b.getY() - y, b.getZ());

                        //Why 50?
                        if (b2.getY() <= 50) return;

                        if (b2.getType() != null && b2.getType() == Material.AIR) y++;
                        else break;
                    }
                    double maxHeight = 10.0; // Maximum height for maximum damage
                    double heightRatio = (double) y / maxHeight;
                    double damage = 20.0 * heightRatio;

                    if(damage > 20.0) damage = 20.0; // Cap the damage at 20.0

                    num = (int) Math.ceil(damage);
                    p.setVelocity(new Vector(0, -1.5, 0).multiply(1.0D));
                    p.playEffect(p.getLocation(), Effect.TILE_BREAK, 0);
                    used = true;
                }
            }
        }
    }
}
