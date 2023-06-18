package anthony.SuperCraftBrawl.Game.classes.killercreepr;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Score;

import java.util.UUID;

public class ClassInstance implements Listener {
    protected final GameInstance instance;
    protected final UUID user;
    protected final SuperClass superClass;

    private int lives = 5;
    private int tokens = 0;
    private Score score;
    private int totalTokens = 0;
    private int totalKills = 0;
    private int eachLifeKills = 0;
    private int totalExp = 0;
    private double baseVerticalJump = 1.0;

    public void register(){
        Bukkit.getPluginManager().registerEvents(this, Core.inst());
    }

    public void unregister(){
        HandlerList.unregisterAll(this);
    }

    public ClassInstance(GameInstance gameInstance, UUID user, SuperClass superClass) {
        this.instance = gameInstance;
        this.user = user;
        this.superClass = superClass;
    }

    public UUID getUser() {
        return user;
    }

    public SuperClass getSuperClass() {
        return superClass;
    }

    public boolean isDead(){ return lives < 1; }
}
