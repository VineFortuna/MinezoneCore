package anthony.SuperCraftBrawl.npcs;

import io.netty.channel.*;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ChannelInjector {
    private static final String HANDLER_NAME = "npc-interact";
    private static final Map<UUID, ChannelDuplexHandler> ACTIVE = new ConcurrentHashMap<>();

    private ChannelInjector() {}

    public static void inject(Player p) {
        try {
            Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
            if (channel.pipeline().get(HANDLER_NAME) != null) return;

            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    try {
                        if (msg instanceof PacketPlayInUseEntity) {
                            PacketPlayInUseEntity packet = (PacketPlayInUseEntity) msg;

                            // 1.8 fields: "a" (int entityId), "action" (enum)
                            int id = getEntityId(packet);
                            String action = getActionName(packet); // "INTERACT", "ATTACK", "INTERACT_AT"

                            boolean rightClick = "INTERACT".equalsIgnoreCase(action) || "INTERACT_AT".equalsIgnoreCase(action);
                            if (rightClick) {
                                NPCRegistry.handleUseEntity(id, true, p);
                            }
                        }
                    } catch (Throwable ignored) {}
                    super.channelRead(ctx, msg);
                }
            };

            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, handler);
            ACTIVE.put(p.getUniqueId(), handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uninject(Player p) {
        try {
            ChannelDuplexHandler h = ACTIVE.remove(p.getUniqueId());
            if (h == null) return;
            Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
            }
        } catch (Exception ignored) {}
    }

    private static int getEntityId(PacketPlayInUseEntity pkt) throws Exception {
        Field a = PacketPlayInUseEntity.class.getDeclaredField("a");
        a.setAccessible(true);
        return a.getInt(pkt);
    }
    private static String getActionName(PacketPlayInUseEntity pkt) throws Exception {
        Field f = PacketPlayInUseEntity.class.getDeclaredField("action");
        f.setAccessible(true);
        Object en = f.get(pkt);
        return en == null ? "" : en.toString();
    }
}