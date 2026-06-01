package anthony.SuperCraftBrawl.cosmetics;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class CosmeticsManager implements Listener {

    private Core main;
    public List<Player> snowParticlePlayers = new ArrayList<Player>();

    public CosmeticsManager(Core main) {
        this.main = main;
        this.main.getServer().getPluginManager().registerEvents(this, main);
    }

    /*
     * This function deals with the Magic Broom cosmetic
     */
    @EventHandler
    public void magicBroom(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        PlayerData data = main.getDataManager().getPlayerData(player);
        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

        if ((player.getWorld() == main.getLobbyWorld())
                || (i != null && (i.state == GameState.WAITING || i.state == GameState.ENDED))) {
            if (item != null && item.getType() == Material.WHEAT
                    && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                double boosterStrength = 2.0;
                Vector vel = player.getLocation().getDirection().multiply(boosterStrength);
                player.setVelocity(vel);
                data.magicbroom = 1;
            }
        }
    }

    /*
     * This function deals with the Melon cosmetic
     */
    @EventHandler
    public void melon(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        PlayerData data = main.getDataManager().getPlayerData(player);
        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);

        if ((player.getWorld() == main.getLobbyWorld()) || (i != null && i.state == GameState.WAITING)) {
            if (item != null && item.getType() == Material.MELON) {
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    if (data.melon > 0) {
                        data.melon--;
                        main.getDataManager().saveData(data);
                        String msg = main.color("&9&l(!) &rYou have &e" + data.melon + " melons");
                        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}"),
                                (byte) 2);
                        CraftPlayer craft = (CraftPlayer) player;
                        craft.getHandle().playerConnection.sendPacket(packet);
                        player.playSound(player.getLocation(), Sound.EAT, 2, 1);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 110, 3));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 110, 3));
                        if (data.melon == 0)
                            player.getInventory().clear(player.getInventory().getHeldItemSlot());
                    }
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (this.snowParticlePlayers.contains(player) && player.getWorld() == main.getLobbyWorld()) {
            Location loc = player.getLocation().add(0, 0.2, 0);

            // Particle Settings
            EnumParticle particleType = EnumParticle.CLOUD; // Example: CLOUD looks like a snow effect
            boolean longDistance = false;
            float offsetX = 0.3f;
            float offsetY = 0.3f;
            float offsetZ = 0.3f;
            float speed = 0f;
            int count = 5;

            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleType, // The EnumParticle type
                    longDistance, // Long distance rendering
                    (float) loc.getX(), (float) loc.getY(), (float) loc.getZ(), offsetX, offsetY, offsetZ, speed,
                    count);

            // Send the packet to all online players, so everyone can see the trail
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) onlinePlayer).getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

}
