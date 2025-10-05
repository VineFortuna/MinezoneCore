package anthony.SuperCraftBrawl.npcs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NPCPlugin extends JavaPlugin {
    private static NPCPlugin instance;
    public static NPCPlugin getInstance(){ return instance; }

    private NPC playNPC, infoNPC;

    @Override public void onEnable() {
        instance = this;

        // Create your NPCs (replace texture/signature with your Base64 values)
        World w = Bukkit.getWorlds().get(0);
        playNPC = new NPC("Play",
                new Location(w, 0.5, 65, 5.5, 180f, 0f),
                "<BASE64_TEXTURE>", "<BASE64_SIGNATURE>",
                p -> p.performCommand("menu"));
        infoNPC = new NPC("Info",
                new Location(w, 2.5, 65, 5.5, 180f, 0f),
                "<BASE64_TEXTURE_2>", "<BASE64_SIGNATURE_2>",
                p -> p.sendMessage("§aWelcome to Minezone! Use §e/menu §ato begin."));

        // Register listener to inject channels and show NPCs
        getServer().getPluginManager().registerEvents(new VisibleHook(playNPC, infoNPC), this);

        // Inject + show for already-online players (e.g., /reload)
        for (Player p : Bukkit.getOnlinePlayers()) {
            ChannelInjector.inject(p);
            playNPC.showTo(p);
            infoNPC.showTo(p);
        }
    }

    @Override public void onDisable() {
        // Hide all and uninject
        if (playNPC != null) playNPC.hideFromAll();
        if (infoNPC != null) infoNPC.hideFromAll();
        for (Player p : Bukkit.getOnlinePlayers()) ChannelInjector.uninject(p);
    }
}

