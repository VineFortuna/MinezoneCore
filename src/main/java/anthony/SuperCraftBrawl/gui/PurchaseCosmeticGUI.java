//package anthony.SuperCraftBrawl.gui;
//
//import anthony.SuperCraftBrawl.Core;
//import anthony.SuperCraftBrawl.cosmetics.Cosmetic;
//import anthony.SuperCraftBrawl.cosmetics.Types.KillEffect;
//import anthony.SuperCraftBrawl.gui.cosmetics.KillEffectsGUI;
//import org.bukkit.entity.Player;
//
//import java.util.function.BiConsumer;
//
//public class PurchaseCosmeticGUI extends ConfirmationGUI {
//    public class PurchaseCosmeticGUI extends ConfirmationGUI {
//        private final BiConsumer<Player, Class<? extends Cosmetic>> confirmAction;
//
//        public PurchaseCosmeticGUI(Core main, String cosmeticName, Cosmetic cosmetic) {
//            super(main, "Purchase " + cosmeticName, createConfirmAction(main));
//            this.confirmAction = createConfirmAction(main);
//        }
//
//    private static BiConsumer<Player, Class<? extends Cosmetic>> createConfirmAction(Core main) {
//        return (player, cosmeticType) -> openCosmeticTypeGUI(player, cosmeticType, main);
//    }
//
//    private void openCosmeticTypeGUI(Player player, Class<? extends Cosmetic> cosmeticType) {
//        // Implement your logic to open the corresponding cosmetic type GUI
//        // You can use the cosmeticType parameter to determine which GUI to open
//        // For example:
//        // CosmeticGUIManager.openCosmeticTypeGUI(player, cosmeticType);
//
//        if (cosmeticType.equals(KillEffect.class)) {
//             new KillEffectsGUI(main).inv.open(player);
//        } else if (cosmeticType.equals(WinEffectsGUI.class)) {
//            new WinEffectsGUI(main).inv.open(player);
//        }
//    }
//}
