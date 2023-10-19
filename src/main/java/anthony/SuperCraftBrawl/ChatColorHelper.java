package anthony.SuperCraftBrawl;

import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class ChatColorHelper {
    public static String color(String c) {
        return ChatColor.translateAlternateColorCodes('&', c);
    }

    public static List<String> colorList(List<String> s) {
        for (int i = s.size() - 1; i >= 0; i--) {
            s.set(i, ChatColor.translateAlternateColorCodes('&', s.get(i)));
        }
        return s;
    }
}