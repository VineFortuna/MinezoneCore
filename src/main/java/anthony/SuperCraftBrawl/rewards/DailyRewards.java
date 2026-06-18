package anthony.SuperCraftBrawl.rewards;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DailyRewards {

    private Core core;
    private static final long DAILY_REWARD_COOLDOWN = 24 * 60 * 60 * 1000L;

    public DailyRewards(Core core) {
        this.core = core;
    }

    /**
     * Called when a player clicks the Daily Reward floating block.
     * Checks the 24-hour cooldown and either gives the reward or tells
     * the player how long they have to wait.
     */
    public void claimDailyReward(Player player) {
        PlayerData data = this.core.getDataManager().getPlayerData(player);
        if (data == null) return;

        UUID uuid = player.getUniqueId();

        // Stops double-clicks / duplicate interact events from giving multiple rewards at once
        if (!this.core.dailyRewardClaimsInProgress.add(uuid)) {
            return;
        }

        try {
            long now = System.currentTimeMillis();
            long elapsed = now - data.lastDailyReward;

            if (data.lastDailyReward > 0 && elapsed < DAILY_REWARD_COOLDOWN) {
                long remaining = DAILY_REWARD_COOLDOWN - elapsed;
                player.sendMessage(this.core.color("&c&l(!) &rYou already claimed your daily reward!"));
                player.sendMessage(this.core.color("&rNext reward available in: &a" + this.core.formatCooldown(remaining)));
                player.playSound(player.getLocation(), org.bukkit.Sound.NOTE_BASS, 1f, 0.5f);
                return;
            }

            // Lock the cooldown immediately so spam-clicking cannot claim again
            data.lastDailyReward = now;

            // Update the Daily Reward hologram immediately for this player
            if (this.core.dailyRewardEntry != null && this.core.floatingBlocks != null) {
                this.core.floatingBlocks.sendSubtitlePacket(player, this.core.dailyRewardEntry);

                // Send it again 1 tick later just in case Minecraft overwrites the name packet this tick
                Bukkit.getScheduler().runTaskLater(core, new Runnable() {
                    @Override
                    public void run() {
                        core.floatingBlocks.sendSubtitlePacket(player, core.dailyRewardEntry);
                    }
                }, 1L);
            }

            // Play the animation ONLY when the reward is actually claimed
            if (this.core.dailyRewardEntry != null) {
                this.core.floatingBlocks.playDailyRewardClaimAnimation(this.core.dailyRewardEntry);
            }

            // Give the reward here
            player.sendMessage(this.core.color("&8&m------------------------------"));
            player.sendMessage(this.core.color("&6&l★ DAILY REWARD CLAIMED! ★"));
            player.sendMessage(this.core.color(""));
            player.sendMessage(this.core.color("&a+20 Tokens"));
            player.sendMessage(this.core.color("&a+100 EXP"));
            player.sendMessage(this.core.color("&a+1 Mystery Chest"));
            player.sendMessage(this.core.color(""));
            player.sendMessage(this.core.color("&rCome back tomorrow for more!"));
            player.sendMessage(this.core.color("&8&m------------------------------"));
            player.playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1f, 1f);

            data.tokens += 20;
            data.exp += 100;
            data.mysteryChests++;

            this.core.getLevelManager().checkLevelUp(player); //Check if player should level up if has >= 2500 exp
            this.core.getScoreboardManager().lobbyBoard(player); //Resend lobby board to update
            this.core.getMysteryChestManager().removeAndAddMysteryStand(player); //Update mystery chest hologram
            this.core.getDataManager().saveData(data); //Save player data

            // Push instant subtitle update
            if (this.core.dailyRewardEntry != null) {
                this.core.floatingBlocks.sendSubtitlePacket(player, this.core.dailyRewardEntry);
            }

        } finally {
            this.core.dailyRewardClaimsInProgress.remove(uuid);
        }
    }
}
