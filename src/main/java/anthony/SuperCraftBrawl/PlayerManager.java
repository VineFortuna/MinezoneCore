package anthony.SuperCraftBrawl;


import org.bukkit.entity.Player;

public class PlayerManager {

    private Core core;
    private Player player;
    private String nickname;

    //CONSTRUCTOR

    public PlayerManager(Core core, Player player) {
        this.core = core;
        this.player = player;
    }

    //GETTERS:

    public String getNickname() {
        return this.nickname;
    }
}
