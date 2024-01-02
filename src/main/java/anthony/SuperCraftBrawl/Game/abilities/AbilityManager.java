package anthony.SuperCraftBrawl.Game.abilities;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.abilities.all.EnchantmentTableAbility;
import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import org.bukkit.entity.Player;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashMap;
import java.util.Map;

public class AbilityManager {
    private Map<String, AbilityNew> abilities;

    /* Created just to compile, don't know how to pass player */
    Player player;

    public AbilityManager() {
        this.abilities = new HashMap<>();

        registerAbility(new EnchantmentTableAbility(
                "Enchant Ability",
                "Enchant your sword to make it really special",
                new CooldownNatowski(0),
                player));
    }

    public void registerAbility(AbilityNew ability) {
        abilities.put(ability.getName(), ability);
    }

    public AbilityNew getAbility(String abilityName) {
        return abilities.get(abilityName);
    }

    public void useAbility(String abilityName, Player player) {
        AbilityNew ability = getAbility(abilityName);

        if (ability != null) {
            // If ability is NOT on cooldown
            if (!isOnCooldown(player, ability)) {

                // Check if player cannot use the ability
                if (canUseInAir(player, ability)) {
                    ability.use();
                    
                } else {
                    // If player is on air and can not use its ability on air
                    player.sendMessage(ChatColorHelper.color("&c&l(!) &rYou have to be on the ground to use &6" + abilityName));
                }
            } else {
                // If ability ON cooldown
                sendRemainingCooldownMessage(ability);
            }
        } else {
            // Handle the case where the ability is not found
        }
    }

    private boolean isOnCooldown(Player player, AbilityNew ability) {
        // Implement cooldown logic here
        // Check if the player is still on cooldown for the given ability
        return false; // Placeholder, implement according to your needs
    }

    public void sendRemainingCooldownMessage(AbilityNew ability) {
        if (ability != null && !ability.isReady()) {
            ability.sendPlayerRemainingCooldownChatMessage();
        }
    }

    public void listAbilities() {
        for (Map.Entry<String, AbilityNew> entry : abilities.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue().getName());
        }
    }

    private boolean canUseInAir(Player player, AbilityNew ability) {
        return player.isOnGround() || ability.canBeUsedInAir();
    }
}
