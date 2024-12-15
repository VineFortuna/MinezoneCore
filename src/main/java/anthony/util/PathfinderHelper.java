package anthony.util;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class PathfinderHelper {
    
    /**
     * Adds a custom pathfinding goal to a given living entity.
     *
     * @param entity         The target living entity.
     * @param priority       The priority of the goal (lower numbers = higher priority).
     * @param pathfinderGoal The custom PathfinderGoal to add.
     */
    public static void addPathfinderGoal(LivingEntity entity, int priority, PathfinderGoal pathfinderGoal) {
        try {
            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
            
            Field goalSelectorField = EntityInsentient.class.getDeclaredField("goalSelector");
            goalSelectorField.setAccessible(true);
            
            PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalSelectorField.get(nmsEntity);
            goalSelector.a(priority, pathfinderGoal); // Add the custom goal with priority
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Clears all pathfinding goals from the given living entity.
     *
     * @param entity The target living entity.
     */
    public static void clearPathfinderGoals(LivingEntity entity) {
        try {
            EntityInsentient nmsEntity = (EntityInsentient) ((CraftLivingEntity) entity).getHandle();
            
            Field goalSelectorField = EntityInsentient.class.getDeclaredField("goalSelector");
            goalSelectorField.setAccessible(true);
            
            PathfinderGoalSelector goalSelector = (PathfinderGoalSelector) goalSelectorField.get(nmsEntity);
            goalSelector.a(); // Clear all current goals
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
