package anthony.SuperCraftBrawl.Game.abilities;

import anthony.SuperCraftBrawl.ChatColorHelper;
import anthony.SuperCraftBrawl.Game.classes.CooldownNatowski;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a Class ability in the game
 *
 */
public class AbilityNew {
    private final String name; // Name of the ability
    private final Player player; // Player regarding that class's ability
    private final String description;
    private CooldownNatowski cooldown; // Cooldown of the ability (in seconds)
    private boolean canBeUsedInAir;
    private ItemStack triggerItem; // Item that is clicked for the ability to be used

    public AbilityNew(String name, String description, CooldownNatowski cooldown, Player player) {
        this(name, description, player, cooldown, true);
    }

    public AbilityNew(String name, String description, Player player, CooldownNatowski cooldown, boolean canBeUsedInAir) {
        this.name = name;
        this.player = player;
        this.description = description;
        this.cooldown = cooldown;
        this.canBeUsedInAir = canBeUsedInAir;
    }

    public void activate() {

    }

    public void deactivate() {

    }

    public boolean isReady() {
        return cooldown.isReady();
    }

    public void use() {
        cooldown.use();
    }

    public void sendPlayerUseAbilityChatMessage() {
        player.sendMessage(ChatColorHelper.color("&a&l(!) &6" + name + "&r was used"));
    }

    public void sendPlayerCustomUseAbilityChatMessage(String message) {
        player.sendMessage(ChatColorHelper.color(message));
    }

    public void sendPlayerRemainingCooldownChatMessage() {
        player.sendMessage(ChatColorHelper.color("&9&l(!) &r&6" + name + "&r is on cooldown for " + "&6" + cooldown.getRemainingCooldownSeconds() + "&r seconds" ));
    }

    public String getName() {
        return name;
    }

    public boolean canBeUsedInAir() {
        return canBeUsedInAir;
    }

    public long getCooldownDurationSeconds() {
        return cooldown.getCooldownDurationSeconds();
    }

    public CooldownNatowski getCooldownInstance() {
        return cooldown;
    }
}
