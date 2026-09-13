package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.GameManager;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;

public class DeathMessageProvider {
    private final Map<DeathCategory, List<String>> deathMessages = new HashMap<>();
    private final Random random = new Random();

    private static final String header = "&2&l(!) &r";

    public DeathMessageProvider() {
        initDeathMessages();
    }

    // Index 0 = default message, indexes 1+ = custom messages
    private void initDeathMessages() {
        deathMessages.put(DeathCategory.PVP_MELEE, Arrays.asList(
                header + "%victim% &cwas killed by &f%killer%",
                header + "%victim% &cwas not strong enough to encounter &r%killer%"
        ));

        deathMessages.put(DeathCategory.PVP_PROJECTILE, Arrays.asList(
                header + "%victim% &cwas killed by &f%killer%",
                header + "%victim% &cwas not strong enough to encounter &r%killer%"
        ));

        deathMessages.put(DeathCategory.PVE_MELEE, Arrays.asList(
                header + "%victim% &cwas killed by &r%killer%&c's &e%mob%"
        ));

        deathMessages.put(DeathCategory.PVE_PROJECTILE, Arrays.asList(
                header + "%victim% &cwas killed by &r%killer%&c's &e%mob%"
        ));

        deathMessages.put(DeathCategory.SUICIDE, Arrays.asList(
                header + "%victim% &ccommitted suicide"
        ));

        deathMessages.put(DeathCategory.VOID_ENVIRONMENTAL, Arrays.asList(
                header + "%victim% &cfell into the void",
                header + "%victim% &csaid NO THANK YOU and took the easy way out",
                header + "%victim% &cwalked off the edge..."
        ));

        deathMessages.put(DeathCategory.VOID_COMBAT, Arrays.asList(
                header + "%victim% &cwas doomed to fall by &f%killer%",
                header + "&cHello? AND GOODBYE TO &f%victim% &cAND ANYONE ELSE STANDING IN &f%killer%'s &cWAY!"
        ));

        deathMessages.put(DeathCategory.MAGIC, Arrays.asList(
                header + "%victim% &cwas murdered via the dark arts"
        ));

        deathMessages.put(DeathCategory.WITHER_ENVIRONMENT, Arrays.asList(
                header + "%victim% &cwithered away"
        ));

        deathMessages.put(DeathCategory.WITHER_COMBAT, Arrays.asList(
                header + "%victim% &cwas withered by &f%killer%"
        ));

        deathMessages.put(DeathCategory.FIRE_ENVIRONMENT, Arrays.asList(
                header + "%victim% &cburned to death"
        ));

        deathMessages.put(DeathCategory.FIRE_COMBAT, Arrays.asList(
                header + "%victim% &cwas burned to death by &f%killer%"
        ));

        deathMessages.put(DeathCategory.UNKNOWN, Arrays.asList(
                header + "%victim% &cdied",
                header + "%victim% &cjust died SO badly"
        ));
    }

    private DeathCategory resolveDeathCategory(Player victim, Player killer) {
        EntityDamageEvent lastDamage = victim.getLastDamageCause();

        if (victim.getLocation().getY() <= 50)
            return (killer == null) ? DeathCategory.VOID_ENVIRONMENTAL : DeathCategory.VOID_COMBAT;

        if (killer != null && killer.equals(victim)) return DeathCategory.SUICIDE;

        //if (lastDamage == null) return DeathCategory.UNKNOWN;

        DamageCause cause = lastDamage.getCause();

        if (cause == DamageCause.MAGIC ) return DeathCategory.MAGIC;
        if (cause == DamageCause.WITHER) return (killer == null) ? DeathCategory.WITHER_ENVIRONMENT : DeathCategory.WITHER_COMBAT;
        if (cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK || cause == DamageCause.LAVA)
            return (killer == null) ? DeathCategory.FIRE_ENVIRONMENT : DeathCategory.FIRE_COMBAT;

        if (lastDamage instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();

            if (damager instanceof Player)   return (damager.equals(victim)) ? DeathCategory.SUICIDE : DeathCategory.PVP_MELEE;
            if (damager instanceof Creature) return DeathCategory.PVE_MELEE;

            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;

                if (projectile.getShooter() instanceof Player)
                    return (projectile.getShooter().equals(victim)) ? DeathCategory.SUICIDE : DeathCategory.PVP_PROJECTILE;
                if (projectile.getShooter() instanceof Creature) return DeathCategory.PVE_PROJECTILE;
            }
        }

        return DeathCategory.UNKNOWN;
    }

    public String getDeathMessage(Player victim, Player killer, Creature creature, boolean customEnabled) {
        DeathCategory category = resolveDeathCategory(victim, killer);

        List<String> messages = deathMessages.getOrDefault(category, deathMessages.get(DeathCategory.UNKNOWN));

        int index = 0;

        if (customEnabled && messages.size() > 1) {
            index = 1 + random.nextInt(messages.size() - 1);
        }

        String rawMessage = messages.get(index);

        return rawMessage.replace("%victim%", victim.getName())
                .replace("%killer%", killer != null ? killer.getName() : "")
                .replace("%mob%", creature != null ? GameManager.getMobTypeName(creature.getType()) : "");
    }
}

enum DeathCategory {
    PVP_MELEE,
    PVP_PROJECTILE,
    PVE_MELEE,
    PVE_PROJECTILE,
    VOID_ENVIRONMENTAL,
    VOID_COMBAT,
    WITHER_ENVIRONMENT,
    WITHER_COMBAT,
    FIRE_ENVIRONMENT,
    FIRE_COMBAT,
    MAGIC,
    SUICIDE,
    UNKNOWN
}
