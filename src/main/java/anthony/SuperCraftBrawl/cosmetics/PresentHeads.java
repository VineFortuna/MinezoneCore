package anthony.SuperCraftBrawl.cosmetics;

import anthony.util.ItemHelper;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * The red and blue present heads used by the Present Rain win effect and the Gift Explosion death effect.
 * Holds no state: every call makes a new head (each with its own profile id), so identical dropped presents
 * never merge into one stack.
 */
public final class PresentHeads {

	// minecraft-heads.com, Custom Head ID 130511 (red) and 112391 (blue)
	private static final String RED_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWM4NmUzNjFlN2EzMzE5YzQyMGExNTg5MDNkZWRkY2E0YmEyYmNhMjU4NWM1YWVlYzQwYzgxOTVjNGJhOGEyMyJ9fX0=";
	private static final String BLUE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRmZGMzNTM0N2Q1NDJmOGYxYWJkYzNkMjM0NmZmZmEyMzJjYTc3Mjc5OGE0MzdlMGI4MTc4OGIyNTNhYmI5ZSJ9fX0=";

	private PresentHeads() {
	}

	public static ItemStack red() {
		return ItemHelper.createSkullTexture(RED_TEXTURE);
	}

	public static ItemStack blue() {
		return ItemHelper.createSkullTexture(BLUE_TEXTURE);
	}

	/** A red or a blue present, picked at random. */
	public static ItemStack random(Random random) {
		return random.nextBoolean() ? red() : blue();
	}
}
