package anthony;

import anthony.Managers.ManagerManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    private final ManagerManager managerManager;

    public Core() {
        managerManager = new ManagerManager();
        super();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {

    }
}

// TODO:
// Disable weather, (override WeatherChangeEvent)
// Disable interactivity with an end crystal
