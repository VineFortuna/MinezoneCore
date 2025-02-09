package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.ActionBarManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import anthony.util.ChatColorHelper;

public class Ability {
    private String abilityName; // Name of the ability
    private final Player player; // Player regarding that class's ability
    private ItemStack triggerItem; // Item that is clicked for the ability to be used
    private CooldownNatowski cooldown; // Cooldown of the ability (in seconds)

    public Ability(String name, Player player) {
        this.abilityName = name;
        this.player = player;
    }

    public Ability(String name, Player player, ItemStack triggerItem) {
        this(name, player);
        this.triggerItem = triggerItem;
    }

    public Ability(String name, double cooldownSeconds, Player player) {
        this(name, player);
        this.cooldown = new CooldownNatowski(cooldownSeconds);
    }

    public Ability(String name, double cooldownSeconds, Player player, ItemStack triggerItem) {
        this(name, player, triggerItem);
        this.cooldown = new CooldownNatowski(cooldownSeconds);
    }

    public String getAbilityNameRightClickMessage() {
        return abilityName + " &7(Right Click)";
    }

    public String getAbilityNameLeftRightClickMessage() {
        return abilityName + " &7(Left/Right Click)";
    }

    public String getOnGroundItemMessage() {
        return "&7You have to be on the ground";
    }
    public String getOnGroundChatMessage() {
        return "&c&l(!) &rYou have to be on the ground to use " + abilityName;
    }

    public boolean isReady() {
        return cooldown.isReady();
    }

    public void use() {
        cooldown.use();
    }

    public void updateActionBar(Player player, BaseClass baseClass) {
        ActionBarManager actionBarManager = baseClass.getActionBarManager();

        ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(baseClass, actionBarManager);
        abilityActionBar.setActionBarAbility(player, this, null);
    }

    public void updateActionBarWhite(Player player, BaseClass baseClass) {
        ActionBarManager actionBarManager = baseClass.getActionBarManager();

        ActionBarManager.AbilityActionBar abilityActionBar = new ActionBarManager.AbilityActionBar(baseClass, actionBarManager);
        abilityActionBar.setActionBarAbilityWhite(player, this, null);
    }

    public void sendPlayerUseAbilityChatMessage() {
        player.sendMessage(ChatColorHelper.color("&a&l(!) &6" + abilityName + "&r was used"));
    }

    public void sendCustomMessage(String message) {
        player.sendMessage(ChatColorHelper.color(message));
    }

    public void sendPlayerRemainingCooldownChatMessage() {
        player.sendMessage(ChatColorHelper.color("&9&l(!) &r&6" + abilityName + "&r is on cooldown for " + "&6" + cooldown.getRemainingCooldownSeconds() + "&r seconds" ));
    }

    public String getAbilityName() {
        return abilityName;
    }

    public double getCooldownDurationSeconds() {
        return cooldown.getCooldownDurationSeconds();
    }

    public CooldownNatowski getCooldownInstance() {
        return cooldown;
    }
}
