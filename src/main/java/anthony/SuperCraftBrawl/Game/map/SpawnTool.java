package anthony.SuperCraftBrawl.Game.map;

import anthony.SuperCraftBrawl.Core;
import anthony.SuperCraftBrawl.Game.map.MapSetupSession.SpawnMode;
import anthony.util.SoundManager;
import anthony.util.TitleHelper;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Locale;

/**
 * What the Spawn Locations item does with each kind of click, in each of its modes (see {@link SpawnMode}):
 *
 * <pre>
 *            normal               cycle                 cycling by itself   editing
 * left       list them in chat    the previous spawn    stop it (cycle)     -
 * right      cycle                the next spawn        stop it (cycle)     -
 * sh. left   add one here         edit this one         edit this one       move it here
 * sh. right  cycle by itself      stop cycling          stop cycling        cancel
 * </pre>
 *
 * The spawn that cycling or editing is on is the player's current one, and is drawn in its own colour. The list in chat
 * has a [+] (add one after it), [~] (edit it) and [X] (delete it) for each spawn. Those buttons run
 * "/scbsetup spawn insert|edit|delete number code", where the code says which spawn it was, so a button of an old list
 * (other spawns added or deleted since, maybe by another player) still finds its spawn or does nothing, and never touches
 * another one.
 *
 * All the state is in the player's {@link MapSetupSession}, which {@link MapSetupManager} ends when they leave.
 */
final class SpawnTool {

    /** Ticks between two spawns while it cycles by itself: 3 seconds. */
    private static final long AUTO_PERIOD = 60L;

    private final Core main;
    private final MapSetupManager manager;

    SpawnTool(Core main, MapSetupManager manager) {
        this.main = main;
        this.manager = manager;
    }

    // ---------------------------------------------------------------- the clicks

    void click(Player player, MapSetupSession session, boolean left) {
        if (!stillOnASpawn(player, session)) return;
        boolean sneaking = player.isSneaking();

        switch (session.spawnMode) {
            case CYCLE:
                if (!sneaking) cycle(player, session, left ? -1 : 1);
                else if (left) startEdit(player, session, session.current);
                else leaveCycle(player, session);
                break;
            case AUTO:
                if (!sneaking) stopAuto(player, session);
                else if (left) startEdit(player, session, session.current);
                else leaveCycle(player, session);
                break;
            case EDIT:
                if (sneaking && left) moveHere(player, session);
                else if (sneaking) cancelEdit(player, session);
                else manager.error(player, "You're editing spawn &e#" + numberOf(session) + "&r. Shift + left click moves it "
                        + "where you stand, shift + right click cancels.");
                break;
            default:
                if (sneaking && left) addHere(player, session);
                else if (sneaking) startAuto(player, session);
                else if (left) sendList(player, session);
                else startCycle(player, session);
                break;
        }
    }

    /** What the buttons of the list in chat run: /scbsetup spawn insert|edit|delete number code. */
    void command(Player player, String[] args) {
        MapSetupSession session = manager.get(player);
        if (session == null || player.getWorld() != session.world) {
            manager.error(player, "You're not setting up a map.");
            return;
        }
        if (args.length < 4) {
            manager.error(player, "Use the buttons of the list. Left click the Spawn Locations item for it.");
            return;
        }

        int number;
        try {
            number = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            manager.error(player, "Use the buttons of the list. Left click the Spawn Locations item for it.");
            return;
        }
        int at = find(session.draft, number - 1, args[3]);
        if (at < 0) {
            manager.error(player, "That list is out of date, that spawn was moved or deleted. Left click the Spawn Locations "
                    + "item for a new one.");
            return;
        }
        if (!stillOnASpawn(player, session)) return;

        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("insert")) insertAfter(player, session, at);
        else if (action.equals("edit")) startEdit(player, session, session.draft.spawns.get(at));
        else if (action.equals("delete")) delete(player, session, at);
        else manager.error(player, "Use the buttons of the list. Left click the Spawn Locations item for it.");
    }

    // ---------------------------------------------------------------- adding, listing

    private void addHere(Player player, MapSetupSession session) {
        List<Vector> spawns = session.draft.spawns;
        spawns.add(MapSetupManager.facing(player));
        manager.ok(player, "Spawn &e#" + spawns.size() + "&r set, facing " + FacingVector.compass(player.getLocation().getYaw()) + ".");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    private void insertAfter(Player player, MapSetupSession session, int at) {
        session.draft.spawns.add(at + 1, MapSetupManager.facing(player));
        manager.ok(player, "Spawn &e#" + (at + 2) + "&r set after &e#" + (at + 1) + "&r, facing "
                + FacingVector.compass(player.getLocation().getYaw()) + ".");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
        sendList(player, session); // the numbers after it moved up
    }

    private void delete(Player player, MapSetupSession session, int at) {
        MapDraft draft = session.draft;
        Vector removed = draft.spawns.remove(at);
        manager.spawnRemoved(draft, removed, player);
        manager.ok(player, "Removed spawn &e#" + (at + 1) + "&r, " + draft.spawns.size() + " left.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
        sendList(player, session); // the numbers after it moved down
    }

    /** The spawns in chat, each with its buttons. */
    private void sendList(Player player, MapSetupSession session) {
        List<Vector> spawns = session.draft.spawns;
        player.sendMessage(main.color("&6Spawn Locations&r"));
        if (spawns.isEmpty()) {
            player.sendMessage(main.color("&7None yet. Shift + left click adds one where you stand."));
            return;
        }

        for (int i = 0; i < spawns.size(); i++) {
            Vector spawn = spawns.get(i);
            String code = codeOf(spawn);
            int number = i + 1;

            TextComponent row = new TextComponent(main.color("&6#" + number + ": &f" + coordinates(spawn) + " "));
            row.addExtra(button("&a[+]", "insert", number, code, "&aAdd a spawn after #" + number + ", where you stand"));
            row.addExtra(new TextComponent(" "));
            row.addExtra(button("&e[~]", "edit", number, code, "&eGo to #" + number + " and edit it"));
            row.addExtra(new TextComponent(" "));
            row.addExtra(button("&c[X]", "delete", number, code, "&cDelete #" + number));
            player.spigot().sendMessage(row);
        }
    }

    private TextComponent button(String text, String action, int number, String code, String hover) {
        TextComponent button = new TextComponent(main.color(text));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scbsetup spawn " + action + " " + number + " " + code));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(main.color(hover)).create()));
        return button;
    }

    // ---------------------------------------------------------------- cycling

    private void startCycle(Player player, MapSetupSession session) {
        if (noSpawns(player, session)) return;

        Vector first = session.draft.spawns.get(0);
        enter(session, SpawnMode.CYCLE, first);
        teleport(player, session, first);
        subtitle(player, "&6#1", 30);
        manager.ok(player, "Cycle mode. Left and right click go to the previous and next spawn. Shift + left click edits "
                + "the one you're on, shift + right click leaves.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    private void cycle(Player player, MapSetupSession session, int step) {
        List<Vector> spawns = session.draft.spawns;
        int size = spawns.size();
        int next = ((indexOf(session.draft, session.current) + step) % size + size) % size;

        Vector spawn = spawns.get(next);
        session.current = spawn;
        teleport(player, session, spawn);
        subtitle(player, "&6#" + (next + 1), 30);
        SoundManager.playClickSound(player);
    }

    private void leaveCycle(Player player, MapSetupSession session) {
        reset(session);
        manager.ok(player, "Left cycle mode.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    private void startAuto(Player player, final MapSetupSession session) {
        if (noSpawns(player, session)) return;

        Vector first = session.draft.spawns.get(0);
        enter(session, SpawnMode.AUTO, first);
        teleport(player, session, first);
        subtitle(player, "&6#1", 50);
        session.setAutoTask(new BukkitRunnable() {
            @Override
            public void run() {
                autoStep(session);
            }
        }.runTaskTimer(main, AUTO_PERIOD, AUTO_PERIOD));

        manager.ok(player, "Cycling by itself, a new spawn every 3 seconds. Left or right click stops it.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    private void autoStep(MapSetupSession session) {
        Player player = Bukkit.getPlayer(session.player);
        if (player == null || !manager.isActive(player, session) || session.spawnMode != SpawnMode.AUTO
                || player.getWorld() != session.world) {
            session.cancelAuto();
            return;
        }
        if (!stillOnASpawn(player, session)) return; // its spawn is gone, which also stops this

        List<Vector> spawns = session.draft.spawns;
        int next = (indexOf(session.draft, session.current) + 1) % spawns.size();
        Vector spawn = spawns.get(next);
        session.current = spawn;
        teleport(player, session, spawn);
        subtitle(player, "&6#" + (next + 1), 50);
    }

    /** A click while it cycles by itself stops that, and it is the plain cycle mode on the spawn it got to. */
    private void stopAuto(Player player, MapSetupSession session) {
        enter(session, SpawnMode.CYCLE, session.current);
        manager.ok(player, "Stopped. You're on spawn &e#" + numberOf(session) + "&r, left and right click go on from there.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    // ---------------------------------------------------------------- editing

    private void startEdit(Player player, MapSetupSession session, Vector spawn) {
        // Cancelling goes back to where it came from: the cycle mode, or the normal one
        SpawnMode before = session.spawnMode == SpawnMode.EDIT ? session.modeBeforeEdit
                : session.spawnMode == SpawnMode.NORMAL ? SpawnMode.NORMAL : SpawnMode.CYCLE;
        session.modeBeforeEdit = before;

        enter(session, SpawnMode.EDIT, spawn);
        teleport(player, session, spawn);
        subtitle(player, "&eEditing &6#" + numberOf(session), 60);
        manager.ok(player, "Editing spawn &e#" + numberOf(session) + "&r. Go where you want it, then shift + left click. "
                + "Shift + right click cancels.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    private void moveHere(Player player, MapSetupSession session) {
        MapDraft draft = session.draft;
        Vector old = session.current;
        int at = indexOf(draft, old);
        Vector moved = MapSetupManager.facing(player);

        draft.spawns.set(at, moved);
        manager.spawnReplaced(draft, old, moved);

        SpawnMode back = session.modeBeforeEdit;
        session.modeBeforeEdit = SpawnMode.NORMAL;
        enter(session, back, moved);
        manager.ok(player, "Spawn &e#" + (at + 1) + "&r moved here, facing " + FacingVector.compass(player.getLocation().getYaw()) + ".");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    private void cancelEdit(Player player, MapSetupSession session) {
        SpawnMode back = session.modeBeforeEdit;
        session.modeBeforeEdit = SpawnMode.NORMAL;
        enter(session, back, session.current);
        manager.ok(player, "Stopped editing, the spawn stays where it was.");
        SoundManager.playClickSound(player);
        manager.refresh(player, session);
    }

    // ---------------------------------------------------------------- the state

    /** Back to the normal mode, with no spawn current. */
    static void reset(MapSetupSession session) {
        session.cancelAuto();
        session.spawnMode = SpawnMode.NORMAL;
        session.modeBeforeEdit = SpawnMode.NORMAL;
        session.current = null;
    }

    private static void enter(MapSetupSession session, SpawnMode mode, Vector current) {
        if (mode != SpawnMode.AUTO) session.cancelAuto();
        session.spawnMode = mode;
        session.current = mode == SpawnMode.NORMAL ? null : current;
    }

    /** False (and back to normal) if the spawn this player was on was deleted, by another player or from the list. */
    private boolean stillOnASpawn(Player player, MapSetupSession session) {
        if (session.spawnMode == SpawnMode.NORMAL || indexOf(session.draft, session.current) >= 0) return true;

        reset(session);
        manager.error(player, "The spawn you were on was deleted, so you're back at the start.");
        manager.refresh(player, session);
        return false;
    }

    private boolean noSpawns(Player player, MapSetupSession session) {
        if (!session.draft.spawns.isEmpty()) return false;
        manager.error(player, "There are no spawns yet. Shift + left click adds one where you stand.");
        return true;
    }

    // ---------------------------------------------------------------- helpers

    private static void teleport(Player player, MapSetupSession session, Vector spawn) {
        player.teleport(FacingVector.toLocation(session.world, spawn));
    }

    private static void subtitle(Player player, String text, int stayTicks) {
        TitleHelper.sendTitle(player, "", text, 0, stayTicks, 10);
    }

    /** Where the spawn is in the list (by the very object, two spawns can be in the same place), or -1. */
    static int indexOf(MapDraft draft, Vector spawn) {
        if (spawn == null) return -1;
        for (int i = 0; i < draft.spawns.size(); i++) {
            if (draft.spawns.get(i) == spawn) return i;
        }
        return -1;
    }

    private static int numberOf(MapSetupSession session) {
        return indexOf(session.draft, session.current) + 1;
    }

    /** The spawn a button was made for: at the number it was, or if the list changed since, wherever it is now. */
    private static int find(MapDraft draft, int hint, String code) {
        List<Vector> spawns = draft.spawns;
        if (hint >= 0 && hint < spawns.size() && codeOf(spawns.get(hint)).equals(code)) return hint;
        for (int i = 0; i < spawns.size(); i++) {
            if (codeOf(spawns.get(i)).equals(code)) return i;
        }
        return -1;
    }

    /** Says which spawn it is from where it is and which way it faces. */
    private static String codeOf(Vector spawn) {
        float yaw = spawn instanceof FacingVector ? ((FacingVector) spawn).getYaw() : 0f;
        float pitch = spawn instanceof FacingVector ? ((FacingVector) spawn).getPitch() : 0f;

        int hash = 17;
        for (double value : new double[]{spawn.getX(), spawn.getY(), spawn.getZ(), yaw, pitch}) {
            hash = hash * 31 + Double.valueOf(value).hashCode();
        }
        return Integer.toHexString(hash);
    }

    private static String coordinates(Vector spawn) {
        return number(spawn.getX()) + " " + number(spawn.getY()) + " " + number(spawn.getZ());
    }

    /** 100 or 100.5, never 100.0. */
    private static String number(double value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        if (text.equals("-0.0")) return "0";
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    // ---------------------------------------------------------------- the item itself

    /** The lines of the item for the mode it is in. */
    void describe(List<String> lore, MapSetupSession session) {
        int count = session.draft.spawns.size();
        lore.add("&7Where players start and respawn,");
        lore.add("&7facing the way you look.");
        lore.add("&7At least " + MapDraft.MIN_SPAWNS + " are needed.");
        lore.add("");
        lore.add("&fSpawns: " + (count >= MapDraft.MIN_SPAWNS ? "&a" : "&c") + count);
        lore.add("");

        switch (session.spawnMode) {
            case CYCLE:
                lore.add("&fMode: &aCycling");
                lore.add("");
                lore.add("&e&nLeft Click&r&e for the previous spawn");
                lore.add("&e&nRight Click&r&e for the next spawn");
                lore.add("&e&nShift + Left Click&r&e to edit this one");
                lore.add("&e&nShift + Right Click&r&e to stop cycling");
                break;
            case AUTO:
                lore.add("&fMode: &aCycling by itself");
                lore.add("");
                lore.add("&e&nLeft or Right Click&r&e to stop it");
                lore.add("&e&nShift + Left Click&r&e to edit this one");
                lore.add("&e&nShift + Right Click&r&e to stop cycling");
                break;
            case EDIT:
                lore.add("&fMode: &eEditing a spawn");
                lore.add("");
                lore.add("&e&nShift + Left Click&r&e to move it here");
                lore.add("&e&nShift + Right Click&r&e to cancel");
                break;
            default:
                lore.add("&e&nLeft Click&r&e to list them");
                lore.add("&e&nRight Click&r&e to cycle through them");
                lore.add("&e&nShift + Left Click&r&e to add one here");
                lore.add("&e&nShift + Right Click&r&e to cycle by itself");
                break;
        }
    }
}
