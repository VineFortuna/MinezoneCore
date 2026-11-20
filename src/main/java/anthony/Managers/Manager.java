package anthony.Managers;

import org.bukkit.event.Listener;

public interface Manager extends Listener {
    default void onLoad() {}
    void onEnable();
    void onDisable();
}
