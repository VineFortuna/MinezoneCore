package anthony.SuperCraftBrawl.Game.classes;

import net.md_5.bungee.api.ChatColor;

public class CooldownNatowski {
    private long lastUsageTime;
    private final long cooldownDurationMillis;
    private final double cooldownDurationSeconds;

    public CooldownNatowski(double cooldownDurationSeconds) {
        this.cooldownDurationSeconds = cooldownDurationSeconds;
        this.cooldownDurationMillis = (long) (cooldownDurationSeconds * 1000); // Convert seconds to milliseconds
        reset();
    }

    public long getRemainingCooldownMillis() {
        long currentTime = System.currentTimeMillis();
        long elapsedMillis = currentTime - lastUsageTime;
        return Math.max(0, cooldownDurationMillis - elapsedMillis);
    }

    public long getRemainingCooldownSeconds() {
        return getRemainingCooldownMillis() / 1000;
    }

    public boolean isReady() {
        // Return true if remainingCooldown is higher than cooldownDuration
        return getRemainingCooldownMillis() <= 0;
    }

    public void use() {
        lastUsageTime = System.currentTimeMillis();
    }

    public void reset() {
        lastUsageTime = 0;  // Set to a value that ensures the first use is allowed
    }

    public double getCooldownDurationSeconds() {
        return cooldownDurationSeconds;
    }

    public long getLastUsageTime() {
        return lastUsageTime;
    }
}