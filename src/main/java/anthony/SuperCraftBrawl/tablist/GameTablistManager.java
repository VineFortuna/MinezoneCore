package anthony.SuperCraftBrawl.tablist;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.GameInstance;
import anthony.SuperCraftBrawl.Game.GameManager;
import anthony.flagwars.FWGameInstance;
import anthony.flagwars.FWGameManager;
import anthony.flagwars.FWTeam;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.PlayerInfoData;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A player in a game only sees the people of that game in their tab list, named the way they are above their
 * heads: "[Y] Name" in Flag Wars, "Class Name" in Super Craft Bros. Players in the lobby still see everyone,
 * with their normal rank names, including the players who are in games.
 *
 * <p>How: the client builds its tab list from "player info" packets, so each viewer's connection gets a small
 * filter (the same kind of Netty handler as npcs.ChannelInjector) that drops the info of players outside their game
 * and swaps the name of the ones inside it. That also catches the packets Bukkit sends by itself (somebody
 * joining, showPlayer, a rank change). A short repeating check moves each viewer in or out of that filter and sends
 * the removals and name changes, so none of the many places that join or leave a game need to know about this.
 *
 * <p>The one rule that must never break: a 1.8 client can't show a player it has no tab entry for, and the server
 * never sends the entry again before it spawns somebody. So (1) only players in a different world than the viewer are
 * ever taken out of their tab list, and (2) the filter puts an entry back on the spot, right before the packet
 * that spawns that player, if the viewer's client could be missing it. Players moving between the lobby and a game
 * world therefore never become invisible, however late the repeating check runs.
 *
 * <p>The filter only touches players who are online, so NPCs and anything else in the tab list pass through.
 */
public class GameTablistManager implements Listener {

    private static final String HANDLER_NAME = "scb-game-tablist";
    private static final long CHECK_TICKS = 5L;

    private static final Field ACTION_FIELD;
    private static final Field LIST_FIELD;
    private static final Field SPAWN_UUID_FIELD;

    static {
        Field action = null;
        Field list = null;
        Field spawnUuid = null;
        try {
            action = PacketPlayOutPlayerInfo.class.getDeclaredField("a");
            list = PacketPlayOutPlayerInfo.class.getDeclaredField("b");
            spawnUuid = PacketPlayOutNamedEntitySpawn.class.getDeclaredField("b");
            action.setAccessible(true);
            list.setAccessible(true);
            spawnUuid.setAccessible(true);
        } catch (Throwable t) {
            action = null;
            list = null;
            spawnUuid = null;
        }
        ACTION_FIELD = action;
        LIST_FIELD = list;
        SPAWN_UUID_FIELD = spawnUuid;
    }

    private final Core main;
    private final Map<UUID, View> views = new ConcurrentHashMap<>();
    private BukkitTask task;
    private boolean loggedError;

    /** One viewer. */
    private static final class View {
        final EntityPlayer handle;

        /** Who this viewer's tab list may show, or null when they're in the lobby and see everyone. */
        volatile Set<UUID> allowed;
        /** The game names to show, for the players of their game. Replaced whole by the main thread. */
        volatile Map<UUID, IChatBaseComponent> names = Collections.emptyMap();
        /** Players their client was told to forget, so they are put back when the game ends. Also changed by the filter. */
        final Set<UUID> removed = ConcurrentHashMap.newKeySet();

        // Main thread only
        /** The game names their client shows right now. */
        Map<UUID, String> sent = new HashMap<>();

        View(EntityPlayer handle) {
            this.handle = handle;
        }
    }

    /** Everyone in one game, with the name each of them has in the tab list (null: their normal rank name). */
    private static final class Context {
        final Map<UUID, String> names = new HashMap<>();
    }

    public GameTablistManager(Core main) {
        if (ACTION_FIELD == null || LIST_FIELD == null || SPAWN_UUID_FIELD == null)
            throw new IllegalStateException("Packet fields not found");

        this.main = main;
        Bukkit.getPluginManager().registerEvents(this, main);

        // Already-online players, for a /reload
        for (Player player : Bukkit.getOnlinePlayers()) attach(player);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                check();
            }
        }.runTaskTimer(main, 20L, CHECK_TICKS);
    }

    /** Puts everyone's tab list back to normal and removes the filters. */
    public void shutdown() {
        if (task != null) task.cancel();
        task = null;

        for (Player player : Bukkit.getOnlinePlayers()) {
            View view = views.get(player.getUniqueId());
            if (view == null) continue;
            try {
                if (view.allowed != null) release(player, view);
                detach(player);
            } catch (Throwable ignored) {}
        }
        views.clear();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        attach(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // The filter goes with their connection
        views.remove(event.getPlayer().getUniqueId());
    }

    // ---------------------------------------------------------------- the filter

    private void attach(final Player player) {
        final View view = new View(((CraftPlayer) player).getHandle());
        views.put(player.getUniqueId(), view);

        try {
            Channel channel = view.handle.playerConnection.networkManager.channel;
            if (channel == null || channel.pipeline().get(HANDLER_NAME) != null) return;

            channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new ChannelDuplexHandler() {
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    if (msg instanceof PacketPlayOutPlayerInfo) {
                        Object result;
                        try {
                            result = filter(view, (PacketPlayOutPlayerInfo) msg);
                        } catch (Throwable t) {
                            // Better a normal tab list than a broken connection
                            result = msg;
                        }
                        if (result == null) {
                            promise.setSuccess();
                            return;
                        }
                        msg = result;
                    } else if (msg instanceof PacketPlayOutNamedEntitySpawn) {
                        try {
                            ensureTabEntry(ctx, view, (PacketPlayOutNamedEntitySpawn) msg);
                        } catch (Throwable ignored) {}
                    }
                    super.write(ctx, msg, promise);
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void detach(Player player) {
        try {
            Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
            if (channel != null && channel.pipeline().get(HANDLER_NAME) != null)
                channel.pipeline().remove(HANDLER_NAME);
        } catch (Throwable ignored) {}
    }

    /**
     * Runs on the viewer's network thread, just before a player is spawned for them. A client that doesn't have that
     * player's tab entry can't show them, so if this viewer's list isn't meant to have them yet (they just came into
     * the viewer's world, say) the entry is sent first, in the same order as the packets that follow.
     */
    private void ensureTabEntry(ChannelHandlerContext ctx, View view, PacketPlayOutNamedEntitySpawn spawn) throws Exception {
        Set<UUID> allowed = view.allowed;
        if (allowed == null) return;

        UUID id = (UUID) SPAWN_UUID_FIELD.get(spawn);
        if (id == null || allowed.contains(id)) return;

        View other = views.get(id);
        if (other == null) return; // Not a real player, they bring their own tab entry

        view.removed.remove(id); // So the next check forgets them again if they still don't belong
        ctx.write(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, other.handle));
    }

    /** Runs on the viewer's network thread. Returns the packet to send: the same one, a changed copy, or null for none. */
    @SuppressWarnings("unchecked")
    private Object filter(View view, PacketPlayOutPlayerInfo packet) throws Exception {
        Set<UUID> allowed = view.allowed;
        if (allowed == null) return packet;

        Object action = ACTION_FIELD.get(packet);
        boolean remove = action == EnumPlayerInfoAction.REMOVE_PLAYER;
        if (!remove && action != EnumPlayerInfoAction.ADD_PLAYER && action != EnumPlayerInfoAction.UPDATE_DISPLAY_NAME)
            return packet; // The client ignores the other updates for players it doesn't have

        Map<UUID, IChatBaseComponent> names = view.names;
        List<PlayerInfoData> original = (List<PlayerInfoData>) LIST_FIELD.get(packet);
        List<PlayerInfoData> kept = new ArrayList<>(original.size());
        PacketPlayOutPlayerInfo copy = new PacketPlayOutPlayerInfo();
        boolean changed = false;

        // The packet is shared with every other viewer, so it is never changed itself, only copied
        for (PlayerInfoData data : original) {
            UUID id = data.a().getId();
            if (remove) {
                // hidePlayer() takes a player out of the tab list along with their body. Here that's wrong: somebody
                // who is out, or invisible, is still in the game. Players who left the server are no longer in
                // views, so their removal goes through.
                if (allowed.contains(id) && views.containsKey(id)) changed = true;
                else kept.add(data);
            } else if (allowed.contains(id)) {
                IChatBaseComponent name = names.get(id);
                if (name != null) {
                    kept.add(copy.new PlayerInfoData(data.a(), data.b(), data.c(), name));
                    changed = true;
                } else {
                    kept.add(data);
                }
            } else if (views.containsKey(id)) {
                // An online player in another world: not in their tab list. The next check takes them out of the
                // client's list too if it had them, and ensureTabEntry puts them back if they show up before then
                changed = true;
            } else {
                kept.add(data); // Not a player (an NPC, say)
            }
        }

        if (!changed) return packet;
        if (kept.isEmpty()) return null;

        ACTION_FIELD.set(copy, action);
        ((List<PlayerInfoData>) LIST_FIELD.get(copy)).addAll(kept);
        return copy;
    }

    // ---------------------------------------------------------------- keeping viewers in sync

    private void check() {
        try {
            Map<Object, Context> contexts = new IdentityHashMap<>();

            for (Player viewer : Bukkit.getOnlinePlayers()) {
                View view = views.get(viewer.getUniqueId());
                if (view == null) continue;

                Context context = contextOf(viewer, contexts);
                if (context == null && view.allowed == null) continue; // Lobby, and nothing to undo

                sync(viewer, view, context);
            }
        } catch (Throwable t) {
            // Once, since this repeats every few ticks
            if (!loggedError) {
                loggedError = true;
                main.getLogger().severe("[Tablist] Game tab list check failed: " + t);
                t.printStackTrace();
            }
        }
    }

    /** The game this player is in or watching, or null when they're in the lobby (or anything else that isn't a game). */
    private Context contextOf(Player viewer, Map<Object, Context> cache) {
        GameManager scb = main.getGameManager();
        if (scb != null) {
            GameInstance game = scb.GetInstanceOfPlayer(viewer);
            if (game == null) game = scb.GetInstanceOfSpectator(viewer);
            if (game != null) {
                Context known = cache.get(game);
                if (known == null) {
                    known = scbContext(game);
                    cache.put(game, known);
                }
                return known;
            }
        }

        FWGameManager fw = main.getFwGameManager();
        if (fw != null) {
            FWGameInstance match = fw.getInstanceOfPlayer(viewer);
            if (match == null) match = fw.getInstanceOfSpectator(viewer);
            if (match != null) {
                Context known = cache.get(match);
                if (known == null) {
                    known = fwContext(match);
                    cache.put(match, known);
                }
                return known;
            }
        }
        return null;
    }

    private Context scbContext(GameInstance game) {
        Context context = new Context();
        if (game.players != null)
            for (Player p : new ArrayList<>(game.players)) context.names.put(p.getUniqueId(), game.getTabName(p));
        if (game.spectators != null)
            for (Player p : new ArrayList<>(game.spectators)) context.names.put(p.getUniqueId(), game.getTabName(p));
        return context;
    }

    private Context fwContext(FWGameInstance match) {
        Context context = new Context();
        for (FWTeam team : match.getTeams().values()) {
            for (UUID id : team.getMembers()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) context.names.put(id, match.getTabName(p));
            }
        }
        for (Player p : new ArrayList<>(match.getSpectators())) context.names.put(p.getUniqueId(), match.getTabName(p));
        return context;
    }

    /** Moves one viewer's client to what their context says: the game's players only, or everybody. */
    private void sync(Player viewer, View view, Context context) {
        if (context == null) {
            release(viewer, view);
            return;
        }

        UUID self = viewer.getUniqueId();
        Map<UUID, String> want = new HashMap<>(context.names);
        if (!want.containsKey(self)) want.put(self, null);

        // Who stays in the tab list: the game, and anybody else standing in this world. Only players in another world
        // are taken out, since those are the only ones the client can't be asked to show a body for.
        Set<UUID> allowed = new HashSet<>(want.keySet());
        for (Player inWorld : viewer.getWorld().getPlayers()) allowed.add(inWorld.getUniqueId());

        // Publish first, so a packet that goes out below is already filtered the new way
        if (view.allowed == null || !allowed.equals(view.allowed) || !want.equals(view.sent)) {
            Map<UUID, IChatBaseComponent> components = new HashMap<>();
            for (Map.Entry<UUID, String> entry : want.entrySet()) {
                // No name means their normal one, so there is nothing to swap
                if (entry.getValue() != null) components.put(entry.getKey(), new ChatComponentText(entry.getValue()));
            }
            view.names = components;
            view.allowed = allowed;
        }

        List<EntityPlayer> forget = new ArrayList<>();
        List<EntityPlayer> bringBack = new ArrayList<>();
        List<EntityPlayer> rename = new ArrayList<>();

        for (Player other : Bukkit.getOnlinePlayers()) {
            UUID id = other.getUniqueId();
            if (!allowed.contains(id)) {
                view.sent.remove(id);
                if (view.removed.add(id)) forget.add(handle(other));
            } else if (view.removed.remove(id)) {
                bringBack.add(handle(other)); // Comes with the game name, if it has one
            } else if (!Objects.equals(want.get(id), view.sent.get(id))) {
                rename.add(handle(other)); // A new name, or back to the normal one
            }
        }

        // Players who left the server don't need remembering
        for (Iterator<UUID> it = view.removed.iterator(); it.hasNext(); )
            if (Bukkit.getPlayer(it.next()) == null) it.remove();

        view.sent = want;

        if (!forget.isEmpty()) send(viewer, new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, forget));
        if (!bringBack.isEmpty()) send(viewer, new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, bringBack));
        if (!rename.isEmpty())
            send(viewer, new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, rename));
    }

    /** Back to the lobby: everybody comes back to the tab list, with their normal names. */
    private void release(Player viewer, View view) {
        view.allowed = null;
        view.names = Collections.emptyMap();

        List<EntityPlayer> add = new ArrayList<>();
        List<EntityPlayer> rename = new ArrayList<>();
        for (Player other : Bukkit.getOnlinePlayers()) {
            UUID id = other.getUniqueId();
            // Adding replaces the entry, which on someone standing next to the viewer could blink their skin,
            // and people in the same world were never taken out unless the list says so
            boolean forgotten = view.removed.contains(id) || other.getWorld() != viewer.getWorld();
            if (forgotten) add.add(handle(other));
            else if (view.sent.get(id) != null) rename.add(handle(other));
        }
        view.removed.clear();
        view.sent = new HashMap<>();

        if (!add.isEmpty()) send(viewer, new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, add));
        if (!rename.isEmpty())
            send(viewer, new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, rename));
    }

    private static EntityPlayer handle(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private static void send(Player viewer, Packet<?> packet) {
        ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
    }
}
