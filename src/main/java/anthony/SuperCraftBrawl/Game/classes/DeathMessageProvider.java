package anthony.SuperCraftBrawl.Game.classes;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;

public class DeathMessageProvider {
    private final Map<DeathCategory, List<String>> killedMessages = new EnumMap<>(DeathCategory.class);
    private final List<String> killerMessages = new ArrayList<>();
    private final Random random = new Random();

    private static final String header = "&2&l(!) &r";

    public DeathMessageProvider() {
        initDeathMessages();
    }

    private void initDeathMessages() {
        killedMessages.put(DeathCategory.PVP_MELEE, Arrays.asList(
                header + "%victim% &cwas not strong enough to encounter &r%killer%",
                header + "%victim% &cwas killed by &f%killer%"
        ));

        killedMessages.put(DeathCategory.PVP_PROJECTILE, Arrays.asList(
                header + "%victim% &cwas not strong enough to encounter &r%killer%",
                header + "%victim% &cwas killed by &f%killer%"
        ));

        killedMessages.put(DeathCategory.PVE_MELEE, Arrays.asList(
                header + "%victim% &cwas killed by a &e%killer%"
        ));

        killedMessages.put(DeathCategory.PVE_PROJECTILE, Arrays.asList(
                header + "%victim% &cwas killed by &r%victim%&c's &e%killer%"
        ));

        killedMessages.put(DeathCategory.SUICIDE, Arrays.asList(
                header + "%victim% &ccommitted suicide"
        ));

        killedMessages.put(DeathCategory.VOID_ENVIRONMENTAL, Arrays.asList(
                header + "%victim% &csaid NO THANK YOU and took the easy way out",
                header + "%victim% &cwalked off the edge...",
                header + "%victim% &cfell into the void"
        ));

        killedMessages.put(DeathCategory.VOID_COMBAT, Arrays.asList(
                header + "&cHello? AND GOODBYE TO &f%victim% &cAND ANYONE ELSE STANDING IN &f%killer%'s &cWAY!",
                header + "%victim% &cwas doomed to fall by &f%killer%"
        ));

        killedMessages.put(DeathCategory.MAGIC, Arrays.asList(
                header + "%victim% &cwas murdered via the dark arts"
        ));

        killedMessages.put(DeathCategory.WITHER_ENVIRONMENT, Arrays.asList(
                header + "%victim% &cwithered away"
        ));

        killedMessages.put(DeathCategory.WITHER_COMBAT, Arrays.asList(
                header + "%victim% &cwas withered by &f%killer%"
        ));

        killedMessages.put(DeathCategory.FIRE_ENVIRONMENT, Arrays.asList(
                header + "%victim% &cburned to death"
        ));

        killedMessages.put(DeathCategory.FIRE_COMBAT, Arrays.asList(
                header + "%victim% &cwas burned to death by &f%killer%"
        ));

        killedMessages.put(DeathCategory.UNKNOWN, Arrays.asList(
                header + "%victim% &cdied",
                header + "%victim% &cjust died SO badly"
        ));


        killerMessages.addAll(Arrays.asList(

        ));
    }

    private DeathCategory resolveDeathCategory(Player victim, Player killer) {
        EntityDamageEvent lastDamage = victim.getLastDamageCause();

        if (lastDamage == null)                      return DeathCategory.UNKNOWN;
        if (killer != null && killer.equals(victim)) return DeathCategory.SUICIDE;

        if (victim.getLocation().getY() <= 50)
            return (killer == null) ? DeathCategory.VOID_ENVIRONMENTAL : DeathCategory.VOID_COMBAT;

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

    public String getDeathMessage(Player victim, Player killer) {
        DeathCategory category = resolveDeathCategory(victim, killer);
        List<String> candidateMessages = killedMessages.getOrDefault(category, killedMessages.get(DeathCategory.UNKNOWN));
        String crudeMessage = candidateMessages.get(random.nextInt(candidateMessages.size()));

        return crudeMessage.replace("%victim%", victim.getName()).replace("%killer%", killer != null ? killer.getName() : "Environment");
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