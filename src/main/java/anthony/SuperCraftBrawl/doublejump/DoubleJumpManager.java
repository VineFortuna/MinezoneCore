package anthony.SuperCraftBrawl.doublejump;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameState;
import anthony.SuperCraftBrawl.Game.classes.BaseClass;
import anthony.SuperCraftBrawl.playerdata.PlayerData;

public class DoubleJumpManager implements Listener {
    private final Core main;
    private final Map<UUID, DoubleJumpData> data = new HashMap<>();

    public DoubleJumpManager(Core main) {
        this.main = main;
        main.getServer().getPluginManager().registerEvents(this, main);
    }

    private double getVerticalVel(Player player) {
        GameInstance instance = main.getGameManager().GetInstanceOfPlayer(player);
        if (instance != null) {
            BaseClass bc = instance.classes.get(player);
            if (bc != null) {
                return bc.baseVerticalJump;
            }
        }
        return 1.0;
    }

    private DoubleJumpData getJumpData(Player player) {
        UUID id = player.getUniqueId();
        DoubleJumpData jumpData = data.get(id);
        if (jumpData == null) {
            jumpData = new DoubleJumpData();
            data.put(id, jumpData);
        }
        return jumpData;
    }

    @EventHandler
    public void OnToggleGameMode(PlayerGameModeChangeEvent event) {
        // Preserve original behavior: allow flight in SURVIVAL/ADVENTURE
        if (event.getNewGameMode() == GameMode.ADVENTURE || event.getNewGameMode() == GameMode.SURVIVAL) {
            event.getPlayer().setAllowFlight(true);
        }
    }

    @EventHandler
    public void OnPlayerLeave(PlayerQuitEvent event) {
        // Remove data when player leaves to prevent leaks
        data.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void OnToggleJump(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Preserve original: skip creative/spectator
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        GameInstance i = main.getGameManager().GetInstanceOfPlayer(player);
        PlayerData playerData = main.getDataManager().getPlayerData(player);

        // If not in a game and player has fly toggled on, don't treat it as double jump
        if (i == null && playerData != null && playerData.fly == 1) {
            return;
        }

        // In a started/ended game, prevent spectators or 0-life players from double jumping
        if (i != null && (i.state == GameState.STARTED || i.state == GameState.ENDED)) {
            BaseClass bc = i.classes.get(player);
            if (bc != null && bc.getLives() <= 0) {
                return;
            }
        } else {
            // If spectating a started/ended game and not in the lobby world, skip
            i = main.getGameManager().GetInstanceOfSpectator(player);
            if (i != null && (i.state == GameState.STARTED || i.state == GameState.ENDED)) {
                if (i.spectators.contains(player) && !player.getWorld().equals(main.getLobbyWorld())) {
                    return;
                }
            }
        }

        try {
            DoubleJumpData jumpData = getJumpData(player);

            if (jumpData.canDoubleJump) {
                // Consume the double jump and launch
                jumpData.canDoubleJump = false;

                double forwardsFactor = 0.0;
                double upFactor = getVerticalVel(player);

                if (player.getWorld().equals(main.getLobbyWorld())) {
                    forwardsFactor = 1.0;
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1, 1);
                } else {
                    player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 3);
                }

                Vector forwards = player.getLocation().getDirection().setY(0);
                double forwardsStrength = forwards.length();
                if (forwardsStrength != 0.0) {
                    forwards.normalize().multiply(forwardsFactor + forwardsStrength / 5.0D);
                }

                player.getWorld().playEffect(player.getLocation(), Effect.SNOWBALL_BREAK, 0);
                player.setVelocity(new Vector(forwards.getX(), upFactor, forwards.getZ()));
                player.getWorld().playEffect(player.getLocation(), Effect.SNOWBALL_BREAK, 0);
            } else {
                // If can't double jump, restore last known velocity (original behavior)
                player.setVelocity(jumpData.lastVelocity);
            }
        } catch (Exception e) {
            player.sendMessage(main.color("&c&l(!) &rError while double jumping. Please contact an Administrator"));
            e.printStackTrace();
        }

        // Always cancel so flight toggle doesn't engage vanilla flying
        event.setCancelled(true);
    }

    @EventHandler
    public void OnMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        DoubleJumpData jumpData = getJumpData(player);

        // Reset double jump when on ground
        if (player.isOnGround()) {
            jumpData.canDoubleJump = true;
        }

        // Track last velocity (clone to avoid accidental external mutation)
        Vector v = player.getVelocity();
        jumpData.lastVelocity = (v == null) ? new Vector(0, 0, 0) : v.clone();
    }
}