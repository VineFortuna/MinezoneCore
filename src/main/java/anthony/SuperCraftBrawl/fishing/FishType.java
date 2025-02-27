package anthony.SuperCraftBrawl.fishing;

import anthony.SuperCraftBrawl.Core;
import anthony.util.ItemHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum FishType {

    COD(1, "Cod", FishRarity.COMMON, null),
    SALMON(2, "Salmon", FishRarity.COMMON, Arrays.asList(FishArea.Pond, FishArea.Woods)),
    TROUT(3, "Trout", FishRarity.COMMON, Arrays.asList(FishArea.Woods)),
    ANCHOVY(90, "Anchovy", FishRarity.COMMON, null),
    DACE(42, "Dace", FishRarity.COMMON, null),
    GOBY(4, "Goby", FishRarity.COMMON, null),
    BASS(5, "Bass", FishRarity.COMMON, Arrays.asList(FishArea.Pond, FishArea.Park, FishArea.Woods)),
    CARP(6, "Carp", FishRarity.COMMON, Arrays.asList(FishArea.Pond, FishArea.Park)),
    PERCH(38, "Perch", FishRarity.COMMON, Arrays.asList(FishArea.Pond)),
    TUNA(68, "Tuna", FishRarity.COMMON, null),
    URCHIN(7, "Sea Urchin", FishRarity.COMMON, null),
    TADPOLE(76, "Tadpole", FishRarity.COMMON, null),
    FROG(40, "Frog", FishRarity.COMMON, null),
    PORGY(98, "Porgy", FishRarity.COMMON, Arrays.asList(FishArea.Pond)),
    CAVEFISH(99, "Blind Cave Fish", FishRarity.COMMON, Arrays.asList(FishArea.Cavern)),
    SEWERFISH(53, "Sewer Fish", FishRarity.COMMON, Arrays.asList(FishArea.Sewers)),
    SLIMYJELLYFISH(89, "Slimy Jellyfish", FishRarity.COMMON, Arrays.asList(FishArea.Sewers)),
    ROCKFISH(63, "Brown Rockfish", FishRarity.COMMON, null),
    FLOUNDER(65, "Flounder", FishRarity.COMMON, null),
    KOI(69, "Koi", FishRarity.COMMON, Arrays.asList(FishArea.Pond, FishArea.Park)),
    GROUPER(93, "Grouper", FishRarity.COMMON, null),
    SEAGULL(83, "Seagull", FishRarity.COMMON, Arrays.asList(FishArea.Park)),
    SQUID(9, "Squid", FishRarity.COMMON, Arrays.asList(FishArea.Pond)),
    SEASNAIL(87, "Sea Snail", FishRarity.COMMON, Arrays.asList(FishArea.Pond)),
    SANDDOLLAR(91, "Sand Dollar", FishRarity.COMMON, null),
    PICKLES(8, "Sea Pickles", FishRarity.RARE, Arrays.asList(FishArea.Pond, FishArea.LushCave)),
    CLOWNFISH(10, "Clownfish", FishRarity.RARE, Arrays.asList(FishArea.LushCave)),
    SHRIMP(11, "Shrimp", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    STARFISH(12, "Starfish", FishRarity.RARE, null),
    LOBSTER(13, "Lobster", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    CRAB(14, "Crab", FishRarity.RARE, null),
    TURTLE(20, "Turtle", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    REDSNAPPER(66, "Red Snapper", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    GOLDFISH(48, "Goldfish", FishRarity.RARE, Arrays.asList(FishArea.Pond, FishArea.Park)),
    CATFISH(51, "Catfish", FishRarity.RARE, Arrays.asList(FishArea.Park, FishArea.Woods)),
    DUCK(54, "Duck", FishRarity.RARE, Arrays.asList(FishArea.Park)),
    OTTER(70, "Otter", FishRarity.RARE, Arrays.asList(FishArea.Park, FishArea.Woods)),
    PELICAN(95, "Pelican", FishRarity.RARE, Arrays.asList(FishArea.Pond, FishArea.Park, FishArea.Woods)),
    MINTYGOBBLER(71, "Minty Gobbler", FishRarity.RARE, Arrays.asList(FishArea.Woods)),
    /*SEAL(##, "Seal", FishRarity.RARE, null),*/
    DOLPHINCICHLID(88, "Blue Dolphin Cichlid", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    YELLOWTAILPARROT(78, "Yellowtail Parrotfish", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    QUEENANGELFISH(79, "Queen Angelfish", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    PINEAPPLEFISH(80, "Pineapplefish", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    RADIOACTIVEFISH(85, "Radioactive Fish", FishRarity.RARE, Arrays.asList(FishArea.Sewers)),
    GREENRAZORFISH(96, "Green Razorfish", FishRarity.RARE, Arrays.asList(FishArea.Pond)),
    HERMITCRAB(15, "Hermit Crab", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    PUFFERFISH(16, "Pufferfish", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    GLOWSQUID(17, "Glow Squid", FishRarity.EPIC, Arrays.asList(FishArea.LushCave)),
    JELLYFISH(18, "Jellyfish", FishRarity.EPIC, Arrays.asList(FishArea.Pond, FishArea.LushCave)),
    OCTOPUS(19, "Octopus", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    AXOLOTL(61, "Axolotl", FishRarity.EPIC, Arrays.asList(FishArea.LushCave)),
    SNAKE(43, "Sea Snake", FishRarity.EPIC, null),
    PARROTFISH(47, "Parrotfish", FishRarity.EPIC, Arrays.asList(FishArea.Pond, FishArea.LushCave)),
    CLAM(49, "Clam", FishRarity.EPIC, Arrays.asList(FishArea.Pond, FishArea.Woods)),
    CHROMIS(52, "Blue Green Chromis", FishRarity.EPIC, Arrays.asList(FishArea.LushCave)),
    NURSESHARK(59, "Nurse Shark", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    AMBERFIN(72, "Amberfin", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    LEAFSEASLUG(84, "Leaf Sheep Sea Slug", FishRarity.EPIC, null),
    VIOLETSEASLUG(94, "Violet Sea Slug", FishRarity.EPIC, null),
    LIZARDFISH(82, "Deepsea Lizardfish", FishRarity.EPIC, Arrays.asList(FishArea.Cavern)),
    SNUBFINDOLPHIN(92, "Snubfin Dolphin", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    PURPLEANTHIAS(97, "Purple Queen Anthias", FishRarity.EPIC, Arrays.asList(FishArea.Pond)),
    OARFISH(41, "Oarfish", FishRarity.MYTHIC, Arrays.asList(FishArea.Pond)),
    NAUTILUS(21, "Nautilus", FishRarity.MYTHIC, null),
    SEAHORSE(39, "Seahorse", FishRarity.MYTHIC, null),
    PIRANHA(36, "Piranha", FishRarity.MYTHIC, Arrays.asList(FishArea.LushCave)),
    ANGLERFISH(22, "Anglerfish", FishRarity.MYTHIC, null),
    EEL(44, "Electric Eel", FishRarity.MYTHIC, Arrays.asList(FishArea.Woods, FishArea.Sewers)),
    BULLHEAD(67, "Bullhead Shark", FishRarity.MYTHIC, Arrays.asList(FishArea.Pond)),
    MANATEE(60, "Manatee", FishRarity.MYTHIC, Arrays.asList(FishArea.Pond)),
    ANEMONE(50, "Sea Anemone", FishRarity.MYTHIC, null),
    FLAPJACK(62, "Flapjack Octopus", FishRarity.MYTHIC, null),
    CROCODILE(64, "Crocodile", FishRarity.MYTHIC, Arrays.asList(FishArea.Pond, FishArea.Sewers)),
    BEARDFISH(77, "Beardfish", FishRarity.MYTHIC, Arrays.asList(FishArea.Cavern)),
    GUARDIAN(55, "Guardian", FishRarity.MYTHIC, null),
    KINGSALMON(73, "King Salmon", FishRarity.MYTHIC, Arrays.asList(FishArea.Woods)),
    BLOBFISH(23, "Blobfish", FishRarity.LEGENDARY, null),
    COELACANTH(86, "Coelacanth", FishRarity.LEGENDARY, null),
    DOLPHIN(46, "Dolphin", FishRarity.LEGENDARY, Arrays.asList(FishArea.Pond)),
    LEVIATHAN(24, "Swamp Monster", FishRarity.LEGENDARY, Arrays.asList(FishArea.LushCave)),
    SHARK(25, "Shark", FishRarity.LEGENDARY, Arrays.asList(FishArea.Pond)),
    WHALE(26, "Whale", FishRarity.LEGENDARY, Arrays.asList(FishArea.Pond)),
    WHALESHARK(58, "Whale Shark", FishRarity.LEGENDARY, Arrays.asList(FishArea.Pond)),
    ELDERGUARDIAN(56, "Elder Guardian", FishRarity.LEGENDARY, null),
    LILYPAD(27, "Lilypad", FishRarity.JUNK, Arrays.asList(FishArea.LushCave)),
    STRING(28, "Fishing Line", FishRarity.JUNK, null),
    BUCKET(29, "Bucket", FishRarity.JUNK, null),
    BOTTLE(30, "Empty Bottle", FishRarity.JUNK, Arrays.asList(FishArea.Pond, FishArea.Park, FishArea.Woods)),
    BOOTS(31, "Old Boots", FishRarity.JUNK, Arrays.asList(FishArea.Pond, FishArea.Park, FishArea.Woods)),
    NAUTILUSSHELL(32, "Nautilus Shell", FishRarity.JUNK, null),
    BOAT(37, "Broken Boat", FishRarity.JUNK, null),
    CLAY(57, "Lump of Clay", FishRarity.JUNK, null),
    SLIMEBALL(74, "Slimeball", FishRarity.JUNK, Arrays.asList(FishArea.Sewers)),
    BONE(75, "Bone", FishRarity.JUNK, Arrays.asList(FishArea.Cavern)),
    FEATHER(81, "Feather", FishRarity.JUNK, Arrays.asList(FishArea.Park)),
    APPLE(100, "Fallen Apple", FishRarity.JUNK, Arrays.asList(FishArea.Woods)),
    EXP(45, "EXP Bottle", FishRarity.TREASURE, null),
    TOKENS(33, "Token Sack", FishRarity.TREASURE, null),
    MAP(34, "Treasure Map", FishRarity.TREASURE, null),
    CRATE(35, "MysteryChest", FishRarity.TREASURE, null);

    private int id;
    private String name;
    private FishRarity rarity;
    private List<FishArea> areas;

    private FishType(int id, String name, FishRarity rarity, List<FishArea> areas) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.areas = areas;
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
    public List<FishArea> getAreas() {
        return this.areas;
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
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2M2ZWEzOTQzNTgxOGNjMTBjOTg0ZjE5MzVkYzZhNTRkZjFlNTQ3ZWVkZTM3ODljZmFhZmQyMDNjOWZhNmQxMyJ9fX0=");
            case ANCHOVY:
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
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ4MDc0OTNkZWYzZWZkZjNjYzQ4MjI3NmNlYTE2ZWVlM2UwNTIxMTgwZjY0ZDVlNThmNjg1MTBkYWY3YjQ0MyJ9fX0=");
            case PERCH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWY3MTZjMWE4MGRhODVkNWU2Nzg0YzMzNmIyNTgzZDYxZGM3NmRlM2Q5OWExOTg0ZDNlNTkzNzIxZTIxMzI3In19fQ==");
            case PORGY:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODMyYjg5YjcwY2YzYTE3MjVlNWZlZDg5YTdiMDI5ZGI4MmU5OTRkOWZhMDgwYmMxZTVkNzdiMGM4NmNhMzRiZSJ9fX0=");
            case CAVEFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzM2YTlhZGQyNTY0NWJmY2MzNzdjMjVlZjBjMmU5OTAxZDE5NDkzYzNlOTgxZWJjNmJhN2ExYTFiNjQ2NmNlNCJ9fX0=");
            case SEWERFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQ4Yjg2NWY4ZjE1NGY3Mzg5NGMyNTgzZGY1N2UxNGJiYzdiYTgwZjU4YjM5OTJlYmIxNmI1MTU1ZTAzNzY2MSJ9fX0=");
            case GROUPER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjBhNTMzMzk3NzFlZGMyZGM2ZWNjYjRmNjg2ZmU2Njg3YmMwZjBiZjk2M2Y4MzVlYjgzOWM3NDlkNDkzNDg2MiJ9fX0=");
            case SANDDOLLAR:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDMyZTI5NGExMTk1NmRlYjBiNWNhZTNiNWZhZWRkMDI2ODExNmQ1MTc2ZjhlYWNkNWVjZmQxMmIxY2Y2MDAwZCJ9fX0=");
            case SLIMYJELLYFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzVkMDRlNTBlZDgxNThhZTZmOWY3YmJiMDMyZDFiZTU5OTVhZmE5OTM5N2NhODRlNTJmMGMzMGIwMWEzMDY1NyJ9fX0=");
            case ROCKFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU0NDQ2M2JmMGIxYzIzMjZhMTYwYjVjYWNmMGYxYzgwZDVmYmVjMDMwZjk1ZjdmM2Q4MTczOTAyNGEzNTA0YiJ9fX0=");
            case TUNA:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmU1ZTQyY2Q4OTE2MWU1ODMzYTFiMzAwODI1MDVhZjYwNWM0YjBhNGZhNDVkMGY3NWNhYTZjYzZhZjA5NTM3OCJ9fX0=");
            case KOI:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmM1NzNkYTk2NzcyOWI1MmFlODMwYjFiMThhNmVkNjkxNTc5NjNiNGQyN2QxYjVlZmMwZWQ4OWVlMGZiM2ZjMSJ9fX0=");
            case SEAGULL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDJhOTM4NjgwNjZmOWFiN2ZhZTFjNWE5NGE3YjRjNTVlMDAyODFkMDcwNzgzY2YxMzM3NzliMmY4YmNlOTQ2YiJ9fX0=");
            case FLOUNDER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGI4OGY4OGYzMDUzYzQzNDY2MGVlYjRjN2IyMzQ0YmMyMWFiNTI1OTZjZWE1YTY2ZDBmOWRiOGMwZTA1MDIwOSJ9fX0=");
            case URCHIN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODJjZDkxNGEzMGU2NGUzMDI4MDMzMGY4ODVhODVkYzc5ODM0ZWYwN2VjZGM4ZDNhY2M0Nzg5YThiZDA5MGE3YSJ9fX0=");
            case PICKLES:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFlZjVlNGViOTU4OWI1ZjQ4NmRhMDU0ZWMzNjY0NjEzYTQ5MTBlM2UyZjBmNjNlY2U1OTg1MTIwYjQxMzUzMCJ9fX0=");
            case TADPOLE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTg3MDM1ZjUzNTIzMzRjMmNiYTZhYzRjNjVjMmI5MDU5NzM5ZDZkMGU4MzljMWRkOThkNzVkMmU3Nzk1Nzg0NyJ9fX0=");
            case FROG:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDU4NTJhOTU5Mjg4OTc3NDYwMTI5ODhmYmQ1ZGJhYTFiNzBiN2E1ZmI2NTE1NzAxNmY0ZmYzZjI0NTM3NGMwOCJ9fX0=");
            case SQUID:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWYyN2M2ZTJjNDhhMzkwYzdlOGJmZGFkZmE0MWI1MjczMWJiMGVjY2Y3MDc1Y2E4NzhmZTliMDBjYzI0MmQ1ZCJ9fX0=");
            case SEASNAIL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVmM2MwZjUzNjEyMzc0NjU5NjA2MzVjZWIzYWRjY2I2OGM0NWZkZTU2NjNiYzcyNTQ4Y2IzZGUyOTA0M2M4In19fQ==");
            case CLOWNFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZkZDVlNmFkZGI1NmFjYmM2OTRlYTRiYTU5MjNiMWIyNTY4ODE3OGZlZmZhNzIyOTAyOTllMjUwNWM5NzI4MSJ9fX0=");
            case SHRIMP:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzMxNGQ0NjY0OTVmNDZjZmU5MDMxNTFhMzUzZDIyY2NkZjVmYTE4YTY0ZmI2NTgzMTJhZmZiMGU3ZTg3YTMwIn19fQ==");
            case STARFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZhMjdlYzVlZDNmMDFlMzAxNjc3Zjg4ZmRiZGQ5NjJjMDgzNjg2MDA5MDdlZWMzN2EyZDRkZDhjN2Y4MzVmYyJ9fX0=");
            case PELICAN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmU2MDljNjVlMDQ2OTg1ZjY0ODg4ZjRiN2I2NGRlOGY2YmYyM2UzOGE2MjYwZGIyNzM1ODY2ODE4YmIxZjMxMyJ9fX0=");
            case OARFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWY3ZWUyMmM1NzI1MTI5NTk3NzU4OTZlYmRjYWM5NDk1Y2Q4MGIyMmMxYjc5NTQzNjFjYjE5Njk3NjIwOTljMyJ9fX0=");
            case GOLDFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTM1MmRmODVhMDJkN2ZhYzVkY2E3MmRmYmM2YmE4YWMwYTdmOTYyMDhiYzgwNDgyNDc3OTFlZjIyMTZmNWM5NCJ9fX0=");
            case CATFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDI1ZjU0ZDNkODRkYzQ0OGQ0MWU4NjFjODVmZmNhNmVkNTUyYjA4ZjdmNjhjYjc1NmE4MzgzYjc1MDUxNzQ2ZSJ9fX0=");
            case OTTER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWE4N2MzMTdjNjRjNjY4NTkyNjBlNzExMTFiMjdiNGIzNTFiN2M4NjAzOWNhZDdjN2U1YjAyNzQ3MjM0YmE5NCJ9fX0=");
            /*case SEAL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVhZWM1ZTE1NTA3NzY4YmU2OWMwYjRmYTkzN2ZhZThjZDk0ZDQ5NDJiNTFiMTIwZmQxMWJjZjRiNGIxY2I3OCJ9fX0=");*/
            case MINTYGOBBLER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjhjNTc0ZWY2ODJlNmM5YzRmNTY4ZWQxYjcwOGQwZmUyODBhMzg5OTA1NDI5OTFjNWQzZWI3YWJhMzEyOGI3ZCJ9fX0=");
            case DOLPHINCICHLID:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTNiYjRkYmQ2MmViNWVmMTdhMWQxMmVkOTY0ZDc4MjlkOWQwYTNkNzg0ZWFkN2E4ZDU5YzExOWM0MzI4YTFmZCJ9fX0=");
            case YELLOWTAILPARROT:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWQ3YjkyYTFiZGM3MzNkMmE1M2ZiMDk1YzcwYjczMmI3MWEyMjNhOWNlY2E3MmI4NzJlMWVlOTkwYTZhOTA3ZSJ9fX0=");
            case QUEENANGELFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWRmYjRkN2RmZjU0NTZkZTcwNDk2NDE0ZTg3NGM2NjUwYTFlODM5NTYzY2M3NWQ0Yjk5MzgwYjIxNmNmZGJjZCJ9fX0=");
            case RADIOACTIVEFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjBlMjM2M2MyZDQxYTlkMzIzYmE2MjVkZThjMDYzNzA2M2EzNmZlODVhMDQ1ZGUyNzVhN2I3NzM5ZGVkNjA1MSJ9fX0=");
            case REDSNAPPER:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI4MjI2YWI5ODcwYjdjNmM1OWQzMWI0MGI4NmVkZjczMjRlY2E2NzRiNzBkNTViZThhYWU2YTBiODllN2Q4YiJ9fX0=");
            case DUCK:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmU2ODY0NzAxMWZmMjI0NzlmYTgzYTM4YzI0N2U5NWFiZmY2ZGUyMTJhMzE3NjAzYjg5MTVkYzg0MDNhOTU0YSJ9fX0=");
            case GREENRAZORFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODYwNjc5ZDA0YmQ3ZTIzMjVmZGYwZDE1ODNlMmMwY2Y4YWQ2NDQ4ZGIwZTU3NjA5NWJjZjI2MDU4ZTU1MzY3OCJ9fX0=");
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
            case CHROMIS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UxN2FhNGYxZDA0MGM0MzZlNmEzYjFhOTcwNGJlMjYxMDIwOGE3ZGI1Mjk0NDlkMzFmMmEyMzQwMzcxMGRmMiJ9fX0=");
            case PURPLEANTHIAS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNjOTYxYmM4YTZiYWYzMTc4ZDczNGE5NGQwODAyYzFkYjYzM2Q4OGZiMmE0YjEwMGRmYjU0ZTEzYzM5NTlmMiJ9fX0=");
            case TURTLE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMGE0MDUwZTdhYWNjNDUzOTIwMjY1OGZkYzMzOWRkMTgyZDdlMzIyZjlmYmNjNGQ1Zjk5YjU3MThhIn19fQ==");
            case ANEMONE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTA4MjM5YmExMjU2NTU4Y2UwNzMzNDMwNTNjMWUyYmQ1ZmNjZWJiMjQ0M2FlYTg4M2MzOGY0ZTEyMzA4NDg0MSJ9fX0=");
            case PARROTFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzViYTM5M2I5MjZlZTY5YTE0ZDRkODUzYjI1ZDI4NWE4NjhiMTZjNTZjOTE5ZDJiODI0NDEzZTY1NjZkNGU1NSJ9fX0=");
            case AXOLOTL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWMxMzhmNDAxYzY3ZmMyZTFlMzg3ZDljOTBhOTY5MTc3MmVlNDg2ZThkZGJmMmVkMzc1ZmM4MzQ4NzQ2ZjkzNiJ9fX0=");
            case JELLYFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjI2MTcyODE5MmI3ZDU5NmQwZTdkYjg2YjkzM2NlYWMyNmQwYzg1MDIwNmU3NDljZmNlYTg2NjM1OTMyNzFjMyJ9fX0=");
            case OCTOPUS:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQ5OTQ0ZmI1ZWI4NjM5YjI4ZTc4ZGMzOGQ1NzlkODhmYjM4MGNhNTc4OWM4MWY1NTM4NDExMmQyMjdlMyJ9fX0=");
            case NURSESHARK:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjlhNGZhZmNlNjk1YmYxNzI5MWViN2VmYzhjNjU1MTJmNzMwZjk2MTEwMjllM2UyZTIwMzMyOTNkMzBiNDg1MyJ9fX0=");
            case AMBERFIN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFjMTVhMjA4YjU4ZDA0Zjg5MmUxZDYwZmU4M2JmYWI5YWQwYjljZmMzMTAzMTkwMjI0ZmEyMjQ2NDAzNTA4OSJ9fX0=");
            case PINEAPPLEFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDlhNTI2NzNjNzdhYzA3OWVkMzQ3MmI2OWZlNGU5MWJkMjgwMTlmMjhmM2U5ZDg1YmY5NGRmYWQ2ZTM5OGM0ZiJ9fX0=");
            case LEAFSEASLUG:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzU1ODAzZWEyMWEyNDM5M2I1ZjVlMTZiNzIxMjFlNjU4YzY3Njk2ZmZhNTk5YjEzZDgxMjdhN2ZlZjUxODNiIn19fQ==");
            case VIOLETSEASLUG:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDFkZWQ2MjQwNTUyMmVjNzJiOGM0ZWYzMDZlMTA5OGIzZWRlNzY1ZDk3MWQ1NzRmZmEwM2MwYjA5OWY4MDNhNiJ9fX0=");
            case SNUBFINDOLPHIN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTU0NjI0ZmUwZTBiMjBmNmZlNjdhMDY4ZWJjMmY1ODhmNjlmZDQ0MmI1NTUzNjhjZDRiNjZmNGFhYmE2MzE3NSJ9fX0=");
            case CRAB:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWZiODEyYWU5Zjg5MzI2YWUyNGY4NzJjODFhYjIzMjliYTYzYmRiYzk2MjBmMGIxOWRhMmFjODYxNTQ2OWUyIn19fQ==");
            case HERMITCRAB:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkwNGUyMGE0MjdjMGRhMDlkYjAxNjYzYzEwMTA5MWNmMDdmNzEzNTdmOWQyMTY3YWY2NGNkODRmMjcwOWIzOCJ9fX0=");
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
            case FLAPJACK:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjQ3YmFkODIyNDlhNWEyNWZlZjhiNTM3MWRhMWMwNDBlNDhmNzZjNTVmZGQ4ZGExOGIyZjRiOTliNzU1NzFjNCJ9fX0=");
            case BULLHEAD:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZlNmZiNjZiZmU5NzM3YTMzYmE1M2YzZDVhOGI5ZmRiYTFjNjVhNWQyNzYwYmNkOWE2ZTU0OTI4NTI3MTAzYiJ9fX0=");
            case CROCODILE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThjMDZkMjE4MjljYjQyMzFmODc4OTM0MTU0YTcyNWQ1ZGMzMzVlZjhjZDU0NzdhZTdhYzczOWE0ZmI1ZjE4YSJ9fX0=");
            case EEL:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTI0ZTRmNGRhNWU5YWJlOTEyMGNjNTFjOTEyZjZkMTc4MTU3NDc0MjMzMTQzMzEyNDg3OTlkMzA0NzhiMjFjOCJ9fX0=");
            case MANATEE:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTdlMjAxMTgwNjQyYTM2YTM4ZmZjNjhiZmJkYjM5ZWQyNDExNWMyM2Y0MzdkMzY3ZDU3ZTY5MDRhYTRhNDc3ZSJ9fX0=");
            case BEARDFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjJmYzlmNzhjZTU5OTFjYmVmZjgxNDIwMjg5NGY4YWIyYTQ3MTJlNDIzYWUzN2UyZGEyNWFhZmYyYWMzYiJ9fX0=");
            case LIZARDFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I3MmZiNWEzODQyYzdjMjAzOTg0NTMxZDI3MjJmYTkxMmZkZTAyNmM4NTI5ZDMzZjcxMTNlYWNmZDhhNjA0ZCJ9fX0=");
            case GUARDIAN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzI4ZGI2YjA1NDIzNWU0NTFkNjY2ZmM2NDRhMDg2NjMyYzZhYjIyYzdjZDUzNTY1YWU4MjZlMWQ1Y2MwYjE3In19fQ==");
            case KINGSALMON:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjc3NTI5OTA3Mzk3MWI1Nzc2ODBiOTUxMzQxMTBhMTMxYWEzMzlhY2QzOGZiMDliMDU2YzE2YWU5MjQ5ZDY3MCJ9fX0=");
            case BLOBFISH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzI2NzdiNzk0NjYzYzkyNzNlZmM4NGY2Y2I0ZTJiMzM5MjUxZGU4NGU0NWUxZjAxZDNkNDk4MmZhN2MzZGQxNyJ9fX0=");
            case COELACANTH:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGFkNjhiNmE5MzYzZGUyMzk2Zjk2N2E1NmMyZTM0YTJkMjNkYjAyZDYxN2RkMWEwZjY1NGE2ZWI3MWIwODc4In19fQ==");
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
            case WHALESHARK:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTJlMDVlYzU2NDYzN2Q5NjI4ZWNkOWUzZjMxMmIwNTg4ZDliOGIyYzhkYjA0NzE0MmYxMjAwNjVhNmFhOTNjOCJ9fX0=");
            case ELDERGUARDIAN:
                return ItemHelper.createSkullTexture(
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM0MGEyNjhmMjVmZDVjYzI3NmNhMTQ3YTg0NDZiMjYzMGE1NTg2N2EyMzQ5ZjdjYTEwN2MyNmViNTg5OTEifX19");
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
            case CLAY:
                return ItemHelper.create(Material.CLAY_BALL);
            case SLIMEBALL:
                return ItemHelper.create(Material.SLIME_BALL);
            case BONE:
                return ItemHelper.create(Material.BONE);
            case FEATHER:
                return ItemHelper.create(Material.FEATHER);
            case APPLE:
                return ItemHelper.create(Material.APPLE);
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
                return "You've cod to be kidding me!";
            case SALMON:
                return "Mr. Salmon, bring me a drink";
            case TROUT:
                return "Don't pout";
            case GOBY:
                return "Rudy";
            case BASS:
                return "Don't try playing this guy";
            case ANCHOVY:
                return "Does not belong on pizza";
            case DACE:
                return "Warning: might hit you in the face";
            case CARP:
                return "Oh carp!";
            case PERCH:
                return "Buy its merch!";
            case SEWERFISH:
                return "Eww";
            case PORGY:
                return "Georgie Porgy pudding and pie, making waves oh my";
            case CAVEFISH:
                return "Legally blind";
            case SLIMYJELLYFISH:
                return "Slime first, sting second";
            case ROCKFISH:
                return "Rockin' the sea";
            case TUNA:
                return "Tuna in, this catch is off the scales!";
            case KOI:
                return "Oh boy...";
            case SANDDOLLAR:
                return "Not accepted at any store";
            case GROUPER:
                return "Group hug? No thanks!";
            case SEAGULL:
                return  "Hide your snacks";
            case FLOUNDER:
                return "Flat out amazing!";
            case URCHIN:
                return "Handle with care";
            case PICKLES:
                return "In a pickle";
            case TADPOLE:
                return "It'll grow on you";
            case FROG:
                return "Ribbit";
            case SQUID:
                return "Surprisingly good at SCB";
            case SEASNAIL:
                return "Slow and steady wins the race";
            case CLOWNFISH:
                return "I found Nemo!";
            case SHRIMP:
                return "It's as shrimple as that";
            case STARFISH:
                return "High five!";
            case OARFISH:
                return "Oarfish more like BOREfish";
            case REDSNAPPER:
                return "Red hot and ready to snap";
            case CATFISH:
                return "This catfish is the real deal";
            case OTTER:
                return "Hello from the otter side";
            case PELICAN:
                return "Pouch game strong";
            case MINTYGOBBLER:
                return "A breath of fresh sea air";
            case DOLPHINCICHLID:
                return "Not a dolphin, just blue";
            case QUEENANGELFISH:
                return "The reef's royal beauty";
            case YELLOWTAILPARROT:
                return "Not much of a talker, despite the name";
            case RADIOACTIVEFISH:
                return "I'm radioactive, radioactive";
            case GREENRAZORFISH:
                return "Ow! The edge!";
            case LOBSTER:
                return "Crustacean sensation";
            case PUFFERFISH:
                return "Handles stress poorly";
            case GLOWSQUID:
                return "Now glows in the dark";
            case ANEMONE:
                return "The enemy of my enemy is my anemone";
            case TURTLE:
                return "I like turtles";
            case OCTOPUS:
                return "That's a lot of arms";
            case NURSESHARK:
                return "Not as scary as it looks";
            case AMBERFIN:
                return "Gold and bold";
            case PINEAPPLEFISH:
                return "An ocean delight";
            case PURPLEANTHIAS:
                return "Purple power";
            case LEAFSEASLUG:
                return "Leaf me alone!";
            case VIOLETSEASLUG:
                return "No need for violence, just a little violet";
            case SNUBFINDOLPHIN:
                return "Short snout, big heart";
            case JELLYFISH:
                return "Is there a peanutbutterfish too?";
            case GOLDFISH:
                return "The snack that smiles back :)";
            case DUCK:
                return "Quack, quack quack, quack quack";
            case CRAB:
                return "Always crabby for some reason";
            case HERMITCRAB:
                return "Home sweet home";
            case NAUTILUS:
                return "Naut my problem";
            case CLAM:
                return "Why so clammy?";
            case CHROMIS:
                return "Doesn't that make turquoise?";
            case AXOLOTL:
                return "Totally a-lotl fun!";
            /*case SEAL:
                return "Sealed and delivered";*/
            case PIRANHA:
                return "Looking sharp";
            case ANGLERFISH:
                return "The ocean's creepiest lamp";
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
            case CROCODILE:
                return "In a while, crocodile";
            case BEARDFISH:
                return "Now that's a look";
            case LIZARDFISH:
                return "Lizard in name, but a fish in the game";
            case BULLHEAD:
                return "Hope you're not wearing red";
            case FLAPJACK:
                return "A jack of all trades";
            case MANATEE:
                return "Would you like a cup-a-tea?";
            case GUARDIAN:
                return "Guardian of the deep";
            case KINGSALMON:
                return "Bow down to the king of the stream";
            case COELACANTH:
                return "A fish so old, it remembers when the ocean was black and white";
            case BLOBFISH:
                return "What a cutie";
            case LEVIATHAN:
                return "Keeper of the loot at the bottom of the lake";
            case SHARK:
                return "Baby shark do do do dododo";
            case WHALE:
                return "How did you even reel this in?";
            case WHALESHARK:
                return "It's a whale! It's a shark! It's a WHALESHARK!!";
            case ELDERGUARDIAN:
                return "Guardian of the deep... but older";
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
            case CLAY:
                return "A solid find";
            case SLIMEBALL:
                return "Icky sticky";
            case BONE:
                return "Looks like someone had a bad day";
            case BOAT:
                return "(Don't) row row row your boat";
            case FEATHER:
                return "No tickling allowed!";
            case APPLE:
                return "An apple a day keeps the doctor away";
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
