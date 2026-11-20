package anthony.Managers;

import java.util.ArrayList;
import java.util.List;

public class ManagerManager {
    private final List<Manager> managers = new ArrayList<>();

    public void register(Manager service) {
        managers.add(service);
    }

    public void enableAll() {
        for (Manager service : managers) {
            try {
                service.onEnable();
            } catch (Exception e) {
                throw new RuntimeException(e); // TODO: better exception handling
            }
        }
    }

    public void disableAll() {
        for (int i = managers.size() - 1; i >= 0; i--) {
            try {
                managers.get(i).onDisable();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
