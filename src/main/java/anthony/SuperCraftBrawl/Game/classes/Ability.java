package anthony.SuperCraftBrawl.Game.classes;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import anthony.SuperCraftBrawl.ChatColorHelper;

public class Ability {
    private final String abilityName; // Name of the ability
    private final Player player; // Player regarding that class's ability
    private ItemStack triggerItem; // Item that is clicked for the ability to be used
    private CooldownNatowski cooldown; // Cooldown of the ability (in seconds)

    public Ability(String name, Player player) {
        this.abilityName = name;
        this.player = player;
    }

    public Ability(String name, Player player, ItemStack triggerItem) {
        this.abilityName = name;
        this.player = player;
        this.triggerItem = triggerItem;
    }

    public Ability(String name, int cooldownSeconds, Player player) {
        this.abilityName = name;
        this.cooldown = new CooldownNatowski(cooldownSeconds);
        this.player = player;
    }

    public Ability(String name, int cooldownSeconds, Player player, ItemStack triggerItem) {
        this.abilityName = name;
        this.cooldown = new CooldownNatowski(cooldownSeconds);
        this.player = player;
        this.triggerItem = triggerItem;
    }

    public boolean isReady() {
        return cooldown.isReady();
    }

    public void use() {
        cooldown.use();
    }

    public void sendPlayerUseAbilityChatMessage() {
        player.sendMessage(ChatColorHelper.color("&a&l(!) &6" + abilityName + "&r was used"));
    }

    public void sendPlayerCustomUseAbilityChatMessage(String message) {
        player.sendMessage(ChatColorHelper.color(message));
    }

    public void sendPlayerRemainingCooldownChatMessage() {
        player.sendMessage(ChatColorHelper.color("&9&l(!) &r&6" + abilityName + "&r is on cooldown for " + "&6" + cooldown.getRemainingCooldownSeconds() + "&r seconds" ));
    }

    public String getAbilityName() {
        return abilityName;
    }

    public long getCooldownDurationSeconds() {
        return cooldown.getCooldownDurationSeconds();
    }

    public CooldownNatowski getCooldownInstance() {
        return cooldown;
    }
}
