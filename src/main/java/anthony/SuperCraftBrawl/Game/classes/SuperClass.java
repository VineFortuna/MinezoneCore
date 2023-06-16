package anthony.SuperCraftBrawl.Game.classes;

import com.avaje.ebean.validation.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class SuperClass {
    private static final Map<String, SuperClass> REGISTRY = new HashMap<>();

    public static final SuperClass NOTCH = register(new SuperClass("notch") {
        //todo
    });

    public static SuperClass register(SuperClass superClass){
        return register(superClass, false);
    }
    public static SuperClass register(SuperClass superClass, boolean override){
        if(!override && REGISTRY.containsKey(superClass.getKey())) return superClass;
        REGISTRY.put(superClass.getKey(), superClass);
        return superClass;
    }

    public static @Nullable SuperClass get(String key){
        return REGISTRY.getOrDefault(key, null);
    }

    //This should hold any information that all or the majority classes will use.
    public final double baseVerticalJump = 1D;
    private final String key;

    //I would use a NamespacedKey here instead but Minecraft 1.8 is still using the ID system
    //and they don't exist, so I believe Strings would be best.
    //todo
    public SuperClass(String key) {
        this.key = key;
    }

    public final @NotNull String getKey() {
        return key;
    }
}
