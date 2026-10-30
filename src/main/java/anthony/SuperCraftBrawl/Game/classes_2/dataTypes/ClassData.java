package anthony.SuperCraftBrawl.Game.classes_2.dataTypes;

import java.util.List;

public record ClassData(
    ClassArmour armour,
    List<Integer> timerLengths,
    ClassItem weapon,
    List<ClassItem> items
) {}