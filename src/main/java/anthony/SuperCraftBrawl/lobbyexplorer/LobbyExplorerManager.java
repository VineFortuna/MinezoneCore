package anthony.SuperCraftBrawl.lobbyexplorer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import anthony.SuperCraftBrawl.Core;

public class LobbyExplorerManager {

    private final Core core;
    // key by UUID to avoid holding Player refs
    private final Map<UUID, AmyLobbyExplorer> selectedExplorer = new HashMap<>();

    public LobbyExplorerManager(Core core) {
        this.core = core;
    }

    public void checkSelectedExplorer(LobbyExplorers explorer, Player player) {
        if (explorer == LobbyExplorers.Amy) { // enums: use ==
            AmyLobbyExplorer amy = selectedExplorer.computeIfAbsent(
                player.getUniqueId(),
                id -> new AmyLobbyExplorer()
            );
            amy.sendMessage(player);
        }
    }
}