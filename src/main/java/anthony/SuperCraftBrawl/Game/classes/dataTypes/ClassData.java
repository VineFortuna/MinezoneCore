package anthony.SuperCraftBrawl.Game.classes.dataTypes;

import java.util.List;

public record ClassData(
    ClassArmour     armour,
    List<Integer>   timerLengths,
    ClassItem       weapon,
    List<ClassItem> items,

    double          jumpVelocity
) {}