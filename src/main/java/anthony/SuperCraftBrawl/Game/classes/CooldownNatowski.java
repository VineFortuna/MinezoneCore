package anthony.SuperCraftBrawl.Game.classes;

import net.md_5.bungee.api.ChatColor;

public class CooldownNatowski {
    private long lastUsageTime;
    private final long cooldownDurationMillis;
    private long cooldownDurationSeconds = 10;
    private static final ChatColor useMessageColor = ChatColor.GREEN;

    public CooldownNatowski(long cooldownDurationSeconds) {
        this.cooldownDurationSeconds = cooldownDurationSeconds;
        this.cooldownDurationMillis = cooldownDurationSeconds * 1000; // Convert seconds to milliseconds
        reset();
    }

    public long remainingCooldownMillis() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastUsageTime;
    }

    public long getRemainingCooldownSeconds() {
        return cooldownDurationSeconds - remainingCooldownMillis() / 1000;
    }

    public boolean isReady() {
        // Return true if remainingCooldown is higher than cooldownDuration
        return remainingCooldownMillis() >= cooldownDurationMillis;
    }

    public void use() {
        lastUsageTime = System.currentTimeMillis();
    }

    public void reset() {
        lastUsageTime = 0;  // Set to a value that ensures the first use is allowed
    }

    public long getCooldownDurationSeconds() {
        return cooldownDurationSeconds;
    }

    public long getLastUsageTime() {
        return lastUsageTime;
    }

    public ChatColor getUseMessageColor() {
        return useMessageColor;
    }
}