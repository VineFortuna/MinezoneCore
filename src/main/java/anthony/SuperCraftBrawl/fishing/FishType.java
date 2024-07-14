package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public enum FishType {
    
    COD(1, "Cod", FishRarity.COMMON),
    SALMON(2, "Salmon", FishRarity.COMMON),
    TROUT(3, "Trout", FishRarity.COMMON),
    DACE(42, "Dace", FishRarity.COMMON),
    GOBY(4, "Goby", FishRarity.COMMON),
    BASS(5, "Bass", FishRarity.COMMON),
    CARP(6, "Carp", FishRarity.COMMON),
    PERCH(38, "Perch", FishRarity.COMMON),
    URCHIN(7, "Sea Urchin", FishRarity.COMMON),
    FROG(40, "Frog", FishRarity.COMMON),
    PICKLES(8, "Sea Pickles", FishRarity.RARE),
    SQUID(9, "Squid", FishRarity.RARE),
    CLOWNFISH(10, "Clownfish", FishRarity.RARE),
    SHRIMP(11, "Shrimp", FishRarity.RARE),
    STARFISH(12, "Starfish", FishRarity.RARE),
    LOBSTER(13, "Lobster", FishRarity.RARE),
    CRAB(14, "Crab", FishRarity.RARE),
    OARFISH(41, "Oarfish", FishRarity.RARE),
    GOLDFISH(48, "Goldfish", FishRarity.RARE),
    HERMITCRAB(15, "Hermit Crab", FishRarity.EPIC),
    PUFFERFISH(16, "Pufferfish", FishRarity.EPIC),
    GLOWSQUID(17, "Glow Squid", FishRarity.EPIC),
    JELLYFISH(18, "Jellyfish", FishRarity.EPIC),
    OCTOPUS(19, "Octopus", FishRarity.EPIC),
    SNAKE(43, "Sea Snake", FishRarity.EPIC),
    PARROTFISH(47, "Parrotfish", FishRarity.EPIC),
    CLAM(49, "Clam", FishRarity.EPIC),
    TURTLE(20, "Turtle", FishRarity.MYTHIC),
    NAUTILUS(21, "Nautilus", FishRarity.MYTHIC),
    SEAHORSE(39, "Seahorse", FishRarity.MYTHIC),
    PIRANHA(36, "Piranha", FishRarity.MYTHIC),
    ANGLERFISH(22, "Anglerfish", FishRarity.MYTHIC),
    EEL(44, "Electric Eel", FishRarity.MYTHIC),
    ANEMONE(50, "Sea Anemone", FishRarity.MYTHIC),
    BLOBFISH(23, "Blobfish", FishRarity.LEGENDARY),
    DOLPHIN(46, "Dolphin", FishRarity.LEGENDARY),
    LEVIATHAN(24, "Swamp Monster", FishRarity.LEGENDARY),
    SHARK(25, "Shark", FishRarity.LEGENDARY),
    WHALE(26, "Whale", FishRarity.LEGENDARY),
    LILYPAD(27, "Lilypad", FishRarity.JUNK),
    STRING(28, "Fishing Line", FishRarity.JUNK),
    BUCKET(29, "Bucket", FishRarity.JUNK),
    BOTTLE(30, "Empty Bottle", FishRarity.JUNK),
    BOOTS(31, "Old Boots", FishRarity.JUNK),
    NAUTILUSSHELL(32, "Nautilus Shell", FishRarity.JUNK),
    BOAT(37, "Broken Boat", FishRarity.JUNK),
    EXP(45, "EXP Bottle", FishRarity.TREASURE),
    TOKENS(33, "Token Sack", FishRarity.TREASURE),
    MAP(34, "Treasure Map", FishRarity.TREASURE),
    CRATE(35, "MysteryChest", FishRarity.TREASURE);
    
    private int id;
    private String name;
    private FishRarity rarity;
    
    private FishType(int id, String name, FishRarity rarity) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
    }
    
    public int getId() {
        return this.id;
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
            case DACE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZiZTFiZTE5ZTRmZmFiYTk3ZDYxYmVlZjdjYTMwMTBiNjI5OTZjNjQxZTU3MDg0ZmMzNjRhNmYzZWIxNDEzOCJ9fX0=");
            case TROUT:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2NjNGE0NTVkNTM3YzgyMTgyMGFhZDIwYjk1MzY4NjQ4NDBhODczYmM5MDE2M2FhMzU1ODY2YjMyZTM1ZDA0MCJ9fX0=");
            case GOBY:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTcwMDg3ZGRlMTMwODQ2YzBlOTc1ZmU2ODQzNWQ4OTQwZjA5MzM5ZmFiYzYwMjZhOTY5OWZkYjYxMmI0ZjEyMSJ9fX0=");
            case BASS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTVjMWMwYTBmNGE0YzNjNDFmZGZmZTY0MTBkMzg2ZjNiMTliMjc0ZjU2NDY1MjM1ZTQwYmIwNGYzZGMxODRjYiJ9fX0=");
            case CARP:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDI2MTdmMjFmZmMyM2NjZDUzNzlmZGM1OGZlNmRlMzdiNThhNGE5YzNlYzRlOWNiZGMwODQ3YjI5NTYzZmJlYiJ9fX0=");
            case PERCH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY3MTZjMWE4MGRhODVkNWU2Nzg0YzMzNmIyNTgzZDYxZGM3NmRlM2Q5OWExOTg0ZDNlNTkzNzIxZTIxMzI3In19fQ==");
            case URCHIN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODJjZDkxNGEzMGU2NGUzMDI4MDMzMGY4ODVhODVkYzc5ODM0ZWYwN2VjZGM4ZDNhY2M0Nzg5YThiZDA5MGE3YSJ9fX0=");
            case PICKLES:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFlZjVlNGViOTU4OWI1ZjQ4NmRhMDU0ZWMzNjY0NjEzYTQ5MTBlM2UyZjBmNjNlY2U1OTg1MTIwYjQxMzUzMCJ9fX0=");
            case FROG:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU4NTJhOTU5Mjg4OTc3NDYwMTI5ODhmYmQ1ZGJhYTFiNzBiN2E1ZmI2NTE1NzAxNmY0ZmYzZjI0NTM3NGMwOCJ9fX0=");
            case SQUID:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYyN2M2ZTJjNDhhMzkwYzdlOGJmZGFkZmE0MWI1MjczMWJiMGVjY2Y3MDc1Y2E4NzhmZTliMDBjYzI0MmQ1ZCJ9fX0=");
            case CLOWNFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZkZDVlNmFkZGI1NmFjYmM2OTRlYTRiYTU5MjNiMWIyNTY4ODE3OGZlZmZhNzIyOTAyOTllMjUwNWM5NzI4MSJ9fX0=");
            case SHRIMP:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzMxNGQ0NjY0OTVmNDZjZmU5MDMxNTFhMzUzZDIyY2NkZjVmYTE4YTY0ZmI2NTgzMTJhZmZiMGU3ZTg3YTMwIn19fQ==");
            case STARFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZhMjdlYzVlZDNmMDFlMzAxNjc3Zjg4ZmRiZGQ5NjJjMDgzNjg2MDA5MDdlZWMzN2EyZDRkZDhjN2Y4MzVmYyJ9fX0=");
            case OARFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWY3ZWUyMmM1NzI1MTI5NTk3NzU4OTZlYmRjYWM5NDk1Y2Q4MGIyMmMxYjc5NTQzNjFjYjE5Njk3NjIwOTljMyJ9fX0=");
            case GOLDFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTM1MmRmODVhMDJkN2ZhYzVkY2E3MmRmYmM2YmE4YWMwYTdmOTYyMDhiYzgwNDgyNDc3OTFlZjIyMTZmNWM5NCJ9fX0=");
            case LOBSTER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTNlOTA2NmVlNDljZGY3MDM3YzA4OTUyYzhmYTkzODFjNjlmYWRlNDdhMjQ5YTMwNDQ5NDkwM2Q4YjkwNTkzZCJ9fX0=");
            case PUFFERFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRmOGMzMTY5NjI5NDliYTNiZTQ0NWM5NGViZjcxNDEwODI1MmQ0NjQ1OWI2NjExMGY0YmMxNGUwZTFiNTlkYyJ9fX0=");
            case GLOWSQUID:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTVlMmI0NmU1MmFjOTJkNDE5YTJkZGJjYzljZGNlN2I0NTFjYjQ4YWU3MzlkODVkNjA3ZGIwNTAyYTAwOGNlMCJ9fX0=");
            case CLAM:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU5YzBlOWQ4M2RiZjA0ZmJkYWE3ZWIyYmRlOGU5ZmM2Nzk5NGY4MzQyMDMwMWU1ODIyNDg2ODBmYTgxOWY2MSJ9fX0=");
            case TURTLE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ==");
            case ANEMONE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA4MjM5YmExMjU2NTU4Y2UwNzMzNDMwNTNjMWUyYmQ1ZmNjZWJiMjQ0M2FlYTg4M2MzOGY0ZTEyMzA4NDg0MSJ9fX0=");
            case PARROTFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzViYTM5M2I5MjZlZTY5YTE0ZDRkODUzYjI1ZDI4NWE4NjhiMTZjNTZjOTE5ZDJiODI0NDEzZTY1NjZkNGU1NSJ9fX0=");
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
            case SEAHORSE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwZDFjZDBhNDY3YjIxNzY0YTE4M2Q2NzUzYjZjYmRjYjRmZjNiYmJmMTk4OTU2ZDYxMDJjODBmZjQ4NzIxMyJ9fX0=");
            case SNAKE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjRkNTgyMTZjNTk4NTY4NjQ0MjZmMDRmNmU0ZDMxZGFmZTgwYWFmZmQwNDdiZDA3ZjkyZDZlYTAyNzUxMzJiYiJ9fX0=");
            case PIRANHA:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTEwNzRiYTc5NjE2YzdkOGNmOGUzMzg0OTAzOWY2NzQxMGEyZjdjOWNlNzkzZDQ0N2UyMWY1YWEyNGQ1MDEwOCJ9fX0=");
            case ANGLERFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjEzOTYwMzIzZGZkMzFiNGY2MzM5ZWEwNThlMDVjMzBmYjk2Yzc1NzA0NzU5OGYxMzVlNTQ1MGZhNWQ3ZWY2MiJ9fX0=");
            case EEL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI0ZTRmNGRhNWU5YWJlOTEyMGNjNTFjOTEyZjZkMTc4MTU3NDc0MjMzMTQzMzEyNDg3OTlkMzA0NzhiMjFjOCJ9fX0=");
            case BLOBFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI2NzdiNzk0NjYzYzkyNzNlZmM4NGY2Y2I0ZTJiMzM5MjUxZGU4NGU0NWUxZjAxZDNkNDk4MmZhN2MzZGQxNyJ9fX0=");
            case LEVIATHAN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDg4YmE4YmI1MGI3OWU0NDFlNDdiN2U0NTI3NjRkNWZmZjY2OTM3NzlkMmRhZGQ5ZjdmNTJmOThkN2VhMCJ9fX0=");
            case DOLPHIN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGU5Njg4Yjk1MGQ4ODBiNTViN2FhMmNmY2Q3NmU1YTBmYTk0YWFjNmQxNmY3OGU4MzNmNzQ0M2VhMjlmZWQzIn19fQ==");
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
            case BOAT:
                return ItemHelper.create(Material.BOAT);
            case EXP:
                return ItemHelper.create(Material.EXP_BOTTLE);
            case TOKENS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2QyNGRjNTgwNjljMTIxMmI1MjlhNGFlNWQ0ZTczYmUwOTkwZDQ2ZmU5MzcxYjFmNzllODE2NGI0Mjg1OWFjOCJ9fX0=");
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
                return "Something's fishy...";
            case SALMON:
                return "Mr. Salmon, bring me a drink";
            case TROUT:
                return "Don't pout";
            case GOBY:
                return "Rudy";
            case BASS:
                return "Don't try playing this guy";
            case CARP:
                return "Holy carp!";
            case PERCH:
                return "Buy its merch!";
            case URCHIN:
                return "Handle with care";
            case PICKLES:
                return "In a pickle";
            case FROG:
                return "Ribbit";
            case SQUID:
                return "Surprisingly good at SCB";
            case CLOWNFISH:
                return "I found Nemo";
            case SHRIMP:
                return "It's as shrimple as that";
            case STARFISH:
                return "High five!";
            case OARFISH:
                return "Oarfish more like BOREfish";
            case DACE:
                return "Warning: might hit you in the face";
            case LOBSTER:
                return "Crustacean sensation";
            case PUFFERFISH:
                return "Don't make it mad";
            case GLOWSQUID:
                return "Now glows in the dark";
            case ANEMONE:
                return "The enemy of my enemy is my anemone";
            case TURTLE:
                return "I like turtles";
            case OCTOPUS:
                return "That's a lot of arms";
            case JELLYFISH:
                return "Is there a peanutbutterfish too?";
            case GOLDFISH:
                return "The snack that smiles back :)";
            case CRAB:
                return "Always crabby for some reason";
            case HERMITCRAB:
                return "Home sweet home";
            case NAUTILUS:
                return "Naut my problem";
            case CLAM:
                return "Why so clammy?";
            case PIRANHA:
                return "Looking sharp";
            case ANGLERFISH:
                return "Lurker of the depths";
            case PARROTFISH:
                return "Polly want seaweed?";
            case SEAHORSE:
                return "Hey! No horsing around";
            case SNAKE:
                return "Hisssssssssss";
            case EEL:
                return "How shocking";
            case DOLPHIN:
                return "It wasn't on porpoise";
            case BLOBFISH:
                return "What a cutie";
            case LEVIATHAN:
                return "Guardian of the loot at the bottom of the lake";
            case SHARK:
                return "Baby shark do do do dododo";
            case WHALE:
                return "How did you even reel this in?";
            case BOOTS:
                return "Who threw these in here?";
            case BOTTLE:
                return "No message in this bottle unfortunately";
            case BUCKET:
                return "There's a hole in my bucket";
            case LILYPAD:
                return "Totally not an industry plant";
            case STRING:
                return "Can't catch anything with this";
            case NAUTILUSSHELL:
                return "Something's missing...";
            case BOAT:
                return "(Don't) row row row your boat";
            case EXP:
                return "There's no experience like fishing!";
            case TOKENS:
                return "Sweet cash";
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
