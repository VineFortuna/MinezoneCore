package anthony.SuperCraftBrawl.Game.classes.dataTypes;

import org.bukkit.Material;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ClassMetaData {
    Material      iconBlock();
    String        iconHead();
    String        masteryHead();
    String        title();
    String        description();
    ClassCategory category();
    int           tokenCount();
    int           uniqueIdentifier();
}
