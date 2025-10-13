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

    // simple debounce to avoid accidental double-fires
    private static final Map<String, Long> DEBOUNCE = new ConcurrentHashMap<>();
    private static final long DEBOUNCE_MS = 150L;

    private ChannelInjector() {}

    public static void inject(Player p) {
        try {
            Channel channel = ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
            if (channel == null || channel.pipeline().get(HANDLER_NAME) != null) return;

            ChannelDuplexHandler handler = new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    try {
                        if (msg instanceof PacketPlayInUseEntity) {
                            PacketPlayInUseEntity packet = (PacketPlayInUseEntity) msg;

                            int id = getEntityId(packet);
                            String action = getActionName(packet); // "INTERACT", "ATTACK", "INTERACT_AT"

                            // Only handle right-click "INTERACT" once. Ignore INTERACT_AT to prevent double fire.
                            if ("INTERACT".equalsIgnoreCase(action)) {
                                // debounce per (player, entity)
                                String key = p.getUniqueId() + ":" + id;
                                long now = System.currentTimeMillis();
                                Long last = DEBOUNCE.get(key);
                                if (last == null || (now - last) >= DEBOUNCE_MS) {
                                    DEBOUNCE.put(key, now);
                                    NPCRegistry.handleUseEntity(id, true, p);
                                }
                            }
                            // ignore ATTACK and INTERACT_AT here
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
            if (channel != null && channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
            }
        } catch (Exception ignored) {}
    }

    private static int getEntityId(PacketPlayInUseEntity pkt) throws Exception {
        Field a = PacketPlayInUseEntity.class.getDeclaredField("a"); // entity id
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