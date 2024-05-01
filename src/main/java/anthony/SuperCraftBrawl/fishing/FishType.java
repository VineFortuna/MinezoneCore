package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public enum FishType {
    
    COD("Cod", FishRarity.COMMON),
    SALMON("Salmon", FishRarity.COMMON),
    TROUT("Trout", FishRarity.COMMON),
    GOBY("Goby", FishRarity.COMMON),
    BASS("Bass", FishRarity.COMMON),
    CARP("Carp", FishRarity.COMMON),
    URCHIN("Sea Urchin", FishRarity.COMMON),
    PICKLES("Sea Pickles", FishRarity.RARE),
    SQUID("Squid", FishRarity.RARE),
    CLOWNFISH("Clownfish", FishRarity.RARE),
    SHRIMP("Shrimp", FishRarity.RARE),
    STARFISH("Starfish", FishRarity.RARE),
    LOBSTER("Lobster", FishRarity.RARE),
    CRAB("Crab", FishRarity.RARE),
    HERMITCRAB("Hermit Crab", FishRarity.EPIC),
    PUFFERFISH("Pufferfish", FishRarity.EPIC),
    GLOWSQUID("Glow Squid", FishRarity.EPIC),
    JELLYFISH("Jellyfish", FishRarity.EPIC),
    OCTOPUS("Octopus", FishRarity.EPIC),
    TURTLE("Turtle", FishRarity.MYTHIC),
    NAUTILUS("Nautilus", FishRarity.MYTHIC),
    PIRANHA("Piranha", FishRarity.MYTHIC),
    ANGLERFISH("Anglerfish", FishRarity.MYTHIC),
    BLOBFISH("Blobfish", FishRarity.LEGENDARY),
    LEVIATHAN("Swamp Monster", FishRarity.LEGENDARY),
    SHARK("Shark", FishRarity.LEGENDARY),
    WHALE("Whale", FishRarity.LEGENDARY),
    LILYPAD("Lilypad", FishRarity.JUNK),
    STRING("Fishing Line", FishRarity.JUNK),
    BUCKET("Bucket", FishRarity.JUNK),
    BOTTLE("Empty Bottle", FishRarity.JUNK),
    BOOTS("Old Boots", FishRarity.JUNK),
    NAUTILUSSHELL("Nautilus Shell", FishRarity.JUNK),
    TROPHY("Fishing Trophy", FishRarity.TREASURE),
    MAP("Treasure Map", FishRarity.TREASURE),
    CRATE("MysteryChest", FishRarity.TREASURE);
    
    private String name;
    private FishRarity rarity;
    
    private FishType(String name, FishRarity rarity) {
        this.name = name;
        this.rarity = rarity;
    }
    
    public String getName() {
        return this.name;
    }
    public FishRarity getRarity() {
        return this.rarity;
    }
    public boolean isFish() {
        return this.rarity != FishRarity.JUNK && this.rarity != FishRarity.TREASURE;
    }
    public boolean isJunk() {
        return this.rarity == FishRarity.JUNK;
    }
    public boolean isTreasure() {
        return this.rarity == FishRarity.TREASURE;
    }
    
    public ItemStack getIcon() {
        switch (this) {
            case COD:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg5MmQ3ZGQ2YWFkZjM1Zjg2ZGEyN2ZiNjNkYTRlZGRhMjExZGY5NmQyODI5ZjY5MTQ2MmE0ZmIxY2FiMCJ9fX0=");
            case SALMON:
              return ItemHelper.createSkullTexture(
                      "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlYjIxYTI1ZTQ2ODA2Y2U4NTM3ZmJkNjY2ODI4MWNmMTc2Y2VhZmU5NWFmOTBlOTRhNWZkODQ5MjQ4NzgifX19");
            case TROUT:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZiZTFiZTE5ZTRmZmFiYTk3ZDYxYmVlZjdjYTMwMTBiNjI5OTZjNjQxZTU3MDg0ZmMzNjRhNmYzZWIxNDEzOCJ9fX0=");
            case GOBY:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcwMDg3ZGRlMTMwODQ2YzBlOTc1ZmU2ODQzNWQ4OTQwZjA5MzM5ZmFiYzYwMjZhOTY5OWZkYjYxMmI0ZjEyMSJ9fX0=");
            case BASS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTVjMWMwYTBmNGE0YzNjNDFmZGZmZTY0MTBkMzg2ZjNiMTliMjc0ZjU2NDY1MjM1ZTQwYmIwNGYzZGMxODRjYiJ9fX0=");
            case CARP:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI2MTdmMjFmZmMyM2NjZDUzNzlmZGM1OGZlNmRlMzdiNThhNGE5YzNlYzRlOWNiZGMwODQ3YjI5NTYzZmJlYiJ9fX0=");
            case URCHIN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODJjZDkxNGEzMGU2NGUzMDI4MDMzMGY4ODVhODVkYzc5ODM0ZWYwN2VjZGM4ZDNhY2M0Nzg5YThiZDA5MGE3YSJ9fX0=");
            case PICKLES:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFlZjVlNGViOTU4OWI1ZjQ4NmRhMDU0ZWMzNjY0NjEzYTQ5MTBlM2UyZjBmNjNlY2U1OTg1MTIwYjQxMzUzMCJ9fX0=");
            case SQUID:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYyN2M2ZTJjNDhhMzkwYzdlOGJmZGFkZmE0MWI1MjczMWJiMGVjY2Y3MDc1Y2E4NzhmZTliMDBjYzI0MmQ1ZCJ9fX0=");
            case CLOWNFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI1MTBiMzAxYjA4ODYzOGVjNWM4NzQ3ZTJkNzU0NDE4Y2I3NDdhNWNlNzAyMmM5YzcxMmVjYmRjNWY2ZjA2NSJ9fX0=");
            case SHRIMP:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzMxNGQ0NjY0OTVmNDZjZmU5MDMxNTFhMzUzZDIyY2NkZjVmYTE4YTY0ZmI2NTgzMTJhZmZiMGU3ZTg3YTMwIn19fQ==");
            case STARFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZhMjdlYzVlZDNmMDFlMzAxNjc3Zjg4ZmRiZGQ5NjJjMDgzNjg2MDA5MDdlZWMzN2EyZDRkZDhjN2Y4MzVmYyJ9fX0=");
            case LOBSTER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNlOTA2NmVlNDljZGY3MDM3YzA4OTUyYzhmYTkzODFjNjlmYWRlNDdhMjQ5YTMwNDQ5NDkwM2Q4YjkwNTkzZCJ9fX0=");
            case PUFFERFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRmOGMzMTY5NjI5NDliYTNiZTQ0NWM5NGViZjcxNDEwODI1MmQ0NjQ1OWI2NjExMGY0YmMxNGUwZTFiNTlkYyJ9fX0=");
            case GLOWSQUID:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTVlMmI0NmU1MmFjOTJkNDE5YTJkZGJjYzljZGNlN2I0NTFjYjQ4YWU3MzlkODVkNjA3ZGIwNTAyYTAwOGNlMCJ9fX0=");
            case TURTLE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ==");
            case JELLYFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjI2MTcyODE5MmI3ZDU5NmQwZTdkYjg2YjkzM2NlYWMyNmQwYzg1MDIwNmU3NDljZmNlYTg2NjM1OTMyNzFjMyJ9fX0=");
            case OCTOPUS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQ5OTQ0ZmI1ZWI4NjM5YjI4ZTc4ZGMzOGQ1NzlkODhmYjM4MGNhNTc4OWM4MWY1NTM4NDExMmQyMjdlMyJ9fX0=");
            case CRAB:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWZiODEyYWU5Zjg5MzI2YWUyNGY4NzJjODFhYjIzMjliYTYzYmRiYzk2MjBmMGIxOWRhMmFjODYxNTQ2OWUyIn19fQ==");
            case HERMITCRAB:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg1ZTY4MzRhNGJmMjZhNjUyNmY3Y2FjNGY2ZWFhOWY3ZmE3N2RiOGMxNDM1M2E4MTU4MmI1ZjY5OSJ9fX0=");
            case NAUTILUS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzI4MDVlNGE3OTgyZTcwNmViZDJlYTZjODIwOTQwMmNkN2MxY2Y5Y2MwOGI3YWFjZTNhOGFmNzcxOGNjYTdkYyJ9fX0=");
            case PIRANHA:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEwNzRiYTc5NjE2YzdkOGNmOGUzMzg0OTAzOWY2NzQxMGEyZjdjOWNlNzkzZDQ0N2UyMWY1YWEyNGQ1MDEwOCJ9fX0=");
            case ANGLERFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjEzOTYwMzIzZGZkMzFiNGY2MzM5ZWEwNThlMDVjMzBmYjk2Yzc1NzA0NzU5OGYxMzVlNTQ1MGZhNWQ3ZWY2MiJ9fX0=");
            case BLOBFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI2NzdiNzk0NjYzYzkyNzNlZmM4NGY2Y2I0ZTJiMzM5MjUxZGU4NGU0NWUxZjAxZDNkNDk4MmZhN2MzZGQxNyJ9fX0=");
            case LEVIATHAN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDg4YmE4YmI1MGI3OWU0NDFlNDdiN2U0NTI3NjRkNWZmZjY2OTM3NzlkMmRhZGQ5ZjdmNTJmOThkN2VhMCJ9fX0=");
            case SHARK:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzkwMTE2ZGE1MTRhNzNkZGQ5Mjk1NDkxODUyYWRjNTg4MDA3ZjJhY2Q3ZGUyNjk3ODhhMjEyOGQ4ZTRhNzY0YiJ9fX0=");
            case WHALE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzNhYTcxNjc2ZTgxZmI1M2EwNDBkZmRjYTNlNWI0N2Q1M2U2ZWZkNjY1ZTY5ZmI0Mzk3NzhlOGM0ZWZiMWNjIn19fQ==");
            case BOOTS:
                return ItemHelper.create(Material.LEATHER_BOOTS);
            case BOTTLE:
                return ItemHelper.create(Material.GLASS_BOTTLE);
            case BUCKET:
                return ItemHelper.create(Material.BUCKET);
            case LILYPAD:
                return ItemHelper.create(Material.WATER_LILY);
            case STRING:
                return ItemHelper.create(Material.STRING);
            case NAUTILUSSHELL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODEyNmNhYzE2ZmQ4ZTQ3NTE2ZTg0NTIwY2QzOTgxYzQ1ZDcwOGY1NWQzNDU4NDk0ZDhmMDgxYzUwNWQ2ZDMwNCJ9fX0=");
            case TROPHY:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2QwZDViNThiM2RlMjQzNGM3NWVhN2MwOGMyYjgwMmQ1OTVlZmQxMGJhZDA4YTZmNjYxZTliNThiNDkwYmFmNyJ9fX0=");
            case MAP:
                return ItemHelper.create(Material.MAP);
            case CRATE:
                return ItemHelper.create(Material.CHEST);
        }
        return ItemHelper.create(Material.BARRIER);
    }
    
    public String getDesc() {
        switch (this) {
            case COD:
                return "Boring";
            case SALMON:
                return "Mr. Salmon bring me a drink";
            case TROUT:
                return "Don't pout";
            case GOBY:
                return "Rudy";
            case BASS:
                return "Not the instrument";
            case CARP:
                return "Holy carp!";
            case URCHIN:
                return "Handle with care";
            case PICKLES:
                return "In a pickle";
            case SQUID:
                return "Surprisingly good at SCB";
            case CLOWNFISH:
                return "Is that Nemo?";
            case SHRIMP:
                return "It's as shrimple as that";
            case STARFISH:
                return "High five!";
            case LOBSTER:
                return "Crustacean sensation";
            case PUFFERFISH:
                return "Don't make it mad";
            case GLOWSQUID:
                return "Now glows in the dark";
            case TURTLE:
                return "I like turtles";
            case OCTOPUS:
                return "";
            case JELLYFISH:
                return "Is there a peanutbutterfish too?";
            case CRAB:
                return "Always crabby for some reason";
            case HERMITCRAB:
                return "";
            case NAUTILUS:
                return "";
            case PIRANHA:
                return "Looking sharp";
            case ANGLERFISH:
                return "Lurker of the depths";
            case BLOBFISH:
                return "What a cutie";
            case LEVIATHAN:
                return "Rumours say it guards loot at the bottom of the lake";
            case SHARK:
                return "Baby shark do do do dododo";
            case WHALE:
                return "How did you even reel this in?";
            case BOOTS:
                return "Who threw these in here?";
            case BOTTLE:
                return "No message in this bottle unfortunately";
            case BUCKET:
                return "";
            case LILYPAD:
                return "";
            case STRING:
                return "Can't catch anything with this";
            case NAUTILUSSHELL:
                return "Something's missing...";
            case TROPHY:
                return "Everyone's a winner!";
            case MAP:
                return "Could it lead to the fabled treasure?";
            case CRATE:
                return "I wonder what's inside";
        }
        return "No description set";
    }
    public ItemStack getItem() {
        ItemStack item = ItemHelper.setDetails(this.getIcon(), "§e§l" + this.getName());
        ArrayList<String> lore = new ArrayList<>();
        lore.add(Core.inst().color(this.getRarity().getColor()) + this.getRarity().getName());
        lore.add(ChatColor.DARK_GRAY + this.getDesc());
        ItemHelper.setLore(item, lore);
        return item;
    }
}
