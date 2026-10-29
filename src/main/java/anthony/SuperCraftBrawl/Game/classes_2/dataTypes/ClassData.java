package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import anthony.SuperCraftBrawl.Timer;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public record ClassData(
    ClassArmour armour,
    List<Integer> timerLengths,
    ClassItem weapon,
    List<ClassItem> items
) {}