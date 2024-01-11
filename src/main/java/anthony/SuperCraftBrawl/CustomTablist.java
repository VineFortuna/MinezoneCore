package anthony.SuperCraftBrawl;

import org.bukkit.event.Listener;

public class CustomTablist implements Listener {

    /*private final Main plugin;
    private final Map<Player, Scoreboard> scoreboards = new HashMap<>();

    public CustomTablist(Main plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPacketSend(PlayerConnectionSendPacketEvent event) {
        Packet packet = event.getPacket();
        if (packet instanceof PacketPlayOutPlayerInfo) {
            PacketPlayOutPlayerInfo infoPacket = (PacketPlayOutPlayerInfo) packet;
            if (infoPacket.action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) {
                // Get the player list header and footer from your config or database
                String header = "Header text here";
                String footer = "Footer text here";

                // Modify the player list header and footer
                PacketPlayOutPlayerListHeaderFooter headerFooterPacket = new PacketPlayOutPlayerListHeaderFooter();
                headerFooterPacket.header = ChatSerializer.a("{\"text\":\"" + header + "\"}");
                headerFooterPacket.footer = ChatSerializer.a("{\"text\":\"" + footer + "\"}");
                event.setPacket(headerFooterPacket);

                // Create a custom scoreboard for the player
                Player player = event.getPlayer();
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
                Objective objective = scoreboard.registerNewObjective("tab", "dummy");
                objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

                // Set the scoreboard for the player
                PacketPlayOutScoreboardObjective objectivePacket = new PacketPlayOutScoreboardObjective(objective, 0);
                PacketPlayOutScoreboardDisplayObjective displayPacket = new PacketPlayOutScoreboardDisplayObjective(1, objective);
                sendPacket(player, objectivePacket);
                sendPacket(player, displayPacket);

                // Store the scoreboard for later use
                scoreboards.put(player, scoreboard);
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        // Update the scoreboard for each player each tick
        for (Player player : scoreboards.keySet()) {
            Scoreboard scoreboard = scoreboards.get(player);
            Objective objective = scoreboard.getObjective("tab");

            // Add players to the scoreboard
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                Score score = objective.getScore(onlinePlayer.getName());
                score.setScore((int) onlinePlayer.getHealthScale());
            }

            // Send the updated scoreboard packet to the player
            PacketPlayOutScoreboardObjective objectivePacket = new PacketPlayOutScoreboardObjective(objective, 2);
            sendPacket(player, objectivePacket);
        }
    }

    private void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }*/
}