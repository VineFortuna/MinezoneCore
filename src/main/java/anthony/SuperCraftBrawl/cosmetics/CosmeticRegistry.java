package anthony.SuperCraftBrawl.cosmetics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CosmeticRegistry {

    private final Map<CosmeticCategory, List<Cosmetic>> byCategory = new EnumMap<>(CosmeticCategory.class);

    public void register(Cosmetic cosmetic) {
        byCategory.computeIfAbsent(cosmetic.category, c -> new ArrayList<>()).add(cosmetic);
    }

    public Cosmetic get(CosmeticCategory category, String id) {
        if (id == null) return null;
        for (Cosmetic cosmetic : getAll(category)) {
            if (cosmetic.id.equals(id)) return cosmetic;
        }
        return null;
    }

    /** Same order they were registered in, so GUI slot order matches registration order. */
    public List<Cosmetic> getAll(CosmeticCategory category) {
        return byCategory.getOrDefault(category, Collections.emptyList());
    }

    /** Every registered cosmetic with a recurring visual (getTickPeriod() > 0), across all categories. */
    public List<Cosmetic> getTicking() {
        List<Cosmetic> ticking = new ArrayList<>();
        for (List<Cosmetic> cosmetics : byCategory.values()) {
            for (Cosmetic cosmetic : cosmetics) {
                if (cosmetic.getTickPeriod() > 0) ticking.add(cosmetic);
            }
        }
        return ticking;
    }
}