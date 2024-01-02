package anthony.SuperCraftBrawl;

import org.bukkit.entity.Player;

public class DuelRequest {
    private final int requestId;
    private final Player sender;
    private final Player target;
    private final int tokenAmount;

    public DuelRequest(int requestId, Player sender, Player target, int tokenAmount) {
        this.requestId = requestId;
        this.sender = sender;
        this.target = target;
        this.tokenAmount = tokenAmount;
    }

    public int getRequestId() {
        return requestId;
    }

    public Player getSender() {
        return sender;
    }

    public Player getTarget() {
        return target;
    }

    public int getTokenAmount() {
        return tokenAmount;
    }
}
