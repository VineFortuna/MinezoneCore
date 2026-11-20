package anthony.SuperCraftBrawl.Game.classes;

import anthony.SuperCraftBrawl.Game.classes.dataTypes.ClassCategory;
import anthony.SuperCraftBrawl.Game.classes.dataTypes.ClassMetaData;
import io.vavr.control.Either;
import org.bukkit.Material;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class ClassMetaDataManager {
    private final Reflections reflections = new Reflections();
    public EnumMap<ClassCategory, Set<Class<? extends ClassBase>>> allClasses;

    private Set<Class<? extends ClassBase>> scaryCast(Set<Class<?>> rawClasses) {
        return (Set<Class<? extends ClassBase>>) (Set<?>) rawClasses;
    }

    private EnumMap<ClassCategory, Set<Class<? extends ClassBase>>> findAllGameClasses() {
        Set<Class<? extends ClassBase>> classSet = scaryCast(reflections.get(Scanners.SubTypes.of(ClassBase.class).asClass()));

        return classSet.stream().collect(Collectors.groupingBy(
                ClassMetaDataManager::getClassCategory,
                () -> new EnumMap<>(ClassCategory.class),
                Collectors.toSet()
        ));
    }

    public ClassMetaDataManager() {
        /* Find and save all relevant classes to the EnumMap */
        allClasses = findAllGameClasses();

        /* ... */
    }

    private static ClassMetaData getClassMetaDataImpl(Class<? extends ClassBase> gameClass) { return gameClass.getAnnotation(ClassMetaData.class);       }
    private static Material          getClassIconImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).iconBlock();        }
    private static String         getClassAltIconImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).iconHead();         }
    private static String     getClassMasteryHeadImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).masteryHead();      }
    private static String           getClassTitleImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).title();            }
    private static String     getClassDescriptionImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).description();      }
    private static ClassCategory getClassCategoryImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).category();         }
    private static int         getClassTokenCountImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).tokenCount();       }
    private static int   getClassUniqueIdentifierImpl(Class<? extends ClassBase> gameClass) { return getClassMetaDataImpl(gameClass).uniqueIdentifier(); }

    public static Either<Material, String> getClassIcon(Class<? extends ClassBase> gameClass) {
        Material primaryIcon = getClassIconImpl(gameClass);
        String secondaryIcon = getClassAltIconImpl(gameClass);
        if (primaryIcon != Material.AIR) {
            return Either.left(primaryIcon);
        } else if (!Objects.equals(secondaryIcon, "")) {
            return Either.right(secondaryIcon);
        } else {
            return null;
        }
    }

    public static String getClassMasteryHead(Class<? extends ClassBase> gameClass) {
        return getClassMasteryHeadImpl(gameClass);
    }

    public static String getClassTitle(Class<? extends ClassBase> gameClass) {
        return getClassTitleImpl(gameClass);
    }

    public static String getClassDescription(Class<? extends ClassBase> gameClass) {
        return getClassDescriptionImpl(gameClass);
    }

    public static ClassCategory getClassCategory(Class<? extends ClassBase> gameClass) {
        return getClassCategoryImpl(gameClass);
    }

    public static Optional<Integer> getClassTokenCount(Class<? extends ClassBase> gameClass) {
        return getClassCategory(gameClass) == ClassCategory.TOKEN ?
                Optional.of(getClassTokenCountImpl(gameClass)) :
                Optional.empty();
    }

    public static int getClassUniqueIdentifier(Class<? extends ClassBase> gameClass) {
        return getClassUniqueIdentifierImpl(gameClass);
    }

    public Set<Class<? extends ClassBase>> getAllClassesOfCategory(ClassCategory category) {
        if (category == ClassCategory.INVALID) {
            throw new RuntimeException("Invalid class category called! Failed to initialize GUI object correctly?");
        }

        return allClasses.get(category);
    }

}
