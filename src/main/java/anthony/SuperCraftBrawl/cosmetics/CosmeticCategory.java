package anthony.SuperCraftBrawl.cosmetics;

public enum CosmeticCategory {
    OUTFIT("Outfit", "Outfits"),
    GADGET("Gadget", "Gadgets"),
    PET("Pet", "Pets"),
    TRAIL("Particles", "Particles"),
    TITLE("Title", "Titles"),
    HOOK_TRAIL("Hook Trail", "Hook Trails"),
    WIN_EFFECT("Win Effect", "Win Effects"),
    DEATH_EFFECT("Death Effect", "Death Effects");

    public final String label;
    /** Plural form used for GUI category tabs/labels. */
    public final String pluralLabel;

    CosmeticCategory(String label, String pluralLabel) {
        this.label = label;
        this.pluralLabel = pluralLabel;
    }
}