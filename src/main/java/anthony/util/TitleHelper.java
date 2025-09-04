package anthony.util;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class TitleHelper {
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        // Send timing packets first
        PacketPlayOutTitle timesPacket = new PacketPlayOutTitle(
                PacketPlayOutTitle.EnumTitleAction.TIMES,
                null,
                fadeIn, stay, fadeOut
        );
        connection.sendPacket(timesPacket);

        // Send title if not null
        if (title != null) {
            String coloredTitle = ChatColorHelper.color(title);
            String jsonTitle = "{\"text\":\"" + coloredTitle.replace("\"", "\\\"") + "\"}";
            IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer.a(jsonTitle);
            connection.sendPacket(new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.TITLE,
                    titleComponent
            ));
        }

        // Send subtitle if not null
        if (subtitle != null) {
            String coloredSubtitle = ChatColorHelper.color(subtitle);
            String jsonSubtitle = "{\"text\":\"" + coloredSubtitle.replace("\"", "\\\"") + "\"}";
            IChatBaseComponent subtitleComponent = IChatBaseComponent.ChatSerializer.a(jsonSubtitle);
            connection.sendPacket(new PacketPlayOutTitle(
                    PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                    subtitleComponent
            ));
        }
    }
}
