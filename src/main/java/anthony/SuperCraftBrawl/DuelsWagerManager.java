package anthony.SuperCraftBrawl;

import anthony.SuperCraftBrawl.playerdata.PlayerData;
import anthony.SuperCraftBrawl.playerdata.PlayerDataManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DuelsWagerManager {
    private final Map<String, String> pendingRequests = new HashMap<>();
    private PlayerDataManager playerDataManager;
    private PlayerData playerData;
    private Core main;
    public Player senderPlayer;
    public Player targetPlayer;
    public int tokenAmount;
    private boolean duelCanceled = false;


    public DuelsWagerManager() {
        this.main = main;
        this.senderPlayer = senderPlayer;
        this.targetPlayer = targetPlayer;
        playerDataManager = main.getPlayerDataManager();

    }

    // adadagagaaagapkfpafa
    public int getTokens(Player player) {
        playerData = playerDataManager.getPlayerData(player);
        return playerData.tokens;
    }

    public void addTokens(Player player, int amount) {
        playerData.addTokens(amount);
    }

    public void removeTokens(Player player, int amount) {

    }

    public void sendRequest(Player senderPlayer, Player targetPlayer, int tokenAmount) {
        PlayerData senderData = playerDataManager.getPlayerData(senderPlayer);
        PlayerData targetData = playerDataManager.getPlayerData(targetPlayer);

//        pendingRequests.put(senderData, targetPlayer);

        int senderTokens = senderData.tokens;
        int targetTokens = targetData.tokens;

        if (senderTokens >= tokenAmount) {
            if (targetTokens >= tokenAmount) {
                senderPlayer.sendMessage(ChatColorHelper.color("Duel request sent to &e" + targetPlayer.getName() + "&r with &a" + tokenAmount));

                // Sending request
                sendClickableMessage(tokenAmount);

                main.getLogger().info("Duel request sent from " + senderPlayer.getName() + " to " + targetPlayer.getName() + ".");

            } else {
                // Target Player does not have enough tokens
                senderPlayer.sendMessage(ChatColorHelper.color("&c&l(!) &r&e" + targetPlayer.getName() + "&rdoes not have enough tokens for &a" + tokenAmount+  "&r wager"));
            }
        } else {
            // Sender Player does not have enough tokens
            senderPlayer.sendMessage(ChatColorHelper.color("&c&l(!) &rYou don't have enough tokens for this wager"));
        }
    }

    public boolean hasPendingRequest(String challenger, String recipient) {
        return pendingRequests.containsKey(challenger) && pendingRequests.get(challenger).equals(recipient);
    }

    public void removePendingRequest(String challenger) {
        pendingRequests.remove(challenger);
    }

    private void sendClickableMessage(int tokenAmount) {
        ComponentBuilder builder = new ComponentBuilder(senderPlayer.getName() + " challenged you in a duel for " + tokenAmount)
                .append("Accept")
                .color(ChatColor.GREEN)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duelrequest accept " + senderPlayer.getDisplayName()))
                .append("Decline")
                .color(ChatColor.RED)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duelrequest decline " + senderPlayer.getDisplayName()))
                .append("Change Bet")
                .color(ChatColor.BLUE)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duelrequest changebet " + senderPlayer.getDisplayName() ));

        targetPlayer.spigot().sendMessage(builder.create());
    }

    public void handleAccept(DuelRequest duelRequest) {

    }

    public void handleDecline(DuelRequest duelRequest, Player responder) {
        duelCanceled = true;


    }

    public void cancelDuelRequest() {
    }

    public void requestDuration() {
        Bukkit.getScheduler().runTaskLater(main, () -> {
            cancelDuelRequest();
        }, 60 * 20);
    }

}
