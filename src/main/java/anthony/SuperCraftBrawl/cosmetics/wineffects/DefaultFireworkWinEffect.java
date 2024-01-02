package anthony.SuperCraftBrawl.cosmetics.wineffects;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import anthony.SuperCraftBrawl.cosmetics.types.WinEffect;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DefaultFireworkWinEffect extends WinEffect {
    public DefaultFireworkWinEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem) {
        super(name, rarity, description, displayItem);
    }

    @Override
    public void playWinEffect(Player player, GameInstance gameInstance) {
        if (player.getWorld() == gameInstance.getMapWorld()) {
            BukkitRunnable runnable = new BukkitRunnable() {
                int sec = 0;

                @Override
                public void run() {
                    if (sec == 9) {
                        this.cancel();
                    } else {
                        // Spawning Firework
                        Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
                        FireworkMeta fireworkMeta = firework.getFireworkMeta();
                        fireworkMeta.setPower(1);

                        // Randomizing Color
                        Color color;
                        Random random = new Random();
                        int randomNumber = random.nextInt(3 + 1);

                        switch (randomNumber) {
                            case 1:
                                color = Color.BLUE;
                                break;
                            case 2:
                                color = Color.LIME;
                                break;
                            case 3:
                                color = Color.GREEN;
                                break;
                            default:
                                color = Color.YELLOW;
                                break;
                        }

                        // Setting Firework Color and Effect
                        fireworkMeta.addEffect(FireworkEffect.builder().withColor(color).flicker(true).build());
                        firework.setFireworkMeta(fireworkMeta);
                    }

                    sec++;
                }

            };
            runnable.runTaskTimer(gameInstance.getGameManager().getMain(), 0, 20);
        }
    }
}
