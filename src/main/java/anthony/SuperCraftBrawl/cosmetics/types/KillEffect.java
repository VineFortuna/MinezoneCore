package anthony.SuperCraftBrawl.cosmetics.types;

import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
import anthony.SuperCraftBrawl.cosmetics.CosmeticRarities;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.ParticleData;

import java.awt.*;

public class KillEffect extends Cosmetic {
    private ParticleEffect particleEffect;
    private org.bukkit.util.Vector particleOffset;
    private int amount;
    private float speed;
    private Color color;
    private ParticleData particleData;
    public KillEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem, ParticleEffect particleEffect) {
        super(name, rarity, description, displayItem, KillEffect.class);
        this.particleEffect = particleEffect;
    }

    public static KillEffect createColorableKillEffect(String name, CosmeticRarities rarity, String description, ItemStack displayItem, ParticleEffect particleEffect, Color color) {
        KillEffect killEffect = new KillEffect(name, rarity, description, displayItem, particleEffect);
        killEffect.setParticleOffset(new Vector(0, 1, 0));
        killEffect.setAmount(30);
        killEffect.setSpeed(0.2f);
        killEffect.setColor(color);

        return killEffect;
    }

    public void playKillEffect(Player killer, Player killedPlayer) {
        ParticleBuilder particleBuilder = buildParticleEffect(particleEffect, amount, particleOffset, speed, color, particleData);

        particleBuilder.setLocation(killedPlayer.getLocation());
        particleBuilder.display(killer);

    }

    public ParticleBuilder buildParticleEffect(ParticleEffect particleEffect, int amount, org.bukkit.util.Vector particleOffset, float speed, Color color, ParticleData particleData) {
        ParticleBuilder particleBuilder = new ParticleBuilder(particleEffect)
                .setAmount(amount)
                .setOffset(particleOffset)
                .setSpeed(speed);

        if (color != null) {
            particleBuilder.setColor(color);
        }

        if (particleData != null) {
            particleBuilder.setParticleData(particleData);
        }

        return particleBuilder;
    }

    public void setParticleEffect(ParticleEffect particleEffect) {
        this.particleEffect = particleEffect;
    }

    public void setParticleOffset(org.bukkit.util.Vector particleOffset) {
        this.particleOffset = particleOffset;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setParticleData(ParticleData particleData) {
        this.particleData = particleData;
    }
}
