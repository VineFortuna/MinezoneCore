package anthony.SuperCraftBrawl.Game.classes.killercreepr;

import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.ranks.Rank;
import com.avaje.ebean.validation.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class SuperClass {
    private static final Map<String, SuperClass> REGISTRY = new HashMap<>();
    //These are where all the class variables would be.
    //They should only ever be set in the registerAll() function.
    public static ExampleAnvilSuperClass ANVIL_CLASS;

    public static void registerAll(){
        ANVIL_CLASS = new ExampleAnvilSuperClass();
    }

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

    public static int getSlot(EquipmentSlot slot){
        switch (slot){
            case HAND: return 0;
            case HEAD: return 40;
            case CHEST: return 39;
            case LEGS: return 38;
            case FEET: return 37;
            default: return -1;
        }
    }

    //This should hold any information that all or the majority classes will use.
    private final String key;

    private final int tokenCost;
    private final Rank donor;

    //I would use a NamespacedKey here instead but Minecraft 1.8 is still using the ID system
    //and they don't exist, so I believe Strings would be best.
    //todo
    public SuperClass(String key, int tokenCost) {
        this.key = key;
        this.tokenCost = tokenCost;
        this.donor = null;
    }

    public SuperClass(String key, int tokenCost, @Nullable Rank donor) {
        this.key = key;
        this.tokenCost = tokenCost;
        this.donor = donor;
    }

    public abstract @Nullable Map<Integer, ItemStack> getEquipment();


    /**
     * @return The same inventory.
     */
    public @NotNull Inventory setItems(Inventory inventory){
        Map<Integer, ItemStack> items = getEquipment();
        if(items == null || items.isEmpty()) return inventory;
        for(Map.Entry<Integer, ItemStack> e : items.entrySet()){
            inventory.setItem(e.getKey(), e.getValue());
        }
        return inventory;
    }

    public int getTokenCost() {
        return tokenCost;
    }

    public @Nullable Rank getDonor() {
        return donor;
    }

    public @NotNull ClassInstance buildInstance(GameInstance gameInstance, Player p){
        return new ClassInstance(gameInstance, p.getUniqueId(), this);
    }

    public final @NotNull String getKey() {
        return key;
    }
}
