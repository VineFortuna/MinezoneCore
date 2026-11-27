package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Core;
import anthony.util.AnvilPrompt;
import anthony.util.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Sets maps up in game. /scbsetup create (a new map in a new void world) and /scbsetup tp (an existing one) start a
 * session: the player is put in creative with the {@link SetupTool}s, one for each thing Maps.java used to set. The
 * tools change the player's draft of the map, and /scbsetup save writes it to maps.yml (see {@link MapStore}), which is
 * also what makes the map playable once everything it needs is set.
 *
 * Sessions end when the player leaves the world or the server, so nothing is kept for players that are gone.
 */
public final class MapSetupManager implements Listener {

    private static final int MARKER_PERIOD = 10; // ticks
    /** Boundary lines are only drawn this far (in blocks) from the player, so a huge map doesn't send thousands. */
    private static final double MARKER_RANGE = 32;
    private static final double MARKER_SPACING = 2;
    /** Particles are only sent to players this close to them. */
    private static final int PARTICLE_RADIUS = 48;

    private final Core main;
    private final MapWorldManager worlds;
    private final Map<UUID, MapSetupSession> sessions = new HashMap<UUID, MapSetupSession>();
    private final AnvilPrompt anvil;
    private BukkitTask markerTask;

    MapSetupManager(Core main, MapWorldManager worlds) {
        this.main = main;
        this.worlds = worlds;
        this.anvil = new AnvilPrompt(main);
        Bukkit.getPluginManager().registerEvents(anvil, main);
    }

    // ---------------------------------------------------------------- sessions

    MapSetupSession get(Player player) {
        return sessions.get(player.getUniqueId());
    }

    /** The player that is setting this map up right now, or null. */
    Player editorOf(String id) {
        for (MapSetupSession session : sessions.values()) {
            if (session.draft.id.equals(id)) return Bukkit.getPlayer(session.player);
        }
        return null;
    }

    /** Starts setting a map up: creative, an empty inventory with the tools. The player is already in the map's world. */
    void start(Player player, MapDraft draft, World world, boolean created) {
        MapSetupSession session = new MapSetupSession(player.getUniqueId(), draft, world, created);
        sessions.put(player.getUniqueId(), session);

        player.setGameMode(GameMode.CREATIVE);
        player.getInventory().clear();
        resetTools(player, session);
        player.getInventory().setHeldItemSlot(0);
        player.updateInventory();
        startMarkers();
    }

    /** Ends it (leaving, quitting, going to another world). The edits that weren't saved are gone with it. */
    void end(Player player) {
        MapSetupSession session = sessions.remove(player.getUniqueId());
        if (session == null) return;

        // Not left as a spectator, which can't use anything
        if (player.getGameMode() == GameMode.SPECTATOR) player.setGameMode(GameMode.CREATIVE);
        removeTools(player);
        if (sessions.isEmpty()) stopMarkers();
    }

    boolean isActive(Player player, MapSetupSession session) {
        return sessions.get(player.getUniqueId()) == session && player.isOnline();
    }

    // ---------------------------------------------------------------- the items

    /**
     * Takes every setup item away, wherever the player put it, and puts each one in its own slot, in place of whatever
     * is in that slot.
     */
    void resetTools(Player player, MapSetupSession session) {
        removeTools(player);

        PlayerInventory inventory = player.getInventory();
        for (SetupTool tool : SetupTool.values()) inventory.setItem(tool.slot, build(tool, session));
        player.updateInventory();
    }

    private static void removeTools(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (SetupTool.of(inventory.getItem(slot)) != null) inventory.setItem(slot, null);
        }
        if (SetupTool.of(player.getItemOnCursor()) != null) player.setItemOnCursor(null);
        player.updateInventory();
    }

    /** Brings what the tools say they are set to up to date, in whichever slots the player has put them. */
    void refresh(Player player, MapSetupSession session) {
        PlayerInventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            SetupTool tool = SetupTool.of(inventory.getItem(slot));
            if (tool != null) inventory.setItem(slot, build(tool, session));
        }
        player.updateInventory();
    }

    private ItemStack build(SetupTool tool, MapSetupSession session) {
        MapDraft draft = session.draft;
        List<String> lore = new ArrayList<String>();
        lore.add("");

        switch (tool) {
            case MODE:
                Player owner = Bukkit.getPlayer(session.player);
                boolean spectating = owner != null && owner.getGameMode() == GameMode.SPECTATOR;
                lore.add("&7Look at the map from inside it.");
                lore.add("&7Spectator goes through blocks.");
                lore.add("");
                lore.add("&fCurrently: &a" + (spectating ? "Spectator" : "Creative"));
                lore.add("");
                lore.add("&e&nClick&r&e to switch");
                if (spectating) lore.add("&7Or use &e/scbsetup mode&7.");
                break;
            case SPAWNS:
                lore.add("&7Where players start and respawn,");
                lore.add("&7facing the way you look.");
                lore.add("&7At least " + MapDraft.MIN_SPAWNS + " are needed.");
                lore.add("");
                lore.add("&fSpawns: " + (draft.spawns.size() >= MapDraft.MIN_SPAWNS ? "&a" : "&c") + draft.spawns.size());
                lore.add("");
                lore.add("&e&nLeft Click&r&e to add one where you stand");
                lore.add("&e&nRight Click&r&e to remove the last one");
                break;
            case BOUNDS:
                lore.add("&7The play area, seen from above.");
                lore.add("&7Two corners make a rectangle.");
                lore.add("");
                lore.add("&fCorner 1: " + place(session.pos1));
                lore.add("&fCorner 2: " + place(session.pos2));
                if (draft.hasBounds()) {
                    lore.add("&fSize: &a" + Math.round(draft.boundsX * 2) + " by " + Math.round(draft.boundsZ * 2));
                }
                lore.add("");
                lore.add("&e&nLeft Click&r&e to set corner 1 here");
                lore.add("&e&nRight Click&r&e to set corner 2 here");
                break;
            case LOBBY:
                lore.add("&7Where players wait before the game,");
                lore.add("&7facing the way you look.");
                lore.add("");
                lore.add("&fCurrently: " + place(draft.lobby));
                lore.add("");
                lore.add("&e&nClick&r&e to set it where you stand");
                break;
            case SPECTATOR:
                lore.add("&7Where spectators watch from,");
                lore.add("&7facing the way you look.");
                lore.add("");
                lore.add("&fCurrently: " + place(draft.spectator));
                lore.add("");
                lore.add("&e&nClick&r&e to set it where you stand");
                break;
            case SETTINGS:
                int missing = draft.missing().size();
                lore.add("&7Source, size, gameplay, category,");
                lore.add("&7game type and the selector icon.");
                lore.add("");
                lore.add(missing == 0 ? "&fStatus: &aReady to save"
                        : "&fStatus: &cStill needs " + missing + (missing == 1 ? " thing" : " things"));
                lore.add("");
                lore.add("&e&nClick&r&e to open");
                break;
        }
        return MapSetupMenus.item(main, new ItemStack(tool.material), "&6&l" + tool.title, lore);
    }

    private static String place(Vector v) {
        if (v == null) return "&cNot set";

        String place = String.format(Locale.ROOT, "&a%.1f, %.1f, %.1f", v.getX(), v.getY(), v.getZ());
        if (v instanceof FacingVector) place += " &7facing " + FacingVector.compass(((FacingVector) v).getYaw());
        return place;
    }

    private static <T> String joined(List<T> values) {
        StringBuilder out = new StringBuilder();
        for (T value : values) out.append(out.length() == 0 ? "" : ", ").append(value);
        return out.toString();
    }

    // ---------------------------------------------------------------- using the items

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        MapSetupSession session = sessions.get(player.getUniqueId());
        if (session == null) return;
        SetupTool tool = SetupTool.of(event.getItem());
        if (tool == null) return;

        event.setCancelled(true); // a tool never breaks, places or uses anything
        if (player.getWorld() != session.world) return;

        boolean left = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        if (session.isRepeat(tool, left)) return;
        use(player, session, tool, left);
    }

    private void use(final Player player, final MapSetupSession session, SetupTool tool, boolean left) {
        final MapDraft draft = session.draft;
        Vector here = here(player);
        Vector facing = facing(player);

        switch (tool) {
            case MODE:
                toggleMode(player, session);
                return;

            case LOBBY:
                draft.lobby = facing;
                ok(player, "Lobby location set, facing " + FacingVector.compass(player.getLocation().getYaw()) + ".");
                break;

            case SPAWNS:
                if (left) {
                    draft.spawns.add(facing);
                    ok(player, "Spawn " + draft.spawns.size() + " set, facing "
                            + FacingVector.compass(player.getLocation().getYaw()) + ".");
                } else if (draft.spawns.isEmpty()) {
                    error(player, "There are no spawns to remove.");
                    return;
                } else {
                    draft.spawns.remove(draft.spawns.size() - 1);
                    ok(player, "Removed spawn " + (draft.spawns.size() + 1) + ", " + draft.spawns.size() + " left.");
                }
                break;

            case SPECTATOR:
                draft.spectator = facing;
                ok(player, "Spectator location set, facing " + FacingVector.compass(player.getLocation().getYaw()) + ".");
                break;

            case BOUNDS:
                if (left) session.pos1 = here;
                else session.pos2 = here;
                session.applyBounds();

                String corner = left ? "Corner 1" : "Corner 2";
                if (session.pos1 == null || session.pos2 == null) {
                    ok(player, corner + " set. Now set corner " + (left ? "2 with a right" : "1 with a left") + " click.");
                } else if (!draft.hasBounds()) {
                    error(player, corner + " set, but both corners are in a line. Move one so they make a rectangle.");
                } else {
                    ok(player, corner + " set. The map is " + Math.round(draft.boundsX * 2) + " by "
                            + Math.round(draft.boundsZ * 2) + " blocks.");
                }
                break;

            case SETTINGS:
                MapSettingsMenu.open(main, this, player, session);
                return;
        }

        SoundManager.playClickSound(player);
        refresh(player, session);
    }

    /**
     * Creative and spectator, back and forth. In spectator the player goes through blocks, to look at the map from
     * inside it.
     */
    void toggleMode(Player player, MapSetupSession session) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
            ok(player, "Creative mode.");
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.getInventory().setHeldItemSlot(SetupTool.MODE.slot); // so that a click in spectator is a click with it
            ok(player, "Spectator mode. Click the compass, or use &e/scbsetup mode&r, to go back.");
        }
        SoundManager.playClickSound(player);
        refresh(player, session);
    }

    /** Where the player stands, to the thousandth of a block like the maps that were in Maps.java. */
    private static Vector here(Player player) {
        Location location = player.getLocation();
        return new Vector(round(location.getX()), round(location.getY()), round(location.getZ()));
    }

    /** The same, and the way the player is looking, for the points that players are put at. */
    private static Vector facing(Player player) {
        Location location = player.getLocation();
        return new FacingVector(round(location.getX()), round(location.getY()), round(location.getZ()),
                location.getYaw(), location.getPitch());
    }

    private static double round(double value) {
        return Math.round(value * 1000) / 1000.0;
    }

    // ---------------------------------------------------------------- the name

    /** Asks for the map's name in an anvil, then checks it and goes back to the settings menu. */
    void renameMap(final Player player, final MapSetupSession session) {
        // A tick later: the settings menu that was clicked is replaced by the anvil, and can't be from inside its own click
        Bukkit.getScheduler().runTask(main, new Runnable() {
            @Override
            public void run() {
                if (!isActive(player, session)) return;

                boolean opened = anvil.open(player, session.draft.displayName, new Consumer<String>() {
                    @Override
                    public void accept(String typed) {
                        if (!isActive(player, session)) return;
                        setName(player, session, typed);
                        reopenSettings(player, session);
                    }
                });
                if (!opened) error(player, "Couldn't open the anvil, see the console.");
            }
        });
    }

    /** Sets the name players see if it is a good one. The map's id and world stay as they are. */
    private void setName(Player player, MapSetupSession session, String typed) {
        String name = MapStore.cleanDisplayName(typed);

        if (name.equals(session.draft.displayName)) {
            error(player, "The name is already &e" + name + "&r.");
        } else if (name.length() < MapStore.MIN_NAME_LENGTH || name.length() > MapStore.MAX_NAME_LENGTH) {
            error(player, "A name is " + MapStore.MIN_NAME_LENGTH + " to " + MapStore.MAX_NAME_LENGTH + " characters.");
        } else if (!MapStore.isValidDisplayName(name)) {
            error(player, "A name can only have letters, numbers, spaces, hyphens and periods, and starts with a letter or number.");
        } else if (main.getMapStore() != null && main.getMapStore().nameTaken(name, session.draft.id)) {
            error(player, "Another map is already called &e" + name + "&r.");
        } else {
            session.draft.displayName = name;
            ok(player, "Name set to &e" + name + "&r. The map ID is still &e" + session.draft.id + "&r.");
            refresh(player, session);
        }
    }

    // ---------------------------------------------------------------- the icon

    /** Makes this item the map's icon. A tool's damage is not part of what it looks like in a menu, so it isn't kept. */
    void setIcon(Player player, MapSetupSession session, Material material, short data) {
        session.draft.iconType = material;
        session.draft.iconData = material.getMaxDurability() > 0 ? 0 : data;
        ok(player, "Icon set to &e" + MapSetupMenus.iconName(session.draft) + "&r.");
        refresh(player, session);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onIconPickerClick(InventoryClickEvent event) {
        if (!MapSetupMenus.isIconPicker(event.getInventory())) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        MapSetupSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        // Only an item in the player's own inventory (below the menu) counts
        if (event.getRawSlot() < event.getView().getTopInventory().getSize()) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (SetupTool.of(clicked) != null) {
            error(player, "That is one of the setup items. Pick another item.");
            return;
        }
        setIcon(player, session, clicked.getType(), clicked.getDurability());
        SoundManager.playClickSound(player);
        reopenSettings(player, session);
    }

    /** Back to the settings menu, a tick later (a menu can't be swapped for another from inside its own click). */
    private void reopenSettings(final Player player, final MapSetupSession session) {
        Bukkit.getScheduler().runTask(main, new Runnable() {
            @Override
            public void run() {
                if (isActive(player, session)) MapSettingsMenu.open(main, MapSetupManager.this, player, session);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onIconPickerDrag(InventoryDragEvent event) {
        if (MapSetupMenus.isIconPicker(event.getInventory())) event.setCancelled(true);
    }

    // ---------------------------------------------------------------- keeping the items safe

    private boolean holdsTool(Player player) {
        return sessions.containsKey(player.getUniqueId()) && SetupTool.of(player.getItemInHand()) != null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreak(BlockBreakEvent event) {
        if (holdsTool(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        if (holdsTool(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (holdsTool(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        if (sessions.containsKey(event.getPlayer().getUniqueId()) && SetupTool.of(event.getItemDrop().getItemStack()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        end(event.getPlayer());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        MapSetupSession session = sessions.get(event.getPlayer().getUniqueId());
        if (session != null && event.getPlayer().getWorld() != session.world) end(event.getPlayer());
    }

    // ---------------------------------------------------------------- saving and what is missing

    /** The Save button of the settings menu: the same as /scbsetup save. */
    void saveFromMenu(Player player) {
        worlds.saveCommand(player);
    }

    /** Writes the map to maps.yml. A complete one is playable from then on. The world itself is saved by the caller. */
    void saveMap(Player player, MapSetupSession session) {
        MapStore store = main.getMapStore();
        if (store == null) {
            error(player, "The maps file isn't loaded, so the map can't be saved. Check the console.");
            return;
        }
        MapDraft draft = session.draft;

        try {
            store.save(draft);
        } catch (IOException | RuntimeException e) {
            main.getLogger().severe("[Maps] Couldn't save " + draft.id + " to maps.yml: " + e);
            error(player, "Couldn't write maps.yml, see the console. The world was saved but the map was not.");
            return;
        }
        worlds.markSaved(session.world);

        if (draft.isPlayable()) {
            ok(player, "Saved &e" + draft.id + "&r. It can be played now.");
        } else if (draft.isComplete()) {
            ok(player, "Saved &e" + draft.id + "&r, but it is switched off. &e/scbsetup enable " + draft.id
                    + " &rturns it on.");
        } else {
            ok(player, "Saved &e" + draft.id + "&r, but it can't be played yet. It still needs:");
            for (String missing : draft.missing()) player.sendMessage(main.color("&c- " + missing));
        }

        for (String problem : problems(session)) {
            player.sendMessage(main.color("&e&l(!) &r" + problem));
        }
    }

    /** Things that are set but would go wrong in a game. They are warnings: the map is still saved. */
    private static List<String> problems(MapSetupSession session) {
        MapDraft draft = session.draft;
        List<String> problems = new ArrayList<String>();
        if (!draft.hasBounds()) return problems;

        StringBuilder outside = new StringBuilder();
        for (int i = 0; i < draft.spawns.size(); i++) {
            if (!insideBounds(draft, draft.spawns.get(i))) outside.append(outside.length() == 0 ? "" : ", ").append(i + 1);
        }
        if (outside.length() > 0) problems.add("Spawn " + outside + " is outside the map boundaries.");
        if (draft.spectator != null && !insideBounds(draft, draft.spectator)) {
            problems.add("The spectator location is outside the map boundaries.");
        }
        return problems;
    }

    private static boolean insideBounds(MapDraft draft, Vector point) {
        return Math.abs(draft.center.getX() - point.getX()) <= draft.boundsX
                && Math.abs(draft.center.getZ() - point.getZ()) <= draft.boundsZ;
    }

    void sendStatus(Player player, MapSetupSession session) {
        MapDraft draft = session.draft;
        player.sendMessage(main.color("&8&m------------------------------------"));
        player.sendMessage(main.color("&6&lMAP SETUP &e" + draft.id));
        player.sendMessage(main.color("&fName: &a" + draft.displayName));
        player.sendMessage(main.color("&fLobby: " + place(draft.lobby)));
        player.sendMessage(main.color("&fSpectator: " + place(draft.spectator)));
        player.sendMessage(main.color("&fSpawns: &a" + draft.spawns.size()));
        player.sendMessage(main.color("&fBoundaries: " + (draft.hasBounds() ? "&a" + Math.round(draft.boundsX * 2) + " by "
                + Math.round(draft.boundsZ * 2) : "&cNot set")));
        player.sendMessage(main.color("&fSource: " + (draft.source == null ? "&cNot set" : "&a" + draft.source)));
        player.sendMessage(main.color("&fSize: " + (draft.size == null ? "&cNot set" : "&a" + draft.size)));
        player.sendMessage(main.color("&fGameplay: " + (draft.gameplay.isEmpty() ? "&cNot set" : "&a" + joined(draft.gameplay))));
        player.sendMessage(main.color("&fCategory: " + (draft.category == null ? "&cNot set" : "&a" + draft.category)));
        player.sendMessage(main.color("&fIcon: " + (draft.iconType == null ? "&cNot set" : "&a" + MapSetupMenus.iconName(draft))));
        player.sendMessage(main.color("&fGame type: &a" + draft.gameType.getName()));

        if (draft.isComplete()) {
            player.sendMessage(main.color("&aReady. &e/scbsetup save &amakes it playable."));
        } else {
            player.sendMessage(main.color("&cStill needs:"));
            for (String missing : draft.missing()) player.sendMessage(main.color("&c- " + missing));
        }
        for (String problem : problems(session)) player.sendMessage(main.color("&e&l(!) &r" + problem));
        player.sendMessage(main.color("&8&m------------------------------------"));
    }

    // ---------------------------------------------------------------- showing the points

    private void startMarkers() {
        if (markerTask != null) return;
        markerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (MapSetupSession session : new ArrayList<MapSetupSession>(sessions.values())) {
                    Player player = Bukkit.getPlayer(session.player);
                    if (player == null || !player.isOnline()) {
                        sessions.remove(session.player);
                        continue;
                    }
                    if (player.getWorld() == session.world) drawMarkers(player, session);
                }
                if (sessions.isEmpty()) stopMarkers();
            }
        }.runTaskTimer(main, MARKER_PERIOD, MARKER_PERIOD);
    }

    private void stopMarkers() {
        if (markerTask == null) return;
        markerTask.cancel();
        markerTask = null;
    }

    /** Dust where the points are, only seen by the player setting up: blue lobby, green spawns, white spectator, red bounds. */
    private void drawMarkers(Player player, MapSetupSession session) {
        MapDraft draft = session.draft;
        World world = session.world;

        if (draft.lobby != null) column(player, world, draft.lobby, 0.2f, 0.45f, 1f);
        if (draft.spectator != null) column(player, world, draft.spectator, 1f, 1f, 1f);
        for (Vector spawn : draft.spawns) column(player, world, spawn, 0.2f, 1f, 0.2f);

        Vector one = session.pos1;
        Vector two = session.pos2;
        if (one != null) column(player, world, one, 1f, 0.15f, 0.15f);
        if (two != null) column(player, world, two, 1f, 0.15f, 0.15f);
        if (one == null || two == null) return;

        double minX = Math.min(one.getX(), two.getX());
        double maxX = Math.max(one.getX(), two.getX());
        double minZ = Math.min(one.getZ(), two.getZ());
        double maxZ = Math.max(one.getZ(), two.getZ());
        Location at = player.getLocation();
        double[] heights = {at.getY() - 1, at.getY() + 1};

        for (double x = minX; x <= maxX; x += MARKER_SPACING) {
            edge(player, world, x, minZ, at, heights);
            edge(player, world, x, maxZ, at, heights);
        }
        for (double z = minZ; z <= maxZ; z += MARKER_SPACING) {
            edge(player, world, minX, z, at, heights);
            edge(player, world, maxX, z, at, heights);
        }
    }

    private void edge(Player player, World world, double x, double z, Location at, double[] heights) {
        if (Math.abs(x - at.getX()) > MARKER_RANGE || Math.abs(z - at.getZ()) > MARKER_RANGE) return;
        for (double y : heights) dust(player, new Location(world, x, y, z), 1f, 0.15f, 0.15f);
    }

    private void column(Player player, World world, Vector point, float red, float green, float blue) {
        for (double dy = 0; dy <= 2; dy += 0.5) {
            dust(player, new Location(world, point.getX(), point.getY() + dy, point.getZ()), red, green, blue);
        }

        if (point instanceof FacingVector) {
            // From the height of the eyes, the way it looks: 3 dots a half block apart
            Location eyes = FacingVector.toLocation(world, point).add(0, 1.62, 0);
            Vector direction = eyes.getDirection().normalize();
            for (double distance = 0.5; distance <= 1.5; distance += 0.5) {
                dust(player, eyes.clone().add(direction.clone().multiply(distance)), red, green, blue);
            }
        }
    }

    /** Spigot 1.8 coloured dust takes its colour from the offsets, with a count of 0 (see CandyAuraManager). */
    private static void dust(Player player, Location location, float red, float green, float blue) {
        player.spigot().playEffect(location, Effect.COLOURED_DUST, 0, 0, Math.max(red, 0.004f), Math.max(green, 0.004f),
                Math.max(blue, 0.004f), 1f, 0, PARTICLE_RADIUS);
    }

    // ---------------------------------------------------------------- messages

    private void ok(Player player, String message) {
        player.sendMessage(main.color("&a&l(!) &r" + message));
    }

    void error(Player player, String message) {
        player.sendMessage(main.color("&c&l(!) &r" + message));
    }
}
