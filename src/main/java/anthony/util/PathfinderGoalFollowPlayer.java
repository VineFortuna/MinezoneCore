package anthony.util;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.PathfinderGoal;

public class PathfinderGoalFollowPlayer extends PathfinderGoal {
    
    private final EntityInsentient mob;
    private final EntityLiving target;
    private final double speed;
    private final double stopDistance;
    private final double startDistance;
    
    public PathfinderGoalFollowPlayer(EntityInsentient mob, EntityLiving target, double speed, double stopDistance, double startDistance) {
        this.mob = mob;
        this.target = target;
        this.speed = speed;
        this.stopDistance = stopDistance;
        this.startDistance = startDistance;
    }
    
    @Override
    public boolean a() {
        // Start the goal if the target exists and is farther than the start distance
        return target != null && mob.h(target) > startDistance * startDistance;
    }
    
    @Override
    public boolean b() {
        // Continue the goal if the mob is farther than the stop distance
        return target != null && mob.h(target) > stopDistance * stopDistance;
    }
    
    @Override
    public void e() {
        double distanceSquared = mob.h(target); // Calculate the squared distance
        
        if (distanceSquared > stopDistance * stopDistance) {
            // Move towards the player if it's farther than the stopDistance
            this.mob.getNavigation().a(target.locX, target.locY, target.locZ, this.speed);
        } else {
            // Once within the stop distance, stop targeting the player, but still move around
            this.mob.getNavigation().n(); // Stop navigation
            
            // Stop the mob from targeting the player without freezing it
            this.mob.setGoalTarget(null); // Remove target (no longer following or attacking)
        }
    }
}
