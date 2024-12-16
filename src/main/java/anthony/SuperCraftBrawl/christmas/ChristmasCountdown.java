package anthony.SuperCraftBrawl.christmas;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;

public class ChristmasCountdown {
	
	private Core main;
	
	public ChristmasCountdown(Core main) {
		this.main = main;
	}

	private final Map<Integer, String> rewards = new HashMap<>();

    public void putRewards() {
        // Populate rewards for each day (December 15 to December 25)
        rewards.put(15, "reward_1");
        rewards.put(16, "reward_2");
        rewards.put(17, "reward_3");
        rewards.put(18, "reward_4");
        rewards.put(19, "reward_5");
        rewards.put(20, "reward_6");
        rewards.put(21, "reward_7");
        rewards.put(22, "reward_8");
        rewards.put(23, "reward_9");
        rewards.put(24, "reward_10");
        rewards.put(25, "special_reward");

        this.main.getServer().getScheduler().runTaskTimer(this.main, this::checkRewards, 0L, 20L * 60 * 60 * 24); // Runs daily
    }

    private void checkRewards() {
        LocalDate today = LocalDate.now();
        if (today.getMonthValue() == 12) { // Ensure it's December
            int day = today.getDayOfMonth();
            if (rewards.containsKey(day)) {
                giveReward(day);
            }
        }
    }

    private void giveReward(int day) {
        String reward = rewards.get(day);
        for (Player player : this.main.getServer().getOnlinePlayers()) {
            player.sendMessage("You've received your daily reward: " + reward);
            // Add logic to give the reward (items, currency, etc.)
        }
    }
	
}
