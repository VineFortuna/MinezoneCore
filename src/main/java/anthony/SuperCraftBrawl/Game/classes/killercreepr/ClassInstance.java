package anthony.SuperCraftBrawl.Game.classes.killercreepr;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Score;

import javax.annotation.Nullable;
import java.util.UUID;

public class ClassInstance implements Listener {
    protected final GameInstance instance;
    protected final UUID user;
    protected final SuperClass superClass;

    protected int lives = 5;
    protected int tokens = 0;
    protected Score score;
    protected int totalTokens = 0;
    protected int totalKills = 0;
    protected int eachLifeKills = 0;
    protected int totalExp = 0;
    protected double baseVerticalJump = 1.0;

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

    /**
     * @return Whether the instance should be cleared from memory.
     */
    public final boolean tick(int gameTicks){
        Player p = getPlayer();
        if(p == null) return true;
        return t(p, gameTicks);
    }
    protected boolean t(Player p, int gameTicks){ return false; }

    public @Nullable Player getPlayer(){
        return Bukkit.getPlayer(user);
    }

    public UUID getUser() {
        return user;
    }

    public SuperClass getSuperClass() {
        return superClass;
    }

    public boolean isDead(){ return lives < 1; }
}
